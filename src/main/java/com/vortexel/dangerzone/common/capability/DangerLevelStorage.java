package com.vortexel.dangerzone.common.capability;

import lombok.val;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class DangerLevelStorage implements Capability.IStorage<IDangerLevel> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IDangerLevel> capability, IDangerLevel instance, EnumFacing side) {
        val tag = new NBTTagCompound();
        tag.setInteger("danger", instance.getDanger());
        return tag;
    }

    @Override
    public void readNBT(Capability<IDangerLevel> capability, IDangerLevel instance, EnumFacing side, NBTBase nbt) {
        val tag = (NBTTagCompound)nbt;
        instance.setDanger(tag.getInteger("danger"));
    }
}
