package top.glidea.framework.proxy;

import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.common.util.ExceptionUtils;
import top.glidea.framework.invoke.FailoverInvoker;
import top.glidea.framework.invoke.Invoker;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Rpc代理对象的InvocationHandler
 */
public class RpcProxyInvocationHandler implements InvocationHandler {
    private Invoker invoker = SingletonFactory.get(FailoverInvoker.class);
    private ServiceKey serviceKey;  // 现在还莫得用
    private Class<?> serviceClass;
    private boolean enableAsync;

    public RpcProxyInvocationHandler(ServiceKey serviceKey, boolean enableAsync) {
        this.serviceKey = serviceKey;
        this.enableAsync = enableAsync;
        try {
            this.serviceClass = Class.forName(serviceKey.getInterfaceName());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("proxy get fail", e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request;
        try {
            if (enableAsync) {
                String methodName = (String) args[0];
                Object[] realArgs = (Object[]) args[1];
                Class<?>[] argTypes = new Class[realArgs.length];
                for (int i = 0; i < argTypes.length; i++) {
                    argTypes[i] = realArgs[i].getClass();
                }
                method = serviceClass.getMethod(methodName, argTypes);
                args = realArgs;
            }

            request = RpcRequest.builder()
                    .interfaceName(serviceClass.getName())
                    .methodName(method.getName())
                    .parameterTypes(method.getParameterTypes())
                    .parameterValues(args)
                    .build();

            if (enableAsync) {
                return invoker.doInvokeAsync(request);
            }
        } catch (Throwable e) {
            throw ExceptionUtils.ensureIsRpcException(e);
        }

        return invoker.doInvoke(request);
    }
}
