package com.vortexel.dangerzone.common.network;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.client.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEntityDangerLevel implements IMessage {

    private int targetId;
    private int level;

    public PacketEntityDangerLevel() {

    }

    public PacketEntityDangerLevel(Entity target, int level) {
        this.targetId = target.getEntityId();
        this.level = level;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        targetId = buf.readIntLE();
        level = buf.readIntLE();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeIntLE(targetId);
        buf.writeIntLE(level);
    }

    public static class Handler implements IMessageHandler<PacketEntityDangerLevel, IMessage> {
        @Override
        public IMessage onMessage(PacketEntityDangerLevel message, MessageContext ctx) {
            if (DangerZone.proxy instanceof ClientProxy) {
                ((ClientProxy)DangerZone.proxy).setEntityDangerLevel(message.targetId, message.level);
            }
            return null;
        }
    }
}
