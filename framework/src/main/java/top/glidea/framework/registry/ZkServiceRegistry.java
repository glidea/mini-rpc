package top.glidea.framework.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import top.glidea.framework.common.Constants;
import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.common.pojo.ServiceKey;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    private final CuratorFramework client = ConfigZkClientHolder.get();

    private static Set<String> registeredServicePaths = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void register(ServiceKey serviceKey, ProviderInfo providerInfo) {
        try {
            String path = String.format(Constants.ZK_REGISTER_ROOT_PATH
                    + "%s/%s", serviceKey.toString(), providerInfo.toString());
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);

            client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
                if (connectionState == ConnectionState.RECONNECTED) {
                    log.info("registry已重新连接");
                    register(serviceKey, providerInfo);
                }
            });
            registeredServicePaths.add(path);
            log.debug(providerInfo.toString() + " registered on " + serviceKey);

        } catch (KeeperException.NodeExistsException ignored) {
        } catch (Exception e) {
            throw new RuntimeException("服务注册失败", e);
        }
    }

    @Override
    public void unRegisterAll() {
        for (String path : registeredServicePaths) {
            try {
                client.delete().deletingChildrenIfNeeded()
                        .forPath(path);
            } catch (Exception e) {
                log.error("unRegisterAll fail", e);
            }
        }
    }
}
