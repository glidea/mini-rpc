package top.glidea.framework.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.springcontext.SpringContext;
import top.glidea.framework.common.annotation.RpcService;
import top.glidea.framework.common.exception.RpcException;
import top.glidea.framework.common.pojo.Address;
import top.glidea.framework.common.util.ReflectUtils;
import top.glidea.framework.common.config.Config;
import top.glidea.framework.common.config.ConfigOption;
import top.glidea.framework.common.config.YmlConfig;
import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.common.factory.ExpansionFactory;
import top.glidea.framework.registry.ServiceRegistry;
import top.glidea.framework.remoting.transport.protocol.codec.MessageCodec;
import top.glidea.framework.remoting.transport.protocol.codec.ProtocolFrameDecoder;
import top.glidea.framework.remoting.transport.handler.OrphanExceptionHandler;
import top.glidea.framework.remoting.transport.handler.PingHandler;
import top.glidea.framework.remoting.transport.handler.ReadIdleEventHandler;
import top.glidea.framework.server.handler.RpcRequestHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RpcServer {
    private final Config config = SingletonFactory.get(YmlConfig.class);

    private int port = config.getNotNull(ConfigOption.PROVIDER_PUBLISH_PORT, Integer.class);
    private ServiceRegistry registry = ExpansionFactory.get(ServiceRegistry.class);
    private NioEventLoopGroup boss = new NioEventLoopGroup(config.getNotNull(ConfigOption.PROVIDER_THREAD_NUM_BOSS, Integer.class));
    private NioEventLoopGroup worker = new NioEventLoopGroup(config.getNotNull(ConfigOption.PROVIDER_THREAD_NUM_WORKER, Integer.class));
    private ThreadPoolExecutor asyncInvokeServiceThreadPool;

    public RpcServer() {
        boolean enableAsync = config.getNotNull(ConfigOption.PROVIDER_ENABLE_ASYNC, Boolean.class);
        int nthreads = config.getNotNull(ConfigOption.PROVIDER_THREAD_NUM_SERVICE, Integer.class);
        if (!enableAsync || nthreads <= 0) {
            asyncInvokeServiceThreadPool = null;
            return;
        }
        asyncInvokeServiceThreadPool = new ThreadPoolExecutor(nthreads, nthreads,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "asyncInvoke-servicePool-thread-" + count.getAndIncrement());
                    }
                }
        );
    }

    public void start() {
        try {
            registerShutdownHook();
            start0();
        } catch (Exception e) {
            throw new RpcException("RpcServer启动失败！", e);
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // 注销服务，进而通知consumer下线情况
            registry.unRegisterAll();
            // 安全地shutdown netty和业务线程。
            // 避免正在处理的请求突然中断掉，同时shutdown netty之中，服务器主动发起挥手，保证连接及时断开
            shutdown();
        }));
    }

    private void start0() throws InterruptedException, UnknownHostException {
        RpcRequestHandler rpcRequestHandler = new RpcRequestHandler(asyncInvokeServiceThreadPool);
        LoggingHandler loggingHandler = new LoggingHandler();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.channel(NioServerSocketChannel.class);
            sb.group(boss, worker);
            sb.childOption(ChannelOption.TCP_NODELAY, true);
            sb.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline()
                            .addLast(new ProtocolFrameDecoder())
                            .addLast(loggingHandler)
                            .addLast(SingletonFactory.get(MessageCodec.class))
                            .addLast(new IdleStateHandler(3 * 60, 0, 0))
                            .addLast(SingletonFactory.get(ReadIdleEventHandler.class))
                            .addLast(SingletonFactory.get(PingHandler.class))
                            .addLast(rpcRequestHandler)
                            .addLast(SingletonFactory.get(OrphanExceptionHandler.class))
                    ;
                }
            });
            Channel channel = sb.bind(port).sync().channel();
            publishServices();  // 尽可能延迟发布服务

            log.info("RpcServer 启动成功。端口：" + port);
            channel.closeFuture().sync();
        } finally {
            shutdown();
        }
    }

    private void publishServices() throws UnknownHostException {
        // build providerInfo
        String host = InetAddress.getLocalHost().getHostAddress();
        Address localAddress = new Address(host, port);
        int weight = config.getNotNull(ConfigOption.PROVIDER_LOAD_BALANCE_WEIGHT, Integer.class);
        ProviderInfo providerInfo = new ProviderInfo(localAddress, weight);

        // register every service with providerInfo
        Collection<Object> rpcServices = SpringContext.getBeansWithAnnotation(RpcService.class);
        for (Object rpcService : rpcServices) {
            Object target = ReflectUtils.getTarget(rpcService);
            if (target != null) {
                // 直接从aop proxy里拿不出注解，只能推测Proxy不继承注解，并且SpringContext.getBeansWithAnnotation可以匹配Target Class
                RpcService annotation = target.getClass().getAnnotation(RpcService.class);
                Class<?> interfaceClass = annotation.interfaceClass();
                ServiceKey serviceKey = new ServiceKey(interfaceClass.getName());
                registry.register(serviceKey, providerInfo);
            }
        }
        log.info("Rpc服务发布完毕");
    }

    private void shutdown() {
        // 先关boss，尽量在更前端拒绝新请求
        Future<?> bossClose = boss.shutdownGracefully();
        if (asyncInvokeServiceThreadPool != null) {
            // 再关业务线程池，关闭过程中默认拒绝新任务
            asyncInvokeServiceThreadPool.shutdown();
        }
        // 最后关worker，确保业务线程池处理完后，能将结果发送出去
        try {
            bossClose.await(5, TimeUnit.SECONDS);
            Future<?> workerClose = worker.shutdownGracefully();
            workerClose.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
