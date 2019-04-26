package com.vortexel.dangerzone.common;

import lombok.AllArgsConstructor;
import lombok.val;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AllArgsConstructor
public class ForgeWorldAdapter implements IWorldAdapter {

    public static final int CHUNK_SIZE = 16;

    private final World world;

    @Override
    public boolean isLocal() {
        return !world.isRemote;
    }

    @Override
    public int getDimension() {
        return world.provider.getDimension();
    }

    @Override
    public long getSeed() {
        return world.getSeed();
    }

    @Nonnull
    @Override
    public BlockPos getSpawnPoint() {
        return world.getSpawnPoint();
    }

    @Override
    public Biome getBiome(int x, int z) {
        val chunkX = x / CHUNK_SIZE;
        val chunkZ = z / CHUNK_SIZE;
        if (isChunkGenerated(chunkX, chunkZ)) {
            return world.getBiome(new BlockPos(x, 30, z));
        } else {
            val biomeList = world.getBiomeProvider().getBiomesForGeneration(null, x, z, 1, 1);
            return biomeList[0];
        }
    }

    @Override
    public Biome[] getChunkBiomes(@Nullable Biome[] listToReuse, int x, int z) {
        if (isChunkGenerated(x, z)) {
            return world.getBiomeProvider().getBiomes(listToReuse, x * CHUNK_SIZE, z * CHUNK_SIZE,
                    CHUNK_SIZE, CHUNK_SIZE);
        } else {
            return world.getBiomeProvider().getBiomesForGeneration(listToReuse, x * CHUNK_SIZE, z * CHUNK_SIZE,
                    CHUNK_SIZE, CHUNK_SIZE);
        }
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return world.getChunkProvider().isChunkGeneratedAt(x, z);
    }
}
