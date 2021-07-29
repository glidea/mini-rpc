package top.glidea.framework.remoting.transport.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import top.glidea.framework.common.factory.ExpansionFactory;
import top.glidea.framework.remoting.transport.protocol.bodybean.*;
import top.glidea.framework.remoting.serialize.Serializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * 协议帧定义
 * 魔数：bytes[]{1, 3, 1, 4}  4b
 * body是否被压缩：byte       1b
 * 消息类型：byte             1b
 * body长度：int              4b
 * body：bytes[]            不定长
 */
@Getter
@Setter
public class ProtocolFrameDefinition {
    /**
     * 协议帧属性
     */
    private static final byte[] MAGIC_NUM = {1, 3, 1, 4};
    public static final int MAX_FRAME_LENGTH = 20480;
    public static final int HEADER_LENGTH = MAGIC_NUM.length + 6;  // 10byte
    public static final int LENGTH_FIELD_OFFSET = 6;
    public static final int LENGTH_FIELD_LENGTH = 4;
    public static final int LENGTH_ADJUSTMENT = 0;

    /**
     * 消息类型
     */
    public static final byte RPC_REQ_TYPE  = 0;
    public static final byte RPC_RESP_TYPE = 1;
    public static final byte PING_TYPE = 2;
    public static final byte PONG_TYPE = 3;
    private static final Map<Byte, Class<? extends Message>> TYPE_CLASS_MAP = new HashMap<>();
    static {
        TYPE_CLASS_MAP.put(RPC_REQ_TYPE, RpcRequest.class);
        TYPE_CLASS_MAP.put(RPC_RESP_TYPE, RpcResponse.class);
        TYPE_CLASS_MAP.put(PING_TYPE, Ping.class);
        TYPE_CLASS_MAP.put(PONG_TYPE, Pong.class);
    }
    public static Class<? extends Message> getMessageClazz(byte messageType) {
        return TYPE_CLASS_MAP.get(messageType);
    }

    /**
     * 协议帧header
     */
    private byte[] magicNum;   // 用于确认发送者说的也是人话
    private byte isCompressed; // body是否被压缩。body length超过阈值，会被压缩（1: true, 0: false）
    private byte messageType;  // 指明序列化成body的bodybean是啥Class
    private int bodyLength;    // 用于确认协议帧的边界，解决粘包半包
    /**
     * 协议帧body
     */
    private byte[] bodyBytes;  // bodybean 序列化后的结果

    public ProtocolFrameDefinition(Message msg) {
        magicNum = MAGIC_NUM;
        messageType = msg.getMessageType();
        Serializer serializer = ExpansionFactory.get(Serializer.class);
        bodyBytes = serializer.toBytes(msg);
        bodyLength = bodyBytes.length;
    }

    public ProtocolFrameDefinition(ByteBuf frame) {
        magicNum = new byte[MAGIC_NUM.length];
        frame.readBytes(magicNum, 0, MAGIC_NUM.length); // readBytes#byte[] 为传出参数
        if (!Arrays.equals(MAGIC_NUM, magicNum)) {
            throw new RuntimeException("非法数据包，魔数对不上号");
        }

        isCompressed = frame.readByte();
        messageType = frame.readByte();
        bodyLength = frame.readInt();
        bodyBytes = new byte[bodyLength];
        frame.readBytes(bodyBytes, 0, bodyLength);
    }
}
