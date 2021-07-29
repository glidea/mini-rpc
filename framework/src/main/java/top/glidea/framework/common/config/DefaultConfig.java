package top.glidea.framework.common.config;

import java.util.HashMap;
import java.util.Map;

public class DefaultConfig {
    private static final Map<String, Object> MAP = new HashMap<>();
    static {
        MAP.put(ConfigOption.COMPRESS_THRESHOLD, 1400);
        MAP.put(ConfigOption.PROVIDER_EXCEPTION_IGNORE_STACK_TRACE, true);
        MAP.put(ConfigOption.PROVIDER_PUBLISH_PORT, 8181);
        MAP.put(ConfigOption.PROVIDER_THREAD_NUM_BOSS, 1);
        MAP.put(ConfigOption.PROVIDER_THREAD_NUM_WORKER, Runtime.getRuntime().availableProcessors() + 1);
        MAP.put(ConfigOption.PROVIDER_THREAD_NUM_SERVICE, 200);
        MAP.put(ConfigOption.PROVIDER_ENABLE_ASYNC, true);
        MAP.put(ConfigOption.PROVIDER_LOAD_BALANCE_WEIGHT, 100);
    }

    public static Object get(String option) {
        return MAP.get(option);
    }
}
