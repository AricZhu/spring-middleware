package com.aric.middleware;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class MyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String msg = "歪比巴卜~茉莉~Are you good~马来西亚~";
        System.out.println("客户端通道可以使用，向服务端发送消息：" + msg);
        ctx.writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf msg1 = (ByteBuf) msg;
        System.out.println("接收到服务端的消息: " + ctx.channel().remoteAddress() + msg1.toString(CharsetUtil.UTF_8));
    }
}
