package com.vortexel.dangerzone;

import com.vortexel.dangerzone.common.IWorldAdapter;
import lombok.AllArgsConstructor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AllArgsConstructor
public class FakeWorldAdapter implements IWorldAdapter {

    private final long seed;
    private final BlockPos spawnPoint;


    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Nonnull
    @Override
    public BlockPos getSpawnPoint() {
        return spawnPoint;
    }

    @Override
    public Biome getBiome(int x, int z) {
        return null;
    }

    @Override
    public Biome[] getChunkBiomes(@Nullable Biome[] listToReuse, int x, int z) {
        return new Biome[0];
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return true;
    }
}
