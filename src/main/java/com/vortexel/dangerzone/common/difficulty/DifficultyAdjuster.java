package com.vortexel.dangerzone.common.difficulty;

import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.DangerMath;
import com.vortexel.dangerzone.common.Reflector;
import com.vortexel.dangerzone.common.capability.DangerLevelProvider;
import com.vortexel.dangerzone.common.capability.DangerLevelStorage;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.config.DZConfig;
import com.vortexel.dangerzone.common.item.ModItems;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

/**
 * A {@link DifficultyAdjuster} takes Forge events passed to it and actually perform the changes
 * to adjust the difficulty accordingly.
 */
public class DifficultyAdjuster {

    //region Event handlers

    /**
     * Event for when an entity drops items. We use this to change the drop loot.
     * We set the priority at lowest so that we are one of the first to receive the event.
     * That way we can modify the drops, and let others deal with if it's allowed or not.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDrops(LivingDropsEvent e) {
        modifyDrops(e);
    }

    @SubscribeEvent
    public void onLootingLevel(LootingLevelEvent e) {
        modifyLootingLevel(e);
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent e) {
        adjustEntityDifficulty(e.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityAttacked(LivingDamageEvent e) {
        val source = e.getSource().getTrueSource();
        if (source instanceof EntityLivingBase) {
            implementDecayTouch(e.getEntityLiving(), (EntityLivingBase)source);
        }
    }

    @SubscribeEvent
    public void onRegisterCapabilities(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof EntityLivingBase) {
            val eLiving = (EntityLivingBase)e.getObject();
            if (!(eLiving instanceof EntityPlayer)) {
                e.addCapability(DangerLevelStorage.RESOURCE_LOCATION, new DangerLevelProvider());
            }
            eLiving.getAttributeMap().registerAttribute(Consts.ATTRIBUTE_DECAY_TOUCH);
        }
    }

    //endregion Event handlers

    /**
     * Call this to actually implement the effects of Decay Touch. <br/>
     * @param target the {@link EntityLivingBase} being attacked
     * @param source the {@link EntityLivingBase} doing the attacking
     */
    public void implementDecayTouch(EntityLivingBase target, EntityLivingBase source) {
        val inst = source.getAttributeMap().getAttributeInstance(Consts.ATTRIBUTE_DECAY_TOUCH);
        if (inst != null && inst.getAttributeValue() > 0) {
            val duration = (int)(Consts.TICKS_PER_SECOND * DZConfig.effects.decayTouchTime * inst.getAttributeValue());
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, duration, (int)inst.getAttributeValue(),
                    false, false));
        }
    }

    /**
     * Adds additional bonus drops to the entity. This doesn't deal with increasing their default drops
     * (that's taken care of in {@link DifficultyAdjuster#modifyLootingLevel}).
     */
    public void modifyDrops(LivingDropsEvent e) {
        val canLoot = DZConfig.general.lootCoinsOnNonPlayerKills
                || shouldLoot(e.getSource().getTrueSource());
        if (shouldModifyWorld(e.getEntity()) && canLoot) {
            val entity = e.getEntity();
            val dangerInfo = MCUtil.getDangerLevelCapability(entity);
            if (dangerInfo != null) {
                val danger = dangerInfo.getDanger();
                val amount = (int) DangerMath.randRange(e.getEntityLiving().getRNG(), danger - 4, danger + 1);
                if (amount > 0) {
                    val stack = new ItemStack(ModItems.lootCoin_1, amount, 0);
                    e.getDrops().add(MCUtil.makeItemAt(entity, stack));
                }
            }
        }
    }

    /**
     * Modify the looting level for any entities so that their normal drops are increased.
     */
    public void modifyLootingLevel(LootingLevelEvent e) {
        val canLoot = DZConfig.general.lootingOnNonPlayerKills
                || shouldLoot(e.getDamageSource().getTrueSource());
        if (shouldModifyWorld(e.getEntity()) && canLoot) {
            val dangerLevelCap = MCUtil.getDangerLevelCapability(e.getEntity());
            if (dangerLevelCap != null) {
                e.setLootingLevel(e.getLootingLevel() + dangerLevelCap.getDanger());
            }
        }
    }

    /**
     * Return true if {@code src} is a player and not a fake player, or a player and dofakePlayersDropLoot is true.
     * @param src The result of {@link DamageSource#getTrueSource()} for your particular event
     */
    protected boolean shouldLoot(Entity src) {
        if (DZConfig.general.doFakePlayersDropLoot) {
            return (src instanceof EntityPlayer);
        } else {
            return (src instanceof EntityPlayer) && !(src instanceof FakePlayer);
        }
    }

    /**
     * Adjust the difficulty of {@code e} as long as it's appropriate to do so. <br/>
     * The entity will only be modified if it's in an enabled world, and it has an IDangerLevel capability attached.
     */
    public void adjustEntityDifficulty(Entity e) {
        // We farm out all the work to EntityAdjuster, because it's enough code for its own class.
        if (shouldModifyWorld(e) && e instanceof EntityCreature) {
            new EntityAdjuster(new ForgeEntityModifier((EntityLivingBase)e)).adjust();
        }
    }

    /**
     * Return true if the world {@code e} is in is both local and enabled in the config.
     */
    protected boolean shouldModifyWorld(Entity e) {
        return MCUtil.isWorldLocal(e) && MCUtil.isWorldEnabled(e);
    }

    public Collection<ItemStack> getAdditionalDrops(LivingDropsEvent e) {
        if (e.getEntity() instanceof EntityMob
                && MCUtil.getDangerLevelCapability(e.getEntity()) != null) {
            EntityMob eMob = (EntityMob)e.getEntity();
            World world = eMob.getEntityWorld();
            IDangerLevel dangerLevel = MCUtil.getDangerLevelCapability(eMob);

            // See EntityLiving#dropLoot for how this is implemented in vanilla and EntityLivingBase#onDeath
            // for where dropLoot is called and how it translates into the LivingDropsEvent.

            ResourceLocation lootTableRes = Reflector.get(eMob, "deathLootTable");
            if (lootTableRes == null) {
                lootTableRes = Reflector.callMethod(eMob, "getLootTable");
            }
            LootTable lootTable = world.getLootTableManager().getLootTableFromLocation(lootTableRes);
            LootContext.Builder ctxBuilder = new LootContext.Builder((WorldServer)world);
        }
        // If we don't need to add any drops, we just return an empty set.
        return null;
    }
}
