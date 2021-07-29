package top.glidea.framework.remoting.transport.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import top.glidea.framework.remoting.transport.protocol.bodybean.Pong;

/**
 * 心跳响应处理器
 * <p>
 * 目前这个处理器是多余的，但以后可以拿来统计健康信息
 */
@ChannelHandler.Sharable
public class PongHandler extends SimpleChannelInboundHandler<Pong> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Pong msg) throws Exception {
        // There is nothing to do now
    }
}
