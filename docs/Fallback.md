这里的 Fallback 特指被动降级
* 如何使用？

  ```java
  @Fallback(handlerClass = FallbackHandler.class,
            methodName = "sayHelloFallback")
  public void sayHello() {
  	//...
  }
  
  private void sayHelloFallback(Throwable e) {
  	//...
  }
  ```

  >`@Fallback#handlerClass`可缺省，默认为方法所在类

* 何时触发 fallback ？

  > 原方法或其内嵌AOP，抛出任何异常都会被捕捉，并触发 fallback
  >
  > 若无 fallback 配置，或执行失败，原异常会被抛出
  >
  > 可结合熔断，限流使用

* fallback 方法规范

  * 返回值：需与原方法保持一致

  * 方法参数

    >1：()。无参
    >
    >2：(被代理的方法原参数..., Throwable e)。e为被代理方法抛出的原异常
    >
    >没有(Throwable e)是因为，没办法优雅地判断e本身是不是被代理方法参数
