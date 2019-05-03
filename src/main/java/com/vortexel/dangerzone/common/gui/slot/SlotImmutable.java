package com.vortexel.dangerzone.common.gui.slot;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotImmutable extends Slot {

    @Getter
    private ItemStack realStack;

    public SlotImmutable(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        realStack = inventoryIn.getStackInSlot(index).copy();
    }

    public void setRealStack(ItemStack stack) {
        this.realStack = stack;
        onSlotChanged();
    }

    @Override
    public void putStack(ItemStack stack) {
        // do nothing
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    public void update() {
        onSlotChanged();
    }

    @Override
    public void onSlotChanged() {
        inventory.setInventorySlotContents(getSlotIndex(), realStack);
        inventory.markDirty();
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }
}
