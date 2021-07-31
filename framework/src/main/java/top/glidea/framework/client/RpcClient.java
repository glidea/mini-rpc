package top.glidea.framework.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.pojo.Address;
import top.glidea.framework.common.util.ExceptionUtils;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcResponse;
import top.glidea.framework.common.util.SequenceIdGenerator;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClient {
    private ChannelManager channelManager = SingletonFactory.get(ChannelManager.class);

    private final Map<Integer, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<Object> send(RpcRequest request, Address address) {
        Channel channel = channelManager.get(address);
        request.setSequenceId(SequenceIdGenerator.nextId());
        CompletableFuture<Object> requestFuture = new CompletableFuture<>();
        pendingRequests.put(request.getSequenceId(), requestFuture);

        channel.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("请求已发往 [{}]", address.toString());
            } else {
                // fail fast
                cancel(request.getSequenceId());
                requestFuture.completeExceptionally(ExceptionUtils.ensureIsRpcException(future.cause()));
            }
        });
        return requestFuture;
    }

    public void receive(RpcResponse response) {
        CompletableFuture<Object> requestFuture = pendingRequests.remove(response.getSequenceId());
        if (requestFuture == null) {
            // 请求已超时，或SequenceId伪造，出差错
            return;
        }

        Throwable e = response.getExceptionValue();
        Object value = response.getReturnValue();
        if (e != null) {
            requestFuture.completeExceptionally(e);
        } else {
            requestFuture.complete(value);
        }
    }

    public void cancel(int sequenceId) {
        pendingRequests.remove(sequenceId);
    }
}
