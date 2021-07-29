package top.glidea.framework.remoting.transport.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.remoting.transport.protocol.bodybean.Ping;

@Slf4j
@ChannelHandler.Sharable
public class WriteIdleEventHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state() == IdleState.WRITER_IDLE) {
            log.info("向 [{}] 发了个心跳", ctx.channel().remoteAddress());
            ctx.writeAndFlush(new Ping());
        }
    }
}
