package com.vortexel.dangerzone.common.item;

public class ItemLootCoin extends BaseItem {

    public final int amount;

    public ItemLootCoin(int amount) {
        super("loot_coin_" + amount);
        this.amount = amount;
    }
}
