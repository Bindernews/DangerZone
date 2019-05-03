package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.common.gui.slot.SlotControlled;
import com.vortexel.dangerzone.common.gui.slot.SlotImmutable;
import com.vortexel.dangerzone.common.inventory.SlotConfig;
import com.vortexel.dangerzone.common.inventory.ConfigInventoryHandler;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ContainerCoinPouch extends BaseContainer {

    public static final SlotConfig[] SLOTS = new SlotConfig[2];
    static {
        SLOTS[0] = SlotConfig.builder().index(0).allowInsert(true).allowExtract(false).build();
        SLOTS[1] = SlotConfig.builder().index(1).allowInsert(false).allowExtract(true).build();
    }

    private ConfigInventoryHandler backingInventory;
    private EntityPlayer openingPlayer;
    private int coinPouchPlayerIndex;

    private SlotImmutable coinPouchSlot;
    private SlotItemHandler inputSlot;
    private SlotItemHandler outputSlot;

    @Getter
    private ItemLootCoin outputType;

    public ContainerCoinPouch(EntityPlayer player, ItemStack coinPouch) {
        this.backingInventory = new ConfigInventoryHandler(SLOTS, null);
        this.openingPlayer = player;
        coinPouchPlayerIndex = GuiUtil.getPlayerInventoryIndex(player.inventory, coinPouch);
        outputType = ModItems.lootCoin_1;

        inputSlot = new SlotItemHandler(backingInventory, 0, 17, 34) {
            @Override
            public void putStack(@Nonnull ItemStack stack) {
                if (stack.getItem() instanceof ItemLootCoin) {
                    addAmount(((ItemLootCoin) stack.getItem()), stack.getCount());
                    updateOutputSlot();
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
        };

        // We override this so that when things are taken from the output, it updates the coinPouch.
        outputSlot = new SlotControlled(backingInventory, 1, 141, 34) {
            @Override
            public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
                ItemStack result = super.onTake(thePlayer, stack);
                updateOutputSlot();
                return result;
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
             * @return
             */
            @Override
            public int getSlotStackLimit() {
                return getStack().getCount();
            }
        };

        addSlotToContainer(inputSlot);
        addSlotToContainer(outputSlot);
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

        updateOutputSlot();
    }

    /**
     * Update the output slot to show a single item of the output type, or be empty
     * if you don't have enough coins.
     */
    protected void updateOutputSlot() {
        long amount = ItemCoinPouch.getAmount(getCoinPouch());
        if (amount >= outputType.amount) {
            backingInventory.setStackInSlot(outputSlot.getSlotIndex(), new ItemStack(outputType, 1));
        } else {
            backingInventory.setStackInSlot(outputSlot.getSlotIndex(), ItemStack.EMPTY);
        }
        outputSlot.onSlotChanged();
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
        if (index == outputSlot.getSlotIndex()) {
            long maxCoins = ItemCoinPouch.getAmount(getCoinPouch()) / outputType.amount;
            int stackSize = (int)Math.min(maxCoins, 64);
            outputSlot.putStack(new ItemStack(outputType, stackSize));
        }
        return super.getShiftClickStack(player, index);
    }

    public ItemStack getCoinPouch() {
        return coinPouchSlot.getRealStack();
    }

    public void setOutputType(ItemLootCoin outputType) {
        Objects.requireNonNull(outputType);
        this.outputType = outputType;
        updateOutputSlot();
    }
}
