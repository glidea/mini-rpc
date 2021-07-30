package top.glidea.provider.service.k_retry;


import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.annotation.RpcService;
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
    public void sayHello() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Integer id, String value) {
        db.put(id, value);
    }

    @Override
    public List<String> list() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collection<String> values = db.values();
        return new ArrayList<>(values);
    }

    @Override
    public String get(Integer id) {
        return db.get(id);
    }
}
