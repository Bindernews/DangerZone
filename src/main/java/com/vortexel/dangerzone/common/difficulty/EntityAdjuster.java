package com.vortexel.dangerzone.common.difficulty;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.DangerMath;
import com.vortexel.dangerzone.common.config.EntityConfig;
import com.vortexel.dangerzone.common.config.ModifierConf;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.Random;

import static com.vortexel.dangerzone.common.DangerMath.*;

/**
 * Adjusts a newly-spawning entity based on its difficulty. <br/>
 * This sets the IDangerLevel CAPABILITY on the entity and applies various modifiers to it to make it harder.
 */
public class EntityAdjuster {

    private static final int TOTAL_MODIFIER_COUNT = ModifierType.values().length;
    private static final double POINT_MODIFIER_SCALE = 1 / 3D;

    private final IEntityModifier entityModifier;
    private EntityConfig eConfig;
    private int level;

    public EntityAdjuster(IEntityModifier entityModifier) {
        this.entityModifier = entityModifier;
    }

    public void adjust() {
        if (!precheck()) {
            return;
        }
        actuallyAdjust();
    }

    /**
     * Ensure that {@code entity} has a level and that we should modify it. If the {@code entity} doesn't have a
     * level, one will be provided for it.
     *
     * @return true to perform adjustments, false to not adjust the entity's properties
     */
    private boolean precheck() {
        // Set the danger level on the entity
        val dangerLevelCap = entityModifier.getCapability();
        if (dangerLevelCap == null) {
            return false;
        }

        // Don't modify an entity that's already been modified
        if (dangerLevelCap.isModified()) {
            return false;
        }
        // Remind ourselves that we've been modified later on
        dangerLevelCap.setModified(true);

        // Determine the danger level for this entity
        level = dangerLevelCap.getDanger();
        // If we don't have a pre-provided value, generate one.
        if (level == -1) {
            level = DangerMath.dangerLevel(entityModifier.getDifficultyAtLocation());
        }
        // Whatever we decided, update the existing danger level.
        dangerLevelCap.setDanger(level);

        // If level == 0 then we don't change anything.
        if (level == 0) {
            return false;
        }

        eConfig = entityModifier.getEntityConfig();
        if (eConfig == null) {
            DangerZone.log.warn("Failed to get entity config for {}", entityModifier.getEntityClassName());
            return false;
        }
        return true;
    }

    private boolean actuallyAdjust() {
        // List of enabled modifier types
        val modifierTypes = new ModifierType[TOTAL_MODIFIER_COUNT];
        // Chance of applying each modifier, plus the chance of the previous modifier.
        val chanceValues = new double[TOTAL_MODIFIER_COUNT];
        // The number of modifiers we're actually using
        int modCount = 0;
        // The running sum of all chances
        double totalChance = 0;
        // Determine which modifiers we're using and our chance to apply each of them
        for (val modType : ModifierType.values()) {
            val conf = getConf(modType);
            if (conf != null && conf.enabled) {
                val modChance = calculateChance(modType, conf);
                if (modChance > Consts.EPSILON) {
                    modifierTypes[modCount] = modType;
                    totalChance += modChance;
                    chanceValues[modCount] = totalChance;
                    modCount++;
                }
            }
        }

        // Failsafe for if we don't apply any modifiers. This happens with low-level entities.
        if (totalChance == 0) {
            return false;
        }

        // The total number of points we can use to modify the entity
        double points = DangerMath.randomDanger(getRNG(), level - (int)DangerMath.levelRange(),
                level + (int)DangerMath.levelRange()) * POINT_MODIFIER_SCALE * modCount;
        // How many points to put into each modifier
        val modPoints = new double[TOTAL_MODIFIER_COUNT];
        // Give points to each modifier
        while (points > Consts.EPSILON) {
            // How many points to put towards the next level: 1-3 or the remaining points.
            val deltaPoints = Math.min(points, randRange(getRNG(), 1, 3));
            val chance = randRange(getRNG(), 0, totalChance);
            // Find which slot our random value fits in
            int i = 0;
            while (i < modCount && chance > chanceValues[i]) {
                i++;
            }
            val scale = getConf(modifierTypes[i]).dangerScale;
            modPoints[i] += (deltaPoints / scale);
            points -= (deltaPoints * scale);
        }

        // Now turn those points into actual modifier values
        for (int i = 0; i < modCount; i++) {
            val conf = getConf(modifierTypes[i]);
            val amount = pointsToModifier(conf, modPoints[i]);
            entityModifier.modify(modifierTypes[i], amount);
        }

        return true;
    }

    private double pointsToModifier(@Nonnull ModifierConf conf, double points) {
        val minLevel = calcMinLevel(conf);
        val maxLevel = calcMaxLevel(conf);
        val pointsUnit = unlerpClamp(points, 0, maxLevel - minLevel);
        return lerp(pointsUnit, conf.min, conf.max);
    }

    private ModifierConf getConf(ModifierType type) {
        return eConfig.modifiers.get(type);
    }

    /**
     * Return the chance [0 - 1) that this modifier will be applied.
     */
    private double calculateChance(@Nonnull ModifierType modifierType, @Nonnull ModifierConf conf) {
        val minLevel = calcMinLevel(conf);
        if (level < minLevel) {
            return 0.0;
        }
        // Determine if we should apply the modifier. Chances decrease as the level decreases.
        // Currently the threshold = ((level - minLevel) / (trueMaxLevel - minLevel)) * chance
        // If we want the threshold to not account for the minLevel, that would change the math.
        // Also note that I use the term "unit" to refer to a value in the range [0-1).
        val levelUnit = unlerpClamp(level, minLevel, calcMaxLevel(conf));
        return lerp(levelUnit, conf.minChance, conf.maxChance);
    }

    private double calcMinLevel(@Nonnull ModifierConf conf) {
        return conf.minLevel == -1 ? 0 : conf.minLevel;
    }

    private double calcMaxLevel(@Nonnull ModifierConf conf) {
        return conf.maxLevel == -1 ? DangerMath.maxDangerLevel() + 1 : conf.maxLevel;
    }

    private Random getRNG() {
        return entityModifier.getRNG();
    }
}
