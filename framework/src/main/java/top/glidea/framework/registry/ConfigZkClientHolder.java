package top.glidea.framework.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.config.Config;
import top.glidea.framework.common.config.ConfigOption;
import top.glidea.framework.common.config.YmlConfig;

import java.util.concurrent.TimeUnit;

public class ConfigZkClientHolder {
    private static Config config = SingletonFactory.get(YmlConfig.class);

    private static final CuratorFramework CLIENT;
    private static final int RETRY_TIMES = 3;
    private static final int RETRY_INTERVAL_MS = 1000;
    private static final int MAX_WAIT_TIME = RETRY_TIMES * (10000 + RETRY_INTERVAL_MS);  // ensure maxWaitTime > connect process

    static {
        String registryAddress = config.getNotNull(ConfigOption.REGISTRY_ADDRESS);
        CLIENT = CuratorFrameworkFactory.newClient(
                registryAddress,
                new RetryNTimes(RETRY_TIMES, RETRY_INTERVAL_MS)
        );
        CLIENT.start();

        try {
            if (!CLIENT.blockUntilConnected(MAX_WAIT_TIME, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("timout to connect");
            }
        } catch (Exception e) {
            throw new RuntimeException("注册中心 [" + registryAddress + "] 连接失败", e);
        }
    }

    public static CuratorFramework get() {
        return CLIENT;
    }
}
