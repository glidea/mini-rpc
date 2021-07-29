package top.glidea.framework.remoting.transport.protocol.bodybean;

import lombok.*;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.remoting.transport.protocol.ProtocolFrameDefinition;

/**
 * Rpc请求
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest extends Message {

    private int sequenceId;
    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameterValues;

    public ServiceKey getServiceKey() {
        return new ServiceKey(interfaceName);
    }

    @Override
    public byte getMessageType() {
        return ProtocolFrameDefinition.RPC_REQ_TYPE;
    }
}
