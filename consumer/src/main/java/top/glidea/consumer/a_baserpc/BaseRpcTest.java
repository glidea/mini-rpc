package top.glidea.consumer.a_baserpc;

import org.springframework.util.Assert;
import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;

import java.util.List;

/**
 * 基本的Rpc通信测试
 *
 * 测试通过，说明Spring注解、后置处理器、动态代理、注册中心、负载均衡、Netty、编解码、序列化等无明显Bug
 */
//@Component
public class BaseRpcTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() {
        testHello();
        testCurd();
    }

    public void testHello() {
        testService.sayHello();
    }

    public void testCurd() {
        testService.add(1, "a");
        testService.add(2, "b");
        testService.add(3, "c");

        String value1 = testService.get(1);
        Assert.notNull(value1);
        String value4 = testService.get(4);
        Assert.isNull(value4);
        List<String> list = testService.list();
        Assert.notEmpty(list);
        System.out.println(list);
    }
}
