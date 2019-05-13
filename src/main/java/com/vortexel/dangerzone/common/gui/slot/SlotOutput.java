package com.vortexel.dangerzone.common.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public abstract class SlotOutput extends SlotControlled {
    public SlotOutput(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    /**
     * Update the output slot to show a single item of the output type.
     */
    public abstract void updateOutputSlot();

    @Override
    public abstract ItemStack onTaken(@Nonnull ItemStack stack);

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }

    /**
     * Make is so that shift-clicking won't combine the output stack with any others.
     */
    @Override
    public int getSlotStackLimit() {
        return getStack().getCount();
    }
}
