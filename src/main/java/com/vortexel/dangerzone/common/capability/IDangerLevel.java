package com.vortexel.dangerzone.common.capability;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public interface IDangerLevel {
    ResourceLocation RESOURCE_LOCATION = new ResourceLocation(DangerZone.MOD_ID, "level");

    /**
     * Get the danger level for this entity.
     * @return a value in the range [0, MAX_DANGER_LEVEL]
     */
    int getDanger();

    /**
     * Sets the danger level for this entity.
     * @param v the new danger level
     */
    void setDanger(int v);

    /**
     * Return true if this entity's attributes have already been modified.
     */
    boolean isModified();

    /**
     * Set to true once this entity's attributes have been modified.
     */
    void setModified(boolean v);


    /**
     * The default implementation of IDangerLevel.
     */
    @Data
    class Basic implements IDangerLevel {
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
