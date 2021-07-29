package top.glidea.consumer.d_ratelimit;

import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;


/**
 * 限流测试
 */
//@Component
public class RateLimitTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() {
        for (int i = 0; i < 20; i++) {
            // Provider设置：@RateLimit(qps = 1, acquireTimeout = 0)
            new Thread(() -> testService.sayHello()).start();
        }
    }
}
