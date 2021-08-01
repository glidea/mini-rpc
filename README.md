### 代码模块说明

-----

* consumer、provider、public-interface：framework 的测试代码
* framework
  * common：公共包。提供注解，配置，异常，pojo，SpringContext，各类工厂等...
  * registry：封装服务地址的注册与发现。以`ServiceKey`、`ProviderInfo`为中心
  * cluster：提供负载均衡，以及熔断器、限流、Fallback 功能
  * proxy：生成支持同步异步调用的代理对象，并实现`@RpcAutowired`自动注入同步代理的功能，以及暴露异步代理接口`RpcAsyncWrapper`
  * invoke：桥接注册中心，负载均衡。为`proxy`提供同步异步调用，重试机制，以及业务非业务异常的相应处理
  * client：为`invoke`提供异步收发请求相应的功能。以`RpcReqest`、`CompletableFuture<Object>`、`Object`为中心
  * server：启动`server`，并发布服务。同步或异步处理`remoting`递交的`RpcReqest`，以及异常过滤
  * remoting：提供底层通信所需的协议，序列化，压缩等
  
### 调用流程图
[![Wxtiy8.png](https://z3.ax1x.com/2021/08/01/Wxtiy8.png)](https://imgtu.com/i/Wxtiy8)
----

### 详细技术点

----

### 快速开始

---
### TODO

----
