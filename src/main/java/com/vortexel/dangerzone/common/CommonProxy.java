package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.capability.DangerLevelProvider;
import com.vortexel.dangerzone.common.capability.DangerLevelStorage;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.capability.SimpleDangerLevel;
import com.vortexel.dangerzone.common.config.BiomeConfig;
import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.HashMap;

/**
 * {@code CommonProxy} contains most of the logic for the mod. It also acts as a hub for most of the events
 * the mod receives.
 */
public class CommonProxy {

    /**
     * Map dimension ID to the DifficultyMap.
     */
    public HashMap<Integer, DifficultyMap> worldDifficultyMaps;

    public DifficultyAdjuster adjuster;

    public void preInit(FMLPreInitializationEvent e) {
        // First things first, load the config.
        ConfigManager.sync(DangerZone.ID, Config.Type.INSTANCE);
        val cfg = new Configuration(e.getSuggestedConfigurationFile());
        cfg.load();
        DZConfig.load(cfg);
        cfg.save();
        BiomeConfig.afterLoad();

        // Create our object instances so they exist when other things try to use them.
        adjuster = new DifficultyAdjuster();
        worldDifficultyMaps = new HashMap<>();

        // Register the IDangerLevel CAPABILITY
        CapabilityManager.INSTANCE.register(IDangerLevel.class, new DangerLevelStorage(), SimpleDangerLevel::new);

        // Register ourselves so we can receive events.
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void init(FMLInitializationEvent e) {

    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    public double getDifficulty(World world, int x, int z) {
        worldDifficultyMaps.putIfAbsent(world.provider.getDimension(),
                new DifficultyMap(new ForgeWorldAdapter(world)));
        return worldDifficultyMaps.get(world.provider.getDimension()).getDifficulty(x, z);
    }

    @SubscribeEvent
    public void onRegisterCapabilities(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof EntityCreature && !(e.getObject() instanceof EntityPlayer)) {
            e.addCapability(IDangerLevel.RESOURCE_LOCATION, new DangerLevelProvider());
        }
    }

    @SubscribeEvent
    public void worldLoaded(WorldEvent.Load e) {
        World w = e.getWorld();
        worldDifficultyMaps.putIfAbsent(w.provider.getDimension(), new DifficultyMap(new ForgeWorldAdapter(w)));
    }

    @SubscribeEvent
    public void worldUnloaded(WorldEvent.Unload e) {
        World w = e.getWorld();
        worldDifficultyMaps.remove(w.provider.getDimension());
    }

    /**
     * Event for when an entity drops items. We use this to change the drop loot.
     * We set the priority at lowest so that we are one of the first to receive the event.
     * That way we can modify the drops, and let others deal with if it's allowed or not.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityDrops(LivingDropsEvent e) {
        adjuster.modifyDrops(e);
    }

    @SubscribeEvent
    public void onLootingLevel(LootingLevelEvent e) {
        adjuster.modifyLootingLevel(e);
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent e) {
        adjuster.adjustEntityDifficulty(e.getEntity());
    }
}
