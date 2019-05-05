package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.common.MerchandiseManager;
import com.vortexel.dangerzone.common.gui.slot.SlotControlled;
import com.vortexel.dangerzone.common.inventory.ConfigInventoryHandler;
import com.vortexel.dangerzone.common.inventory.SlotConfig;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import com.vortexel.dangerzone.common.item.ModItems;
import com.vortexel.dangerzone.common.util.CoinUtil;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.List;

public class ContainerTradeVillager extends BaseContainer {

    private static final int ROWS = 3;
    private static final int COLS = 9;

    private static final SlotConfig[] INVENTORY_CONFIG = (SlotConfig[])SlotConfig.buildSeveral(
            SlotConfig.builder().allowExtract(true).allowInsert(false), 0, ROWS * COLS).toArray();

    protected ConfigInventoryHandler backingInventory;
    protected EntityPlayer player;
    protected long playerMoney;
    protected boolean playerHasMoreMoney;
    protected int offerPage;

    public ContainerTradeVillager(EntityPlayer player) {
        backingInventory = new ConfigInventoryHandler(INVENTORY_CONFIG, null);
        GuiUtil.addInventory(this, backingInventory, 8, 20, ROWS, COLS, (s) ->
                new OutputSlot(backingInventory, s.index, s.x, s.y));

        GuiUtil.addPlayerInventory(this, player.inventory, 8, 84, 4,
                (index, x, y) -> new Slot(player.inventory, index, x, y));

        offerPage = 0;
        updatePlayerMoney();
        for (int i = 0; i < firstPlayerSlot(); i++) {
            ((OutputSlot) getSlot(i)).updateOutputSlot();
        }
    }

    @Override
    protected ItemStack getShiftClickStack(EntityPlayer player, int index) {
        if (index < firstPlayerSlot()) {
            val perItemCost = getCostForSlot(index);
            val merchandise = getMerchandiseForSlot(index, 1);
            // The maximum number of items they can take is either (money / perItemCost),
            // or (max stack size / items per sale).
            val maxItems = (int)Math.min(playerMoney / perItemCost,
                    merchandise.getMaxStackSize() / merchandise.getCount());
            if (maxItems == 0) {
                return ItemStack.EMPTY;
            }
            backingInventory.setStackInSlot(index, getMerchandiseForSlot(index, maxItems));
            return getSlot(index).getStack();
        } else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Get the loot coin cost of the item in slot {@code index}.
     */
    protected long getCostForSlot(int index) {
        return MerchandiseManager.instance.getCost(index + (ROWS * COLS * offerPage));
    }

    protected ItemStack getMerchandiseForSlot(int index, int count) {
        val stack = MerchandiseManager.instance.getItemStack(index + (ROWS * COLS * offerPage));
        return ItemHandlerHelper.copyStackWithSize(stack, count * stack.getCount());
    }

    /**
     * Take money from the player, and give change. If the player doesn't have enough inventory space
     * for the change, put it in a {@link ItemCoinPouch}. If that fails, drop the extra money and
     * close the GUI. <br/>
     *
     * Preconditions:
     * - The player MUST have enough money in their inventory
     *
     * @return true if the money was successfully taken, false if we have to close the GUI.
     */
    protected boolean takeMoney(long cost) {
        long remainingCost = cost;
        for (int i = firstPlayerSlot(); i < lastPlayerSlot(); i++) {
            val stack = getSlot(i).getStack();
            val amount = CoinUtil.takeFrom(stack, remainingCost);
            if (amount > 0) {
                remainingCost -= amount;
                putStackInSlot(i, stack);
                if (remainingCost < 0) {
                    break;
                }
            }
        }
        // At this point, remainingCost is <= 0, specifically less than if we took coins.
        remainingCost = -remainingCost;
        playerMoney -= cost + remainingCost;
        // Now that we've collected enough money, give back change.
        // If our cost is 0, then there's no change.
        if (remainingCost == 0) {
            return true;
        }
        // First try to put into a coin pouch.
        for (int i = firstPlayerSlot(); i < lastPlayerSlot(); i++) {
            val stack = getSlot(i).getStack();
            if (stack.getItem() == ModItems.coinPouch) {
                // Give the player their money back
                ItemCoinPouch.addAmount(stack, remainingCost);
                putStackInSlot(i, stack);
                playerMoney += remainingCost;
                remainingCost = 0;
                break;
            }
        }
        // If we put the chance into a coin pouch, then we're good.
        if (remainingCost == 0) {
            return true;
        }
        // Try to return it as change.
        val change = CoinUtil.makeChange(remainingCost);
        for (int i = 0; i < change.size(); i++) {
            while (!change.get(i).isEmpty()) {
                val stack = change.get(i);
                val destSlot = findMergeableSlot(stack, firstPlayerSlot(), lastPlayerSlot(), false);
                if (destSlot == BAD_SLOT) {
                    dropItems(change);
                    return false;
                }
                val itemCount = stack.getCount();
                change.set(i, insertStack(stack, destSlot));
                val numItemsInserted = itemCount - change.get(i).getCount();
                playerMoney += CoinUtil.getStackValue(stack.getItem(), numItemsInserted);
            }
        }
        return true;
    }

    protected void dropItems(List<ItemStack> items) {
        if (MCUtil.isWorldLocal(player)) {
            for (val stack : items) {
                if (!stack.isEmpty()) {
                    player.dropItem(stack, true);
                }
            }
        }
    }

    protected int firstPlayerSlot() {
        return ROWS * COLS;
    }

    protected int lastPlayerSlot() {
        return inventorySlots.size();
    }

    /**
     * Sum up all the money items in the container from slot {@code slotStart} to {@code slotEnd}.
     * If the total money overflows, sets {@code playerMoney} to Long.MAX_VALUE.
     */
    protected void updatePlayerMoney() {
        val slotStart = firstPlayerSlot();
        val slotEnd = lastPlayerSlot();
        try {
            playerHasMoreMoney = false;
            long total = 0;
            for (int i = slotStart; i < slotEnd; i++) {
                val amount = CoinUtil.getStackValue(getSlot(i).getStack());
                if (amount > 0) {
                    total = Math.addExact(total, amount);
                }
            }
            playerMoney = total;
        } catch (ArithmeticException e) {
            playerHasMoreMoney = true;
            playerMoney = Long.MAX_VALUE;
        }
    }

    protected class OutputSlot extends SlotControlled {
        public OutputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        /**
         * Update the output slot to show a single item of the output type.
         */
        protected void updateOutputSlot() {
            backingInventory.setStackInSlot(this.getSlotIndex(), getMerchandiseForSlot(getSlotIndex(), 1));
            onSlotChanged();
        }

        @Override
        public ItemStack onTaken(@Nonnull ItemStack stack) {
            takeMoney(CoinUtil.getStackValue(stack));
            updateOutputSlot();
            // We do this so the player only gets 1 stack at a time
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return playerMoney >= getCostForSlot(getSlotIndex());
        }

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
