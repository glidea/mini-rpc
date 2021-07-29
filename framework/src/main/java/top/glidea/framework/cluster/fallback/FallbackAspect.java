package top.glidea.framework.cluster.fallback;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.annotation.Fallback;

import java.lang.reflect.Method;

@Aspect
@Order(1)
@Component
@Slf4j
public class FallbackAspect {

    @Pointcut("@annotation(top.glidea.framework.common.annotation.Fallback)")
    public void targetMethodPointcut() {}

    @Around("targetMethodPointcut() && @annotation(fallbackAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Fallback fallbackAnnotation) throws Throwable {
        try {
            return joinPoint.proceed();

        } catch (Throwable joinPointException) {
            // get fallbackMethod
            Class<?> handlerClass = fallbackAnnotation.handlerClass();
            Class<?> targetClass = joinPoint.getTarget().getClass();
            if (handlerClass.equals(Fallback.class)) {
                handlerClass = targetClass;
            }
            String methodName = fallbackAnnotation.methodName();
            try {
                Method fallbackMethod = getFallbackMethod(handlerClass, methodName, joinPoint);

                // invoke fallbackMethod
                Object[] originArgs = joinPoint.getArgs();
                int fallbackArgCount = fallbackMethod.getParameterCount();
                Object[] fallbackArgs = new Object[fallbackArgCount];
                if (fallbackArgCount == 0) {
                    // noting to do when fallbackMethod no args
                    // 仅增加可读性
                } else if (fallbackArgCount == originArgs.length + 1) {
                    System.arraycopy(originArgs, 0, fallbackArgs, 0, fallbackArgCount - 1);
                    fallbackArgs[fallbackArgCount - 1] = joinPointException;
                } else {
                    throw new RuntimeException("fallback方法参数不合法");
                }
                Object handlerObj = handlerClass.equals(targetClass)
                        ? joinPoint.getTarget()
                        : SingletonFactory.get(handlerClass);
                return fallbackMethod.invoke(handlerObj, fallbackArgs);

            } catch (Exception e) {
                log.error("方法" + joinPoint.getSignature().getName() + "的fallback调用失败", e);
                throw joinPointException;
            }
        }
    }

    private Method getFallbackMethod(Class<?> handlerClass, String methodName, ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        // get method args
        MethodSignature signature =  (MethodSignature) joinPoint.getSignature();
        Class[] argTypes = signature.getParameterTypes();
        Class[] fallbackArgTypes = new Class[argTypes.length + 1];
        System.arraycopy(argTypes, 0, fallbackArgTypes, 0, fallbackArgTypes.length - 1);
        fallbackArgTypes[fallbackArgTypes.length - 1] = Throwable.class;

        // get method
        Method method = null;
        try {
            method = handlerClass.getDeclaredMethod(methodName, fallbackArgTypes);
        } catch (NoSuchMethodException e) {
            method = handlerClass.getDeclaredMethod(methodName);
        }
        method.setAccessible(true);

        // check fallbackMethod returnType
        if (!method.getReturnType().equals(signature.getReturnType())) {
            throw new RuntimeException("fallbackMethod returnType must eq origin method");
        }
        return method;
    }
}
