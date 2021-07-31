package top.glidea.framework.invoke;

import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.exception.RpcException;
import top.glidea.framework.common.pojo.Address;
import top.glidea.framework.common.pojo.ServiceInfo;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.common.util.ExceptionUtils;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;
import top.glidea.framework.client.RpcClient;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class FailoverInvoker implements Invoker {
    private RpcClient rpcClient = SingletonFactory.get(RpcClient.class);
    private ServiceFinder serviceFinder = SingletonFactory.get(ServiceFinder.class);

    @Override
    public CompletableFuture<Object> doInvokeAsync(RpcRequest request) {
        ServiceKey serviceKey = new ServiceKey(request.getInterfaceName());
        Address address = serviceFinder.choseAddress(serviceKey, request);
        if (address == null) {
            throw new RpcException("没有Provider能够提供 [" + serviceKey.toString() + "] 服务");
        }
        log.debug("已选择 [{}] 作为服务 [{}] 的 Provider", address.toString(), serviceKey.toString());
        return rpcClient.send(request, address);
    }

    @Override
    public Object doInvoke(RpcRequest request) throws Throwable {
        ServiceKey serviceKey = new ServiceKey(request.getInterfaceName());
        ServiceInfo serviceInfo = serviceFinder.getServiceInfo(serviceKey);
        long timeout = serviceInfo.getMethodInvokeInfo(request.getMethodName()).getTimeout();
        int retries = serviceInfo.getMethodInvokeInfo(request.getMethodName()).getRetries();

        RpcException lastNonBisException = null;
        Set<Address> excludeAddresses = new HashSet<>();
        for (int i = 0; i < retries + 1; i++) {
            try {
                // get future
                Address address = serviceFinder.choseAddress(serviceKey, request, excludeAddresses);
                if (address == null) {
                    break;
                }
                if (i > 0) {
                    log.debug("开始第 {} 次重试。请求 {}#{}", i, serviceKey.toString(), request.getMethodName());
                }
                log.debug("已选择 [{}] 作为服务 [{}] 的 Provider。准备发起请求", address.toString(), serviceKey.toString());
                excludeAddresses.add(address);
                CompletableFuture<Object> requestFuture = rpcClient.send(request, address);
                // wait and get response
                return requestFuture.get(timeout, TimeUnit.MILLISECONDS);

            } catch (InterruptedException | TimeoutException | RuntimeException e) {
                rpcClient.cancel(request.getSequenceId());
                lastNonBisException = ExceptionUtils.ensureIsRpcException(e);
            } catch (ExecutionException e) {
                Throwable providerCause = e.getCause();
                if (providerCause instanceof RpcException) {
                    rpcClient.cancel(request.getSequenceId());
                    lastNonBisException = (RpcException) providerCause;
                } else {
                    // throw business exception
                    throw providerCause;
                }
            }
        }

        if (excludeAddresses.size() == 0) {
            throw new RpcException("没有Provider能够提供 [" + serviceKey.toString() + "] 服务");
        }
        //noinspection ConstantConditions
        throw lastNonBisException;
    }
}
