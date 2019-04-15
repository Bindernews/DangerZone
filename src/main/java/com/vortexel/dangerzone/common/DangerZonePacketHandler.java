package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class DangerZonePacketHandler {

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(DangerZone.ID);


}
