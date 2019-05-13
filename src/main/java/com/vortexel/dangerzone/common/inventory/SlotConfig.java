package com.vortexel.dangerzone.common.inventory;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import net.minecraft.item.ItemStack;

import java.util.List;
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

    public static List<SlotConfig> buildSeveral(SlotConfigBuilder builderIn, int index, int count) {
        List<SlotConfig> slots = Lists.newArrayListWithCapacity(count);
        for (int i = 0; i < count; i++) {
            slots.add(builderIn.index(index + i).build());
        }
        return slots;
    }
}
