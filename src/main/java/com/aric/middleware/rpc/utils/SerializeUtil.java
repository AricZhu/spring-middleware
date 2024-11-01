package com.aric.middleware.rpc.utils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class SerializeUtil {

    public static <T> byte[] seriazlize(T obj) {
        Class<T> clzz = (Class<T>)obj.getClass();
        // 创建一个schema
        Schema<T> schema = RuntimeSchema.getSchema(clzz);

        // 序列化
        LinkedBuffer buffer = LinkedBuffer.allocate(512);

        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> clzz) throws Exception {
        T obj = clzz.getDeclaredConstructor().newInstance();
        Schema<T> schema = RuntimeSchema.getSchema(clzz);

        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }
}
