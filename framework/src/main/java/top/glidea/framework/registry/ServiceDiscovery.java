package top.glidea.framework.registry;

import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.common.pojo.ServiceKey;

import java.util.List;

public interface ServiceDiscovery {
    String NAME = "service-discovery";

    /**
     * 康康有哪些Provider能提供serviceKey对应的服务
     * @param serviceKey service id
     * @return provider infos of can provide this service from registry
     */
    List<ProviderInfo> listProviderInfo(ServiceKey serviceKey);
}
