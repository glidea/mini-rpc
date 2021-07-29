package top.glidea.provider;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import top.glidea.framework.remoting.transport.transporter.RpcServer;

/**
 * 如果你想测试某个功能，找到对应的包的TestServiceImpl类，
 * 为该类打上@RpcService(interfaceClass = TestService.class)，并确保其它功能的TestServiceImpl类没有加入Ioc
 */
@Configuration
@ComponentScan({"top.glidea.provider", "top.glidea.framework"})
@EnableAspectJAutoProxy
public class RpcProviderTestMainApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RpcProviderTestMainApp.class);
        RpcServer rpcServer = new RpcServer();
        rpcServer.start();
    }
}
