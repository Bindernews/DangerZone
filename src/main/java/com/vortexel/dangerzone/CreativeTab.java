package com.vortexel.dangerzone;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTab extends CreativeTabs {

    public CreativeTab() {
        super(getNextID(), DangerZone.NAME);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(Block.getBlockById(1));
    }

    @Override
    public String getTranslatedTabLabel() {
        return getTabLabel();
    }
}
