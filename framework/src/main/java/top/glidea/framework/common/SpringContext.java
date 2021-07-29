package top.glidea.framework.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.glidea.framework.common.util.ReflectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Component
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static Collection<Object> getBeansWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<String, Object> beans = context.getBeansWithAnnotation(annotationClass);
        return beans.values();
    }

    public static List<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotationClass) {
        List<Class<?>> classes = new ArrayList<>();
        Collection<Object> beansWithAnnotation = getBeansWithAnnotation(annotationClass);
        for (Object bean : beansWithAnnotation) {
            Class<?> beanClass = bean.getClass();
            classes.add(beanClass);
        }
        return classes;
    }

    public static Map<Class<?>, Object> getInterfaceAndImplBeanMap(Class<? extends Annotation> annotationWithInterfaceProp) {
        Map<Class<?>, Object> map = new HashMap<>();
        Collection<Object> beansWithAnnotation = getBeansWithAnnotation(annotationWithInterfaceProp);
        for (Object bean : beansWithAnnotation) {
            Object target = ReflectUtil.getTarget(bean);
            if (target == null) {
                target = bean;
            }
            Annotation annotation = target.getClass().getAnnotation(annotationWithInterfaceProp);
            Class<?> interfaceProp;
            try {
                Method method = annotationWithInterfaceProp.getMethod("interfaceClass");
                interfaceProp = (Class<?>) method.invoke(annotation);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(annotationWithInterfaceProp.getName() + "没有interfaceClass属性", e);
            }

            if (map.containsKey(interfaceProp)) {
                throw new RuntimeException(interfaceProp.getName() + "找到多个实现类");
            }
            if (!interfaceProp.isAssignableFrom(bean.getClass())) {
                throw new RuntimeException(bean.getClass().getName() +
                        "未实现" + interfaceProp.getName() + "接口");
            }
            map.put(interfaceProp, bean);
        }
        return map;
    }
}
