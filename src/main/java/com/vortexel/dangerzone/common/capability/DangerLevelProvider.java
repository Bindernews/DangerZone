package com.vortexel.dangerzone.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DangerLevelProvider implements ICapabilitySerializable<NBTBase> {

    @CapabilityInject(IDangerLevel.class)
    public static Capability<IDangerLevel> CAP_DANGER_LEVEL = null;

    private IDangerLevel instance = CAP_DANGER_LEVEL.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CAP_DANGER_LEVEL;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CAP_DANGER_LEVEL ? CAP_DANGER_LEVEL.cast(instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return CAP_DANGER_LEVEL.getStorage().writeNBT(CAP_DANGER_LEVEL, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        CAP_DANGER_LEVEL.getStorage().readNBT(CAP_DANGER_LEVEL, instance, null, nbt);
    }
}
