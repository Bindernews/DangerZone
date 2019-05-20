package com.vortexel.dangerzone.common;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.config.EntityConfigManager;
import com.vortexel.dangerzone.common.difficulty.DifficultyAdjuster;
import com.vortexel.dangerzone.common.difficulty.DifficultyMap;
import com.vortexel.dangerzone.common.difficulty.ForgeWorldAdapter;
import com.vortexel.dangerzone.common.integration.ModIntegrations;
import com.vortexel.dangerzone.common.network.PacketDangerLevel;
import com.vortexel.dangerzone.common.network.PacketHandler;
import com.vortexel.dangerzone.common.trade.MerchandiseManager;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.Getter;
import lombok.val;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.*;
import java.util.Map;

/**
 * {@code CommonProxy} contains most of the logic for the mod. It also acts as a hub for most of the events
 * the mod receives.
 */
public class CommonProxy {

    public static final String DEFAULT_ENTITY_CONFIG_PATH = "assets/dangerzone/other/entity_data.json";
    public static final String DEFAULT_MERCHANDISE_PATH = "assets/dangerzone/other/merchandise/default.json";
    public static final String ENTITY_CONFIG_FILE = "entity_config.json";
    public static final String MERCHANDISE_FILE = "default.json";

    /**
     * Map dimension ID to the DifficultyMap.
     */
    protected Map<Integer, DifficultyMap> worldDifficultyMaps;

    protected DifficultyAdjuster adjuster;
    @Getter
    protected EntityConfigManager entityConfigManager;
    @Getter
    protected MerchandiseManager merchandise;

    // Flag to track if we've baked entityConfig or not.
    private boolean entityConfigFinalized = false;

    public void preInit(FMLPreInitializationEvent event) {
        // Create our object instances so they exist when other things try to use them.
        adjuster = new DifficultyAdjuster();
        entityConfigManager = new EntityConfigManager();
        merchandise = new MerchandiseManager();
        worldDifficultyMaps = Maps.newHashMap();

        // Load default entity config
        entityConfigManager.addFile(MCUtil.openResource(DEFAULT_ENTITY_CONFIG_PATH));

        // Register our event handlers (including this)
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(adjuster);
    }

    public void init(FMLInitializationEvent event) {
        ModIntegrations.initCommon();
    }

    public void postInit(FMLPostInitializationEvent event) {

    }

    public void finalizeEntityConfig() {
        if (entityConfigFinalized) {
            return;
        }
        entityConfigFinalized = true;
        // Load the user config file(s) last so they override any mod-provided data
        loadUserEntityConfig();
        // Optimize the config data for performance. We do this once the server is starting
        // so we know that other mods have had the chance to send us other info.
        getEntityConfigManager().bake();
    }

    /**
     * Get the RAW difficulty value in world {@code world} at {@code (x, z)}. <br/>
     * On the server this always works, but on the client this will return either a cached value from
     * the server, or 0 if the difficulty is unknown.
     */
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

    public void loadUserEntityConfig() {
        val entityDataConfig = new File(DangerZone.getMod().getConfigDir(), ENTITY_CONFIG_FILE);
        if (entityDataConfig.isFile()) {
            try {
                val reader = new FileReader(entityDataConfig);
                entityConfigManager.addFile(reader);
                reader.close();
            } catch (IOException e) {
                DangerZone.getLog().error(e);
            }
        }
    }

    public void loadMerchandise() {
        boolean shouldAddDefaults = true;
        val merchandiseConfigFile = new File(DangerZone.getMod().getConfigDir(), MERCHANDISE_FILE);
        if (merchandiseConfigFile.isFile()) {
            try {
                val reader = new FileReader(merchandiseConfigFile);
                merchandise.addFromReader(reader);
                reader.close();
                shouldAddDefaults = false;
            } catch (IOException e) {
                DangerZone.getLog().error(e);
            }
        }
        if (shouldAddDefaults) {
            merchandise.addFromReader(MCUtil.openResource(DEFAULT_MERCHANDISE_PATH));
        }
    }

    @SubscribeEvent
    public void onWorldLoaded(WorldEvent.Load e) {
        World w = e.getWorld();
        if (MCUtil.isWorldLocal(w)) {
            worldDifficultyMaps.putIfAbsent(w.provider.getDimension(), new DifficultyMap(new ForgeWorldAdapter(w)));
        }
        finalizeEntityConfig();
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
