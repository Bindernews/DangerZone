package com.vortexel.dangerzone.common.gui;

import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BaseContainer extends Container {

    @Override
    public Slot addSlotToContainer(Slot slotIn) {
        return super.addSlotToContainer(slotIn);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    /**
     * Called when the player tries to shift-click a slot.
     * @param playerIn the player who did the clicking
     * @param index index of the slot in the GUI (NOT in the player's inventory).
     * @return the leftover item stack?
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        // This is copied pretty much straight up from
        // https://shadowfacts.net/tutorials/forge-modding-112/tile-entities-inventory-gui/
        ItemStack result = ItemStack.EMPTY;
        val slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            val stackIn = getShiftClickStack(playerIn, index);
            result = stackIn.copy();
            val containerSlots = inventorySlots.size() - playerIn.inventory.mainInventory.size();
            if (index < containerSlots) {
                if (!mergeItemStack(stackIn, containerSlots, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(stackIn, 0, containerSlots, false)) {
                return ItemStack.EMPTY;
            }
            if (stackIn.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            if (stackIn.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            result = slot.onTake(playerIn, result);
        }
        return result;
    }

    /**
     * Called to determine what's in a Slot when a player shift-clicks it. This may be overridden by machines
     * which produce one of an item, or as many as possible when shift-clicked. <br/>
     * This will NOT be called if {@code slot.getHasStack()} returns {@code false}.
     * @param player the player doing the clicking
     * @param index index of the container slot
     * @return the ItemStack in the slot
     */
    protected ItemStack getShiftClickStack(EntityPlayer player, int index) {
        return inventorySlots.get(index).getStack();
    }
}
