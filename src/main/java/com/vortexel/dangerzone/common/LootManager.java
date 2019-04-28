package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import lombok.val;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

public class LootManager {

    public static final ResourceLocation ORE_LOOT_TABLE_RESOURCE =
            new ResourceLocation(DangerZone.MOD_ID, "ore_table");

    @SubscribeEvent
    public void onLootLoad(LootTableLoadEvent event) {
//        if (event.getName().equals(ORE_LOOT_TABLE_RESOURCE)) {
            // TODO dynamically add in all ores
//        }
    }

    public Collection<ItemStack> getLootBagLoot(WorldServer worldIn, int level) {
        val lootTable = worldIn.getLootTableManager().getLootTableFromLocation(ORE_LOOT_TABLE_RESOURCE);
        int difficultyAtLevel = (int)DangerMath.dangerDouble(level);
        val lootContext = new LootContext.Builder(worldIn)
                .withLuck((float)DangerMath.dangerDouble(level))
                .build();
        return lootTable.generateLootForPools(worldIn.rand, lootContext);
    }
}
