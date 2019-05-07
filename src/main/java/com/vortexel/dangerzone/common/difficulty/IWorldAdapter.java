package com.vortexel.dangerzone.common.difficulty;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface to wrap useful functionality of a World. <br/>
 * This can be mocked or implemented differently to help unit test the difficulty generation algorithms.
 */
public interface IWorldAdapter {

    /**
     * Returns true if the world is local, false if not.
     */
    boolean isLocal();

    /**
     * Returns the dimension ID of the world.
     */
    int getDimension();

    /**
     * Get the world's seed.
     */
    long getSeed();

    /**
     * Returns the world's spawn point.
     */
    @Nonnull BlockPos getSpawnPoint();

    /**
     * Returns the biome at block position (x, z), or the expected biome if the chunk containing the
     * coordinates hasn't been generated yet.
     */
    Biome getBiome(int x, int z);

    /**
     * Returns the biomes for all blocks in the given chunk. <br/>
     * If the chunk hasn't been generated yet then returns the expected biomes. <br/>
     *
     * {@code listToReuse} will be filled with the data if it is at least size (16 * 16).
     * If it's {@code null} or not large enough, a new array will be allocated and returned.
     *
     * @param listToReuse array of at least size (16 * 16) to hold the return data, or null
     * @param x chunk X-coordinate
     * @param z chunk Y-coordinate
     */
    Biome[] getChunkBiomes(@Nullable Biome[] listToReuse, int x, int z);

    /**
     * Returns true if the chunk at (x, z) is generated, false if not.
     */
    boolean isChunkGenerated(int x, int z);
}
