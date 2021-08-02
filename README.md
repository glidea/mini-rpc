### 代码模块说明

* consumer、provider、public-interface：framework 的测试代码

* framework

  * **common**：公共包。提供注解，配置，异常，pojo，SpringContext，各类工厂等...
  * **registry**：封装服务地址的注册与发现。以`ServiceKey`、`ProviderInfo`为中心
  * **cluster**：提供负载均衡，以及熔断器、限流、Fallback 功能
  * **proxy**：生成支持同步异步调用的代理对象，并实现`@RpcAutowired`自动注入同步代理的功能，以及暴露异步代理接口`RpcAsyncWrapper`
  * **invoke**：桥接注册中心，负载均衡。为`proxy`提供同步异步调用，重试机制，以及业务非业务异常的相应处理
  * **client**：为`invoke`提供异步收发请求相应的功能。以`RpcReqest`、`CompletableFuture<Object>`、`Object`为中心
  * **server**：启动`server`，并发布服务。同步或异步处理`remoting`递交的`RpcReqest`，以及异常过滤
  * **remoting**：提供底层通信所需的协议，序列化，压缩等

---

### 调用流程图

[![Wxtiy8.png](https://z3.ax1x.com/2021/08/01/Wxtiy8.png)](https://imgtu.com/i/Wxtiy8)

---

### 项目技术点细则（待施工）

* [负载均衡](/docs/负载均衡.md)
* 异步调用
* [线程模型](/docs/线程模型.md)
* 动态代理
* [注册中心](/docs/注册中心.md)
* [异常处理](/docs/异常处理.md)
* 重试
* 熔断
* 限流
* Fallback
* [协议](/docs/协议.md)
* [序列化](/docs/序列化.md)
* 压缩
* [拓展](/docs/拓展.md)
* 其它

----

### TODO

- [ ] 健康监测，路由
- [ ] 启动，关闭流程优化
- [ ] 添加并发限流
- [ ] 代码重构

---

### 快速开始


* public-interface

  ```java
  public interface HelloService {
      String hi(String name);
  }
  ```

* Provider

  * HelloServiceImpl

    ```java
    @RpcService(interfaceClass = HelloService.class)
    public class HelloServiceImpl implements HelloService {
    
        @Override
        public String hi(String name) {
            return "hi!" + name;
        }
    }
    ```

  * MainApp

    ```java
    @Configuration
    @ComponentScan({"HelloServiceImpl所在包", "top.glidea.framework"})
    @EnableAspectJAutoProxy
    public class RpcProviderTestMainApp {
        public static void main(String[] args) {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RpcProviderTestMainApp.class);
            RpcServer rpcServer = new RpcServer();
            rpcServer.start();
        }
    }
    ```

  * rpc-config.yml

    ```yaml
    # 默认端口为8181，更多配置选项及缺省值详见：common.config
    registry:
      address: xxxxxxx
    ```

* Consumer

  * bean

    ```java
    @Component
    public class Bean {
        @RpcAutowired
        private HelloService helloService;
    
        public void testSync() {
            String echo = helloService.hi("myname");
        }
      
        public void testASync() {
            RpcAsyncWrapper helloServiceAsync = RpcProxyFactory.getAsync(new ServiceKey(HelloService.class.getName()));
            CompletableFuture<Object> future = helloServiceAsync.call("hi", "myname");
          	// futue get or register callback. or other thing that CompletableFuture can do
        }
    }
    ```

  * MainApp

    ```java
    @Configuration
    @ComponentScan({"Bean所在的包", "top.glidea.framework"})
    @EnableAspectJAutoProxy
    public class RpcConsumerTestMainApp {
    
        public static void main(String[] args) throws Exception {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RpcConsumerTestMainApp.class);
            Bean bean = context.getBean(Bean.class);
            bean.testSync();
            bean.testASync();
        }
    }
    ```

  * rpc-config.yml

    ```yaml
    registry:
      address: xxxxxxx
    ```