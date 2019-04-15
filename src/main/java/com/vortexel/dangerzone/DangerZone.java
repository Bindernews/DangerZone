package com.vortexel.dangerzone;

import com.vortexel.dangerzone.common.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = DangerZone.ID, name = DangerZone.NAME, version = DangerZone.VERSION, useMetadata = true)
public class DangerZone {

    public static final String ID = "dangerzone";
    public static final String NAME = "Danger Zone";
    public static final String VERSION = "@VERSION@";

    public static Logger log;

    public CreativeTabs tab;

    @Mod.Instance(ID)
    public static DangerZone instance;

    @SidedProxy(clientSide = "com.vortexel.dangerzone.client.Proxy",
                serverSide = "com.vortexel.dangerzone.server.Proxy")
    public static CommonProxy proxy = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        tab = new CreativeTab();
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
}
