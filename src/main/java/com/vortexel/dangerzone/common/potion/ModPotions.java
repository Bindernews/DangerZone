package com.vortexel.dangerzone.common.potion;

import com.google.common.collect.Sets;
import net.minecraft.potion.Potion;

import java.util.Set;

public class ModPotions {

    public static final Set<Potion> POTIONS = Sets.newHashSet();

    public static Potion decayingTouch;

    public static void init() {
        decayingTouch = new PotionDecayTouch();
        POTIONS.add(decayingTouch);
    }
}
