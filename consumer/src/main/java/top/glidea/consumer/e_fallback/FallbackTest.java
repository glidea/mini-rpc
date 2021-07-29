package top.glidea.consumer.e_fallback;

import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;

import java.util.List;

/**
 * Fallback测试
 */
//@Component
public class FallbackTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() {
        testService.sayHello();
        List<String> list = testService.list();
        System.out.println(list);

        for (int i = 0; i < 20; i++) {
            String s = testService.get(1);
            System.out.println(s);
        }
    }
}
