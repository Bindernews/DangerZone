package com.vortexel.dangerzone.client;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.common.CommonProxy;
import com.vortexel.dangerzone.common.DangerMath;
import com.vortexel.dangerzone.common.MCUtil;
import com.vortexel.dangerzone.common.block.ModBlocks;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class ClientProxy extends CommonProxy {

    private Map<Integer, DifficultyMapCache> worldDifficultyCaches = Maps.newHashMap();

    @SubscribeEvent
    public void onDebugOverlay(RenderGameOverlayEvent.Text e) {
        if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            EntityPlayerSP ep = Minecraft.getMinecraft().player;
            BlockPos bp = new BlockPos(ep.posX, 60.0, ep.posZ);

            val danger = getDifficulty(ep.getEntityWorld(), bp.getX(), bp.getZ());
            e.getLeft().add("Difficulty: " + danger);
            e.getLeft().add("Level: " + DangerMath.dangerLevel(danger));
//            e.getLeft().add("Chunk Difficulty: " + dmap.getChunkDifficulty(new ChunkPos(bp)));
        }
    }

    @Override
    public double getDifficulty(World world, int x, int z) {
        if (MCUtil.isWorldLocal(world)) {
            return getDifficultyMap(world).getDifficulty(x, z);
        } else {
            return getDifficultyMapCache(world.provider.getDimension()).getDifficulty(x, z);
        }
    }

    public DifficultyMapCache getDifficultyMapCache(int dimID) {
        worldDifficultyCaches.computeIfAbsent(dimID, DifficultyMapCache::new);
        return worldDifficultyCaches.get(dimID);
    }

    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent e) {
        registerItemModels();
    }

    /**
     * It's important that we unload data from our worldDifficultyCaches because if we don't and then we switch
     * to a different server or a single-player world, then the difficulties will be out of date.
     */
    @Override
    public void onWorldUnloaded(WorldEvent.Unload e) {
        super.onWorldUnloaded(e);
        if (!MCUtil.isWorldLocal(e.getWorld())) {
            val dimID = e.getWorld().provider.getDimension();
            worldDifficultyCaches.remove(dimID);
        }
    }

    private void registerItemModels() {
        for (Item item : ModItems.ITEMS) {
            registerItemModel(item);
        }
        for (val block : ModBlocks.BLOCKS) {
            val item = Item.getItemFromBlock(block);
            if (item != null) {
                registerItemModel(item);
            }
        }
    }

    private void registerItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName().toString(), "inventory"));
    }
}
