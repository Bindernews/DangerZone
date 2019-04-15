package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BiomesConfig implements ConfigHelper.Loadable {

    public Map<String, Set<Integer>> biomeGroups;
    public Map<Integer, String> biomeIdToGroup;

    public BiomesConfig() {
        biomeGroups = Maps.newHashMap();
        biomeIdToGroup = Maps.newHashMap();

        // Initialize the default values
        put("ocean", 0, 10, 24);
        put("plains", 1, 129);
        put("desert", 2, 17, 130);
        put("hills", 3, 20, 34, 131);
        put("forest", 4, 132);
        put("taiga", 5, 19, 30, 31, 32, 33, 133, 158, 160, 161);
        put("swamp", 6, 134);
        put("river", 7, 10);
        put("hell", 8);
        put("sky", 9);
        put("ice", 12, 13, 140);
        put("mushroom", 14, 15);
        put("beach", 16, 25, 26);
        put("jungle", 21, 22, 23, 149, 151);
        put("birch", 27, 28, 155, 156);
        put("roofed_forest", 29, 157);
        put("savanna", 35, 36, 163, 164);
        put("mesa", 37, 38, 39, 165, 166, 167);
        put("void", 127);
    }

    private void put(String key, Integer... elements) {
        biomeGroups.put(key, Sets.newHashSet(elements));
    }

    public void load(ConfigCategory cat) {
        // Add default values
        for (Map.Entry<String, Set<Integer>> group : biomeGroups.entrySet()) {
            final String[] defaultValues = ConfigHelper.toStringArray(
                    ConfigHelper.integerToIntList(group.getValue()));
            final Property orElse = new Property(group.getKey(), defaultValues, Property.Type.INTEGER);
            ConfigHelper.getProp(cat, group.getKey(), orElse);
        }
        // Now read the real values
        biomeGroups.clear();
        biomeIdToGroup.clear();
        Set<Integer> knownIds = Sets.newHashSet();
        for (Map.Entry<String, Property> group : cat.entrySet()) {
            List<Integer> biomeIdList = ConfigHelper.intToIntegerList(group.getValue().getIntList());
            Set<Integer> biomeIds = Sets.newHashSet(biomeIdList);
            if (!Sets.intersection(knownIds, biomeIds).isEmpty()) {
                throw new RuntimeException("Same biome ID specified in multiple groups");
            }
            // Add the forward map
            biomeGroups.put(group.getKey(), biomeIds);
            // Add the backwards map
            for (Integer id : biomeIds) {
                biomeIdToGroup.put(id, group.getKey());
            }
            // Update the config values
            group.getValue().set(ConfigHelper.integerToIntList(biomeIdList));
        }
    }
}
