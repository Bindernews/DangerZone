package com.vortexel.dangerzone.common.item;

import com.vortexel.dangerzone.DangerZone;
import lombok.val;
import net.minecraft.item.Item;

import java.util.Objects;

public class BaseItem extends Item {

    public BaseItem(final String name) {
        setRegistryName(DangerZone.MOD_ID, name);
        val regName = Objects.requireNonNull(getRegistryName());
        setUnlocalizedName(regName.toString());
        setCreativeTab(DangerZone.creativeTab);
    }
}
