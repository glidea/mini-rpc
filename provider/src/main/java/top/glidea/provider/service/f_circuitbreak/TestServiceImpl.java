package top.glidea.provider.service.f_circuitbreak;


import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.annotation.CircuitBreak;
import top.glidea.framework.common.annotation.RpcService;
import top.glidea.interfaces.TestService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
//@RpcService(interfaceClass = TestService.class)
public class TestServiceImpl implements TestService {
    private Map<Integer, String> db = new ConcurrentHashMap<>();

    @CircuitBreak(rule = CircuitBreak.RuleConstant.COMB_RATIO,
            slowThreshold = 1000, ratioThreshold = 0.7,
            statIntervalMs = 2000, minRequestAmount = 50, breakTime = 5)
    @Override
    public void sayHello() {
        try {
            if (ThreadLocalRandom.current().nextInt(100) > 20) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
