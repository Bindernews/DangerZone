package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.common.gui.slot.SlotControlled;
import com.vortexel.dangerzone.common.gui.slot.SlotImmutable;
import com.vortexel.dangerzone.common.inventory.SlotConfig;
import com.vortexel.dangerzone.common.inventory.ConfigInventoryHandler;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import com.vortexel.dangerzone.common.item.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerCoinPouch extends BaseContainer {

    public static final SlotConfig[] SLOTS = new SlotConfig[5];
    static {
        SLOTS[0] = SlotConfig.builder().index(0).allowInsert(true).allowExtract(false).build();
        SLOTS[1] = SlotConfig.builder().index(1).allowInsert(false).allowExtract(true).build();
        SLOTS[2] = SlotConfig.builder().index(2).allowInsert(false).allowExtract(true).build();
        SLOTS[3] = SlotConfig.builder().index(3).allowInsert(false).allowExtract(true).build();
        SLOTS[4] = SlotConfig.builder().index(4).allowInsert(false).allowExtract(true).build();
    }

    private ConfigInventoryHandler backingInventory;
    private EntityPlayer openingPlayer;
    private int coinPouchPlayerIndex;

    private SlotImmutable coinPouchSlot;

    public ContainerCoinPouch(EntityPlayer player) {
        this.backingInventory = new ConfigInventoryHandler(SLOTS, null);
        this.openingPlayer = player;
        coinPouchPlayerIndex = player.inventory.currentItem;

        addSlotToContainer(new SlotItemHandler(backingInventory, 0, 34, 28) {
            @Override
            public void putStack(@Nonnull ItemStack stack) {
                if (stack.getItem() instanceof ItemLootCoin) {
                    addAmount(((ItemLootCoin) stack.getItem()), stack.getCount());
                    updateAllOutputSlots();
                }
                onSlotChanged();
            }

            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                if (!super.isItemValid(stack)) {
                    return false;
                }
                return stack.getItem() instanceof ItemLootCoin;
            }
        });

        // These are the output slots.
        addSlotToContainer(new OutputSlot(backingInventory, 1, 70, 28, ModItems.lootCoin_1));
        addSlotToContainer(new OutputSlot(backingInventory, 2, 88, 28, ModItems.lootCoin_8));
        addSlotToContainer(new OutputSlot(backingInventory, 3, 106, 28, ModItems.lootCoin_64));
        addSlotToContainer(new OutputSlot(backingInventory, 4, 124, 28, ModItems.lootCoin_512));

        // We return a normal Slot EXCEPT for when it's the slot with the Coin Pouch. Then we return an
        // immutable slot so we can modify it, but the player cannot. This is how we sync the inventory information.
        GuiUtil.addPlayerInventory(this, player.inventory, 8, 84, 4, (index, x, y) -> {
            if (index == coinPouchPlayerIndex) {
                coinPouchSlot = new SlotImmutable(player.inventory, index, x, y);
                return coinPouchSlot;
            } else {
                return new Slot(player.inventory, index, x, y);
            }
        });

        updateAllOutputSlots();
    }

    protected void updateAllOutputSlots() {
        // Make sure all of our output slots are updated properly
        for (int i = 1; i < 5; i++) {
            Slot inventorySlot = inventorySlots.get(i);
            if (inventorySlot instanceof OutputSlot) {
                ((OutputSlot) inventorySlot).updateOutputSlot();
            }
        }
    }

    protected void addAmount(ItemLootCoin coinType, int count) {
        long total = coinType.amount * count;
        ItemCoinPouch.addAmount(getCoinPouch(), total);
        coinPouchSlot.update();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.isEntityEqual(openingPlayer);
    }

    @Override
    protected ItemStack getShiftClickStack(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot instanceof OutputSlot) {
            OutputSlot oSlot = (OutputSlot)slot;
            // This determines the maximum number of coins the player can take.
            long maxCoins = ItemCoinPouch.getAmount(getCoinPouch()) / oSlot.outputType.amount;
            int stackSize = (int)Math.min(maxCoins, 64);
            oSlot.putStack(new ItemStack(oSlot.outputType, stackSize));
        }
        return super.getShiftClickStack(player, index);
    }

    public ItemStack getCoinPouch() {
        return coinPouchSlot.getRealStack();
    }

    protected class OutputSlot extends SlotControlled {
        ItemLootCoin outputType;

        public OutputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, ItemLootCoin outputType) {
            super(itemHandler, index, xPosition, yPosition);
            this.outputType = outputType;
        }

        @Override
        public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
            ItemStack result = super.onTake(thePlayer, stack);
            updateOutputSlot();
            return result;
        }

        /**
         * Update the output slot to show a single item of the output type, or be empty
         * if you don't have enough coins.
         */
        protected void updateOutputSlot() {
            long amount = ItemCoinPouch.getAmount(getCoinPouch());
            if (amount >= outputType.amount) {
                backingInventory.setStackInSlot(getSlotIndex(), new ItemStack(outputType, 1));
            } else {
                backingInventory.setStackInSlot(getSlotIndex(), ItemStack.EMPTY);
            }
            onSlotChanged();
        }

        @Override
        public ItemStack onTaken(@Nonnull ItemStack stack) {
            addAmount(outputType, -stack.getCount());
            // We do this so the player only gets 1 stack at a time
            return ItemStack.EMPTY;
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
