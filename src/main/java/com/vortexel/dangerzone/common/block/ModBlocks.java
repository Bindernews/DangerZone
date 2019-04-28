package com.vortexel.dangerzone.common.block;

import com.google.common.collect.Sets;
import com.vortexel.dangerzone.common.tile.TileLootBagExtractor;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

public class ModBlocks {

    public static final Set<Block> BLOCKS = Sets.newHashSet();

    public static BaseBlock lootBagExtractor;

    public static void init() {
        lootBagExtractor = new BlockLootBagExtractor();
        BLOCKS.add(lootBagExtractor);

        MinecraftForge.EVENT_BUS.register(new Registrar());
    }

    public static class Registrar {
        @SubscribeEvent
        public void registerBlocks(RegistryEvent.Register<Block> e) {
            val registry = e.getRegistry();
            for (val block : BLOCKS) {
                registry.register(block);
            }
        }

        @SubscribeEvent
        public void registerItems(RegistryEvent.Register<Item> e) {
            val registry = e.getRegistry();
            for (val block : BLOCKS) {
                val item = new ItemBlock(block);
                item.setRegistryName(block.getRegistryName());
                item.setUnlocalizedName(block.getUnlocalizedName());
                registry.register(item);
            }
        }
    }
}
