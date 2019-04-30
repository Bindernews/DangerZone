package com.vortexel.dangerzone.common.potion;

import net.minecraft.potion.Potion;

/**
 * A potion effect that inflicts Wither on anyone touched by the affected entity.
 */
public class PotionDecayTouch extends Potion {

    protected PotionDecayTouch() {
        super(false, 0x681860);
        setPotionName("effect.decayTouch");
    }

    @Override
    public boolean isInstant() {
        return false;
    }

    /**
     * Called every tick to determine if the effect is ready to be applied. <br/>
     * If this returns true, {@code performEffect} will be called.
     */
    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
