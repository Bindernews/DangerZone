package com.vortexel.dangerzone.common;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class CommonProxy {

    /**
     * Map dimension ID to the DifficultyMap.
     */
    public HashMap<Integer, DifficultyMap> worldDifficultyMaps;

    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);

        worldDifficultyMaps = new HashMap<>();
    }

    public void init(FMLInitializationEvent e) {

    }

    public void postInit(FMLPostInitializationEvent e) {

    }

    @SubscribeEvent
    public void entitySpawn(LivingSpawnEvent e) {
        // TODO modify the entity based on the difficulty of its spawn area
    }

    @SubscribeEvent
    public void worldLoaded(WorldEvent.Load e) {
        World w = e.getWorld();
        worldDifficultyMaps.put(w.provider.getDimension(), new DifficultyMap(w));
    }

    @SubscribeEvent
    public void worldUnloaded(WorldEvent.Unload e) {
        World w = e.getWorld();
        worldDifficultyMaps.remove(w.provider.getDimension());
    }
}
