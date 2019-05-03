package com.vortexel.dangerzone.common.item;

public class ItemLootCoin extends BaseItem {

    public static final int[] AMOUNTS = { 1, 8, 64, 512 };

    public final int amount;

    public ItemLootCoin(int amount) {
        super("loot_coin_" + amount);
        this.amount = amount;
    }

    public static ItemLootCoin fromAmount(int amount) {
        switch (amount) {
            case 1:
                return ModItems.lootCoin_1;
            case 8:
                return ModItems.lootCoin_8;
            case 64:
                return ModItems.lootCoin_64;
            case 512:
                return ModItems.lootCoin_512;
            default:
                return null;
        }
    }

    public static ItemLootCoin[] makeCoinArray() {
        return new ItemLootCoin[] { ModItems.lootCoin_1, ModItems.lootCoin_8, ModItems.lootCoin_64,
                ModItems.lootCoin_512 };
    }
}
