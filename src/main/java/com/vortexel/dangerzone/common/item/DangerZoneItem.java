package com.vortexel.dangerzone.common.item;

import com.vortexel.dangerzone.DangerZone;
import lombok.val;
import net.minecraft.item.Item;

import java.util.Objects;

public class DangerZoneItem extends Item {

    public DangerZoneItem(final String name) {
        setRegistryName(DangerZone.MOD_ID, name);
        val regName = Objects.requireNonNull(getRegistryName());
        setUnlocalizedName(regName.toString());
        setCreativeTab(DangerZone.creativeTab);
    }
}
