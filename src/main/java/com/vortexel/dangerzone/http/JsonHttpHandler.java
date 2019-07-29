package com.vortexel.dangerzone.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public abstract class JsonHttpHandler implements IHttpHandler {

    private StringBuilder contentBuffer;
    private HttpRequest request;

    public JsonHttpHandler() {
        this.contentBuffer = new StringBuilder();
    }

    @Override
    public void readRequest(ChannelHandlerContext ctx, HttpRequest request) {
        this.request = request;
    }

    @Override
    public void readContent(ChannelHandlerContext ctx, HttpContent content) {
        contentBuffer.append(content.content().toString(CharsetUtil.UTF_8));
        if (content instanceof LastHttpContent) {
            parseJson(ctx, contentBuffer.toString());
        }
    }

    public abstract void parseJson(ChannelHandlerContext ctx, String body);
}
