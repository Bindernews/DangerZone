package com.vortexel.dangerzone.api.trading;

import lombok.Value;
import net.minecraft.item.ItemStack;

@Value
public class Offer {
    public final int cost;
    public final ItemStack stack;
}
