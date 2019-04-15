package com.vortexel.dangerzone.common;

import net.minecraft.world.biome.Biome;

/**
 * Compare two biomes to see if they are "equal" based on their base biome types.
 * For example: Extreme Hills and Extreme Hills with Trees would be considered equal.
 */
public class BiomeComparator {
    public static boolean equal(Biome b1, Biome b2) {
        return b1.getBiomeClass() == b2.getBiomeClass();
//        return getBiomeBaseId(b1) == getBiomeBaseId(b2);
    }

    public static int getBiomeBaseId(Biome b) {
        if (b.isMutation()) {
            return Biome.MUTATION_TO_BASE_ID_MAP.get(b);
        } else {
            return Biome.getIdForBiome(b);
        }
    }
}
