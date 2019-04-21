package com.vortexel.dangerzone.client;

import com.vortexel.dangerzone.common.CommonProxy;
import com.vortexel.dangerzone.common.DifficultyMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Proxy extends CommonProxy {

    @SubscribeEvent
    public void onDebugOverlay(RenderGameOverlayEvent.Text e) {
        EntityPlayerSP ep = Minecraft.getMinecraft().player;
        BlockPos bp = new BlockPos(ep.posX, 60.0, ep.posZ);
        DifficultyMap dmap = worldDifficultyMaps.get(ep.dimension);
        e.getLeft().add("Avg Difficulty: " + dmap.getDifficulty(bp.getX(), bp.getZ()));
        e.getLeft().add("Chunk Difficulty: " + dmap.getChunkDifficulty(new ChunkPos(bp)));
    }

    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent e) {

    }
}
