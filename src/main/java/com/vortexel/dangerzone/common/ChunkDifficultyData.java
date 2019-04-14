package com.vortexel.dangerzone.common;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;

public class ChunkDifficultyData {

    private ChunkPos pos;
    private Biome biome;
    private double difficulty;

    public ChunkDifficultyData(ChunkPos pos, Biome biome, double difficulty) {
        this.pos = pos;
        this.biome = biome;
        this.difficulty = difficulty;
    }

    public static ChunkDifficultyData fromWorld(World world, DifficultyMap difficultyMap, ChunkPos pos) {
        HashMap<Integer, Integer> biomeCount = new HashMap<>();
        // Default value
        biomeCount.put(-1, -1);

        Biome[] biomes = new Biome[16 * 16];
        biomes = world.getBiomeProvider().getBiomes(biomes, pos.getXStart(), pos.getZStart(), 16, 16);
        for (Biome b : biomes) {
            int biomeId = BiomeComparator.getBiomeBaseId(b);
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
        for (int i = 0; i < difficulties.length; i++) {
            sum += difficulties[i];
        }

        return new ChunkDifficultyData(pos, Biome.getBiomeForId(bestBiome), sum / difficulties.length);
    }

    public Biome getBiome() {
        return biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    public ChunkPos getPos() {
        return pos;
    }

    public void setPos(ChunkPos pos) {
        this.pos = pos;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }
}
