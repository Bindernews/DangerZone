package com.vortexel.dangerzone.common;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.capability.DangerLevelProvider;
import com.vortexel.dangerzone.common.capability.DangerLevelStorage;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.capability.SimpleDangerLevel;
import com.vortexel.dangerzone.common.config.EntityConfigManager;
import com.vortexel.dangerzone.common.network.PacketDangerLevel;
import com.vortexel.dangerzone.common.network.PacketHandler;
import lombok.Getter;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * {@code CommonProxy} contains most of the logic for the mod. It also acts as a hub for most of the events
 * the mod receives.
 */
public class CommonProxy {

    /**
     * Map dimension ID to the DifficultyMap.
     */
    public Map<Integer, DifficultyMap> worldDifficultyMaps;

    @Getter
    protected DifficultyAdjuster adjuster;
    @Getter
    protected LootManager lootManager;
    @Getter
    protected EntityConfigManager entityConfigManager;

    public void preInit(FMLPreInitializationEvent e) {
        // Create our object instances so they exist when other things try to use them.
        adjuster = new DifficultyAdjuster();
        lootManager = new LootManager();
        entityConfigManager = new EntityConfigManager();
        worldDifficultyMaps = Maps.newHashMap();

        // Register the IDangerLevel capability
        CapabilityManager.INSTANCE.register(IDangerLevel.class, new DangerLevelStorage(), SimpleDangerLevel::new);
        // Register ourselves so we can receive events.
        MinecraftForge.EVENT_BUS.register(this);
        // Register our ore loot table
        LootTableList.register(LootManager.ORE_LOOT_TABLE_RESOURCE);
    }

    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(adjuster);
        MinecraftForge.EVENT_BUS.register(lootManager);

        entityConfigManager.addFile(openResource("assets/dangerzone/other/entity_data.json"));
        entityConfigManager.bake();
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    private Reader openResource(String path) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path));
    }

    public double getDifficulty(World world, int x, int z) {
        if (MCUtil.isWorldLocal(world)) {
            return getDifficultyMap(world).getDifficulty(x, z);
        } else {
            throw new UnsupportedOperationException("Can't get the difficulty if the world is remote.");
        }
    }

    /**
     * Get the difficulty map for the given world. This only works on the LOGICAL SERVER.
     *
     * @return the {@link DifficultyMap} for the world, or {@code null} if this is a logical client.
     */
    public DifficultyMap getDifficultyMap(World world) {
        if (MCUtil.isWorldLocal(world)) {
            val dimID = world.provider.getDimension();
            worldDifficultyMaps.computeIfAbsent(dimID, (id) -> new DifficultyMap(new ForgeWorldAdapter(world)));
            return worldDifficultyMaps.get(dimID);
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public void onRegisterCapabilities(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof EntityCreature && !(e.getObject() instanceof EntityPlayer)) {
            e.addCapability(IDangerLevel.RESOURCE_LOCATION, new DangerLevelProvider());
        }
    }

    @SubscribeEvent
    public void onWorldLoaded(WorldEvent.Load e) {
        World w = e.getWorld();
        if (MCUtil.isWorldLocal(w)) {
            worldDifficultyMaps.putIfAbsent(w.provider.getDimension(), new DifficultyMap(new ForgeWorldAdapter(w)));
        }
    }

    @SubscribeEvent
    public void onWorldUnloaded(WorldEvent.Unload e) {
        World w = e.getWorld();
        if (MCUtil.isWorldLocal(w)) {
            worldDifficultyMaps.remove(w.provider.getDimension());
        }
    }

    /**
     * Event for when the player loads a chunk from the server. <br/>
     * Here we send that player the difficulty of the chunk whenever they load said chunk.
     */
    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent e) {
        val chunk = e.getChunkInstance();
        PacketHandler.NETWORK.sendTo(new PacketDangerLevel(chunk.getWorld(), chunk.getPos()), e.getPlayer());
    }
}
