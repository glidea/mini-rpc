package top.glidea.framework.remoting.transport.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.remoting.transport.transporter.RpcClient;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcResponse;

@ChannelHandler.Sharable
@Slf4j
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private RpcClient rpcClient = SingletonFactory.get(RpcClient.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) {
        log.debug("收到来自 [{}] 响应消息", ctx.channel().remoteAddress());
        rpcClient.receive(msg);
        // 健康信息统计...
    }
}
