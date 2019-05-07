package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.vortexel.dangerzone.common.difficulty.ModifierType;
import com.vortexel.dangerzone.common.util.JsonUtil;
import lombok.val;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityConfig {
    private static final String INHERITS = "inherits";

    public List<ResourceLocation> inherits;
    public Map<ModifierType, ModifierConf> modifiers;

    public EntityConfig(ResourceLocation... inherits) {
        this.inherits = Lists.newArrayList(inherits);
        this.modifiers = Maps.newEnumMap(ModifierType.class);
    }

    public EntityConfig(List<ResourceLocation> inherits) {
        this.inherits = Lists.newArrayList(inherits);
        this.modifiers = Maps.newEnumMap(ModifierType.class);
    }


    /**
     * Merges {@code other} into this object, overwriting values in {@code this}.
     */
    public void merge(EntityConfig other) {
        if (other.inherits != null) {
            inherits = other.inherits;
        }
        modifiers.putAll(other.modifiers);
    }


    public static class Serializer implements JsonSerializer<EntityConfig>, JsonDeserializer<EntityConfig> {
        @Override
        public EntityConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (!json.isJsonObject()) {
                throw new JsonParseException("Element must be JSON object");
            }
            val obj = json.getAsJsonObject();
            val result = new EntityConfig();
            result.inherits = JsonUtil.readStringList(obj, INHERITS)
                    .stream().map(ResourceLocation::new).collect(Collectors.toList());
            for (val entry : obj.entrySet()) {
                val modifier = Stream.of(ModifierType.values())
                        .filter((v) -> v.name().equalsIgnoreCase(entry.getKey())).findAny();
                if (modifier.isPresent()) {
                    val conf = context.<ModifierConf>deserialize(entry.getValue(), ModifierConf.class);
                    result.modifiers.put(modifier.get(), conf);
                }
            }
            return result;
        }

        @Override
        public JsonElement serialize(EntityConfig src, Type typeOfSrc, JsonSerializationContext context) {
            val obj = new JsonObject();
            if (src.inherits != null) {
                obj.addProperty(INHERITS, src.inherits.toString());
            }
            for (val entry : src.modifiers.entrySet()) {
                obj.add(entry.getKey().name().toLowerCase(), context.serialize(entry.getValue()));
            }
            return obj;
        }
    }
}
