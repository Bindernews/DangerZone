package com.vortexel.dangerzone.common.capability;

import com.vortexel.dangerzone.DangerZone;
import net.minecraft.util.ResourceLocation;

public interface IDangerLevel {
    ResourceLocation RESOURCE_LOCATION = new ResourceLocation(DangerZone.ID, "dangerLevel");

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
}
