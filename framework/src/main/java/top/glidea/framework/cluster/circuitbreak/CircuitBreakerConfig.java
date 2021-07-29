package top.glidea.framework.cluster.circuitbreak;

import lombok.Builder;
import lombok.Getter;
import top.glidea.framework.common.annotation.CircuitBreak;

@Getter
@Builder
public class CircuitBreakerConfig {
    private int rule;
    private long slowThreshold;
    private double ratioThreshold;
    private long statIntervalMs;
    private int minRequestAmount;
    private int breakTime;

    public boolean isSlowRequestRatioRule() {
        return rule == CircuitBreak.RuleConstant.SLOW_REQUEST_RATIO;
    }

    public boolean isErrorRatioRule() {
        return rule == CircuitBreak.RuleConstant.ERROR_RATIO;
    }

    public boolean isCombRatioRule() {
        return rule == CircuitBreak.RuleConstant.COMB_RATIO;
    }
}
