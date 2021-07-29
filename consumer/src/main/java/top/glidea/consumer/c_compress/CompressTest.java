package top.glidea.consumer.c_compress;

import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;

import java.util.List;


/**
 * 协议包压缩，及压缩阈值测试
 */
//@Component
public class CompressTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() {
        testService.add(11, "hello");
        List<String> list = testService.list();
        System.out.println(list);
    }
}
