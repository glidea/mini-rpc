package top.glidea.provider.service.e_fallback;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FallbackHandler {

    private List<String> listFallback() {
        log.debug("FallbackHandler#listFallback");
        List<String> list = new ArrayList<>();
        list.add("备用数据1");
        list.add("备用数据2");
        list.add("备用数据3");
        return list;
    }
}
