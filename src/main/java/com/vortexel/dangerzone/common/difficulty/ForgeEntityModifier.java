package com.vortexel.dangerzone.common.difficulty;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.DangerMath;
import com.vortexel.dangerzone.common.util.Reflector;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.config.EntityConfig;
import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class ForgeEntityModifier implements IEntityModifier {

    private static final int OP_ADD = 0;
    private static final int OP_ADD_MULTIPLY = 1;
    private static final int OP_MULTIPLY = 2;

    private static final String FIELD_EXPLOSION_RADIUS = "explosionRadius"; // "field_82226_g"

    private final EntityLivingBase entity;

    public ForgeEntityModifier(EntityLivingBase entity) {
        this.entity = entity;
    }

    @Override
    public void modify(ModifierType modifierType, double initialAmount) {
        val event = new ModifierAppliedEvent(entity, modifierType, initialAmount);
        if (!MinecraftForge.EVENT_BUS.post(event) && event.amount > 0) {
            selectApplicator(modifierType).accept(event.amount);
        }
    }

    @Override
    public IDangerLevel getCapability() {
        return entity.getCapability(DangerZone.CAP_DANGER_LEVEL, null);
    }

    @Override
    public EntityConfig getEntityConfig() {
        return DangerZone.proxy.getEntityConfigManager().getConfigDynamic(entity.getClass());
    }

    @Override
    public Random getRNG() {
        return entity.getRNG();
    }

    @Override
    public double getDifficultyAtLocation() {
        val ePos = entity.getPosition();
        return DangerZone.proxy.getDifficulty(entity.getEntityWorld(), ePos.getX(), ePos.getZ());
    }

    @Override
    public String getEntityClassName() {
        return entity.getClass().getName();
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
            case SPARE:
                return this::applySpare;
            default:
                return null;
        }
    }

    public void applyAttributeModifier(IAttribute attr, UUID uuid, String name, double amount, int operation) {
        // If it's nearly zero, then just ignore it.
        if (amount <= Consts.NOT_ZERO) {
            return;
        }
        if (operation == OP_MULTIPLY) {
            // The implementation automatically adds a 1D on for us
            amount -= 1D;
        }
        // Not all entities have all Attributes (e.g. Pigs can't attack).
        val attrInstance = entity.getAttributeMap().getAttributeInstance(attr);
        if (attrInstance == null) {
            return;
        }
        val modifier = new AttributeModifier(uuid, DangerZone.NAME + "-" + name, amount, operation);
        if (attrInstance.hasModifier(modifier)) {
            DangerZone.getLog().warn("Entity already has attribute applied.");
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
                "attack", amount, OP_MULTIPLY);
    }

    private void applyAttackSpeed(double amount) {
        applyAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE, Consts.MODIFIER_ATTACK_SPEED_UUID,
                "attack-speed", amount, OP_MULTIPLY);
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

    private void applySpare(double amount) {
        // Does nothing
    }

    //endregion
}
