package top.glidea.framework.cluster.circuitbreak;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 熔断器状态
 */
@Slf4j
public class CircuitBreakerState {
    private final int breakTime;
    private AtomicBoolean cut;
    private volatile long lastOpenStartTime;
    private AtomicBoolean tryClosing;
    private volatile Thread acquireClosedThread;

    public CircuitBreakerState(int breakTime) {
        this.breakTime = breakTime;
        cut = new AtomicBoolean();
        tryClosing = new AtomicBoolean();
    }

    public void open(long now) {
        if (cut.compareAndSet(false, true)) {
            lastOpenStartTime = now;
            log.debug("熔断器状态打开，{}秒后尝试关闭（半开）。real timestamp: {}", breakTime, now);
        }
    }

    public boolean tryAcquireClosedChance(long now) {
        if (isHalfOpen(now)) {
            if (tryClosing.compareAndSet(false, true)) {
                if (isHalfOpen(now)) {
                    acquireClosedThread = Thread.currentThread();
                    log.debug("准备尝试关闭熔断器。real timestamp: {}", now);
                    return true;
                } else {
                    tryClosing.set(false);
                }
            }
        }
        return false;
    }

    public boolean checkPendingClosedTry(long now) {
        return isHalfOpen(now)
                && tryClosing.get()
                && Thread.currentThread().equals(acquireClosedThread);
    }

    public void reportClosedTry(boolean isSuccess, long now) {
        acquireClosedThread = null;
        tryClosing.set(false);
        if (isSuccess) {
            log.debug("尝试关闭熔断器成功");
            close0(now);
        } else {
            log.debug("尝试关闭熔断器失败，熔断器即将从半开 --> 打开");
            open(now);
        }
    }

    private void close0(long now) {
        cut.set(false);
        log.debug("熔断器已关闭。real timestamp: {}", now);
    }

    public boolean isOpen(long now) {
        return cut.get() &&
                now - lastOpenStartTime < breakTime * 1000;
    }

    public boolean isHalfOpen(long now) {
        return cut.get() && !isOpen(now);
    }

    public boolean isClosed(long now) {
        return !isOpen(now) && !isHalfOpen(now);
    }
}
