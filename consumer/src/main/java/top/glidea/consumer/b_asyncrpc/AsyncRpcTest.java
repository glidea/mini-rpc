package top.glidea.consumer.b_asyncrpc;

import top.glidea.consumer.Test;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.proxy.RpcAsyncWrapper;
import top.glidea.framework.proxy.RpcProxyFactory;
import top.glidea.interfaces.TestService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 异步Rpc测试
 */
//@Component
public class AsyncRpcTest implements Test {

    @Override
    public void test() {
        ServiceKey serviceKey = new ServiceKey(TestService.class.getName());
        RpcAsyncWrapper testServiceAsync = RpcProxyFactory.getAsync(serviceKey);
        testServiceAsync.call("sayHello");

        CompletableFuture<Object> addFuture1 = testServiceAsync.call("add", 1, "a");
        CompletableFuture<Object> addFuture2 = testServiceAsync.call("add", 2, "b");
        CompletableFuture<Object> addFuture3 = testServiceAsync.call("add", 3, "c");
        try {
            addFuture1.get();
            addFuture2.get();
            addFuture3.get();
            CompletableFuture<Object> listFuture = testServiceAsync.call("list");
            List<String> list = (List<String>) listFuture.get();
            System.out.println(list);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            // 业务异常
            Throwable cause = e.getCause();
        }
    }
}
