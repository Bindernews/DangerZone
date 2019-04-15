package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.config.DZConfig;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
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

    public void preInit(FMLPreInitializationEvent e) {
        DZConfig.INSTANCE.loadFromDirectory(new File(e.getModConfigurationDirectory(), DangerZone.ID));

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
        int dim = e.getWorld().provider.getDimension();
        int difficulty = (int)(8 * worldDifficultyMaps.get(dim).getDifficulty((int)e.getX(), (int)e.getZ()));
        double amount = 1.0;
        if (difficulty <= 2) {
            amount = 0.5;
        }
        else if (difficulty <= 5) {
            amount = 1.5;
        }
        else if (difficulty <= 7) {
            amount = 2.0;
        }
        else {
            amount = 3.0;
        }
        e.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(
                new AttributeModifier("DangerZone", amount, 2)
        );
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
