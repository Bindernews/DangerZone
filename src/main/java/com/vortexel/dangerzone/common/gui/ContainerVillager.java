package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.common.gui.slot.SlotControlled;
import com.vortexel.dangerzone.common.inventory.ConfigInventoryHandler;
import com.vortexel.dangerzone.common.inventory.SlotConfig;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ContainerVillager extends BaseContainer {

    private static final SlotConfig[] INVENTORY_CONFIG = (SlotConfig[])SlotConfig.buildSeveral(
            SlotConfig.builder().allowExtract(true).allowInsert(false), 0, 3 * 9).toArray();

    protected ConfigInventoryHandler backingInventory;
    protected EntityPlayer player;

    public ContainerVillager(EntityPlayer player) {
        backingInventory = new ConfigInventoryHandler(INVENTORY_CONFIG, null);
        GuiUtil.addInventory(this, backingInventory, 8, 20, 3, 9, (s) ->
                new OutputSlot(backingInventory, s.index, s.x, s.y));

        GuiUtil.addPlayerInventory(this, player.inventory, 8, 84, 4,
                (index, x, y) -> new Slot(player.inventory, index, x, y));
    }

    protected class OutputSlot extends SlotControlled {
        public OutputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
            ItemStack result = super.onTake(thePlayer, stack);
//            updateOutputSlot();
            return result;
        }

//        /**
//         * Update the output slot to show a single item of the output type.
//         */
//        protected void updateOutputSlot() {
//            backingInventory.setStackInSlot();
//            long amount = ItemCoinPouch.getAmount(getCoinPouch());
//            if (amount >= outputType.amount) {
//                backingInventory.setStackInSlot(getSlotIndex(), new ItemStack(outputType, 1));
//            } else {
//                backingInventory.setStackInSlot(getSlotIndex(), ItemStack.EMPTY);
//            }
//            onSlotChanged();
//        }
//
//        @Override
//        public ItemStack onTaken(@Nonnull ItemStack stack) {
//            addAmount(outputType, -stack.getCount());
//            // We do this so the player only gets 1 stack at a time
//            return ItemStack.EMPTY;
//        }

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
}
