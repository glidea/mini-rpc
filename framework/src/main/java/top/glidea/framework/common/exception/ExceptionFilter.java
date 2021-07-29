package top.glidea.framework.common.exception;

import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.exception.BusinessException;
import top.glidea.framework.common.exception.RpcException;
import top.glidea.framework.common.util.ExceptionUtil;
import top.glidea.framework.common.config.Config;
import top.glidea.framework.common.config.ConfigOption;
import top.glidea.framework.common.config.YmlConfig;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Provider准备往Consumer发异常的时候经过此过滤
 * 1、清理堆栈信息
 * 2、把非业务异常包装成RpcException
 * 3、去除BusinessException的包装，获取原始业务异常
 * 4、在100%确定Consumer也引用了的异常类型（检查、方法签名、jdk、框架异常，以及配置的自定义RuntimeException），直接抛出该业务异常
 * 5、否则，toString后包装成RuntimeException，保证Consumer反序列化不会失败
 * <p>
 * 过滤策略参考dubbo
 */
public class ExceptionFilter {
    private Config config = SingletonFactory.get(YmlConfig.class);

    public Throwable doFilter(Method causeMethod, Throwable e) {
        // 是否清理堆栈信息
        boolean ignored = config.getNotNull(ConfigOption.PROVIDER_EXCEPTION_IGNORE_STACK_TRACE, Boolean.class);
        if (ignored) {
            ExceptionUtil.clearStackTraceRecursive(e);
        }

        // 非业务异常，抛出
        if (!(e.getClass().equals(BusinessException.class))) {
            return ExceptionUtil.ensureIsRpcException(e);
        }

        // 原始业务异常
        Throwable oe = e.getCause();

        // 检查异常，直接抛出
        if (!(oe instanceof RuntimeException)) {
            return oe;
        }

        // 方法签名上有说明抛出非检查异常，直接抛出
        Class<?>[] exceptionClassses = causeMethod.getExceptionTypes();
        for (Class<?> exceptionClass : exceptionClassses) {
            if (exceptionClass.equals(oe.getClass())) {
                return oe;
            }
        }

        // JDK异常，直接抛出
        String className = oe.getClass().getName();
        if (className.startsWith("java.") || className.startsWith("javax.")) {
            return oe;
        }

        // 配置的自定义异常，直接抛出
        List customExceptions = config.get(ConfigOption.PROVIDER_CUSTOM_EXCEPTIONS, List.class);
        try {
            if (customExceptions != null) {
                for (Object customException : customExceptions) {
                    Class<?> customExceptionClass = Class.forName((String) customException);
                    if (customExceptionClass.equals(oe.getClass())) {
                        return oe;
                    }
                }
            }
        } catch (ClassNotFoundException classNotFoundException) {
            throw new RpcException(ConfigOption.PROVIDER_CUSTOM_EXCEPTIONS + "配置出错", classNotFoundException);
        }

        // 否则，包装成RuntimeException抛给客户端
        return new RuntimeException(ExceptionUtil.toString(oe));
    }
}
