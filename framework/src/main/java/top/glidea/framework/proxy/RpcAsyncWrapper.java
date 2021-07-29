package top.glidea.framework.proxy;

import java.util.concurrent.CompletableFuture;

public interface RpcAsyncWrapper {
    /**
     * call method. async wait and deal return value
     * @param methodName Which method of Service do you want to call
     * @param args method args
     * @return a future with return value
     */
    CompletableFuture<Object> call(String methodName, Object... args);
}
