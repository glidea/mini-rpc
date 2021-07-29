package top.glidea.consumer.g_expansion;

import org.springframework.stereotype.Component;
import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;

/**
 * 自定义拓展组件测试
 */
//@Component
public class ExpansionTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() throws Exception {
        testService.sayHello();
    }
}
