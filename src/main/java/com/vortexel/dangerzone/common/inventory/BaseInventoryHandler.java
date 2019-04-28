package com.vortexel.dangerzone.common.inventory;

import lombok.val;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public abstract class BaseInventoryHandler implements IItemHandler, IItemHandlerModifiable,
        INBTSerializable<NBTTagCompound> {

    protected ItemStack[] slots;


    /**
     * Get the stack limit for the slot {@code slot} if you were going to put {@code stack} into {@code slot}. <br/>
     * Usually if there something already in {@code slot} then it will use that as the stack limit,
     * otherwise it will use {@code stack}'s {@code maxStackSize}.
     *
     * @param slot the index of the slot to test
     * @param stack the stack that may or may not be inserted into slot
     * @return the maximum stack size for slot in this case
     */
    public abstract int getLimitFor(int slot, @Nonnull ItemStack stack);

    /**
     * Insert into a slot, overriding the settings of subclasses. <br/>
     * This should be used internally by subclasses to implement {@code insertItem} and externally by
     * other parts of the code, such as the owning TileEntity, to manually perform inventory management.
     *
     * @return the remaining ItemStack that wasn't inserted or ItemStack.EMPTY.
     */
    @Nonnull
    public ItemStack bypassInsert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        val current = slots[slot];
        int limit;
        int size;
        if (current.isEmpty()) {
            limit = getLimitFor(slot, stack);
            size = 0;
        } else if (ItemHandlerHelper.canItemStacksStack(current, stack)) {
            limit = getLimitFor(slot, stack) - current.getCount();
            size = current.getCount();
        } else {
            // We can't combine the stacks, so we can't insert anything
            return stack;
        }

        val totalCount = size + stack.getCount();
        val nextCount = Math.min(totalCount, limit);

        if (!simulate) {
            // If the current stack is empty, then we can safely replace it
            if (current.isEmpty()) {
                slots[slot] = stack.copy();
            }
            slots[slot].setCount(nextCount);
        }
        onChange(slot);
        return totalCount > nextCount
                ? ItemHandlerHelper.copyStackWithSize(stack, totalCount - nextCount)
                : ItemStack.EMPTY;
    }

    /**
     * Extract from a slot, overriding subclass' checks. <br/>
     * This should be used internally by subclasses to implement {@code extractItem} and externally by
     * other parts of the code, such as the owning TileEntity, to manually perform inventory management.
     *
     * @return the ItemStack that was extracted or ItemStack.EMPTY.
     */
    @Nonnull
    public ItemStack bypassExtract(int slot, int amount, boolean simulate) {
        val current = slots[slot];
        if (amount == 0 || current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        val toTake = Math.min(current.getCount(), amount);
        val result = ItemHandlerHelper.copyStackWithSize(current, toTake);
        if (!simulate) {
            current.grow(-toTake);
            if (current.isEmpty()) {
                slots[slot] = ItemStack.EMPTY;
            }
        }
        onChange(slot);
        return result;
    }

    /**
     * This is called when a slot has been changed. It may be overridden by subclasses to do anything they want.
     * @param slot the index of the slot which has been changed
     */
    protected void onChange(int slot) {}

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        slots[slot] = stack;
    }

    @Override
    public int getSlots() {
        return slots.length;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return slots[slot];
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < slots.length; i++) {
            if (!slots[i].isEmpty()) {
                val itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                slots[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            val itemTag = (NBTTagCompound)list.get(i);
            int slot = itemTag.getInteger("Slot");
            slots[i] = new ItemStack(itemTag);
        }
    }
}
