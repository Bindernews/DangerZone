package com.vortexel.dangerzone.common.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

@AllArgsConstructor
@Builder
public class SlotConfig {

    public static final int USE_STACK_MAX = -1;

    /**
     * The index of the slot.
     */
    public final int index;

    @Builder.Default
    public final int maxStackSize = USE_STACK_MAX;
    public final boolean allowInsert;
    public final boolean allowExtract;

    @Builder.Default
    public final Predicate<ItemStack> insertFilter = (s) -> true;
}
