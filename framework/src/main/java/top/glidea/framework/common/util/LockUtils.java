package top.glidea.framework.common.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取synchronize锁"对象"的工具类
 */
public class LockUtils {

    /**
     * get one lock obj from a private ConcurrentHashMap
     * @return synchronize锁对象（注意是"对象"，而不是锁）
     */
    public static Object get(ConcurrentHashMap<Object, Object> lockMap, Object key) {
        return lockMap.computeIfAbsent(key, k-> new Object());
    }
}
