package top.glidea.framework.cluster.circuitbreak;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.glidea.framework.common.annotation.CircuitBreak;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 熔断器环绕切面
 */
@Aspect
@Order(2)
@Component
@Slf4j
public class CircuitBreakAspect {
    private Map<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();

    @Pointcut("@annotation(top.glidea.framework.common.annotation.CircuitBreak)")
    public void targetMethodPointcut() {}

    @Around("targetMethodPointcut() && @annotation(circuitBreakAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, CircuitBreak circuitBreakAnnotation) throws Throwable {
        // get CircuitBreaker
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        CircuitBreaker circuitBreaker = getCircuitBreaker(methodSignature, circuitBreakAnnotation);
        String reqTargetName = String.format("%s#%s", methodSignature.getDeclaringTypeName(), methodSignature.getName());

        // checkPass
        boolean pass = circuitBreaker.checkPass();
        if (!pass) {
            throw new RuntimeException(reqTargetName + " 已被熔断");
        }
        log.debug("{} 方法的请求通过了熔断器", reqTargetName);

        // pass to handle request
        Object proceed;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable e) {
            // handle req fail. count for CircuitBreaker
            log.debug("{} 方法的请求发生了异常", reqTargetName);
            circuitBreaker.countFail();
            throw e;
        }

        // handle success. But the request may make the CircuitBreaker unhappy
        circuitBreaker.countOkAndSlow();
        return proceed;
    }

    /**
     * get a CircuitBreaker for reqTarget
     */
    private CircuitBreaker getCircuitBreaker(MethodSignature signature, CircuitBreak circuitBreakAnnotation) {
        String key = signature.getDeclaringTypeName() + ":" + signature.getName();
        return circuitBreakerMap.computeIfAbsent(key, k -> {
            CircuitBreakerConfig config = CircuitBreakerConfig.builder()
                    .rule(circuitBreakAnnotation.rule())
                    .slowThreshold(circuitBreakAnnotation.slowThreshold())
                    .ratioThreshold(circuitBreakAnnotation.ratioThreshold())
                    .statIntervalMs(circuitBreakAnnotation.statIntervalMs())
                    .minRequestAmount(circuitBreakAnnotation.minRequestAmount())
                    .breakTime(circuitBreakAnnotation.breakTime())
                    .build();
            return new CircuitBreaker(config);
        });
    }
}
