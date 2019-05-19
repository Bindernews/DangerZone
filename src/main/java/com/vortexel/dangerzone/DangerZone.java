package com.vortexel.dangerzone;

import com.vortexel.dangerzone.api.IDangerZoneAPI;
import com.vortexel.dangerzone.common.CommonProxy;
import com.vortexel.dangerzone.common.CreativeTab;
import com.vortexel.dangerzone.common.api.IMCHandler;
import com.vortexel.dangerzone.common.api.ImplDangerZoneAPI;
import com.vortexel.dangerzone.common.capability.DangerLevelStorage;
import com.vortexel.dangerzone.common.entity.ModEntities;
import com.vortexel.dangerzone.common.gui.GuiHandler;
import com.vortexel.dangerzone.common.network.PacketHandler;
import com.vortexel.dangerzone.common.block.ModBlocks;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.config.DZConfig;
import com.vortexel.dangerzone.common.item.ModItems;
import com.vortexel.dangerzone.common.sound.ModSounds;
import com.vortexel.dangerzone.common.tile.ModTiles;
import com.vortexel.dangerzone.common.trade.MerchandiseLoader;
import com.vortexel.dangerzone.common.trade.MerchandiseManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * This is the entry point for the Danger Zone mod. All it really does is group a couple of singleton
 * instances and pass off all the work to CommonProxy.
 */
@Mod(modid = DangerZone.MOD_ID, name = DangerZone.NAME, version = DangerZone.VERSION, useMetadata = true)
public class DangerZone {

    // These constants will be filled in by Gradle
    public static final String MOD_ID = "@MOD_ID@";
    public static final String NAME = "@MOD_NAME@";
    public static final String VERSION = "@VERSION@";

    /**
     * The singleton instance of the mod class.
     */
    @Mod.Instance(MOD_ID)
    protected static DangerZone instance = null;

    /**
     * CommonProxy actually contains most of the mod implementation code.
     * If you're trying to read the code, start there.
     */
    @SidedProxy(clientSide = "com.vortexel.dangerzone.client.ClientProxy",
                serverSide = "com.vortexel.dangerzone.common.CommonProxy")
    public static CommonProxy proxy = null;


    /**
     * This is a singleton instance so code can manipulate IDangerLevel capabilities on mobs.
     * Read the Forge docs for more information on Capabilities.
     */
    @CapabilityInject(IDangerLevel.class)
    public static Capability<IDangerLevel> CAP_DANGER_LEVEL = null;

    /**
     * The Danger Zone creative creativeTab.
     */
    public static CreativeTabs creativeTab;

    /**
     * The configuration directory.
     */
    private File configDir;

    /**
     * The API implementation.
     */
    private ImplDangerZoneAPI apiImpl;

    /**
     * The Logger instance for this mod. All classes should use this instance for logging.
     */
    private Logger log;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        // The order of things here is somewhat important.
        // The logger HAS to be set FIRST and the configuration HAS to be loaded second, but after
        // that things don't really matter too much.

        log = e.getModLog();
        // Set the config dir so we know what it is later
        configDir = new File(e.getModConfigurationDirectory(), MOD_ID);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // Initialize some variables that start as null
        creativeTab = new CreativeTab();
        // Load the config.
        DZConfig.cfg = new Configuration(e.getSuggestedConfigurationFile());
        DZConfig.loadAll();
        // Make it so we can display GUIs
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        // Create our API instance
        apiImpl = new ImplDangerZoneAPI();
        // Register the IDangerLevel capability
        CapabilityManager.INSTANCE.register(IDangerLevel.class, new DangerLevelStorage(),
                DangerLevelStorage.Basic::new);
        MinecraftForge.EVENT_BUS.register(this);
        // Initialize ALL the things. These can be in any order.
        PacketHandler.init();
        ModBlocks.init();
        ModItems.init();
        ModTiles.init();
        ModEntities.init();
        ModSounds.init();
        IMCHandler.init();
        // Finally pre-init the proxy.
        proxy.preInit(e);
        log.info("PreInit complete");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MerchandiseLoader.loadDefaultMerchandise();
        MerchandiseLoader.loadModMerchandise();
        proxy.init(e);
        log.info("Init complete");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
        log.info("PostInit complete");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // Bake the entity config only after mods have had a chance to add their configs.
        proxy.finalizeEntityConfig();
    }

    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
        IMCHandler.onIMCEvent(event);
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent event) {
        if (event.getModID().equals(MOD_ID)) {
            DZConfig.loadAll();
        }
    }

    // region Getters

    public MerchandiseManager getMerchandise() {
        return proxy.getMerchandise();
    }

    public IDangerZoneAPI getAPI() {
        return apiImpl;
    }

    public File getConfigDir() {
        return configDir;
    }

    // endregion Getters

    // region Static getters

    public static Logger getLog() {
        return instance.log;
    }

    public static DangerZone getMod() {
        return instance;
    }

    public static ResourceLocation prefix(String resourcePath) {
        return new ResourceLocation(DangerZone.MOD_ID, resourcePath);
    }

    // endregion Static getters
}
