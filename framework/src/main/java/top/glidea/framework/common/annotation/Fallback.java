package top.glidea.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fallback {

    /**
     * fallback处理器类，处理方法所在类
     * default Fallback.class仅做标识，在APO切面处会被替换成@Fallback修饰的所在类
     */
    Class<?> handlerClass() default Fallback.class;

    /**
     * 处理方法名。
     *
     * 处理方法形参有且仅有两种合法情况
     * 1：()。无参
     * 2：(被代理的方法原参数..., Throwable e)。e为被代理方法抛出的原异常
     * 没有(Throwable e)是因为，没办法优雅地判断e本身是不是被代理方法参数
     *
     * 返回值必须与被代理方法相同
     */
    String methodName();
}
