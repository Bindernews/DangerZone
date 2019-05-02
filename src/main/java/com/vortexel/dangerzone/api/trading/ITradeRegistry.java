package com.vortexel.dangerzone.api.trading;

import net.minecraft.item.ItemStack;

public interface ITradeRegistry {

    void register(ItemStack stack, int cost);

    Integer getCost(ItemStack stack);
    boolean contains(ItemStack stack);
}
