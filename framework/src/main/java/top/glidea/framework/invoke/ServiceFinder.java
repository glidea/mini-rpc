package top.glidea.framework.invoke;

import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.pojo.Address;
import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.common.pojo.ServiceInfo;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.common.util.ReflectUtil;
import top.glidea.framework.common.config.Config;
import top.glidea.framework.common.config.ConfigOption;
import top.glidea.framework.common.config.YmlConfig;
import top.glidea.framework.cluster.loadbalance.LoadBalance;
import top.glidea.framework.common.factory.ExpansionFactory;
import top.glidea.framework.registry.ServiceDiscovery;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceFinder {
    private final Config config = SingletonFactory.get(YmlConfig.class);

    private ServiceDiscovery serviceDiscovery = ExpansionFactory.get(ServiceDiscovery.class);
    private LoadBalance loadBalance = ExpansionFactory.get(LoadBalance.class);

    private Map<ServiceKey, ServiceInfo> serviceInfoMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public ServiceInfo getServiceInfo(ServiceKey serviceKey) {
        List<ProviderInfo> providerInfos = serviceDiscovery.listProviderInfo(serviceKey);
        ServiceInfo serviceInfo = serviceInfoMap.computeIfAbsent(serviceKey, k -> {
            HashMap<String, Object> matchArgs = new HashMap<>();
            matchArgs.put("interface", serviceKey.getInterfaceName());
            List<Map> methodConfigList = config.getFromMapList(ConfigOption.CONSUMER_SERVICES, "methods", List.class, matchArgs);
            Map<String, ServiceInfo.MethodInvokeInfo> methodConfigMap = new HashMap<>();
            if (methodConfigList != null) {
                for (Map map : methodConfigList) {
                    ServiceInfo.MethodInvokeInfo methodInvokeInfo = ReflectUtil.mapToObject(map, ServiceInfo.MethodInvokeInfo.class);
                    methodConfigMap.put((String) map.get("name"), methodInvokeInfo);
                }
            }
            return new ServiceInfo(providerInfos, methodConfigMap);
        });
        serviceInfo.setProviderInfos(providerInfos);
        return serviceInfo;
    }

    public Address choseAddress(ServiceKey serviceKey, RpcRequest rpcRequest, Set<Address> excludeAddresses) {
        List<ProviderInfo> providerInfos = getServiceInfo(serviceKey).getProviderInfos();
        List<ProviderInfo> bakProviderInfos = providerInfos;
        if (excludeAddresses.size() > 0) {
            bakProviderInfos = new ArrayList<>(providerInfos);
            for (Address excludeAddress : excludeAddresses) {
                bakProviderInfos.removeIf(providerInfo -> providerInfo.getAddress().equals(excludeAddress));
            }
        }
        ProviderInfo providerInfo = loadBalance.select(bakProviderInfos, rpcRequest);
        return providerInfo == null ? null : providerInfo.getAddress();
    }

    public Address choseAddress(ServiceKey serviceKey, RpcRequest rpcRequest) {
        Set<Address> excludeAddresses = new HashSet<>();
        return choseAddress(serviceKey, rpcRequest, excludeAddresses);
    }
}
