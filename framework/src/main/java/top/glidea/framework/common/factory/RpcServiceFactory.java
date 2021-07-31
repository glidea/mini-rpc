package top.glidea.framework.common.factory;

import top.glidea.framework.common.springcontext.SpringContext;
import top.glidea.framework.common.annotation.RpcService;
import top.glidea.framework.common.pojo.ServiceKey;

import java.util.HashMap;
import java.util.Map;


public class RpcServiceFactory {
    private static final Map<ServiceKey, Object> SERVICE_MAP = new HashMap<>();
    static {
        // build mapping
        Map<Class<?>, Object> beanMap = SpringContext.getInterfaceAndImplBeanMap(RpcService.class);
        for (Class<?> serviceClass : beanMap.keySet()) {
            ServiceKey serviceKey = new ServiceKey(serviceClass.getName());
            Object serviceBean = beanMap.get(serviceClass);
            SERVICE_MAP.put(serviceKey, serviceBean);
        }
    }

    public static Object get(ServiceKey serviceKey) {
        Object serviceBean = SERVICE_MAP.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException("无此服务：" + serviceKey.toString());
        }
        return serviceBean;
    }
}
