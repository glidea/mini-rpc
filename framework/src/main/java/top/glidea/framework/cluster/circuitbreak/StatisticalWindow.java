package top.glidea.framework.cluster.circuitbreak;

import lombok.extern.slf4j.Slf4j;

/**
 * 熔断器统计信息窗口
 */
@Slf4j
public class StatisticalWindow {

    private StatDataCircularArray statDataCircularArray;
    private final long statIntervalMs;
    private volatile long lastWindowOpenTime;

    public StatisticalWindow(long statIntervalMs) {
        // 我也不知道这个size多大合适，但5应该是足够充裕的
        // 除非大量请求在同个时间段完成，最终统计结果耗费了很久的时间，才有可能导致该时间段的data被reset
        // reset了，除非情况比较极端，否则造不成多大误差
        statDataCircularArray = new StatDataCircularArray(5);
        this.statIntervalMs = statIntervalMs;
        lastWindowOpenTime = System.currentTimeMillis();
    }

    public StatisticalData getStatData() {
        return statDataCircularArray.getCurrent();
    }

    public void syncClock(long now) {
        if (now - lastWindowOpenTime > statIntervalMs) {
            synchronized (this) {
                if (now - lastWindowOpenTime > statIntervalMs) {
                    statDataCircularArray.goNext();
                    lastWindowOpenTime = now;
                    log.debug("熔断器统计信息窗口已刷新。上个real timestamp: {}", now);
                }
            }
        }
    }

    private static class StatDataCircularArray {
        private StatisticalData[] statDataArr;
        private int index;
        private int size;

        public StatDataCircularArray(int size) {
            this.size = size;
            statDataArr = new StatisticalData[size];
            for (int i = 0; i < size; i++) {
                statDataArr[i] = new StatisticalData();
            }
        }

        public StatisticalData getCurrent() {
            return statDataArr[index];
        }

        public void goNext() {
            int nextIndex = (index + 1) % size;
            statDataArr[nextIndex].reset();
            index = nextIndex;
        }
    }
}
