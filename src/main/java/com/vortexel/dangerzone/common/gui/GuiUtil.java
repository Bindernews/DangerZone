package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.common.util.FnUtil;
import lombok.AllArgsConstructor;
import lombok.val;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Function;

public class GuiUtil {

    public static final int ITEM_SLOT_SIZE = 18;

    public static void addInventory(BaseContainer container, IItemHandler inventory, int x, int y, int cols, int rows,
                                    Function<InventoryPos, Slot> slotProducer) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                val pos = new InventoryPos(j + (i * cols),
                        x + (j * ITEM_SLOT_SIZE),
                        y + (i * ITEM_SLOT_SIZE));
                container.addSlotToContainer(slotProducer.apply(pos));
            }
        }
    }

    public static void addPlayerInventory(BaseContainer container, InventoryPlayer playerInv, int x, int y,
                                          int hotbarOffset,
                                          FnUtil.TriFunction<Integer, Integer, Integer, Slot> slotProducer) {
        for (int i = 0; i < 9; i++) {
            val xPos = x + (i * ITEM_SLOT_SIZE);
            val yPos = y + hotbarOffset + (3 * ITEM_SLOT_SIZE);
            container.addSlotToContainer(slotProducer.apply(i, xPos, yPos));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                val index = j + (i * 9 + 9);
                val xPos = x + (j * ITEM_SLOT_SIZE);
                val yPos = y + (i * ITEM_SLOT_SIZE);
                container.addSlotToContainer(slotProducer.apply(index, xPos, yPos));
            }
        }
    }

    @AllArgsConstructor
    public static class InventoryPos {
        public final int index;
        public final int x;
        public final int y;
    }

    private GuiUtil() {}
}
