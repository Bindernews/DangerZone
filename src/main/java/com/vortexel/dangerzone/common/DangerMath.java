package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.val;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
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

    public static double dangerDouble(double scaledLevel) {
        return scaledLevel * multiplierStep();
    }

    public static double divideSub(double a, double b, double min) {
        return (a - min) / (b - min);
    }

    public static double randomDanger(Random rng, int levelMin, int levelMax) {
        final int maxDanger = maxDangerLevel();
        val min = dangerDouble(MathHelper.clamp(levelMin, 0, maxDanger));
        val max = dangerDouble(MathHelper.clamp(levelMax, 0, maxDanger + 1));
        return randRange(rng, min, max);
    }

    /**
     * Interpolates {@code v} (usually in range [0-1]) into a value between {@code min} and {@code max}.
     */
    public static double lerp(double v, double min, double max) {
        return (v * (max - min)) + min;
    }

    public static double lerpClamp(double v, double min, double max) {
        val t = clamp(v, 0, 1);
        return lerp(t, min, max);
    }

    /**
     * Takes {@code v} which is range {@code [min, max]} and puts it in the range {@code [0-1]}.
     */
    public static double unlerp(double v, double min, double max) {
        return (v - min) / (max - min);
    }

    public static double unlerpClamp(double v, double min, double max) {
        val t = clamp(v, min, max);
        return unlerp(t, min, max);
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

    public static double randRange(@Nonnull Random rng, double min, double max) {
        return lerp(rng.nextDouble(), min, max);
    }

    public static double randRange(@Nonnull Random rng, double max) {
        return randRange(rng, 0, max);
    }

    public static long clamp(long num, long min, long max) {
        if (num < min) {
            return min;
        } else {
            return num > max ? max : num;
        }
    }

    public static double clamp(double num, double min, double max) {
        if (num < min) {
            return min;
        } else {
            return num > max ? max : num;
        }
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
        return DZConfig.general.maxDangerLevel;
    }

    /**
     * Shortcut for getting the danger multiplier from the config.
     */
    public static double dangerMultiplier() {
        return DZConfig.general.dangerMultiplier;
    }

    public static double levelRange() {
        return DZConfig.general.levelRange;
    }
}
