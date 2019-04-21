package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.Getter;
import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Consumer;

/**
 * Adjusts a newly-spawning entity based on its difficulty. <br/>
 * This sets the IDangerLevel CAPABILITY on the entity and applies various modifiers to it to make it harder.
 */
public class EntityAdjuster {

    private static final int OP_ADD = 1;
    private static final int OP_MULTIPLY = 2;

    private final EntityLivingBase entity;

    @Getter
    private int dangerLevel;

    public EntityAdjuster(EntityLivingBase entity) {
        this.entity = entity;
    }

    public void adjust() {
        // Set the danger level on the entity
        val dangerLevelCap = entity.getCapability(DangerZone.CAP_DANGER_LEVEL, null);
        if (dangerLevelCap == null) {
            return;
        }

        val rng = entity.getRNG();
        val ePos = entity.getPosition();
        val rawDanger = DangerZone.proxy.getDifficulty(entity.getEntityWorld(), ePos.getX(), ePos.getZ());
        dangerLevel = DangerMath.dangerLevel(rawDanger);

        if (dangerLevel == 0) {
            return;
        }

        dangerLevelCap.setDanger(dangerLevel);

        // Apply all the modifiers to the entity
        val cfg = DZConfig.INSTANCE.general;
        applyModifier(ModifierType.MOVEMENT_SPEED,
                DangerMath.getEffectAmount(dangerLevel, cfg.dangerLevelHaste, cfg.hasteChance, rng),
                this::applyMovementSpeed);
        applyModifier(ModifierType.REGENERATION,
                DangerMath.getEffectAmount(dangerLevel, cfg.dangerLevelRegenerate, cfg.regenChance, rng),
                this::applyRegeneration);
        applyModifier(ModifierType.MAX_HEALTH,
                DangerMath.randomDanger(dangerLevel, rng),
                this::applyMaxHealth);
        applyModifier(ModifierType.ATTACK_DAMAGE,
                DangerMath.randomDanger(dangerLevel - 1, dangerLevel + 1, rng),
                this::applyAttackDamage);
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


    public void applyAttributeModifier(String name, IAttribute attr, double amount, int operation) {
        // Not all entities have all Attributes (e.g. Pigs can't attack).
        val attrInstance = entity.getAttributeMap().getAttributeInstance(attr);
        if (attrInstance != null) {
            val realName = DangerZone.NAME + "-" + name;
            val modifier = new AttributeModifier(realName, amount, operation);
            attrInstance.applyModifier(modifier);
        }
    }

    //region Modifier appliers

    private void applyMovementSpeed(double amount) {
        applyAttributeModifier("haste", SharedMonsterAttributes.MOVEMENT_SPEED, amount, OP_ADD);
    }

    private void applyRegeneration(double amount) {
        val realRegen = DangerMath.roundDecimal(amount, 100);
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + (float)realRegen);
    }

    private void applyMaxHealth(double amount) {
        applyAttributeModifier("health", SharedMonsterAttributes.MAX_HEALTH, amount, OP_MULTIPLY);
        entity.setHealth(entity.getMaxHealth());
    }

    private void applyAttackDamage(double amount) {
        applyAttributeModifier("attack", SharedMonsterAttributes.ATTACK_DAMAGE, amount, OP_ADD);
    }

    //endregion
}
