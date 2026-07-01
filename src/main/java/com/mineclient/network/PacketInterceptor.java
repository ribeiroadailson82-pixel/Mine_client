package com.mineclient.network;

import com.mineclient.event.PacketEvent;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Injeta um handler no pipeline netty da conexão para publicar
 * {@link PacketEvent} a cada pacote enviado/recebido.
 */
public final class PacketInterceptor {

    private static final String HANDLER_NAME = "mineclient_packet_handler";

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChannelPipeline pipeline = event.manager.channel().pipeline();
        if (pipeline.get(HANDLER_NAME) == null && pipeline.get("packet_handler") != null) {
            pipeline.addBefore("packet_handler", HANDLER_NAME, new InterceptHandler());
        }
    }

    private static final class InterceptHandler extends ChannelDuplexHandler {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Packet) {
                PacketEvent event = new PacketEvent((Packet<?>) msg, PacketEvent.Direction.INBOUND);
                if (MinecraftForge.EVENT_BUS.post(event)) {
                    return;
                }
            }
            super.channelRead(ctx, msg);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof Packet) {
                PacketEvent event = new PacketEvent((Packet<?>) msg, PacketEvent.Direction.OUTBOUND);
                if (MinecraftForge.EVENT_BUS.post(event)) {
                    return;
                }
            }
            super.write(ctx, msg, promise);
        }
    }
}
