package com.vortexel.dangerzone.common;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.capability.DangerLevelProvider;
import com.vortexel.dangerzone.common.capability.DangerLevelStorage;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.config.EntityConfigManager;
import com.vortexel.dangerzone.common.difficulty.DifficultyAdjuster;
import com.vortexel.dangerzone.common.difficulty.DifficultyMap;
import com.vortexel.dangerzone.common.difficulty.ForgeWorldAdapter;
import com.vortexel.dangerzone.common.network.PacketDangerLevel;
import com.vortexel.dangerzone.common.network.PacketHandler;
import com.vortexel.dangerzone.common.trade.MerchandiseManager;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.Getter;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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

    /**
     * Map dimension ID to the DifficultyMap.
     */
    public Map<Integer, DifficultyMap> worldDifficultyMaps;

    @Getter
    protected DifficultyAdjuster adjuster;
    @Getter
    protected EntityConfigManager entityConfigManager;
    @Getter
    protected MerchandiseManager merchandise;

    public void preInit(FMLPreInitializationEvent event) {
        // Create our object instances so they exist when other things try to use them.
        adjuster = new DifficultyAdjuster();
        entityConfigManager = new EntityConfigManager();
        merchandise = new MerchandiseManager();
        worldDifficultyMaps = Maps.newHashMap();

        // Register the IDangerLevel capability
        CapabilityManager.INSTANCE.register(IDangerLevel.class, new DangerLevelStorage(), IDangerLevel.Basic::new);
        // Register our event handlers (including this)
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(adjuster);
    }

    public void init(FMLInitializationEvent event) {
        // Load entity config
        reloadEntityConfig();
        // Load merchandise list
        merchandise.addFromReader(openResource("assets/dangerzone/other/merchandise.json"));
    }

    public void postInit(FMLPostInitializationEvent event) {
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

    public void reloadEntityConfig() {
        entityConfigManager = new EntityConfigManager();
        entityConfigManager.addFile(openResource("assets/dangerzone/other/entity_data.json"));
        val entityDataConfig = new File(DangerZone.instance.configDir, "entity_config.json");
        if (entityDataConfig.isFile()) {
            try {
                val reader = new FileReader(entityDataConfig);
                entityConfigManager.addFile(reader);
                reader.close();
            } catch (IOException e) {
                DangerZone.log.error(e);
            }
        }
        entityConfigManager.bake();
    }

    private Reader openResource(String path) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path));
    }

    @SubscribeEvent
    public void onRegisterCapabilities(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof EntityLivingBase) {
            val eLiving = (EntityLivingBase)e.getObject();
            if (!(eLiving instanceof EntityPlayer)) {
                e.addCapability(IDangerLevel.RESOURCE_LOCATION, new DangerLevelProvider());
            }
            eLiving.getAttributeMap().registerAttribute(Consts.ATTRIBUTE_DECAY_TOUCH);
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
