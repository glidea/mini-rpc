package top.glidea.framework.invoke;

import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;

import java.util.concurrent.CompletableFuture;

public interface Invoker {

    /**
     * 异步调用
     * 不支持重试
     * @param request rpc request msg
     * @return rpc调用结果的future
     */
    CompletableFuture<Object> doInvokeAsync(RpcRequest request);

    /**
     * 同步调用
     * @param request rpc request msg
     * @return rpc调用结果
     * @throws Throwable 执行业务方法抛出异常。或框架自身抛出非业务异常，如调用Consumer的超时异常，Provider的限流异常等
     */
    Object doInvoke(RpcRequest request) throws Throwable;
}
