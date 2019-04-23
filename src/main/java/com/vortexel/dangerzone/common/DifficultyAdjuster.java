package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.config.DZConfig;
import com.vortexel.dangerzone.common.item.ItemLootBag;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;

import java.util.Collection;

/**
 * A {@link DifficultyAdjuster} takes Forge events passed to it and actually perform the changes
 * to adjust the difficulty accordingly.
 */
public class DifficultyAdjuster {

    /**
     * Adds additional bonus drops to the entity. This doesn't deal with increasing their default drops
     * (that's taken care of in {@link DifficultyAdjuster#modifyLootingLevel}).
     */
    public void modifyDrops(LivingDropsEvent e) {
        val canLoot = DZConfig.INSTANCE.general.lootBagOnNonPlayerKills
                || shouldLoot(e.getSource().getTrueSource());
        if (shouldModifyWorld(e.getEntity()) && canLoot) {
            val entity = e.getEntity();
            val dangerInfo = MCUtil.getDangerLevelCapability(entity);
            if (dangerInfo != null) {
                val stack = new ItemStack(ModItems.loot_bag, 1, 0);
                ItemLootBag.setLootBagLevel(stack, dangerInfo.getDanger());
                e.getDrops().add(MCUtil.makeItemAt(entity, stack));
            }
        }
    }

    /**
     * Modify the looting level for any entities so that their normal drops are increased.
     */
    public void modifyLootingLevel(LootingLevelEvent e) {
        val canLoot = DZConfig.INSTANCE.general.lootingOnNonPlayerKills
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
        if (DZConfig.INSTANCE.general.doFakePlayersDropLoot) {
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
            new EntityAdjuster((EntityLivingBase)e).adjust();
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

            ResourceLocation lootTableRes = Reflector.getField(eMob, "deathLootTable");
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
