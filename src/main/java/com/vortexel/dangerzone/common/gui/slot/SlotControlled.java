package com.vortexel.dangerzone.common.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotControlled extends SlotItemHandler {

    private boolean wasDecrCalled = false;

    public SlotControlled(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    /**
     * Called AFTER a stack of items has been taken. <br/>
     * If this is called from onTake, your result will be returned, otherwise it will be ignored.
     * @param stack the stack of items that was removed
     */
    public ItemStack onTaken(@Nonnull ItemStack stack) {
        return null;
    }

    /**
     * Called any time things are taken from this slot, INCLUDING when shift-clicked.
     */
    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
        ItemStack result = stack;
        if (!wasDecrCalled) {
            result = onTaken(stack);
        }
        wasDecrCalled = false;
        return result;
    }

    /**
     * Called when the slot is clicked normally, but NOT when it's shift-clicked.
     */
    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        ItemStack itemsTaken = ItemHandlerHelper.copyStackWithSize(getStack(), amount);
        ItemStack result = super.decrStackSize(amount);
        onTaken(itemsTaken);
        // We do this so that onTake does NOT call onTaken
        wasDecrCalled = true;
        return result;
    }
}
