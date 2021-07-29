package top.glidea.framework.cluster.loadbalance;

import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.config.Config;
import top.glidea.framework.common.config.YmlConfig;
import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {
    private Config config = SingletonFactory.get(YmlConfig.class);

    @Override
    public ProviderInfo select(List<ProviderInfo> providerInfos, RpcRequest rpcRequest) {
        if (providerInfos == null || providerInfos.size() == 0) {
            return null;
        }
        if (providerInfos.size() == 1) {
            return providerInfos.get(0);
        }
        return doSelect(providerInfos, rpcRequest);
    }

    protected abstract ProviderInfo doSelect(List<ProviderInfo> providerInfos, RpcRequest rpcRequest);
}
