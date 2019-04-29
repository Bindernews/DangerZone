package com.vortexel.dangerzone.client;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.MCUtil;
import lombok.val;
import net.minecraft.util.math.ChunkPos;

import java.util.Map;

public class DifficultyMapCache {

    public final int dimension;
    private Map<ChunkPos, float[]> chunks;

    public DifficultyMapCache(int dimension) {
        this.dimension = dimension;
        this.chunks = Maps.newHashMap();
    }

    public float getDifficulty(int x, int z) {
        val cPos = MCUtil.chunkPosFrom(x, z);
        val levels = chunks.getOrDefault(cPos, null);
        if (levels == null) {
            return -1;
        }
        return levels[(Consts.CHUNK_SIZE * (z - cPos.getZStart())) + (x - cPos.getXStart())];
    }

    public void updateDifficulty(ChunkPos pos, float[] levels) {
        chunks.put(pos, levels);
    }
}
