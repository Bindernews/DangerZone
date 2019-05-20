package com.vortexel.dangerzone.common.capability;

/**
 * This is a Forge Capability representing the danger level of each entity. This ONLY applies to Living Entities.
 */
public interface IDangerLevel {
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
}
