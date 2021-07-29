package top.glidea.framework.remoting.serialize;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import top.glidea.framework.remoting.transport.protocol.ProtocolFrameDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Protostuff 序列化器
 */
public class ProtostuffSerializer implements Serializer {
    private static ThreadLocal<LinkedBuffer> localBuffer = ThreadLocal.withInitial(() -> LinkedBuffer.allocate(
            ProtocolFrameDefinition.MAX_FRAME_LENGTH - ProtocolFrameDefinition.HEADER_LENGTH
    ));


    @Override
    public byte[] toBytes(Object obj) {
        Class<?> clazz = obj.getClass();
        // 虽然schema的创建比较耗时，但RuntimeSchema#getSchema自带缓存，所以不用自己在外头建
        Schema schema = RuntimeSchema.getSchema(clazz);
        byte[] bytes;
        /*
          我见网上不少例子，都把buffer作为共享的成员变量，说是避免分配。。。
          原作者996累了偶尔低并发编程一会，没什么，但水博客的看都不看一眼，直接cv是真的过分。。。
         */
        LinkedBuffer buffer = localBuffer.get();
        try {
            //noinspection unchecked
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
        return bytes;
    }

    @Override
    public <T> T toObj(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }
}
