package top.glidea.framework.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.KeeperException;
import top.glidea.framework.common.Constants;
import top.glidea.framework.common.util.LockUtil;
import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.common.pojo.ServiceKey;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final ConcurrentHashMap<Object, Object> LOCK_MAP = new ConcurrentHashMap<>();
    private CuratorFramework client = ConfigZkClientHolder.get();

    private Map<ServiceKey, List<ProviderInfo>> providerInfoMap = new ConcurrentHashMap<>();

    /**
     * 为什么不使用ConcurrentHashMap#computeIfAbsent？
     * <p>
     * 假设同时刻，有两个线程get serviceKeyA，两个线程get serviceKeyB，
     * 并且serviceKeyA和serviceKeyB恰巧位于ConcurrentHashMap的同个桶内。
     * 此时并发度只有1，而computeIfAbsent的初始化操作包含网络请求，so...
     * 而dcl方式，即使serviceKeyA和serviceKeyB产生了Hash冲突，网络请求依然能够并行，
     * 虽然增加了一、两次加解锁，但和网络请求相比可以忽略。
     * <p>
     * 所以在map value的初始化过程较慢的情况下，DCL + ConcurrentHashMap是更好的选择，但要注意DCL的锁粒度
     */
    @Override
    public List<ProviderInfo> listProviderInfo(ServiceKey serviceKey) {
        if (!providerInfoMap.containsKey(serviceKey)) {
            // 考虑到serviceKey有被外界也作为锁的可能性，所以不考虑使用serviceKey作为锁
            // 考虑到锁粒度，使用从LockUtil#get获取的锁
            synchronized (LockUtil.get(LOCK_MAP, serviceKey)) {
                if (!providerInfoMap.containsKey(serviceKey)) {
                    try {
                        List<String> providerInfoStrs = client.getChildren()
                                .forPath(Constants.ZK_REGISTER_ROOT_PATH + serviceKey);
                        providerInfoMap.put(serviceKey, ProviderInfo.castAll(providerInfoStrs));
                        registerWatcher(serviceKey);

                    } catch (KeeperException.NoNodeException ignored) {
                    } catch (Exception e) {
                        throw new RuntimeException("服务拉取失败", e);
                    }
                }
            }
        }
        // 考虑到性能，这里直接暴露引用
        // 泄漏出去后，修改需要在备份基础上修改
        return providerInfoMap.get(serviceKey);
    }

    private void registerWatcher(ServiceKey serviceKey) throws Exception {
        String servicePath = Constants.ZK_REGISTER_ROOT_PATH + serviceKey.toString();
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            log.debug("服务Provider的Zk信息有变。changed on" + serviceKey);
            List<String> providerInfoStrs = curatorFramework.getChildren().forPath(servicePath);
            providerInfoMap.put(serviceKey, ProviderInfo.castAll(providerInfoStrs));
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }
}
