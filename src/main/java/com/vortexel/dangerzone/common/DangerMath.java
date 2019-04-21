package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.val;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * Math utility class for calculating danger stuff.
 */
public class DangerMath {

    public static int dangerLevel(double rawDanger) {
        return (int)(rawDanger * (maxDangerLevel() + 1));
    }

    public static double dangerDouble(int level) {
        return (double)level * multiplierStep();
    }

    public static double randomDanger(int level, Random rng) {
        return randomDanger(level, level + 1, rng);
    }

    public static double randomDanger(int levelMin, int levelMax, Random rng) {
        final int maxDanger = maxDangerLevel();
        val min = dangerDouble(MathHelper.clamp(levelMin, 0, maxDanger));
        val max = dangerDouble(MathHelper.clamp(levelMax, 0, maxDanger + 1));
        return nextDoubleRange(rng, min, max);
    }

    /**
     * Returns a value greater than 0 if the effect should be applied, which should be used as it's "amount". <br/>
     * If the returned value is 0, then the effect shouldn't be applied.
     * @param level the current danger level
     * @param minCutoff the minimum level at which the effect can be applied
     * @param effectChance the chance of the effect being applied at maximum level
     * @param rng RNJesus
     */
    public static double getEffectAmount(int level, int minCutoff, double effectChance, Random rng) {
        if (level < minCutoff) {
            return 0;
        }
        val min = dangerDouble(minCutoff);
        val max = dangerDouble(maxDangerLevel() + 1);
        val threshold = dangerDouble(level);
        val amount = nextDoubleRange(rng, min, max) * effectChance;
        return amount <= threshold ? amount : 0;
    }


    /**
     * Rounds {@code d} by doing {@code floor(d * multiplier) / multiplier}. That's it. <br/>
     * If you want to round to 2 decimal places, multiplier should be 100. <br/>
     * Three decimal places: 1000. <br/>
     *
     * @param d number to be rounded
     * @param multiplier 10^(the number of decimal places you want to round to)
     */
    public static double roundDecimal(double d, int multiplier) {
       return Math.floor(d * multiplier) / multiplier;
    }

    public static double nextDoubleRange(Random rng, double min, double max) {
        return (rng.nextDouble() * (max - min)) + min;
    }

    /**
     * Each danger level increases the danger by {@code multiplierStep}. This value is as arbitrary as
     * dangerMultipier is in the config.
     */
    public static double multiplierStep() {
        return dangerMultiplier() / maxDangerLevel();
    }

    /**
     * Shortcut for getting the maximum danger level from the config.
     */
    public static int maxDangerLevel() {
        return DZConfig.INSTANCE.general.maxDangerLevel;
    }

    /**
     * Shortcut for getting the danger multiplier from the config.
     */
    public static double dangerMultiplier() {
        return DZConfig.INSTANCE.general.dangerMultiplier;
    }
}
