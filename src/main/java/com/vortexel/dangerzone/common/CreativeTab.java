package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTab extends CreativeTabs {

    public CreativeTab() {
        super(getNextID(), DangerZone.NAME);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(ModItems.lootCoin_1);
    }

    @Override
    public String getTranslatedTabLabel() {
        return getTabLabel();
    }
}
