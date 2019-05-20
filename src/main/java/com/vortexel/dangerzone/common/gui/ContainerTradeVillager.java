package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.entity.EntityTraderVillager;
import com.vortexel.dangerzone.common.gui.slot.SlotOutput;
import com.vortexel.dangerzone.common.trade.MerchandiseManager;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.List;

public class ContainerTradeVillager extends BaseContainer {

    public static final int ROWS = 4;
    public static final int COLS = 9;
    public static final int VISIBLE_SLOTS = ROWS * COLS;

    private static final SlotConfig[] INVENTORY_CONFIG = SlotConfig.buildSeveral(
            SlotConfig.builder().allowExtract(true).allowInsert(false), 0, ROWS * COLS)
            .toArray(new SlotConfig[0]);

    protected ConfigInventoryHandler backingInventory;
    protected EntityPlayer player;
    protected long playerMoney;
    protected boolean playerHasMoreMoney;
    protected int scrollRow;
    private boolean isOpen;

    public ContainerTradeVillager(EntityPlayer player) {
        this.player = player;
        this.isOpen = true;
        backingInventory = new ConfigInventoryHandler(INVENTORY_CONFIG, null);
        GuiUtil.addInventory(this, backingInventory, 8, 18, COLS, ROWS, (s) ->
                new OutputSlot(backingInventory, s.index, s.x, s.y));

        GuiUtil.addPlayerInventory(this, player.inventory, 8, 104, 4,
                (index, x, y) -> new Slot(player.inventory, index, x, y));

        scrollRow = 0;
        updatePlayerMoney();
        for (int i = 0; i < firstPlayerSlot(); i++) {
            ((OutputSlot) getSlot(i)).updateOutputSlot();
        }
    }

    @Override
    public void onUpdatePacket(EntityPlayer sender, NBTTagCompound tag) {
        if (tag.hasKey("row", Constants.NBT.TAG_INT)) {
            this.scrollTo(tag.getInteger("row"));
        }
    }

    protected EntityTraderVillager findVillager(EntityPlayer player) {
        val aabb = player.getEntityBoundingBox().grow(10, 4, 10);
        for (val entity : player.world.getEntitiesWithinAABB(EntityTraderVillager.class, aabb)) {
            if (entity.getCustomer() != null && entity.getCustomer().equals(player)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (MCUtil.isWorldLocal(playerIn) && isOpen) {
            isOpen = false;
            val villager = findVillager(playerIn);
            if (villager != null) {
                villager.setCustomer(null);
            }
        }
    }

    @Override
    protected ItemStack getShiftClickStack(EntityPlayer player, int index) {
        if (getSlot(index) instanceof OutputSlot) {
            val slot = (OutputSlot)getSlot(index);
            // If we don't check, then we reset the number of items in the inventory even if part of a stack is
            // already there. This will end up giving the player an extra stack.
            if (!slot.isTaking) {
                val merchandise = getMerchandiseForSlot(index, 1);
                val perItemCost = getCostForSlot(index);
                // The maximum number of items they can take is either (money / perItemCost),
                // or (max stack size / items per sale).
                val maxItems = (int)Math.min(playerMoney / perItemCost,
                        merchandise.getMaxStackSize() / merchandise.getCount());
                if (maxItems == 0) {
                    return ItemStack.EMPTY;
                }
                backingInventory.setStackInSlot(index, getMerchandiseForSlot(index, maxItems));
                slot.isTaking = true;
            }
            return getSlot(index).getStack();
        } else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Get the loot coin cost of the item in slot {@code index}.
     */
    public long getCostForSlot(int index) {
        return getMerchandise().getCost(index + (COLS * scrollRow));
    }

    public ItemStack getMerchandiseForSlot(int index, int count) {
        val stack = getMerchandise().getItemStack(index + (COLS * scrollRow));
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
                if (remainingCost <= 0) {
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

    public void scrollTo(int row) {
        val maxRow = (int)Math.ceil((double)getMerchandise().getTotalOffers() / COLS);
        scrollRow = MathHelper.clamp(row, 0, maxRow);
        for (int i = 0; i < ContainerTradeVillager.VISIBLE_SLOTS; i++) {
            ((OutputSlot)getSlot(i)).updateOutputSlot();
        }
    }

    public int firstPlayerSlot() {
        return ROWS * COLS;
    }

    public int lastPlayerSlot() {
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

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    public class OutputSlot extends SlotOutput {
        public boolean isTaking;
        public boolean enabled;

        public OutputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
            isTaking = false;
            enabled = false;
        }

        /**
         * Update the output slot to show a single item of the output type.
         */
        @Override
        public void updateOutputSlot() {
            val merch = getMerch(1);
            isTaking = false;
            enabled = !merch.isEmpty();
            super.putStack(merch);
        }

        protected ItemStack getMerch(int count) {
            return getMerchandiseForSlot(getSlotIndex(), 1);
        }

        @Override
        public ItemStack onTaken(@Nonnull ItemStack stack) {
            // Determine how much to take. If we would have to take a partial-coin, round up. Yes it takes more,
            // but rounding down potentially allows for exploits.
            val countTaken = (float)stack.getCount() / getMerch(1).getCount();
            val cost = (long)Math.ceil(getCostForSlot(slotNumber) * countTaken);
            // If we can't return all the player's money to them, close the GUI
            if (!takeMoney(cost)) {
                player.closeScreen();
            }
            // We do this so the player only gets 1 stack at a time
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return playerMoney >= getCostForSlot(getSlotIndex());
        }
    }


    public static MerchandiseManager getMerchandise() {
        return DangerZone.proxy.getMerchandise();
    }
}
