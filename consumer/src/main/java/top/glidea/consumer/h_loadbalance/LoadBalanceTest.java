package top.glidea.consumer.h_loadbalance;

import org.springframework.stereotype.Component;
import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;

import java.util.ArrayList;
import java.util.List;

/**
 * 负载均衡测试
 */
//@Component
public class LoadBalanceTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String s = testService.get(1);
            list.add(s);
        }
        for (String s : list) {
            System.out.println(s);
        }
    }
}
