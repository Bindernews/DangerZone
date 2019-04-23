package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.common.config.BiomeConfig;
import com.vortexel.dangerzone.common.config.DZConfig;
import net.minecraft.world.biome.Biome;

import java.util.Map;

/**
 * Compare two biomes to see if they are "equal" based on their base biome types.
 * For example: Extreme Hills and Extreme Hills with Trees would be considered equal.
 */
public class BiomeComparator {
    public static boolean equal(int b1Id, int b2Id) {
        return BiomeConfig.areGrouped(b1Id, b2Id);
    }

    public static boolean equal(Biome b1, Biome b2) {
        return equal(Biome.getIdForBiome(b1), Biome.getIdForBiome(b2));
    }
}
