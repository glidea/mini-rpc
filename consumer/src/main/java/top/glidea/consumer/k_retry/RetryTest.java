package top.glidea.consumer.k_retry;

import org.springframework.stereotype.Component;
import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.framework.common.exception.RpcException;
import top.glidea.interfaces.TestService;

/**
 * 线程池模型测试
 */
//@Component
public class RetryTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() {
        // 一定会触发超时异常，看其如何重试（log）
        try {
            testService.sayHello();
        } catch (RpcException e) {
            e.printStackTrace();
        }
        testService.list();
    }
}
