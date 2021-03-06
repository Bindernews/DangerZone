package com.vortexel.dangerzone.common.network;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.client.ClientProxy;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.util.MCUtil;
import io.netty.buffer.ByteBuf;
import lombok.val;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDangerLevel implements IMessage {

    private int dimID;
    private ChunkPos pos;
    private float[] levels;

    public PacketDangerLevel() {
    }

    public PacketDangerLevel(World worldIn, ChunkPos pos) {
        this.dimID = worldIn.provider.getDimension();
        this.pos = pos;
        this.levels = new float[Consts.CHUNK_SIZE_SQ];

        // fill in levels
        if (MCUtil.isWorldLocal(worldIn)) {
            val dMap = DangerZone.proxy.getDifficultyMap(worldIn);
            for (int z = 0; z < Consts.CHUNK_SIZE; z++) {
                for (int x = 0; x < Consts.CHUNK_SIZE; x++) {
                    levels[(z * Consts.CHUNK_SIZE) + x] = (float)dMap.getDifficulty(
                            pos.getXStart() + x, pos.getZStart() + z);
                }
            }
        } else {
            throw new UnsupportedOperationException("Cannot create PacketDangerLevel with remote world.");
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimID = buf.readInt();
        int chunkX = buf.readInt();
        int chunkZ = buf.readInt();
        pos = new ChunkPos(chunkX, chunkZ);
        levels = new float[Consts.CHUNK_SIZE_SQ];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = buf.readFloat();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimID);
        buf.writeInt(pos.x);
        buf.writeInt(pos.z);
        for (float level : levels) {
            buf.writeFloat(level);
        }
    }

    public static class Handler implements IMessageHandler<PacketDangerLevel, IMessage> {
        @Override
        public IMessage onMessage(final PacketDangerLevel message, final MessageContext ctx) {
            if (DangerZone.proxy instanceof ClientProxy) {
                val proxy = (ClientProxy)DangerZone.proxy;
                val cache = proxy.getDifficultyMapCache(message.dimID);
                cache.updateDifficulty(message.pos, message.levels);
            }
            return null;
        }
    }
}
