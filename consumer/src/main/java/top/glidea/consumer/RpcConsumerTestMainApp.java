package top.glidea.consumer;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 如果你想测试某个功能，找到对应的Test类，为该类打上@Component，并确保其它功能的Test类没有加入Ioc
 */
@Configuration
@ComponentScan({"top.glidea.consumer", "top.glidea.framework"})
@EnableAspectJAutoProxy
public class RpcConsumerTestMainApp {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RpcConsumerTestMainApp.class);
        Test bean = context.getBean(Test.class);
        bean.test();
    }
}
