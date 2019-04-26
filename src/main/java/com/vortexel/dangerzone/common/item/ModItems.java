package com.vortexel.dangerzone.common.item;

import com.google.common.collect.Sets;
import lombok.val;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Set;

public class ModItems {
    public static final Set<Item> ITEMS = Sets.newHashSet();

    public static ItemLootBag lootBag;

    public static void init() {
        lootBag = new ItemLootBag();
        ITEMS.add(lootBag);

        MinecraftForge.EVENT_BUS.register(new Registrar());
    }

    public static class Registrar {
        /**
         * Register this mod's {@link Item}s
         */
        @SubscribeEvent
        public void registerItems(RegistryEvent.Register<Item> e) {
            val registry = e.getRegistry();
            for (val item : ITEMS) {
                registry.register(item);
            }
        }
    }
}
