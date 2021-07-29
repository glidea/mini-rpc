package top.glidea.consumer.j_threadmodel;

import org.springframework.stereotype.Component;
import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;

/**
 * 线程池模型测试
 */
//@Component
public class ThreadModelTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() throws Exception {
        testService.sayHello();
    }
}
