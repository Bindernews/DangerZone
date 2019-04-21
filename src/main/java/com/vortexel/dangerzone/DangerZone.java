package com.vortexel.dangerzone;

import com.vortexel.dangerzone.common.CommonProxy;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

/**
 * This is the entry point for the Danger Zone mod. All it really does is group a couple of singleton
 * instances and pass off all the work to CommonProxy.
 */
@Mod(modid = DangerZone.ID, name = DangerZone.NAME, version = DangerZone.VERSION, useMetadata = true)
public class DangerZone {

    // These constants will be filled in by Gradle
    public static final String ID = "@MOD_ID@";
    public static final String NAME = "@MOD_NAME@";
    public static final String VERSION = "@VERSION@";

    // These are all public static values so Forge can inject them and they can be accessed easily
    // by other classes in the mod. Poor design. Big sad.

    /**
     * The Logger instance for this mod. All classes should use this instance for logging.
     */
    public static Logger log;

    /**
     * The singleton instance of the mod class.
     */
    @Mod.Instance(ID)
    public static DangerZone INSTANCE = null;

    /**
     * CommonProxy actually contains most of the mod implementation code.
     * If you're trying to read the code, start there.
     */
    @SidedProxy(clientSide = "com.vortexel.dangerzone.client.Proxy",
                serverSide = "com.vortexel.dangerzone.server.Proxy")
    public static CommonProxy proxy = null;


    /**
     * This is a singleton instance so code can manipulate IDangerLevel capabilities on mobs.
     * Read the Forge docs for more information on Capabilities.
     */
    @CapabilityInject(IDangerLevel.class)
    public static Capability<IDangerLevel> CAP_DANGER_LEVEL = null;

    /**
     * The Danger Zone creative creativeTag. At this point I just make everything static, because why not? =(
     */
    public static CreativeTabs creativeTag;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        log = e.getModLog();
        creativeTag = new CreativeTab();
        proxy.preInit(e);
        log.info("PreInit complete");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
        log.info("Init complete");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
        log.info("PostInit complete");
    }
}
