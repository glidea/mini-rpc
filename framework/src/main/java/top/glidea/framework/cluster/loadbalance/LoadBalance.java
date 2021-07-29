package top.glidea.framework.cluster.loadbalance;

import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;

import java.util.List;

public interface LoadBalance {
    String NAME = "load-balance";

    ProviderInfo select(List<ProviderInfo> providerInfos, RpcRequest rpcRequest);
}
