package com.aric.middleware.rpc.network;

import com.aric.middleware.rpc.utils.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {
    private Class<?> clzz;

    public Decoder(Class<?> clzz) {
        this.clzz = clzz;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return; // 如果可读字节少于4，长度字段还没完全接收到
        }

        in.markReaderIndex(); // 记下当前位置的索引
        int length = in.readInt(); // 读取消息内容的长度

        if (in.readableBytes() < length) {
            in.resetReaderIndex(); // 如果没有足够的字节供读取，将索引重置回记号
            return;
        }

        byte[] data = new byte[length];
        in.readBytes(data);

        Object deserialize = SerializeUtil.deserialize(data, this.clzz);
        out.add(deserialize);
    }
}
