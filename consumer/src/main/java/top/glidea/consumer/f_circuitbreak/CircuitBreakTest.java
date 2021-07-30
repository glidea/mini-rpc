package top.glidea.consumer.f_circuitbreak;

import org.springframework.stereotype.Component;
import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;

/**
 * 熔断器测试
 *
 * emmm，熔断器我也不知道啥TestCase比较合适，全面。
 * 主要靠日志，打断点吧。熔断器大体上没啥Bug了，功能基本符合预期，但不敢保证bugfree
 */
//@Component
public class CircuitBreakTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> testService.sayHello()).start();
        }
        Thread.sleep(1000);
        for (int i = 0; i < 100; i++) {
            new Thread(() -> testService.sayHello()).start();
        }
        Thread.sleep(3000);
        for (int i = 0; i < 100; i++) {
            new Thread(() -> testService.sayHello()).start();
        }
        Thread.sleep(5000);
        for (int i = 0; i < 100; i++) {
            new Thread(() -> testService.sayHello()).start();
        }
    }
}
