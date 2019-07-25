package com.vortexel.dangerzone.common.difficulty;

public enum ModifierType {
    MAX_HEALTH(1),
    REGENERATION(2),
    ATTACK_DAMAGE(3),
    ATTACK_SPEED(4),
    MOVE_SPEED(5),
    FLY_SPEED(6),
    ARMOR(7),
    ARMOR_TOUGHNESS(8),
    WITHER(9),
    EXPLOSION_RADIUS(10),
    SPARE(12);

    public final int id;

    ModifierType(int id) {
        this.id = id;
    }
}
