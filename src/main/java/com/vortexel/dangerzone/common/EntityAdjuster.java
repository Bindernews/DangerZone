package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.config.ModifierConf;
import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Consumer;

import static com.vortexel.dangerzone.common.DangerMath.lerp;
import static com.vortexel.dangerzone.common.DangerMath.unlerpClamp;

/**
 * Adjusts a newly-spawning entity based on its difficulty. <br/>
 * This sets the IDangerLevel CAPABILITY on the entity and applies various modifiers to it to make it harder.
 */
public class EntityAdjuster {

    private static final int OP_ADD = 0;
    private static final int OP_MULTIPLY = 1;
    private static final int OP_MULTIPLY_EXTRA = 2;
    private static final String FIELD_EXPLOSION_RADIUS = "explosionRadius";
    private static final double BAD_MODIFIER = -1;

    private final EntityLivingBase entity;

    private int level;

    public EntityAdjuster(EntityLivingBase entity) {
        this.entity = entity;
    }

    public void adjust() {
        // Set the danger level on the entity
        val dangerLevelCap = entity.getCapability(DangerZone.CAP_DANGER_LEVEL, null);
        if (dangerLevelCap == null) {
            return;
        }

        // Don't modify an entity that's already been modified
        if (dangerLevelCap.isModified()) {
            return;
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
            return;
        }

        val eConfig = DangerZone.proxy.getEntityConfigManager().getConfigDynamic(entity.getClass());
        if (eConfig == null) {
            DangerZone.log.warn("Failed to get entity config for {}", entity.getClass().getName());
            return;
        }

        val modTypes = ModifierType.values();
        val amounts = new double[modTypes.length];
        double totalDanger = 0;
        for (int i = 0; i < modTypes.length; i++) {
            val modifier = modTypes[i];
            val conf = eConfig.modifiers.get(modifier);
            // If there's no configuration for this value, then assume it's not being applied
            if (conf == null || !conf.enabled) {
                amounts[i] = BAD_MODIFIER;
            } else {
                // Calculate the value for this modifier
                amounts[i] = calculateInitialAmount(modifier, conf);
                if (amounts[i] != BAD_MODIFIER) {
                    totalDanger += amounts[i] * conf.dangerScale;
                }
            }
        }
        // Finally we actually apply the modifiers. We also adjust the modifiers to make sure we're not
        // too dangerous on the whole.
        val maxDanger = DangerMath.randomDanger(entity.getRNG(), level - 1, level + 1);
        // If we don't check this then it might cause a divide-by-zero.
        if (totalDanger == 0) {
            return;
        }
        val ratio = maxDanger / totalDanger;
        for (int i = 0; i < amounts.length; i++) {
            if (amounts[i] != BAD_MODIFIER) {
                val conf = eConfig.modifiers.get(modTypes[i]);
                val realAmount = DangerMath.clamp(amounts[i] * ratio, conf.min, conf.max);
                applyModifier(modTypes[i], realAmount, selectApplicator(modTypes[i]));
            }
        }
    }

    /**
     * Calculate the initial, randomized, value for the modifier. <br/>
     */
    private double calculateInitialAmount(@Nonnull ModifierType modifier, @Nonnull ModifierConf conf) {
        if (level < conf.minLevel) {
            return BAD_MODIFIER;
        }
        val rng = entity.getRNG();
        val plusMinus = DangerMath.levelRange();
        val maxLevel = conf.maxLevel == -1 ? DangerMath.maxDangerLevel() + 1 : conf.maxLevel;
        val minLevel = conf.minLevel == -1 ? 0 : conf.minLevel;
        // Determine if we should apply the modifier. Chances decrease as the level decreases.
        // Currently the threshold = ((level - minLevel) / (trueMaxLevel - minLevel)) * chance
        // If we want the threshold to not account for the minLevel, that would change the math.
        // Also note that I use the term "unit" to refer to a value in the range [0-1).
        val levelUnit = unlerpClamp(level, minLevel, maxLevel);
        val threshold = lerp(levelUnit, conf.minChance, conf.maxChance);
        val chanceRand = rng.nextDouble();
        if (chanceRand > threshold) {
            return BAD_MODIFIER;
        }
        // Now that we know we're going to try to get a value, let's generate one.
        // We cap our level at "max level".
        val randLevel = DangerMath.randRange(rng, level - plusMinus, level + plusMinus);
        val randUnit = unlerpClamp(randLevel, minLevel, maxLevel);
        return lerp(randUnit, conf.min, conf.max);
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
