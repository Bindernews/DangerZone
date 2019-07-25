package com.vortexel.dangerzone.common.integration;

import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ModIntegrations {

    public static void initCommon() {
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe",
                "com.vortexel.dangerzone.common.integration.TheOneProbe$GetAPI");
    }

    public static void initClient() {
    }
}
