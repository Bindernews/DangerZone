package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.common.util.FnUtil;
import lombok.val;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class GuiUtil {

    public static final int ITEM_SLOT_SIZE = 18;

    public static void addPlayerInventory(BaseContainer container, InventoryPlayer playerInv, int x, int y,
                                          int hotbarOffset,
                                          FnUtil.TriFunction<Integer, Integer, Integer, Slot> slotProducer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                val index = j + (i * 9 + 9);
                val xPos = x + (j * ITEM_SLOT_SIZE);
                val yPos = y + (i * ITEM_SLOT_SIZE);
                container.addSlotToContainer(slotProducer.apply(index, xPos, yPos));
            }
        }
        val yPos = y + hotbarOffset + (3 * ITEM_SLOT_SIZE);
        for (int i = 0; i < 9; i++) {
            val xPos = x + (i * ITEM_SLOT_SIZE);
            container.addSlotToContainer(slotProducer.apply(i, xPos, yPos));
        }
    }

    public static int getPlayerInventoryIndex(InventoryPlayer inventory, ItemStack stack) {
        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            if (inventory.mainInventory.get(i).isItemEqual(stack)) {
                return i;
            }
        }
        return -1;
    }

    private GuiUtil() {}
}
