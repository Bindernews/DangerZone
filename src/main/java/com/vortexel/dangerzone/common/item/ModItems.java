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

    public static ItemLootCoin lootCoin_1;
    public static ItemLootCoin lootCoin_8;
    public static ItemLootCoin lootCoin_64;
    public static ItemLootCoin lootCoin_512;
    public static Item coinPouch;
    public static ItemCoinPumpShotgun coinPumpShotgun;

    public static void init() {
        lootCoin_1 = new ItemLootCoin(ItemLootCoin.AMOUNTS[0]);
        ITEMS.add(lootCoin_1);
        lootCoin_8 = new ItemLootCoin(ItemLootCoin.AMOUNTS[1]);
        ITEMS.add(lootCoin_8);
        lootCoin_64 = new ItemLootCoin(ItemLootCoin.AMOUNTS[2]);
        ITEMS.add(lootCoin_64);
        lootCoin_512 = new ItemLootCoin(ItemLootCoin.AMOUNTS[3]);
        ITEMS.add(lootCoin_512);
        coinPouch = new ItemCoinPouch();
        ITEMS.add(coinPouch);
        coinPumpShotgun = new ItemCoinPumpShotgun();
        ITEMS.add(coinPumpShotgun);

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
