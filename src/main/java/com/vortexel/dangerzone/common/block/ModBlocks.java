package com.vortexel.dangerzone.common.block;

import com.google.common.collect.Sets;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

public class ModBlocks {

    public static final Set<Block> BLOCKS = Sets.newHashSet();



    public static void init() {


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
    }
}
