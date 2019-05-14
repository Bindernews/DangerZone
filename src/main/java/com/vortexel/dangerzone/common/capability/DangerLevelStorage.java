package com.vortexel.dangerzone.common.capability;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.api.IDangerLevel;
import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class DangerLevelStorage implements Capability.IStorage<IDangerLevel> {

    public static final ResourceLocation RESOURCE_LOCATION = DangerZone.prefix("level");

    public static final String KEY_LEVEL = "level";
    public static final String KEY_MODIFIED = "modified";

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IDangerLevel> capability, IDangerLevel instance, EnumFacing side) {
        val tag = new NBTTagCompound();
        tag.setInteger(KEY_LEVEL, instance.getDanger());
        tag.setBoolean(KEY_MODIFIED, instance.isModified());
        return tag;
    }

    @Override
    public void readNBT(Capability<IDangerLevel> capability, IDangerLevel instance, EnumFacing side, NBTBase nbt) {
        // This is here to make it more convenient to set the level using /summon or anything similar.
        if (nbt instanceof NBTPrimitive) {
            val tag = (NBTPrimitive)nbt;
            instance.setDanger(tag.getInt());
            instance.setModified(false);
        } else if (nbt instanceof NBTTagCompound) {
            val tag = (NBTTagCompound)nbt;
            instance.setDanger(tag.getInteger(KEY_LEVEL));
            instance.setModified(tag.getBoolean(KEY_MODIFIED));
        } else {
            throw new UnsupportedOperationException("Invalid NBT data for DangerLevel capability.");
        }
    }

    /**
     * The default implementation of IDangerLevel.
     */
    @Data
    public static class Basic implements IDangerLevel {
        @Getter
        private int danger = -1;
        @Getter @Setter
        private boolean modified = false;

        @Override
        public void setDanger(int v) {
            danger = MathHelper.clamp(v, -1, DZConfig.general.maxDangerLevel);
        }
    }
}
