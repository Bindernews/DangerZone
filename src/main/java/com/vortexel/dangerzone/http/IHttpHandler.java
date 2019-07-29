package com.vortexel.dangerzone.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;

public interface IHttpHandler {


    void readRequest(ChannelHandlerContext ctx, HttpRequest request);
    void readContent(ChannelHandlerContext ctx, HttpContent content);

}
