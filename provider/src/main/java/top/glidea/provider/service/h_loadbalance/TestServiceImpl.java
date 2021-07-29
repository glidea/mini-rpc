package top.glidea.provider.service.h_loadbalance;


import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.annotation.RpcService;
import top.glidea.framework.common.config.Config;
import top.glidea.framework.common.config.ConfigOption;
import top.glidea.framework.common.config.YmlConfig;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.interfaces.CustomException;
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
        System.out.println("hello");
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
        Config config = SingletonFactory.get(YmlConfig.class);
        int port = config.getNotNull(ConfigOption.PROVIDER_PUBLISH_PORT, Integer.class);
        int weight = config.getNotNull(ConfigOption.PROVIDER_LOAD_BALANCE_WEIGHT, Integer.class);
        return String.format("请求到了这，我的端口是：%s，权重是：%s", port, weight);
    }
}
