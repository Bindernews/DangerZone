package com.vortexel.dangerzone.common.network;

import com.vortexel.dangerzone.common.gui.BaseContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class PacketContainerUpdate implements IMessage {

    private NBTTagCompound tag;

    public PacketContainerUpdate() {
    }

    public PacketContainerUpdate(NBTTagCompound tag) {
        this.tag = tag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<PacketContainerUpdate, IMessage> {
        @Override
        public IMessage onMessage(PacketContainerUpdate message, MessageContext ctx) {
            Container c = ctx.getServerHandler().player.openContainer;
            if (c instanceof BaseContainer) {
                ((BaseContainer)c).onUpdatePacket(ctx.getServerHandler().player, message.tag);
            }
            return null;
        }
    }
}
