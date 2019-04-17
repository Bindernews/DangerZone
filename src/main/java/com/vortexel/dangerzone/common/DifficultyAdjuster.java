package com.vortexel.dangerzone.common;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vortexel.dangerzone.DangerZone;
import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A {@code DifficultyAdjuster} interfaces with Forge to receive events and actually perform the changes
 * to adjust the difficulty accordingly.
 */
public class DifficultyAdjuster {

    private AttributeModifierManager attributeManager;
    private DropLootManager lootManager;

    /**
     * Map of Entity IDs to their related DropData. DropData is calculated in onEntityDeath but we don't
     * actually manipulate the drops until onEntityDrops. This map stores the relevant information in the
     * meantime.
     */
    private Map<Integer, DropData> dropDataMap;


    public DifficultyAdjuster() {
        attributeManager = new AttributeModifierManager();
        lootManager = new DropLootManager();
        dropDataMap = Maps.newHashMap();
    }

    /**
     * Event for when an entity drops items. We use this to change the drop loot.
     * We set the priority at lowest so that we are one of the first to receive the event.
     * That way we can modify the drops, and let others deal with if it's allowed or not.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDrops(LivingDropsEvent e) {
        val data = dropDataMap.get(e.getEntity().getEntityId());
        if (data != null) {
            // Now we do the drop manipulation

        }
    }

    /**
     * Here we get the information we'll need in order to modify the drops. We have more info here than in
     * the LivingDropsEvent. This is set to the highest priority so that we only receive it if it hasn't been
     * cancelled or anything else.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof EntityMob) {
            EntityMob eMob = (EntityMob)e.getEntity();
            World world = eMob.getEntityWorld();

            // See EntityLiving#dropLoot for how this is implemented in vanilla.

            ResourceLocation lootTableRes = Reflector.getField(eMob, "deathLootTable");
            if (lootTableRes == null) {
                Reflector.callMethod(eMob, "getLootTable");
            }
            LootTable lootTable = world.getLootTableManager().getLootTableFromLocation(lootTableRes);
            LootContext.Builder ctxBuilder = new LootContext.Builder((WorldServer)world);
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(LivingSpawnEvent.CheckSpawn e) {
        adjustEntityDifficulty(e);
    }

    @SubscribeEvent
    public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn e) {
        adjustEntityDifficulty(e);
    }

    public void adjustEntityDifficulty(LivingSpawnEvent e) {
        // TODO modify the entity based on the difficulty of its spawn area
        int difficulty = (int)(8 * DangerZone.proxy.getDifficulty(e.getWorld(), (int)e.getX(), (int)e.getZ()));
        double amount = 1.0;
        if (difficulty <= 2) {
            amount = 0.5;
        }
        else if (difficulty <= 5) {
            amount = 1.5;
        }
        else if (difficulty <= 7) {
            amount = 2.0;
        }
        else {
            amount = 3.0;
        }

        checkDuplicateEntity(e.getEntity().getUniqueID());

        EntityLivingBase ent = e.getEntityLiving();
        attributeManager.applyModifier(ent, SharedMonsterAttributes.MAX_HEALTH, amount, 2);
        ent.setHealth(ent.getMaxHealth());
    }


    private Set<UUID> knownEntities = Sets.newHashSet();
    private void checkDuplicateEntity(UUID id) {
        if (knownEntities.contains(id)) {
            DangerZone.log.warn("Duplicate entity ID");
        }
        knownEntities.add(id);
    }

}
