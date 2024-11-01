package com.aric.middleware.rpc.network;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.time.LocalTime;

public class ServerHanlder extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request request = (Request) msg;

        System.out.println("server 接收到信息: " + LocalTime.now() + ": " + JSON.toJSONString(request));

        Response response = new Response();
        response.setRequestId(request.getUuid());
        response.setResult("hello world!");

        System.out.println("server 返回信息: " + LocalTime.now() + ": " + JSON.toJSONString(response));

        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
