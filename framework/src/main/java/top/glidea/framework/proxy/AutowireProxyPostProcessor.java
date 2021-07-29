package top.glidea.framework.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.framework.common.pojo.ServiceKey;

import java.lang.reflect.Field;

/**
 * Spring Post Processor for RpcAutowired
 * <p>
 * eg: @RpcAutowired
 *     HelloService helloService;
 * 为helloService注入HelloService的同步rpc代理对象
 */
@Component
@Slf4j
public class AutowireProxyPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            RpcAutowired annotation = field.getAnnotation(RpcAutowired.class);
            Class<?> fieldClass = field.getType();

            if (annotation != null && fieldClass.isInterface()) {
                ServiceKey serviceKey = new ServiceKey(fieldClass.getName());
                Object proxy = RpcProxyFactory.get(serviceKey);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    // should not be here
                    log.error("rpc代理Autowire失败", e);
                }
            }
        }
        return bean;
    }
}
