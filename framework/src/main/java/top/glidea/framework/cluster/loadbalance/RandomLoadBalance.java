package top.glidea.framework.cluster.loadbalance;

import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * 加权随机负载均衡
 * <p>
 * 参考Dubbo实现
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    private final Random random = new Random();

    @Override
    protected ProviderInfo doSelect(List<ProviderInfo> providerInfos, RpcRequest rpcRequest) {
        int totalWeight = 0;
        boolean sameWeight = true;
        for (int i = 0; i < providerInfos.size(); i++) {
            int weight = providerInfos.get(i).getWeight();
            totalWeight += weight;

            if (sameWeight && i > 0
                    && weight != providerInfos.get(i - 1).getWeight()) {
                sameWeight = false;
            }
        }

        if (totalWeight > 0 && !sameWeight) {
            int offset = random.nextInt(totalWeight);
            for (ProviderInfo providerInfo : providerInfos) {
                offset -= providerInfo.getWeight();
                if (offset < 0) {
                    return providerInfo;
                }
            }
        }
        return providerInfos.get(random.nextInt(providerInfos.size()));
    }
}
