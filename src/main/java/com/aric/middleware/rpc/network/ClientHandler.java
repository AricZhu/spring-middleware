package com.aric.middleware.rpc.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response response = (Response) msg;

        WriteFuture writeFuture = WriteFutureMap.getWriteFuture(response.getRequestId());
        writeFuture.setResponse(response);
    }
}
