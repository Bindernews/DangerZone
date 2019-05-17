package com.vortexel.dangerzone.common.difficulty;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.config.BiomeConfig;
import com.vortexel.dangerzone.common.config.DZConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;
import java.util.WeakHashMap;

/**
 * Generates the difficulty map for a given world. It simply returns values between 0 and 1 based on where
 * the location is in the world.
 *
 *
 */
public class DifficultyMap {

    private static final int NEIGHBOR_SIZE = 5;

    private IWorldAdapter world;
    private NoiseGeneratorPerlin generator;
    private WeakHashMap<ChunkPos, ChunkDifficultyData> weakChunkInfoCache;
    private DZConfig.WorldConfig worldConfig;

    public DifficultyMap(IWorldAdapter world) {
        this.world = world;
        generator = new NoiseGeneratorPerlin(new Random(world.getSeed()), 1);
        weakChunkInfoCache = new WeakHashMap<>();
        worldConfig = DZConfig.getWorld(world.getDimension());
    }

    /**
     * Determine the difficulty for a specific column of blocks at (x, z).
     * @param x block X
     * @param z block Z
     * @return the difficulty value, between 0.0 inclusive and 1.0 exclusive
     */
    public double getDifficulty(int x, int z) {
        // This should look like a list where each function takes as input the output of the previous call.
        // If any modifiers are added or removed, they should be done here.
        double d = 0.0;
        d = genChunkDifficulty(d, x, z);
        d = adjustIndividual(d, x, z);
        d = adjustForSpawn(d, x, z);
        d = adjustClamp(d, x, z);
        return d;
    }

    /**
     * Adjust the difficulty of the area based on distance from spawn.
     * @param d input difficulty
     * @param x block X
     * @param z block Z
     * @return adjusted difficulty
     */
    private double adjustForSpawn(double d, int x, int z) {
        BlockPos bp = world.getSpawnPoint();
        double distSq = bp.distanceSq(x, bp.getY(), z);
        double spawnRadiusSq = pow2(worldConfig.spawnRadius);
        double transitionRadiusSq = pow2(worldConfig.spawnRadius + worldConfig.spawnTransitionRadius);
        // difficulty = 0 in spawn area
        if (distSq <= spawnRadiusSq) {
            return 0;
        }
        // Deal with areas within the transition radius
        if (distSq <= transitionRadiusSq) {
            return d * (distSq - spawnRadiusSq) / (transitionRadiusSq - spawnRadiusSq);
        }
        return d;
    }

    private double adjustClamp(double d, int x, int z) {
        return MathHelper.clamp(d, 0.0, Consts.NOT_ONE);
    }

    private double adjustIndividual(double d, int x, int z) {
        return (getRaw(x, z) * 0.1) + (d * 0.9);
    }

    private double genChunkDifficulty(double d, int x, int z) {
        return getChunkDifficulty(new ChunkPos(x / 16, z / 16));
    }

    private double getDifficultySpecific(int x, int z) {
        int minX = x - NEIGHBOR_SIZE / 2;
        int minZ = z - NEIGHBOR_SIZE / 2;

        // The biome of our block. We only want to average with chunks who have the same biome.
        Biome targetBiome = world.getBiome(x, z);
        double sumDifficulty = 0.0;
        int usedCount = 0;
        final int targetBiomeId = Biome.getIdForBiome(targetBiome);

        for (int zo = 0; zo < NEIGHBOR_SIZE; zo++) {
            for (int xo = 0; xo < NEIGHBOR_SIZE; xo++) {
                final ChunkPos key = new ChunkPos((minX + xo) / Consts.CHUNK_SIZE,
                        (minZ + zo) / Consts.CHUNK_SIZE);
                ChunkDifficultyData cdd = computeChunkInfo(key);
                if (BiomeConfig.areGrouped(targetBiomeId, Biome.getIdForBiome(cdd.getBiome()))) {
                    sumDifficulty += cdd.getDifficulty();
                    usedCount += 1;
                }
            }
        }

        // If we're in a river or something, just use the difficulty of the current chunk
        if (usedCount < 5) {
            final ChunkPos key = new ChunkPos(x, z);
            sumDifficulty = computeChunkInfo(key).getDifficulty();
            usedCount = 1;
        }

        return sumDifficulty / usedCount;
    }

    public double getRaw(int x, int z) {
        double v = generator.getValue((double)x * DZConfig.general.stretchFactor,
                (double)z * DZConfig.general.stretchFactor);
        if (v < -1 || v > 1) {
            DangerZone.getLog().warn("Noise value outside range " + v);
        }
        // Bind v to [0, 1)
        v = (MathHelper.clamp(v, -1, Consts.NOT_ONE) + 1) / 2.0;
        // v = v^2 to make lower difficulties more common
        v = Math.pow(v, 1.6);
        return v;
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

    public double getChunkDifficulty(ChunkPos pos) {
        return computeChunkInfo(pos).getDifficulty();
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

    private static double pow2(double v) {
        return v * v;
    }
}
