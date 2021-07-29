package top.glidea.provider.service.b_asyncrpc;


import top.glidea.interfaces.TestService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//@RpcService(interfaceClass = TestService.class)
public class TestServiceImpl implements TestService {
    private Map<Integer, String> db = new ConcurrentHashMap<>();

    @Override
    public void sayHello() {
        System.out.println("hello mini rpc!");
    }

    @Override
    public void add(Integer id, String value) {
        db.put(id, value);
    }

    @Override
    public List<String> list() {
        Collection<String> values = db.values();
        return new ArrayList<>(values);
    }

    @Override
    public String get(Integer id) {
        return db.get(id);
    }
}
