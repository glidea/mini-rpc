package top.glidea.framework.common.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局的Sequence id生成器
 */
public class SequenceIdGenerator {
    private static final AtomicInteger COUNT = new AtomicInteger();

    /**
     * get id
     * 由于id会回滚，所以仅适合id具有时效性的场景
     */
    public static int nextId() {
        if (COUNT.get() == Integer.MAX_VALUE) {
            // 回滚 id，防止溢出
            COUNT.set(0);
        }
        return COUNT.incrementAndGet();
    }
}
