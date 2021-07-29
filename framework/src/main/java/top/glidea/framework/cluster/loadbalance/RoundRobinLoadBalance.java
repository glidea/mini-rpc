package top.glidea.framework.cluster.loadbalance;

import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 加权轮询负载均衡
 * <p>
 * 参考Dubbo实现
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {
    private static final int RECYCLE_PERIOD = 60000;

    /**
     * 虽然这把锁粒度比较粗，但是不影响性能。
     * 换个说法是就算把锁细化成每个ConcurrentMap<ProviderInfo, WeightedRoundRobin>一把，性能不会有啥改变。
     * 这里的粗锁唯一的坏处是内存清理延迟相对高一点
     */
    private AtomicBoolean updateLock = new AtomicBoolean();
    private Map<ServiceKey, ConcurrentMap<ProviderInfo, WeightedRoundRobin>> serviceWeightMap = new ConcurrentHashMap<>();

    @Override
    protected ProviderInfo doSelect(List<ProviderInfo> providerInfos, RpcRequest rpcRequest) {
        ServiceKey key = rpcRequest.getServiceKey();
        ConcurrentMap<ProviderInfo, WeightedRoundRobin> wrrMap = serviceWeightMap.get(key);
        if (wrrMap == null) {
            serviceWeightMap.putIfAbsent(key, new ConcurrentHashMap<>());
            wrrMap = serviceWeightMap.get(key);
        }

        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        ProviderInfo selectedProviderInfo = null;
        WeightedRoundRobin selectedWRR = null;

        for (ProviderInfo providerInfo : providerInfos) {
            WeightedRoundRobin wrr = wrrMap.get(providerInfo);
            int weight = providerInfo.getWeight();
            if (wrr == null) {
                wrr = new WeightedRoundRobin();
                wrr.setWeight(weight);
                wrrMap.putIfAbsent(providerInfo, wrr);
                wrr = wrrMap.get(providerInfo);
            }

            long cur = wrr.increaseCurrent();
            wrr.setLastUpdate(now);
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedProviderInfo = providerInfo;
                selectedWRR = wrr;
            }
            totalWeight += weight;
        }

        if (!updateLock.get() && providerInfos.size() != wrrMap.size()) {
            if (updateLock.compareAndSet(false, true)) {
                try {
                    ConcurrentMap<ProviderInfo, WeightedRoundRobin> newMap = new ConcurrentHashMap<>(wrrMap);
                    newMap.entrySet().removeIf(e -> now - e.getValue().getLastUpdate() > RECYCLE_PERIOD);
                    serviceWeightMap.put(key, newMap);
                } finally {
                    updateLock.set(false);
                }
            }
        }

        if (selectedProviderInfo != null) {
            selectedWRR.sel(totalWeight);
            return selectedProviderInfo;
        }
        return providerInfos.get(0);
    }


    protected static class WeightedRoundRobin {
        private int weight;
        private AtomicLong current = new AtomicLong();
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }
        public void setWeight(int weight) {
            this.weight = weight;
            current.set(0);
        }
        public long increaseCurrent() {
            return current.addAndGet(weight);
        }
        public void sel(int total) {
            current.addAndGet(-1 * total);
        }
        public long getLastUpdate() {
            return lastUpdate;
        }
        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
}
