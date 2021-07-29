package top.glidea.framework.remoting.serialize;

/**
 * 序列化器接口
 */
public interface Serializer {
    String NAME = "serializer";

    /**
     * 序列化
     * @param obj 要序列化的对象
     * @return 对应的字节数组
     */
    byte[] toBytes(Object obj);

    /**
     * 反序列化
     * @param bytes 序列化后的字节数组
     * @param classOfT 这个字节数组本来是啥对象类型
     * @return 反序列化成的对象
     */
    <T> T toObj(byte[] bytes, Class<T> classOfT);
}
