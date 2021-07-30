package top.glidea.framework.remoting.transport.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.common.exception.ExceptionFilter;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcResponse;
import top.glidea.framework.common.factory.RpcServiceFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final ExceptionFilter exceptionFilter;
    private final ThreadPoolExecutor invokeServiceThreadPool;
    private final boolean enableAsync;

    public RpcRequestHandler(ThreadPoolExecutor invokeServiceThreadPool) {
        this.exceptionFilter = SingletonFactory.get(ExceptionFilter.class);
        this.invokeServiceThreadPool = invokeServiceThreadPool;
        this.enableAsync = invokeServiceThreadPool != null;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        if (enableAsync) {
            invokeServiceThreadPool.execute(() -> {
                doBusiness(ctx, msg);
            });
        } else {
            doBusiness(ctx, msg);
        }
    }

    private void doBusiness(ChannelHandlerContext ctx, RpcRequest msg) {
        log.debug("开始为 [{}] 处理业务请求", ctx.channel().remoteAddress());
        RpcResponse response = RpcResponse.builder().sequenceId(msg.getSequenceId()).build();
        Method method = null;
        try {
            ServiceKey serviceKey = new ServiceKey(msg.getInterfaceName());
            Object serviceBean = RpcServiceFactory.get(serviceKey);
            Class<?> instanceClass = serviceBean.getClass();
            method = instanceClass.getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object invoke = method.invoke(serviceBean, msg.getParameterValues());
            response.setReturnValue(invoke);

        } catch (Throwable e) {
            Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
            log.warn("[{}] 的业务请求，抛出异常", ctx.channel().remoteAddress(), cause);
            Throwable ex = exceptionFilter.doFilter(method, cause);
            response.setExceptionValue(ex);
        }

        ctx.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("[{}] 的调用结果发送成功", ctx.channel().remoteAddress());
            } else {
                log.warn("[{}] 的调用结果发送失败", ctx.channel().remoteAddress(), future.cause());
            }
        });
    }
}
