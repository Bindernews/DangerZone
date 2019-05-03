package com.vortexel.dangerzone.common.network;

import com.vortexel.dangerzone.client.gui.GuiCoinPouch;
import com.vortexel.dangerzone.common.gui.ContainerCoinPouch;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NoArgsConstructor
@AllArgsConstructor
public class PacketCoinPouchCoinType implements IMessage {

    /**
     * The {@link ItemLootCoin} to use.
     */
    private ItemLootCoin coin;

    /**
     * Is this a request (true, sent by the client) or a response (false, sent by the server).
     */
    private boolean request;

    @Override
    public void fromBytes(ByteBuf buf) {
        request = buf.readBoolean();
        coin = ItemLootCoin.fromAmount(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(request);
        buf.writeInt(coin.amount);
    }

    public static class Handler implements IMessageHandler<PacketCoinPouchCoinType, IMessage> {
        @Override
        public IMessage onMessage(PacketCoinPouchCoinType message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT && !message.request) {
                updateGUIView(message);
            } else if (ctx.side == Side.SERVER && message.request) {
                // If the request asked for an invalid coin, then we don't do anything.
                if (message.coin == null) {
                    return null;
                }
                Container container = ctx.getServerHandler().player.openContainer;
                if (container instanceof ContainerCoinPouch) {
                    ((ContainerCoinPouch) container).setOutputType(message.coin);
                    return new PacketCoinPouchCoinType(message.coin, false);
                }
            }
            return null;
        }


        @SideOnly(Side.CLIENT)
        public void updateGUIView(PacketCoinPouchCoinType message) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof GuiCoinPouch) {
                GuiCoinPouch gui = (GuiCoinPouch)screen;
                gui.getContainer().setOutputType(message.coin);
            }
        }
    }
}
