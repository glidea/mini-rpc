package top.glidea.provider.service.e_fallback;


import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.annotation.Fallback;
import top.glidea.framework.common.annotation.RateLimit;
import top.glidea.interfaces.TestService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
//@RpcService(interfaceClass = TestService.class)
public class TestServiceImpl implements TestService {
    private Map<Integer, String> db = new ConcurrentHashMap<>();

    @Override
    @Fallback(methodName = "sayHelloFallback")
    public void sayHello() {
        if (1 == 1) {
            throw new RuntimeException();
        }
        System.out.println("hello mini rpc!");
    }

    @Override
    public void add(Integer id, String value) {
        db.put(id, value);
    }

    @Override
//    @Fallback(methodName = "listFallback")
    @Fallback(handlerClass = FallbackHandler.class, methodName = "listFallback")
    public List<String> list() {
        if (1 == 1) {
            throw new RuntimeException();
        }
        Collection<String> values = db.values();
        return new ArrayList<>(values);
    }

    @Override
    @Fallback(methodName = "getFallback")
    @RateLimit(qps = 1, acquireTimeout = 0) // 配合限流
    public String get(Integer id) {
        return db.get(id);
    }

    /**
     * fallback return type and method args format, @see: top.glidea.framework.common.annotation.Fallback
     */
    private void sayHelloFallback(Throwable e) {
        log.warn("fallback enable", e);
        System.out.println("hello fallback");
    }

    private List<String> listFallback() {
        log.debug("TestServiceImpl#listFallback");
        List<String> list = new ArrayList<>();
        list.add("备用数据1");
        list.add("备用数据2");
        list.add("备用数据3");
        return list;
    }

    private String getFallback(Integer id, Throwable e) {
        log.warn("fallback enable", e);
        return "fallbackString";
    }
}
