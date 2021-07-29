package top.glidea.framework.remoting.transport.protocol.bodybean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


/**
 * 消息父类
 * Message为body反序列化后的结果
 * 为后续pipeline handler的处理对象
 */
@Getter
@Setter
public abstract class Message {

    /**
     * 获取message对象对应的消息类型
     * 以便在封装协议帧时，填写对应字段
     * @return msg type. define in ProtocolFrameDefinition
     */
    public abstract byte getMessageType();
}
