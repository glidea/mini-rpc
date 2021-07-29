package top.glidea.framework.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 保证SpringContext首先被加载
 */
@Component
public class LoadOrderPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        List<String> orderBeanNames = new ArrayList<>();
        Map<String, BeanDefinition> beans = new HashMap<>();
        orderBeanNames.add("springContext");
        for (String beanDefinitionName : beanDefinitionNames) {
            if (!"springContext".equals(beanDefinitionName)) {
                orderBeanNames.add(beanDefinitionName);
            }
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            registry.removeBeanDefinition(beanDefinitionName);
            beans.put(beanDefinitionName, beanDefinition);
        }
        for (String orderBeanName : orderBeanNames) {
            registry.registerBeanDefinition(orderBeanName, beans.get(orderBeanName));
        }
    }
}
