package top.glidea.framework.remoting.transport.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.remoting.transport.protocol.bodybean.Ping;
import top.glidea.framework.remoting.transport.protocol.bodybean.Pong;

/**
 * 心跳请求处理器
 */
@Slf4j
@ChannelHandler.Sharable
public class PingHandler extends SimpleChannelInboundHandler<Ping> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Ping msg) throws Exception {
        log.debug("服务器收到来自 [{}] 的心跳包", ctx.channel().remoteAddress());
        ctx.writeAndFlush(new Pong());
    }
}
