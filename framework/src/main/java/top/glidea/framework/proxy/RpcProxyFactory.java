package top.glidea.framework.proxy;

import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.pojo.ServiceKey;

import java.lang.reflect.Proxy;

/**
 * Rpc代理对象工厂
 */
@Slf4j
public class RpcProxyFactory {

    public static Object get(ServiceKey serviceKey) {
        try {
            Class<?> serviceClass = Class.forName(serviceKey.getInterfaceName());
            Object proxy = Proxy.newProxyInstance(
                    serviceClass.getClassLoader(),
                    new Class[]{serviceClass},
                    new RpcProxyInvocationHandler(serviceKey, false)
            );

            return serviceClass.cast(proxy);
        } catch (ClassNotFoundException e) {
            log.error("rpc proxy 获取失败", e);
            return null;
        }
    }

    public static RpcAsyncWrapper getAsync(ServiceKey serviceKey) {
        Object proxy = Proxy.newProxyInstance(
                RpcAsyncWrapper.class.getClassLoader(),
                new Class[]{RpcAsyncWrapper.class},
                new RpcProxyInvocationHandler(serviceKey, true)
        );
        return (RpcAsyncWrapper) proxy;
    }
}
