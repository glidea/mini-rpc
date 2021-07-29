package top.glidea.framework.remoting.transport.protocol.bodybean;


import lombok.Getter;
import lombok.Setter;
import top.glidea.framework.remoting.transport.protocol.ProtocolFrameDefinition;

/**
 * consumer心跳请求
 */
@Getter
@Setter
public class Ping extends Message {

    @Override
    public byte getMessageType() {
        return ProtocolFrameDefinition.PING_TYPE;
    }
}
