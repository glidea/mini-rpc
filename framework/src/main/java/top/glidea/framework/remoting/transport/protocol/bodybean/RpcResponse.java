package top.glidea.framework.remoting.transport.protocol.bodybean;

import lombok.*;
import top.glidea.framework.remoting.transport.protocol.ProtocolFrameDefinition;

/**
 * Rpc调用结果
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse extends Message {
    // TODO 携带Provider健康信息（负载信息等等...）

    private int sequenceId;
    private Object returnValue;
    private Throwable exceptionValue;

    @Override
    public byte getMessageType() {
        return ProtocolFrameDefinition.RPC_RESP_TYPE;
    }
}
