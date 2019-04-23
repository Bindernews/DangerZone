package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import lombok.val;
import net.minecraftforge.common.config.Config;

import java.util.Map;

@Config(modid = DangerZone.ID, type = Config.Type.INSTANCE, name = DangerZone.ID + "_biome")
public class BiomeConfig {

    @Config.Comment({"A map of arbitrary group names to lists of biome IDs. If two biome IDs are in the same list,",
            "they will be considered the same for the purposes of the mod."})
    public static Map<String, Integer[]> biomes;

    @Config.Ignore
    protected static Map<Integer, Integer> biomeIdToGroup;
    @Config.Ignore
    protected static Map<String, Integer> groupNameToId;

    static {
        initialize();
    }

    public static void initialize() {
        biomes = Maps.newHashMap();
        biomeIdToGroup = Maps.newHashMap();
        groupNameToId = Maps.newHashMap();

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

    private static void put(String key, Integer... elements) {
        biomes.put(key, elements);
    }

    /**
     * Returns the group ID of the biome with ID {@code biomeId}, or -1 if that biome has no group.
     */
    public static int getGroupId(int biomeId) {
        return biomeIdToGroup.getOrDefault(biomeId, -1);
    }

    /**
     * Returns true if two biome IDs are in the same group.
     */
    public static boolean areGrouped(int biomeA, int biomeB) {
        val groupA = getGroupId(biomeA);
        val groupB = getGroupId(biomeB);
        // && groupB != -1 is implied because if groupB == -1 && groupA == groupB then groupA == -1
        return groupA != -1 && groupA == groupB;
    }

    /**
     * Call this after the config has been loaded. This populates the reverse-lookup maps to improve performance.
     */
    public static void afterLoad() {
        biomeIdToGroup.clear();
        groupNameToId.clear();
        int groupId = 1;
        for (val entry : biomes.entrySet()) {
            groupNameToId.put(entry.getKey(), groupId);
            for (val biomeId : entry.getValue()) {
                biomeIdToGroup.put(biomeId, groupId);
            }
            groupId += 1;
        }
    }
}
