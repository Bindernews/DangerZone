package com.vortexel.dangerzone.common;

import com.google.common.collect.Lists;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.config.EntityConfig;
import com.vortexel.dangerzone.common.config.ModifierConf;
import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

import static com.vortexel.dangerzone.common.DangerMath.*;

/**
 * Adjusts a newly-spawning entity based on its difficulty. <br/>
 * This sets the IDangerLevel CAPABILITY on the entity and applies various modifiers to it to make it harder.
 */
public class EntityAdjuster {

    private static final int OP_ADD = 0;
    private static final int OP_MULTIPLY = 1;
    private static final int OP_MULTIPLY_EXTRA = 2;
    private static final String FIELD_EXPLOSION_RADIUS = "explosionRadius";
    private static final int TOTAL_MODIFIER_COUNT = ModifierType.values().length;
    private static final double POINT_MODIFIER_SCALE = 1 / 3D;

    private final EntityLivingBase entity;
    private EntityConfig eConfig;

    private int level;

    public EntityAdjuster(EntityLivingBase entity) {
        this.entity = entity;
    }

    public void adjust() {
        if (!precheck()) {
            return;
        }
        actuallyAdjust();
    }

    private boolean precheck() {
        // Set the danger level on the entity
        val dangerLevelCap = entity.getCapability(DangerZone.CAP_DANGER_LEVEL, null);
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
            level = generateDangerLevel();
        }
        // Whatever we decided, update the existing danger level.
        dangerLevelCap.setDanger(level);

        // If level == 0 then we don't change anything.
        if (level == 0) {
            return false;
        }

        eConfig = DangerZone.proxy.getEntityConfigManager().getConfigDynamic(entity.getClass());
        if (eConfig == null) {
            DangerZone.log.warn("Failed to get entity config for {}", entity.getClass().getName());
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
            applyModifier(modifierTypes[i], amount, selectApplicator(modifierTypes[i]));
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
        return entity.getRNG();
    }

    /**
     * Helper method which fires an event and if the event isn't cancelled, calls {@code applicator} to apply
     * the modified value to the entity.
     * @param modifier the type of attribute which will be modified
     * @param initialAmount initial
     * @param applicator function which will apply the modifier to the entity
     */
    private void applyModifier(ModifierType modifier, double initialAmount, Consumer<Double> applicator) {
        val event = new ModifierAppliedEvent(entity, modifier, initialAmount);
        if (!MinecraftForge.EVENT_BUS.post(event) && event.amount > 0) {
            applicator.accept(event.amount);
        }
    }

    private int generateDangerLevel() {
        val ePos = entity.getPosition();
        val rawDanger = DangerZone.proxy.getDifficulty(entity.getEntityWorld(), ePos.getX(), ePos.getZ());
        return DangerMath.dangerLevel(rawDanger);
    }

    private Consumer<Double> selectApplicator(ModifierType modifier) {
        switch (modifier) {
            case MAX_HEALTH:
                return this::applyMaxHealth;
            case REGENERATION:
                return this::applyRegeneration;
            case ATTACK_DAMAGE:
                return this::applyAttackDamage;
            case ATTACK_SPEED:
                return this::applyAttackSpeed;
            case MOVE_SPEED:
                return this::applyMovementSpeed;
            case FLY_SPEED:
                return this::applyFlySpeed;
            case ARMOR:
                return this::applyArmor;
            case ARMOR_TOUGHNESS:
                return this::applyArmorToughness;
            case EXPLOSION_RADIUS:
                return this::applyExplosionRadius;
            case WITHER:
                return this::applyWither;
            default:
                return null;
        }
    }


    public void applyAttributeModifier(IAttribute attr, UUID uuid, String name, double amount, int operation) {
        // If it's nearly zero, then just ignore it.
        if (amount <= Consts.NOT_ZERO) {
            return;
        }
        // Not all entities have all Attributes (e.g. Pigs can't attack).
        val attrInstance = entity.getAttributeMap().getAttributeInstance(attr);
        if (attrInstance == null) {
            return;
        }
        val modifier = new AttributeModifier(uuid, DangerZone.NAME + "-" + name, amount, operation);
        if (attrInstance.hasModifier(modifier)) {
            DangerZone.log.warn("Entity already has attribute applied.");
        } else {
            attrInstance.applyModifier(modifier);
        }
    }

    //region Modifier appliers

    private void applyMovementSpeed(double amount) {
        applyAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, Consts.MODIFIER_MOVE_SPEED_UUID,
                "haste", amount, OP_MULTIPLY);
    }

    private void applyFlySpeed(double amount) {
        applyAttributeModifier(SharedMonsterAttributes.FLYING_SPEED, Consts.MODIFIER_FLY_SPEED_UUID,
                "fly-speed", amount, OP_MULTIPLY);
    }

    private void applyRegeneration(double amount) {
        val realRegen = DangerMath.roundDecimal(amount, 100);
        entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, Consts.POTION_DURATION_FOREVER,
                (int)realRegen, false, true));
    }

    private void applyMaxHealth(double amount) {
        applyAttributeModifier(SharedMonsterAttributes.MAX_HEALTH, Consts.MODIFIER_MAX_HEALTH_UUID,
                "health", amount, OP_MULTIPLY);
        entity.setHealth(entity.getMaxHealth());
    }

    private void applyAttackDamage(double amount) {
        applyAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE, Consts.MODIFIER_ATTACK_DAMAGE_UUID,
                "attack", amount, OP_ADD);
    }

    private void applyAttackSpeed(double amount) {
        applyAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE, Consts.MODIFIER_ATTACK_SPEED_UUID,
                "attack-speed", amount, OP_ADD);
    }

    private void applyArmor(double amount) {
        applyAttributeModifier(SharedMonsterAttributes.ARMOR, Consts.MODIFIER_ARMOR_UUID,
                "armor", amount, OP_MULTIPLY);
    }

    private void applyArmorToughness(double amount) {
        applyAttributeModifier(SharedMonsterAttributes.ARMOR_TOUGHNESS, Consts.MODIFIER_ARMOR_TOUGHNESS_UUID,
                "armor-toughness", amount, OP_MULTIPLY);
    }

    private void applyExplosionRadius(double amount) {
        if (Reflector.hasField(entity, FIELD_EXPLOSION_RADIUS)) {
            Reflector.computeField(entity, FIELD_EXPLOSION_RADIUS, (Integer v) -> v + (int)amount);
        }
    }

    private void applyWither(double amount) {
        applyAttributeModifier(Consts.ATTRIBUTE_DECAY_TOUCH, Consts.MODIFIER_DECAY_TOUCH_UUID,
                "decay-touch", amount, OP_ADD);
    }

    //endregion
}
