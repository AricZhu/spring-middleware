package com.aric.middleware.rpc.network;

import com.aric.middleware.rpc.utils.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder {
    private Class<?> clzz;

    public Encoder(Class<?> clzz) {
        this.clzz = clzz;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf out) throws Exception {
        byte[] data = SerializeUtil.seriazlize(o);
        out.writeInt(data.length); // 写入长度
        out.writeBytes(data);      // 写入消息内容
    }
}
