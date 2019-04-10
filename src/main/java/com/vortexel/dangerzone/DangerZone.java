package com.vortexel.dangerzone;

import com.vortexel.dangerzone.common.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = DangerZone.ID, name = DangerZone.NAME, version = DangerZone.VERSION, useMetadata = true)
public class DangerZone {

    public static final String ID = "levelzones";
    public static final String NAME = "LevelZones";
    public static final String VERSION = "@VERSION@";

    private static DangerZone instance;

    public Logger log;

    @SidedProxy(clientSide = "com.vortexel.dangerzone.client.Proxy",
                serverSide = "com.vortexel.dangerzone.server.Proxy")
    public CommonProxy proxy = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        instance = this;
        log = e.getModLog();
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

    public static DangerZone getInstance() {
        return instance;
    }

    public static Logger getLog() {
        return getInstance().log;
    }
}
