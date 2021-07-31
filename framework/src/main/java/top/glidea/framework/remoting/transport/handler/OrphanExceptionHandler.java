package top.glidea.framework.remoting.transport.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.exception.RpcException;
import top.glidea.framework.common.util.ExceptionUtils;
import top.glidea.framework.common.exception.ExceptionFilter;

/**
 * 孤儿异常处理器
 * <p>
 * 孤儿异常指的是，Netty IO线程中无法找到对应请求的SequenceId的异常。
 * 例如作为Consumer接收消息时的序列化，压缩异常等。
 */
@Slf4j
@ChannelHandler.Sharable
public class OrphanExceptionHandler extends ChannelInboundHandlerAdapter {
    private ExceptionFilter exceptionFilter = SingletonFactory.get(ExceptionFilter.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        RpcException rpcException = ExceptionUtils.ensureIsRpcException(cause);
        log.error("孤儿异常：", rpcException);
    }
}
