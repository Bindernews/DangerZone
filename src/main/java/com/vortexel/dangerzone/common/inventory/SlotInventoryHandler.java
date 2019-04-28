package com.vortexel.dangerzone.common.inventory;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class SlotInventoryHandler extends BaseInventoryHandler {

    public static final int DEFAULT_MAX_STACK_SIZE = 64;

    private SlotConfig[] configs;
    private Consumer<Integer> changeListener;

    public SlotInventoryHandler(SlotConfig[] configs, Consumer<Integer> changeListener) {
        this.configs = Objects.requireNonNull(configs);
        this.slots = new ItemStack[configs.length];
        this.changeListener = changeListener;
        Arrays.fill(slots, ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!configs[slot].allowInsert || !configs[slot].insertFilter.test(stack)) {
            return stack;
        }
        return bypassInsert(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!configs[slot].allowExtract) {
            return ItemStack.EMPTY;
        }
        return bypassExtract(slot, amount, simulate);
    }

    @Override
    protected void onChange(int slot) {
        if (changeListener != null) {
            changeListener.accept(slot);
        }
    }

    @Override
    public int getLimitFor(int slot, @Nonnull ItemStack stack) {
        return getSlotLimit(slot, stack.getMaxStackSize());
    }

    @Override
    public int getSlotLimit(int slot) {
        return getSlotLimit(slot, DEFAULT_MAX_STACK_SIZE);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return configs[slot].insertFilter.test(stack);
    }

    @SuppressWarnings("ConstantConditions")
    protected int getSlotLimit(int slot, int defaultLimit) {
        if (configs[slot].maxStackSize == SlotConfig.USE_STACK_MAX) {
            if (slots[slot].isEmpty()) {
                return defaultLimit;
            } else {
                return slots[slot].getMaxStackSize();
            }
        } else {
            return configs[slot].maxStackSize;
        }
    }
}
