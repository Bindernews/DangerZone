package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.common.inventory.SingleSlotHelper;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public abstract class BaseContainer extends Container {

    public static final int BAD_SLOT = -1;

    protected final SingleSlotHelper inventoryHelper = new SingleSlotHelper();

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
     * @return a stack of the items remaining in the slot
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        val slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            val stackIn = getShiftClickStack(playerIn, index);
            val destSlot = getShiftClickDestination(playerIn, index);
            if (destSlot == BAD_SLOT) {
                return ItemStack.EMPTY;
            }
            val originalCount = stackIn.getCount();
            val leftover = insertStack(stackIn, destSlot);
            val amountTaken = originalCount - leftover.getCount();
            if (amountTaken != 0) {
                slot.decrStackSize(amountTaken);
                result = slot.onTake(playerIn, ItemHandlerHelper.copyStackWithSize(stackIn, amountTaken));
            } else {
                // If we couldn't put anything in, then return that there was nothing
                // left to shift-click so that this method isn't run in an infinite loop.
                return ItemStack.EMPTY;
            }
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

    /**
     * Returns the best destination slot for the {@link ItemStack} in slot {@code slotClicked}. <br/>
     * By default this just finds the first matching item in the other inventory, or the first empty one. <br/>
     * This MAY be overridden by subclasses.
     *
     * @param player player who clicked
     * @param slotClicked index of the slot that was shift-clicked
     * @return the index to place stuff in, or BAD_SLOT to do nothing
     */
    protected int getShiftClickDestination(EntityPlayer player, int slotClicked) {
        val containerSlots = inventorySlots.size() - player.inventory.mainInventory.size();
        val stack = getSlot(slotClicked).getStack();
        if (slotClicked < containerSlots) {
            return findMergeableSlot(stack, containerSlots, inventorySlots.size(), false);
        } else {
            // Search container inventory slots from first to last
            return findMergeableSlot(stack, 0, containerSlots, false);
        }
    }

    protected int findMergeableSlot(@Nonnull ItemStack stack, int start, int end, boolean reverseDirection) {
        // Make sure we don't have a nearly infinite loop
        Validate.isTrue(start <= end);
        int realStart = start;
        int realEnd = end - 1;
        int delta = 1;
        if (reverseDirection) {
            realStart = end - 1;
            realEnd = start;
            delta = -1;
        }
        for (int i = realStart; i != realEnd; i += delta) {
            if (testMergeStack(stack, getSlot(i))) {
                return i;
            }
        }
        return BAD_SLOT;
    }

    /**
     * Inserts {@code stack} into slot number {@code slotIndex}. Returns any remaining items.
     *
     * @param stack the stack to be inserted
     * @param slotIndex the slot index to insert into
     * @return anything from {@code stack} that didn't fit into the slot
     */
    protected ItemStack insertStack(@Nonnull ItemStack stack, int slotIndex) {
        val slot = getSlot(slotIndex);
        if (!testMergeStack(stack, slot)) {
            return stack;
        }
        val current = slot.getStack();
        inventoryHelper.setStack(current);
        val leftovers = inventoryHelper.insertItem(stack);
        slot.putStack(inventoryHelper.getStack());
        return leftovers;
    }

    /**
     * Returns true if {@code stack} can be merged into {@code slot}, false otherwise. <br/>
     * This takes into account empty slots, {@code slot.isItemValid()}, {@code canMergeSlot()},
     * and stack limits.
     */
    protected boolean testMergeStack(@Nonnull ItemStack stack, Slot slot) {
        if (!canMergeSlot(stack, slot)) {
            return false;
        }
        val current = slot.getStack();
        val canMerge = current.isEmpty()
                || (ItemHandlerHelper.canItemStacksStack(current, stack)
                    && current.getCount() < getStackLimit(current, slot));
        return canMerge && slot.isItemValid(stack);
    }

    /**
     * Returns the maximum stack size of the item or the slot, whichever is lower.
     */
    public int getStackLimit(ItemStack stack, Slot slot) {
        return Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
    }
}
