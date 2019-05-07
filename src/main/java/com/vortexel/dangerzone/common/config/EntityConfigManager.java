package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vortexel.dangerzone.common.difficulty.ModifierType;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.GameData;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class EntityConfigManager {
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(ModifierConf.class, new ModifierConf.Serializer())
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(EntityConfig.class, new EntityConfig.Serializer())
            .create();
    private static final Type MAP_TYPE = new TypeToken<Map<ResourceLocation, EntityConfig>>(){}.getType();
    private static final ResourceLocation RES_GENERIC_CREATURE = new ResourceLocation("generic:creature");
    private static final ResourceLocation RES_GENERIC_ANIMAL = new ResourceLocation("generic:animal");
    private static final ResourceLocation RES_GENERIC_HOSTILE = new ResourceLocation("generic:hostile");


    private Map<ResourceLocation, EntityConfig> tempConfig;
    private Map<ResourceLocation, EntityConfig> bakedConfig;
    private Set<ResourceLocation> baking;

    public EntityConfigManager() {
        tempConfig = Maps.newHashMap();
        baking = null;
        bakedConfig = null;
    }

    /**
     * Get the EntityConfig for a known resource name. If this doesn't exist, returns {@code null}.
     */
    public EntityConfig getConfig(ResourceLocation entityResource) {
        return bakedConfig.get(entityResource);
    }

    /**
     * Get the EntityConfig for the {@code entityClass}, or its closest parent-class that has a known
     * {@link EntityConfig}. If nothing is found, returns {@code null}.
     *
     * @param entityClass the class of entity to search for configs for
     * @return the {@link EntityConfig} for {@code entityClass} or {@code null} if nothing could be found.
     */
    @SuppressWarnings("unchecked")
    public EntityConfig getConfigDynamic(Class<? extends Entity> entityClass) {
        Class<? extends Entity> eClass = entityClass;
        while (eClass != Entity.class && !bakedConfig.containsKey(getEntityName(entityClass))) {
            eClass = (Class<? extends Entity>)eClass.getSuperclass();

            // If we reach one of these classes, use the default values.
            // Basically, if we find a mob/animal with no custom overrides, use the default.
            ResourceLocation defaultValue = null;
            if (eClass == EntityMob.class) {
                defaultValue = RES_GENERIC_HOSTILE;
            } else if (eClass == EntityCreature.class) {
                defaultValue = RES_GENERIC_CREATURE;
            } else if (eClass == EntityAnimal.class) {
                defaultValue = RES_GENERIC_ANIMAL;
            }
            // If we're using a default value for this entity, then add it to the list of baked values
            if (defaultValue != null) {
                // Update the baked config
                val originalName = getEntityName(entityClass);
                if (originalName != null) {
                    bakedConfig.put(originalName, bakedConfig.get(defaultValue));
                }
                return bakedConfig.get(defaultValue);
            }
        }
        return bakedConfig.get(getEntityName(eClass));
    }

    public void addFile(Reader source) {
        mergeConfig(GSON.fromJson(source, MAP_TYPE));
    }

    public void addFile(String source) {
        mergeConfig(GSON.fromJson(source, MAP_TYPE));
    }

    private void mergeConfig(Map<ResourceLocation, EntityConfig> configMap) {
        for (val entry : configMap.entrySet()) {
            val eConfig = tempConfig.getOrDefault(entry.getKey(), new EntityConfig());
            eConfig.merge(entry.getValue());
            tempConfig.put(entry.getKey(), eConfig);
        }
    }

    public boolean isBaked() {
        return bakedConfig != null;
    }

    public Collection<ResourceLocation> getBakedEntityNames() {
        return bakedConfig.keySet();
    }

    /**
     * Bake the config values so that they can be quickly accessed.
     */
    public void bake() {
        baking = Sets.newHashSet();
        bakedConfig = Maps.newHashMap();

        // Pre-bake specific resource so we can make sure they exist for dynamic resolution.
        bakeName(RES_GENERIC_CREATURE);
        bakeName(RES_GENERIC_ANIMAL);
        bakeName(RES_GENERIC_HOSTILE);

        for (ResourceLocation eName : tempConfig.keySet()) {
            if (!isEntityReal(eName)) {
                continue;
            }
            bakeName(eName);
        }
    }

    private void bakeName(ResourceLocation name) {
        // If we've already baked this value, then do nothing
        if (bakedConfig.containsKey(name)) {
            return;
        }
        // If we're currently trying to bake this, but now we're trying to bake it again, that's a problem.
        if (baking.contains(name)) {
            throw new RuntimeException("Recursive \"inherits\" loop in entity config.");
        }
        baking.add(name);

        val eConfig = new EntityConfig();
        for (val mod : ModifierType.values()) {
            // If we can't resolve a modifier for the entity, disable it for that entity
            val modConf = resolveModifier(name, mod);
            if (modConf != null) {
                eConfig.modifiers.put(mod, modConf);
            } else {
                eConfig.modifiers.put(mod, ModifierConf.makeDisabled());
            }
        }
        baking.remove(name);
        bakedConfig.put(name, eConfig);
    }

    /**
     * Determine the {@link ModifierConf} for type {@code modifier} for the referenced {@link EntityConfig}.
     * This handles modifier inheritance as well.
     *
     * @param location name of the {@link EntityConfig} to resolve for
     * @param modifier type of modifier to be resolved
     * @return the configuration for this modifier, for this {@link EntityConfig} or {@code null}
     */
    private ModifierConf resolveModifier(@Nonnull ResourceLocation location, ModifierType modifier) {
        val entityCfg = tempConfig.get(location);
        if (entityCfg == null) {
            return null;
        }
        if (entityCfg.modifiers.containsKey(modifier)) {
            return entityCfg.modifiers.get(modifier);
        }
        if (entityCfg.inherits != null) {
            for (val name : entityCfg.inherits) {
                bakeName(name);
            }
            ModifierConf resolved = null;
            for (int i = entityCfg.inherits.size() - 1; i >= 0 && resolved == null; i--) {
                resolved = resolveModifier(entityCfg.inherits.get(i), modifier);
            }
            return resolved;
        }
        return null;
    }

    //region Entity registry helpers

    private static boolean isEntityReal(ResourceLocation name) {
        return GameData.getEntityRegistry().containsKey(name);
    }

    private static Collection<ResourceLocation> getAllEntityNames() {
        return GameData.getEntityRegistry().getKeys();
    }

    private static Class<? extends Entity> getEntityClass(ResourceLocation resource) {
        return GameData.getEntityRegistry().getValue(resource).getEntityClass();
    }

    private static ResourceLocation getEntityName(Class<? extends Entity> entityClass) {
        return GameData.getEntityClassMap().get(entityClass).getRegistryName();
    }

    //endregion Entity registry helpers
}
