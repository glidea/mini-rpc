package top.glidea.framework.cluster.circuitbreak;

import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.annotation.CircuitBreak;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 熔断器统计信息
 */
@Slf4j
public class StatisticalData {
    private ReadWriteLock rw = new ReentrantReadWriteLock();
    private int slowCount;
    private int failCount;
    private int combCount;
    private int totalCount;

    public boolean enoughBad(int rule, int minRequestAmount, double ratioThreshold) {
        rw.readLock().lock();
        try {
            double badRatio;
            if (totalCount >= minRequestAmount
                    && (badRatio = getRatio(rule)) >= ratioThreshold) {
                log.debug("enough Bad!!! 准备尝试以CAS方式打开熔断器。[totalCount = {}]、[badRatio = {}]", totalCount, badRatio);
                return true;
            }
            return false;
        } finally {
            rw.readLock().unlock();
        }
    }

    public void increaseSlowCount() {
        atomicUpdate(() -> {
            slowCount++;
            totalCount++;
        });
    }

    public void increaseFailCount() {
        atomicUpdate(() -> {
            failCount++;
            totalCount++;
        });
    }

    public void increaseCombCount() {
        atomicUpdate(() -> {
            combCount++;
            totalCount++;
        });
    }

    public void increaseTotalCountOnly() {
        atomicUpdate(() -> totalCount++);
    }

    public void reset() {
        atomicUpdate(() -> {
            slowCount = 0;
            failCount = 0;
            totalCount = 0;
        });
    }

    public double getRatio(int rule) {
        rw.readLock().lock();
        try {
            int unhappyCount;
            if (rule == CircuitBreak.RuleConstant.SLOW_REQUEST_RATIO) {
                unhappyCount = slowCount;
            } else if (rule == CircuitBreak.RuleConstant.ERROR_RATIO) {
                unhappyCount = failCount;
            } else {
                unhappyCount = combCount;
            }
            return (double) unhappyCount / totalCount;
        } finally {
            rw.readLock().unlock();
        }
    }

    private void atomicUpdate(Runnable updateTask) {
        rw.writeLock().lock();
        try {
            updateTask.run();
        } finally {
            rw.writeLock().unlock();
        }
    }
}
