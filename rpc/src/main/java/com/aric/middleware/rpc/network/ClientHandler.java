package com.aric.middleware.rpc.network;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response response = (Response) msg;
        logger.info("client 获取到数据: {}", JSON.toJSONString(response));
        WriteFuture writeFuture = WriteFutureMap.getWriteFuture(response.getRequestId());
        writeFuture.setResponse(response);
    }
}
