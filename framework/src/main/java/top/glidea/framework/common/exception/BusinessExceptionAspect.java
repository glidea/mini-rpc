package top.glidea.framework.common.exception;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 包装原始业务异常，避免在非业务异常包装成RpcException之前，与其混淆
 */
@Aspect
@Order(4)
@Component
public class BusinessExceptionAspect {

    @Pointcut("@within(top.glidea.framework.common.annotation.RpcService)")
    public void targetMethodPointcut() {}

    @Around("targetMethodPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 标记这是一个业务异常
            throw new BusinessException(e);
        }
    }
}
