package com.vortexel.dangerzone.common.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotSync extends SlotItemHandler {

    public SlotSync(int index, int xPosition, int yPosition) {
        super(new ItemStackHandler(1), index, xPosition, yPosition);
        super.putStack(new ItemStack(Items.PAPER, 1, 0, new NBTTagCompound()));
    }

    public NBTTagCompound getData() {
        return getStack().getTagCompound();
    }

    public void setData(NBTTagCompound data) {
        if (data != null) {
            ItemStack stack = getStack();
            stack.setTagCompound(data);
            super.putStack(stack);
        }
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        // do nothing
    }

    @Override
    public int getSlotStackLimit() {
        return 0;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        return ItemStack.EMPTY;
    }
}
