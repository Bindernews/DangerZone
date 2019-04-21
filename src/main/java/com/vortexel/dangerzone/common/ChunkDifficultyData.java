package com.vortexel.dangerzone.common;

import lombok.Value;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;

@Value
public class ChunkDifficultyData {

    private ChunkPos pos;
    private Biome biome;
    private double difficulty;

    public static ChunkDifficultyData fromWorld(World world, DifficultyMap difficultyMap, ChunkPos pos) {
        HashMap<Integer, Integer> biomeCount = new HashMap<>();
        // Default value
        biomeCount.put(-1, -1);

        Biome[] biomes = world.getBiomeProvider().getBiomesForGeneration(null,
                pos.getXStart(), pos.getZStart(), 16, 16);
        for (Biome b : biomes) {
            final int biomeId = Biome.getIdForBiome(b);
            biomeCount.putIfAbsent(biomeId, 0);
            biomeCount.put(biomeId, biomeCount.get(biomeId) + 1);
        }

        // Determine the biome with the most blocks in the chunk. In case of a tie, it picks randomly.
        int bestBiome = -1;
        for (Integer biomeId : biomeCount.keySet()) {
            if (biomeCount.get(biomeId) > bestBiome) {
                bestBiome = biomeId;
            }
        }

        double[] difficulties = new double[16 * 16];
        difficultyMap.getRawRegion(difficulties, pos.getXStart(), pos.getZStart(), 16, 16);
        double sum = 0.0;
        for (double v : difficulties) {
            sum += v;
        }

        return new ChunkDifficultyData(pos, Biome.getBiomeForId(bestBiome), sum / difficulties.length);
    }
}
