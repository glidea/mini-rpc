package top.glidea.framework.common.factory;

import top.glidea.framework.common.util.LockUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例工厂
 */
public class SingletonFactory {
    private static final Map<Class<?>, Object> CLASS_OBJECT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Object, Object> LOCK_MAP = new ConcurrentHashMap<>();

    public static <T> T get(Class<T> instanceClass) {
        return get(instanceClass, null);  // pass null. to avoid new Map
    }

    /**
     * 以下一种情况千万别用ConcurrentHashMap#computeIfAbsent实现单例工厂
     * class A {
     *     B b = SingletonFactory.get(B.class)
     * }
     * A b = SingletonFactory.get(A.class)
     * 当A.class和B.class恰巧产生哈希冲突，会产生死锁。调试了好久，淦！
     */
    public static <T> T get(Class<T> instanceClass, Map<Class<?>, Object> constructArgs) {
        if (CLASS_OBJECT_MAP.get(instanceClass) == null) {
            // 锁对象详细说明，见：ZkServiceDiscovery
            // 设想对象刚暴露出去，外部调了某个方法，这个方法上了类锁...
            synchronized (LockUtils.get(LOCK_MAP, instanceClass)) {
                if (CLASS_OBJECT_MAP.get(instanceClass) == null) {
                    try {
                        Object o;
                        if (constructArgs == null) {
                            o = instanceClass.getDeclaredConstructor().newInstance();
                        } else {
                            Class<?>[] argTypes = (Class<?>[]) constructArgs.keySet().toArray();
                            Object[] argValues = constructArgs.values().toArray();
                            o = instanceClass.getDeclaredConstructor(argTypes).newInstance(argValues);
                        }
                        CLASS_OBJECT_MAP.put(instanceClass, o);
                    } catch (Exception e) {
                        throw new RuntimeException("init object fail", e);
                    }
                }
            }
        }
        return instanceClass.cast(CLASS_OBJECT_MAP.get(instanceClass));
    }
}
