package top.glidea.framework.cluster.ratelimit;

import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.glidea.framework.common.annotation.RateLimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Order(3)
@Component
public class RateLimitAspect {
    private Map<String, RateLimiter> rateLimiterCache = new ConcurrentHashMap<>();

    @Pointcut("@annotation(top.glidea.framework.common.annotation.RateLimit)")
    public void targetMethodPointcut() {}

    @Before("targetMethodPointcut() && @annotation(rateLimitAnnotation)")
    public void before(JoinPoint joinPoint, RateLimit rateLimitAnnotation) {
        // 获取对应的rateLimiter
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String key = signature.getDeclaringTypeName() + ":" + signature.getName();
        RateLimiter rateLimiter = rateLimiterCache.computeIfAbsent(key, k ->
                RateLimiter.create(rateLimitAnnotation.qps())
        );

        // do rate limit
        boolean pass = rateLimiter.tryAcquire(
                rateLimitAnnotation.acquireTimeout(),
                rateLimitAnnotation.unit()
        );
        if (!pass) {
            throw new RuntimeException("请求被限流");
        }
    }

}
