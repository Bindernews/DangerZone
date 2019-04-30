package com.vortexel.dangerzone.common.config;

import com.google.gson.*;
import lombok.Value;
import lombok.val;
import lombok.var;

import java.lang.reflect.Type;

import static com.vortexel.dangerzone.common.util.JsonUtil.getOrDefault;

@Value
public class ModifierConf {

    public static final int DEFAULT_MIN_LEVEL = 0;
    public static final int DEFAULT_MAX_LEVEL = -1;
    public static final float DEFAULT_SCALE = 1f;
    public static final float DEFAULT_MIN_CHANCE = 0f;
    public static final float DEFAULT_MAX_CHANCE = 1f;
    public static final float DEFAULT_DANGER_SCALE = 1f;
    public static final float DEFAULT_MAX = 1024f;

    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_MIN_LEVEL = "min_level";
    private static final String KEY_MAX_LEVEL = "max_level";
    private static final String KEY_SCALE = "scale";
    private static final String KEY_DANGER_SCALE = "danger_scale";
    private static final String KEY_MAX = "max";
    private static final String KEY_CHANCE = "chance";

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

    public static ModifierConf makeDisabled() {
        return new ModifierConf(false, DEFAULT_MIN_LEVEL, DEFAULT_MAX_LEVEL, DEFAULT_MIN_CHANCE,
                DEFAULT_MAX_CHANCE, DEFAULT_SCALE, DEFAULT_DANGER_SCALE, DEFAULT_MAX);
    }

    public static class Serializer implements JsonSerializer<ModifierConf>, JsonDeserializer<ModifierConf> {
        @Override
        public JsonElement serialize(ModifierConf src, Type typeOfSrc, JsonSerializationContext context) {
            val obj = new JsonObject();
            obj.addProperty(KEY_ENABLED, src.enabled);
            obj.addProperty(KEY_MIN_LEVEL, src.minLevel);
            obj.addProperty(KEY_MAX_LEVEL, src.maxLevel);
            obj.addProperty(KEY_SCALE, src.scale);
            obj.addProperty(KEY_DANGER_SCALE, src.dangerScale);
            obj.addProperty(KEY_MAX, src.max);
            val chance = new JsonArray();
            chance.add(src.minChance);
            chance.add(src.maxChance);
            obj.add(KEY_CHANCE, chance);
            return obj;
        }

        @Override
        public ModifierConf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonPrimitive()) {
                val enabled = json.getAsJsonPrimitive().getAsBoolean();
                return new ModifierConf(enabled, DEFAULT_MIN_LEVEL, DEFAULT_MAX_LEVEL, DEFAULT_MIN_CHANCE,
                        DEFAULT_MAX_CHANCE, DEFAULT_SCALE, DEFAULT_DANGER_SCALE, DEFAULT_MAX);
            } else if (json.isJsonObject()) {
                val obj = json.getAsJsonObject();
                val enabled = getOrDefault(obj, KEY_ENABLED, true);
                val minLevel = getOrDefault(obj, KEY_MIN_LEVEL, DEFAULT_MIN_LEVEL);
                val maxLevel = getOrDefault(obj, KEY_MAX_LEVEL, DEFAULT_MAX_LEVEL);
                val scale = getOrDefault(obj, KEY_SCALE, DEFAULT_SCALE);
                val dangerScale = getOrDefault(obj, KEY_DANGER_SCALE, DEFAULT_DANGER_SCALE);
                val max = getOrDefault(obj, KEY_MAX, DEFAULT_MAX);

                // Parse chance as either an array [min, max] or a value <max>.
                var minChance = DEFAULT_MIN_CHANCE;
                var maxChance = DEFAULT_MAX_CHANCE;
                val objChance = obj.get(KEY_CHANCE);
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
                return new ModifierConf(enabled, minLevel, maxLevel, minChance, maxChance, scale,
                        dangerScale, max);
            } else {
                throw new JsonParseException("ModifierConf must be either a boolean or object");
            }
        }
    }
}
