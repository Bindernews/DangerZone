package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.item.ItemLootBag;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
        if (MCUtil.isWorldLocal(e) && shouldDropLootBag(e)) {
            val entity = e.getEntity();
            val dangerInfo = MCUtil.getDangerLevelCapability(entity);
            if (dangerInfo != null) {
                val stack = new ItemStack(ModItems.loot_bag, 1, 0);
                ItemLootBag.setLootBagLevel(stack, dangerInfo.getDanger());
                e.getDrops().add(MCUtil.makeItemAt(entity, stack));
            }
        }
    }

    public boolean shouldDropLootBag(LivingDropsEvent e) {
        // Only drop the loot bag if the source is REALLY a player. No fake players.
        val src = e.getSource().getTrueSource();
        return (src instanceof EntityPlayer) && !(src instanceof FakePlayer);
    }

    /**
     * Modify the looting level for any entities so that their normal drops are increased.
     */
    public void modifyLootingLevel(LootingLevelEvent e) {
        val dangerLevelCap = MCUtil.getDangerLevelCapability(e.getEntity());
        if (MCUtil.isWorldLocal(e) && dangerLevelCap != null) {
            e.setLootingLevel(e.getLootingLevel() + dangerLevelCap.getDanger());
        }
    }

    public void adjustEntityDifficulty(Entity e) {
        // We farm out all the work to EntityAdjuster, because it's enough code for its own class.
        if (MCUtil.isWorldLocal(e) && e instanceof EntityCreature) {
            new EntityAdjuster((EntityLivingBase)e).adjust();
        }
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
