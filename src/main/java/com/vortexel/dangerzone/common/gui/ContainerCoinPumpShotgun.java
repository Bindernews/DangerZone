package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.common.gui.slot.SlotImmutable;
import com.vortexel.dangerzone.common.inventory.ConfigInventoryHandler;
import com.vortexel.dangerzone.common.inventory.SlotConfig;
import com.vortexel.dangerzone.common.item.ItemCoinPumpShotgun;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerCoinPumpShotgun extends BaseContainer {

    public static final SlotConfig[] INVENTORY_CONFIG = new SlotConfig[1];
    static {
        INVENTORY_CONFIG[0] = SlotConfig.builder().index(0).allowInsert(true).allowExtract(true).build();
    }

    private ConfigInventoryHandler backingInventory;
    private EntityPlayer openingPlayer;
    private int coinPumpShotgunPlayerIndex;
    private SlotImmutable coinPumpShotgunSlot;

    public ContainerCoinPumpShotgun(EntityPlayer player) {
        this.backingInventory = new ConfigInventoryHandler(INVENTORY_CONFIG, null);
        this.openingPlayer = player;
        coinPumpShotgunPlayerIndex = player.inventory.currentItem;

        addSlotToContainer(new SlotItemHandler(backingInventory, 0, 80, 34) {
            @Override
            public void onSlotChanged() {
                ItemCoinPumpShotgun.setContents(coinPumpShotgunSlot.getRealStack(), getStack());
                coinPumpShotgunSlot.update();
            }

            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                if (!super.isItemValid(stack)) {
                    return false;
                }
                return stack.getItem() instanceof ItemLootCoin;
            }
        });

        // We return a normal Slot EXCEPT for when it's the slot with the Coin Pump Shotgun. Then we return an
        // immutable slot so we can modify it, but the player cannot. This is how we sync the inventory information.
        GuiUtil.addPlayerInventory(this, player.inventory, 8, 84, 4, (index, x, y) -> {
            if (index == coinPumpShotgunPlayerIndex) {
                coinPumpShotgunSlot = new SlotImmutable(player.inventory, index, x, y);
                return coinPumpShotgunSlot;
            } else {
                return new Slot(player.inventory, index, x, y);
            }
        });
        val ammo = ItemCoinPumpShotgun.getContents(coinPumpShotgunSlot.getRealStack());
        backingInventory.setStackInSlot(0, ammo);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.isEntityEqual(openingPlayer);
    }
}
