package top.glidea.framework.common.factory;

import top.glidea.framework.cluster.loadbalance.LoadBalance;
import top.glidea.framework.cluster.loadbalance.RandomLoadBalance;
import top.glidea.framework.common.config.Config;
import top.glidea.framework.common.config.ConfigOption;
import top.glidea.framework.common.config.YmlConfig;
import top.glidea.framework.registry.ServiceDiscovery;
import top.glidea.framework.registry.ServiceRegistry;
import top.glidea.framework.registry.ZkServiceDiscovery;
import top.glidea.framework.registry.ZkServiceRegistry;
import top.glidea.framework.remoting.compress.Compressor;
import top.glidea.framework.remoting.compress.GzipCompressor;
import top.glidea.framework.remoting.serialize.ProtostuffSerializer;
import top.glidea.framework.remoting.serialize.Serializer;

import java.util.HashMap;
import java.util.Map;

public class ExpansionFactory {
    private static final Config CONFIG = SingletonFactory.get(YmlConfig.class);
    private static final Map<String, Class<?>> DEFAULT_IMPL_CLASS_MAP = new HashMap<>();
    static {
        DEFAULT_IMPL_CLASS_MAP.put(Serializer.NAME, ProtostuffSerializer.class);
        DEFAULT_IMPL_CLASS_MAP.put(Compressor.NAME, GzipCompressor.class);
        DEFAULT_IMPL_CLASS_MAP.put(LoadBalance.NAME, RandomLoadBalance.class);
        DEFAULT_IMPL_CLASS_MAP.put(ServiceDiscovery.NAME, ZkServiceDiscovery.class);
        DEFAULT_IMPL_CLASS_MAP.put(ServiceRegistry.NAME, ZkServiceRegistry.class);
    }
    private static final Map<Class<?>, Object> INTERFACE_IMPLOBJ_MAP = new HashMap<>();
    static {
        try {
            INTERFACE_IMPLOBJ_MAP.put(Serializer.class, getExpansion(Serializer.NAME));
            INTERFACE_IMPLOBJ_MAP.put(Compressor.class, getExpansion(Compressor.NAME));
            INTERFACE_IMPLOBJ_MAP.put(LoadBalance.class, getExpansion(LoadBalance.NAME));
            INTERFACE_IMPLOBJ_MAP.put(ServiceDiscovery.class, getExpansion(ServiceDiscovery.NAME));
            INTERFACE_IMPLOBJ_MAP.put(ServiceRegistry.class, getExpansion(ServiceRegistry.NAME));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("getExpansion fail", e);
        }
    }

    public static <T> T get(Class<T> interfaceClass) {
        Object o = INTERFACE_IMPLOBJ_MAP.get(interfaceClass);
        return interfaceClass.cast(o);
    }

    private static Object getExpansion(String name) throws ClassNotFoundException {
        Map map = CONFIG.get(ConfigOption.EXPANSION, Map.class);
        if (map == null) {
            map = new HashMap();
        }

        Object expansion;
        Class<?> expansionClass;
        String className = (String) map.get(name);
        if (className == null) {
            expansionClass = DEFAULT_IMPL_CLASS_MAP.get(name);
        } else {
            expansionClass = Class.forName(className);
        }

        expansion = SingletonFactory.get(expansionClass);
        return expansion;
    }
}
