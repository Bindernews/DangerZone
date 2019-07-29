package com.vortexel.dangerzone.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.Map;

public class DHttpServer {

    private int port;
    private Thread thread;
    private Channel channel;
    private Map<String, IHttpHandler> handlerMap;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public DHttpServer(int port) {
        this.port = port;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.handlerMap = new HashMap<>();
    }

    public void start() {
        synchronized (this) {
            if (thread == null) {
                thread = new Thread(this::run);
                thread.start();
            }
        }
    }

    public void shutdown() {

    }

    public void addHandler(String path, IHttpHandler handler) {
        this.handlerMap.put(path, handler);
    }

    private void run() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ServerInitializer())
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = b.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class Handler extends SimpleChannelInboundHandler<Object> {

        private HttpRequest request;
        private IHttpHandler handler;

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                request = (HttpRequest)msg;

                if (HttpUtil.is100ContinueExpected(request)) {
                    send100Continue(ctx);
                }

                QueryStringDecoder qsd = new QueryStringDecoder(request.uri());
                handler = handlerMap.get(qsd.path());
                if (handler != null) {
                    handler.readRequest(ctx, request);
                } else {
                    ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
                    return;
                }
            }
            if (msg instanceof HttpContent) {
                if (handler == null) {
                    return;
                }
                HttpContent httpContent = (HttpContent)msg;
                if (httpContent.content().isReadable()) {
                    handler.readContent(ctx, httpContent);
                }
            }
        }

        private void send100Continue(ChannelHandlerContext ctx) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE,
                    Unpooled.EMPTY_BUFFER);
            ctx.write(response);
        }
    }

    private class ServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpServerCodec());
            p.addLast(new Handler());
        }
    }
}
