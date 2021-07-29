package top.glidea.framework.remoting.transport.protocol.bodybean;

import lombok.Getter;
import lombok.Setter;
import top.glidea.framework.remoting.transport.protocol.ProtocolFrameDefinition;

/**
 * provider心跳响应
 *
 */
@Getter
@Setter
public class Pong extends Message {
    // TODO 携带Provider健康信息（负载信息等等...）

    @Override
    public byte getMessageType() {
        return ProtocolFrameDefinition.PONG_TYPE;
    }
}
