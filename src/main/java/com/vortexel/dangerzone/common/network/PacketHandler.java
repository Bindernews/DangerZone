package com.vortexel.dangerzone.common.network;

import com.vortexel.dangerzone.DangerZone;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(DangerZone.MOD_ID);


    public static void init() {
        int id = 0;
        NETWORK.registerMessage(PacketDangerLevel.Handler.class, PacketDangerLevel.class, id++, Side.CLIENT);
        NETWORK.registerMessage(PacketCoinPouchCoinType.Handler.class, PacketCoinPouchCoinType.class, id++,
                Side.SERVER);
        NETWORK.registerMessage(PacketCoinPouchCoinType.Handler.class, PacketCoinPouchCoinType.class, id++,
                Side.CLIENT);
    }
}
