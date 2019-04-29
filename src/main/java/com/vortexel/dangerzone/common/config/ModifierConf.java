package com.vortexel.dangerzone.common.config;

import com.google.gson.*;
import lombok.Value;
import lombok.val;
import lombok.var;

import java.lang.reflect.Type;

@Value
public class ModifierConf {

    public static final int DEFAULT_MIN_LEVEL = 0;
    public static final int DEFAULT_MAX_LEVEL = -1;
    public static final float DEFAULT_SCALE = 1f;
    public static final float DEFAULT_MIN_CHANCE = 0f;
    public static final float DEFAULT_MAX_CHANCE = 1f;
    public static final float DEFAULT_DANGER_SCALE = 1f;
    public static final float DEFAULT_MAX = 1024f;

    public final boolean enabled;

    /**
     * Minimum level at which this modifier will be applied.
     */
    public final int minLevel;

    /**
     * Level at which this modifier maxes out. <br/>
     * If {@code maxLevel == -1} then it maxes out at the configured maximum level.
     */
    public final int maxLevel;

    /**
     * The chance for this modifier to be applied at the maximum level.
     */
    public final float minChance;
    public final float maxChance;

    /**
     * How much this value scales per level.
     */
    public final float scale;
    public final float dangerScale;
    public final float max;
    public final Operation maxOp;



    public static ModifierConf makeDisabled() {
        return new ModifierConf(false, 0, 0,  0, 0,0, 1, 0, Operation.LIMIT);
    }

    public enum Operation {
        TIMES,
        EXTRA,
        LIMIT,
        NONE;

        public static Operation fromString(String s) {
            switch (s.toLowerCase()) {
                case "*":
                case "times":
                    return TIMES;
                case "+":
                case "extra":
                case "add":
                    return EXTRA;
                case "=":
                case "limit":
                    return LIMIT;
                case "none":
                    return NONE;
                default:
                    return null;
            }
        }
    }

    public static class Serializer implements JsonDeserializer<ModifierConf> {
        @Override
        public ModifierConf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonPrimitive()) {
                val enabled = json.getAsJsonPrimitive().getAsBoolean();
                return new ModifierConf(enabled, DEFAULT_MIN_LEVEL, DEFAULT_MAX_LEVEL, DEFAULT_MIN_CHANCE,
                        DEFAULT_MAX_CHANCE, DEFAULT_SCALE, DEFAULT_DANGER_SCALE, 0f, Operation.NONE);
            } else if (json.isJsonObject()) {
                val obj = json.getAsJsonObject();
                val enabled = getOr(obj, "enabled", true).getAsBoolean();
                val minLevel = getOr(obj, "min_level", DEFAULT_MIN_LEVEL).getAsInt();
                val maxLevel = getOr(obj, "max_level", DEFAULT_MAX_LEVEL).getAsInt();
                val scale = getOr(obj, "scale", DEFAULT_SCALE).getAsFloat();
                val dangerScale = getOr(obj, "danger_scale", DEFAULT_DANGER_SCALE).getAsFloat();

                // Parse chance as either an array [min, max] or a value <max>.
                var minChance = DEFAULT_MIN_CHANCE;
                var maxChance = DEFAULT_MAX_CHANCE;
                val objChance = obj.get("chance");
                if (objChance != null) {
                    if (objChance.isJsonPrimitive()) {
                        maxChance = objChance.getAsFloat();
                    } else if (objChance.isJsonArray()) {
                        val ar = objChance.getAsJsonArray();
                        minChance = ar.get(0).getAsFloat();
                        maxChance = ar.get(1).getAsFloat();
                    } else {
                        throw new JsonParseException("\"chance\" must be either a number or an array of 2 values");
                    }
                }

                // Parse max and maxOp
                float max = DEFAULT_MAX;
                Operation maxOp = Operation.NONE;
                if (obj.has("max")) {
                    val maxArray = obj.getAsJsonArray("max");
                    max = maxArray.get(0).getAsFloat();
                    maxOp = Operation.fromString(maxArray.get(1).getAsString());
                }
                if (maxOp == null) {
                    throw new JsonParseException("\"max\" must be an array with two values");
                }
                return new ModifierConf(enabled, minLevel, maxLevel, minChance, maxChance, scale,
                        dangerScale, max, maxOp);
            } else {
                throw new JsonParseException("ModifierConf must be either a boolean or object");
            }
        }

        private JsonPrimitive getOr(JsonObject obj, String field, Boolean defaultValue) {
            return obj.has(field) ? obj.get(field).getAsJsonPrimitive() : new JsonPrimitive(defaultValue);
        }

        private JsonPrimitive getOr(JsonObject obj, String field, Number defaultValue) {
            return obj.has(field) ? obj.get(field).getAsJsonPrimitive() : new JsonPrimitive(defaultValue);
        }
    }
}
