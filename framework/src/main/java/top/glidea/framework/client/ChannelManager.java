package top.glidea.framework.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.exception.RpcException;
import top.glidea.framework.common.pojo.Address;
import top.glidea.framework.remoting.transport.handler.OrphanExceptionHandler;
import top.glidea.framework.client.handler.RpcResponseHandler;
import top.glidea.framework.remoting.transport.protocol.codec.MessageCodec;
import top.glidea.framework.remoting.transport.protocol.codec.ProtocolFrameDecoder;
import top.glidea.framework.remoting.transport.handler.WriteIdleEventHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ChannelManager {
    private Map<Address, Channel> addressChannelMap = new ConcurrentHashMap<>();
    private NioEventLoopGroup group = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();

    public ChannelManager() {
        LoggingHandler loggingHandler = new LoggingHandler();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline()
                        .addLast(new ProtocolFrameDecoder())
                        .addLast(loggingHandler)
                        .addLast(SingletonFactory.get(MessageCodec.class))
                        .addLast(new IdleStateHandler(0, 60, 0))
                        .addLast(SingletonFactory.get(WriteIdleEventHandler.class))
                        .addLast(SingletonFactory.get(RpcResponseHandler.class))
                        .addLast(SingletonFactory.get(OrphanExceptionHandler.class))
                ;
            }
        });
        registerShutdownHook();
    }

    public Channel get(Address address) {
        return addressChannelMap.computeIfAbsent(address, k -> connect(address));
    }

    private Channel connect(Address address) {
        try {
            Channel channel = bootstrap.connect(address.getHost(), address.getPort()).sync().channel();
            channel.closeFuture().addListener(future -> {
                log.debug("和 [{}] 的连接以断开", channel.remoteAddress());
                addressChannelMap.remove(address);
            });
            return channel;
        } catch (Exception e) {
            throw new RpcException(address.toString() + "连接失败", e);
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Future<?> future = group.shutdownGracefully(0, 10, TimeUnit.SECONDS);
            try {
                future.await();
            } catch (InterruptedException e) {
                log.warn("非正常shutdown", e);
            }
        }));
    }
}
