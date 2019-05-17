package com.vortexel.dangerzone.client;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.client.render.RenderTraderVillager;
import com.vortexel.dangerzone.common.CommonProxy;
import com.vortexel.dangerzone.common.DangerMath;
import com.vortexel.dangerzone.common.entity.EntityTraderVillager;
import com.vortexel.dangerzone.common.integration.ModIntegrations;
import com.vortexel.dangerzone.common.util.MCUtil;
import com.vortexel.dangerzone.common.block.ModBlocks;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class ClientProxy extends CommonProxy {

    private Map<Integer, DifficultyMapCache> worldDifficultyCaches = Maps.newHashMap();

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ModIntegrations.initClient();
    }

    @SubscribeEvent
    public void onDebugOverlay(RenderGameOverlayEvent.Text e) {
        if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            EntityPlayerSP ep = Minecraft.getMinecraft().player;
            val danger = getDifficulty(ep.getEntityWorld(), (int)ep.posX, (int)ep.posZ);
            e.getLeft().add(I18n.format("misc.dangerzone.level", DangerMath.dangerLevel(danger)));
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
        registerEntityModels();
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

    private void registerEntityModels() {
        RenderingRegistry.registerEntityRenderingHandler(EntityTraderVillager.class, RenderTraderVillager::new);
    }

    private void registerItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName().toString(), "inventory"));
    }
}
