package com.vortexel.dangerzone.common.config;

import com.google.gson.*;
import com.vortexel.dangerzone.common.util.JsonUtil;
import lombok.Value;
import lombok.val;

import java.lang.reflect.Type;

import static com.vortexel.dangerzone.common.util.JsonUtil.getOrDefault;

@Value
public class ModifierConf {

    public static final int DEFAULT_MIN_LEVEL = 0;
    public static final int DEFAULT_MAX_LEVEL = -1;
    public static final float DEFAULT_MIN_CHANCE = 0f;
    public static final float DEFAULT_MAX_CHANCE = 1f;
    public static final float DEFAULT_DANGER_SCALE = 1f;
    public static final float DEFAULT_MIN = 0f;
    public static final float DEFAULT_MAX = 1024f;

    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_MIN_LEVEL = "min_level";
    private static final String KEY_MAX_LEVEL = "max_level";
    private static final String KEY_DANGER_SCALE = "danger_scale";
    private static final String KEY_MAX = "max";
    private static final String KEY_CHANCE = "chance";
    private static final String KEY_RANGE = "range";

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

    public final float dangerScale;
    public final float min;
    public final float max;

    public static ModifierConf makeDisabled() {
        return new ModifierConf(false, DEFAULT_MIN_LEVEL, DEFAULT_MAX_LEVEL, DEFAULT_MIN_CHANCE,
                DEFAULT_MAX_CHANCE, DEFAULT_DANGER_SCALE, DEFAULT_MIN, DEFAULT_MAX);
    }

    public static class Serializer implements JsonSerializer<ModifierConf>, JsonDeserializer<ModifierConf> {
        @Override
        public JsonElement serialize(ModifierConf src, Type typeOfSrc, JsonSerializationContext context) {
            val obj = new JsonObject();
            obj.addProperty(KEY_ENABLED, src.enabled);
            obj.addProperty(KEY_MIN_LEVEL, src.minLevel);
            obj.addProperty(KEY_MAX_LEVEL, src.maxLevel);
            obj.addProperty(KEY_DANGER_SCALE, src.dangerScale);
            obj.add(KEY_RANGE, JsonUtil.arrayOf(src.min, src.max));
            obj.add(KEY_CHANCE, JsonUtil.arrayOf(src.minChance, src.maxChance));
            return obj;
        }

        @Override
        public ModifierConf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonPrimitive()) {
                val enabled = json.getAsJsonPrimitive().getAsBoolean();
                return new ModifierConf(enabled, DEFAULT_MIN_LEVEL, DEFAULT_MAX_LEVEL, DEFAULT_MIN_CHANCE,
                        DEFAULT_MAX_CHANCE, DEFAULT_DANGER_SCALE, DEFAULT_MIN, DEFAULT_MAX);
            } else if (json.isJsonObject()) {
                val obj = json.getAsJsonObject();
                val enabled = getOrDefault(obj, KEY_ENABLED, true);
                val minLevel = getOrDefault(obj, KEY_MIN_LEVEL, DEFAULT_MIN_LEVEL);
                val maxLevel = getOrDefault(obj, KEY_MAX_LEVEL, DEFAULT_MAX_LEVEL);
                val dangerScale = getOrDefault(obj, KEY_DANGER_SCALE, DEFAULT_DANGER_SCALE);

                val valueRange = JsonUtil.readPair(obj, KEY_RANGE, DEFAULT_MIN, DEFAULT_MAX,
                        JsonUtil.SingleValue.RIGHT);
                val chanceRange = JsonUtil.readPair(obj, KEY_CHANCE, DEFAULT_MIN_CHANCE, DEFAULT_MAX_CHANCE,
                        JsonUtil.SingleValue.ERROR);
                return new ModifierConf(enabled, minLevel, maxLevel, chanceRange.getLeft(), chanceRange.getRight(),
                        dangerScale, valueRange.getLeft(), valueRange.getRight());
            } else {
                throw new JsonParseException("ModifierConf must be either a boolean or object");
            }
        }
    }
}
