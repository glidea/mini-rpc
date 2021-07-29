package top.glidea.framework.cluster.circuitbreak;

import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.annotation.CircuitBreak;

import java.util.function.Predicate;

/**
 * 熔断器
 * <p>
 * 不敢保证没有坑，^_^
 */
@Slf4j
public class CircuitBreaker {
    private final CircuitBreakerConfig CONFIG;
    private CircuitBreakerState state;
    private StatisticalWindow window;
    private ThreadLocal<Long> startPassTime;

    public CircuitBreaker(CircuitBreakerConfig config) {
        this.CONFIG = config;
        this.state = new CircuitBreakerState(config.getBreakTime());
        this.window = new StatisticalWindow(config.getStatIntervalMs());
        this.startPassTime = new ThreadLocal<>();
    }

    public boolean checkPass() {
        long now = System.currentTimeMillis();
        // 熔断器打开
        if (state.isOpen(now)) {
            return false;
        }
        // 或者熔断器半开，但获取尝试机会失败
        if (state.isHalfOpen(now) && !state.tryAcquireClosedChance(now)) {
            return false;
        }

        // pass
        startPassTime.set(now);
        return true;
    }

    public void countOkAndSlow() {
        countIfNoOpen(invokeTime ->
                invokeTime < CONFIG.getSlowThreshold() || CONFIG.isErrorRatioRule()
        );
    }

    public void countFail() {
        countIfNoOpen(invokeTime ->
                CONFIG.isSlowRequestRatioRule() && invokeTime < CONFIG.getSlowThreshold()
        );
    }

    /**
     * count if state is close or half open
     */
    private void countIfNoOpen(Predicate<Long> happyCondition) {
        long now = System.currentTimeMillis();
        if (state.isOpen(now)) {
            log.debug("熔断器已打开，暂停统计。real timestamp: {}", now);
            return;
        }

        // get invokeTime;
        Long startTime = startPassTime.get();
        long invokeTime = now - startTime;
        startPassTime.remove();
        log.debug("调用耗时：{}", invokeTime);

        // do count0
        if (happyCondition.test(invokeTime)) {
            count0(true);
        } else {
            count0(false);
        }
    }

    private void count0(boolean happyComplete) {
        long now = System.currentTimeMillis();
        if (state.checkPendingClosedTry(now)) {
            // after invoke. the thread that tried to close the CircuitBreaker came back
            state.reportClosedTry(happyComplete, now);
        }

        if (state.isClosed(now)) {
            // refresh window
            window.syncClock(now);

            // get current statData. update it
            StatisticalData statData = window.getStatData();
            if (happyComplete) {
                // happy count
                statData.increaseTotalCountOnly();
                log.debug("happy++ done. real timestamp: {}", now);
                return;
            }

            // unhappy count
            if (CONFIG.isSlowRequestRatioRule()) {
                statData.increaseSlowCount();
            } else if (CONFIG.isErrorRatioRule()) {
                statData.increaseFailCount();
            } else {
                statData.increaseCombCount();
            }

            // try open breaker. if the situation of this statData is bad enough
            if (state.isClosed(now) &&
                    statData.enoughBad(CONFIG.getRule(), CONFIG.getMinRequestAmount(), CONFIG.getRatioThreshold())) {
                state.open(now);
            }
            log.debug("bad++ done. real timestamp: {}", now);
        } else {
            log.debug("熔断器状态：打开或半开，暂停统计。real timestamp: {}", now);
        }
    }
}
