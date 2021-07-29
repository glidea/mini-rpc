package top.glidea.consumer.i_exception;

import top.glidea.consumer.Test;
import top.glidea.framework.common.annotation.RpcAutowired;
import top.glidea.interfaces.TestService;

/**
 * 自定义异常测试
 *
 * #detail: Exception Test Config
 * and ExceptionFilter
 */
//@Component
public class ExceptionTest implements Test {
    @RpcAutowired
    private TestService testService;

    @Override
    public void test() throws Exception {
        testService.sayHello();
    }
}
