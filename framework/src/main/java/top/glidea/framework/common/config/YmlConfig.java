package top.glidea.framework.common.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YmlConfig implements Config {
    private final Map<String, Object> CONFIG;

    public YmlConfig() {
        InputStream in = YmlConfig.class.getClassLoader()
                .getResourceAsStream("rpc-config.yml");
        if (in == null) {
            throw new RuntimeException("yml配置文件找不到");
        }
        CONFIG = new Yaml().load(in);
    }

    @Override
    public <T> T get(String option, Class<T> valueClass) {
        Object value = null;
        try {
            String[] subOptions = option.split("\\.");
            Map map = CONFIG;
            for (int i = 0; i < subOptions.length - 1; i++) {
                map = (Map) map.get(subOptions[i]);
            }
            value = map.get(subOptions[subOptions.length - 1]);
        } catch (Exception ignored) {
            // This exception means that the user did not set the option, and the default value is followed
            // so, ignore exception, even no log
        }

        if (value == null) {
            value = DefaultConfig.get(option);
        }
        return value == null ? null : valueClass.cast(value);
    }

    @Override
    public String get(String option) {
        return get(option, String.class);
    }

    @Override
    public <T> T getNotNull(String option, Class<T> valueClass) {
        T value = get(option, valueClass);
        if (value == null) {
            throw new RuntimeException("[" + option + "]未配置，并且不存在默认值");
        }
        return value;
    }

    @Override
    public String getNotNull(String option) {
        return getNotNull(option, String.class);
    }

    @Override
    public <T> T getFromMapList(String optionOfList, String targetProp, Class<T> valueClass, Map<String, Object> matchKvs) {
        List opMapList = get(optionOfList, List.class);
        if (opMapList == null) {
            return null;
        }
        Object opMapObj = opMapList.get(0);
        if (opMapObj == null || !Map.class.isAssignableFrom(opMapObj.getClass())) {
            throw new RuntimeException();
        }

        Map matchMap = null;
        for (Object e : opMapList) {
            Map opMap = (Map) e;
            boolean matched = true;
            for (String matchK : matchKvs.keySet()) {
                Object v1 = opMap.get(matchK);
                Object v2 = matchKvs.get(matchK);
                if (!v1.equals(v2)) {
                    matched = false;
                }
            }
            if (matched) {
                matchMap = opMap;
                break;
            }
        }

        if (matchMap == null) {
            return null;
        }
        Object value = matchMap.get(targetProp);
        return value == null ? null : valueClass.cast(value);
    }
}
