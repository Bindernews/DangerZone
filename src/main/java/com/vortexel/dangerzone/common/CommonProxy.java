package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.config.DZConfig;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.HashMap;

public class CommonProxy {

    /**
     * Map dimension ID to the DifficultyMap.
     */
    public HashMap<Integer, DifficultyMap> worldDifficultyMaps;

    public DifficultyAdjuster adjuster;

    public void preInit(FMLPreInitializationEvent e) {
        DZConfig.INSTANCE.loadFromDirectory(new File(e.getModConfigurationDirectory(), DangerZone.ID));

        adjuster = new DifficultyAdjuster();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(adjuster);

        worldDifficultyMaps = new HashMap<>();
    }

    public void init(FMLInitializationEvent e) {

    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    public double getDifficulty(World world, int x, int z) {
        return worldDifficultyMaps.get(world.provider.getDimension()).getDifficulty(x, z);
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
