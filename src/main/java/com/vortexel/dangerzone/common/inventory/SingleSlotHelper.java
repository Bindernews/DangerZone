package com.vortexel.dangerzone.common.inventory;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Inventory with a single slot, useful for performing inventory calculations in Containers.
 */
public class SingleSlotHelper extends BaseInventoryHandler {

    public SingleSlotHelper() {
        this.slots = new ItemStack[1];
        slots[0] = ItemStack.EMPTY;
    }

    public ItemStack getStack() {
        return getStackInSlot(0);
    }

    public void setStack(@Nonnull ItemStack stack) {
        setStackInSlot(0, stack);
    }

    public ItemStack insertItem(@Nonnull ItemStack stack) {
        return insertItem(0, stack, false);
    }

    public ItemStack extractItem(int amount) {
        return extractItem(0, amount, false);
    }

    @Override
    public int getLimitFor(int slot, @Nonnull ItemStack stack) {
        return stack.getMaxStackSize();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return bypassInsert(slot, stack, simulate, false);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return bypassExtract(slot, amount, false, false);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
}
