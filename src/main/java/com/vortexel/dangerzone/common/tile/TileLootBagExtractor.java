package com.vortexel.dangerzone.common.tile;

import com.vortexel.dangerzone.DangerZone;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collection;

public class TileLootBagExtractor extends TileEntity implements ITickable {

    private final IItemHandler inventory = new ItemStackHandler(1);
    private Collection<ItemStack> lootBuffer = null;

    public TileLootBagExtractor() {

    }

    @Override
    public void update() {
        moveLootOut();
        processLootBag();
    }

    /**
     * Move loot out of our internal buffer and into the target inventory.
     */
    protected void moveLootOut() {

    }

    /**
     * Open a loot bag.
     */
    protected void processLootBag() {
        if (lootBuffer == null) {

        }
    }
}
