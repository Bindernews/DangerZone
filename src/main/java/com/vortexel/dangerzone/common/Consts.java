package com.vortexel.dangerzone.common;

import net.minecraft.entity.ai.attributes.RangedAttribute;

import java.util.UUID;

public final class Consts {

    public static final int MAX_DANGER_LEVEL = (1 << 15);
    public static final int STACK_SIZE = 64;

    /**
     * Mouse button left
     */
    public static final int MOUSE_LEFT = 0;
    public static final int MOUSE_RIGHT = 1;

    //region modifier UUIDs

    public static final UUID MODIFIER_MAX_HEALTH_UUID = UUID.fromString("6d9f6292-6e4c-42a1-b538-53be07b3b076");
    public static final UUID MODIFIER_MOVE_SPEED_UUID = UUID.fromString("1bb0a9c6-622b-4327-92df-c3811013ea51");
    public static final UUID MODIFIER_FLY_SPEED_UUID = UUID.fromString("77c036fd-d72e-4424-9215-001a3084b3a9");
    public static final UUID MODIFIER_ATTACK_DAMAGE_UUID = UUID.fromString("b74abb55-069e-48e6-8158-cf4bf7a6b543");
    public static final UUID MODIFIER_ATTACK_SPEED_UUID = UUID.fromString("9c7bd5e9-f98c-4af5-9811-03a5fc4a1da8");
    public static final UUID MODIFIER_ARMOR_UUID = UUID.fromString("6bdc3d1a-6baf-4ece-9ce7-8ed5ce139582");
    public static final UUID MODIFIER_ARMOR_TOUGHNESS_UUID = UUID.fromString("6a5ca8e0-3e1c-4e11-a213-6a9efe516552");
    public static final UUID MODIFIER_DECAY_TOUCH_UUID = UUID.fromString("91cfb822-be8a-4ba9-adf3-cfae53d77973");

    //endregion modifier UUIDs

    public static final RangedAttribute ATTRIBUTE_DECAY_TOUCH = new RangedAttribute(null,
            "effect.decayTouch", 0, 0, 5.0);

    //region time
    public static final int TICKS_PER_SECOND = 20;
    public static final int TICKS_PER_DAY = TICKS_PER_SECOND * 60 * 60 * 24;
    public static final int DAYS_PER_YEAR = 365;
    public static final int TICKS_PER_YEAR = TICKS_PER_DAY * DAYS_PER_YEAR;

    /**
     * The duration to use if you want a potion to last effectively forever. Technically this will make a potion
     * last for 3 years, which is the longest number of years I can get without overflowing an integer.
     */
    public static final int POTION_DURATION_FOREVER = TICKS_PER_YEAR * 3;
    //endregion time

    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_SIZE_SQ = CHUNK_SIZE * CHUNK_SIZE;

    public static final double EPSILON = 1e-12;
    public static final double NOT_ONE = 1.0 - EPSILON;
    public static final double NOT_ZERO = 0.0 + EPSILON;

    public static final int COLOR_BLACK = 0x000000;
}
