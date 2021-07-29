package top.glidea.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CircuitBreak {
    interface RuleConstant {
        /**
         * 慢调用比例
         */
        int SLOW_REQUEST_RATIO = 1;
        /**
         * 异常比例
         */
        int ERROR_RATIO = 2;
        /**
         * 组合（慢调用-异常）比例
         * 我yy的，看看就好，而上面两种是Sentinel自带的，经过了生产环境的验证
         */
        int COMB_RATIO = 3;
    }

    /**
     * 触发熔断的策略
     */
    int rule() default RuleConstant.SLOW_REQUEST_RATIO;
    /**
     * 慢调用阈值 RT（超出该值计为慢调用）(ms)
     * 仅慢调用比例/组合比例模式有效
     */
    long slowThreshold() default 2000;
    /**
     * 比例阈值
     * 0.0 - 1.0
     */
    double ratioThreshold() default 0.8;
    /**
     * 统计时长(ms)
     */
    long statIntervalMs() default 1000;
    /**
     * 熔断触发的最小请求数，请求数小于该值时即使慢调用/异常比例超出阈值也不会熔断
     */
    int minRequestAmount() default 5;
    /**
     * 熔断时长，单位为 s
     */
    int breakTime() default 10;
}
