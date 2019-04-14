package com.vortexel.dangerzone.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;
import java.util.WeakHashMap;

public class DifficultyMap {

    private static final double SCALE_FACTOR = 0.5;
    private static final int NEIGHBOR_SIZE = 7;
    private static final int CENTER_INDEX = 4 * NEIGHBOR_SIZE + 4;
    private static final int CHUNK_SIZE = 16;

    private World world;
    private NoiseGeneratorPerlin generator;
    private WeakHashMap<ChunkPos, ChunkDifficultyData> weakChunkInfoCache;

    public DifficultyMap(World world) {
        this.world = world;
        generator = new NoiseGeneratorPerlin(new Random(world.getSeed()), 1);
        weakChunkInfoCache = new WeakHashMap<>();
    }

    /**
     * Determine the difficulty for a specific column of blocks at (x, z).
     * @param x
     * @param z
     * @return the difficulty value, between 1 and 16
     */
    public double getDifficulty(int x, int z) {
        int minX = x - NEIGHBOR_SIZE / 2;
        int minZ = z - NEIGHBOR_SIZE / 2;

        // The biome of our block. We only want to average with chunks who have the same biome.
        Biome targetBiome = world.getBiome(new BlockPos(x, 60, z));
        double sumDifficulty = 0.0;
        int usedCount = 0;

        for (int zo = 0; zo < NEIGHBOR_SIZE; zo++) {
            for (int xo = 0; xo < NEIGHBOR_SIZE; xo++) {
                final ChunkPos key = new ChunkPos((minX + xo) / CHUNK_SIZE, (minZ + zo) / CHUNK_SIZE);
                ChunkDifficultyData cdd = computeChunkInfo(key);
                if (BiomeComparator.equal(targetBiome, cdd.getBiome())) {
                    sumDifficulty += cdd.getDifficulty();
                    usedCount += 1;
                }
            }
        }

        return sumDifficulty / usedCount;
    }

    public double getRaw(int x, int z) {
        return generator.getValue((double)x * SCALE_FACTOR, (double)z * SCALE_FACTOR);
    }

    public double[] getRawRegion(double[] out_values, int minX, int minZ, int width, int depth) {
        if (out_values == null || out_values.length < width * depth) {
            out_values = new double[width * depth];
        }
        for (int zo = 0; zo < depth; zo++) {
            for (int xo = 0; xo < width; xo++) {
                out_values[zo * depth + xo] = getRaw(minX + xo, minZ + zo);
            }
        }
        return out_values;
    }

    private ChunkDifficultyData computeChunkInfo(ChunkPos pos) {
        ChunkDifficultyData info = weakChunkInfoCache.get(pos);
        if (info != null) {
            return info;
        }
        ChunkDifficultyData cdd = ChunkDifficultyData.fromWorld(world, this, pos);
        weakChunkInfoCache.put(pos, cdd);
        return cdd;
    }
}
