package top.glidea.framework.common.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import top.glidea.framework.common.config.Constants;

import java.util.List;
import java.util.Map;

@Setter
@NoArgsConstructor
public class ServiceInfo {
    private List<ProviderInfo> providerInfos;
    private Map<String, MethodInvokeInfo> methodInvokeInfos;
    private MethodInvokeInfo defaultMethodInvokeInfo = new MethodInvokeInfo();

    public ServiceInfo(List<ProviderInfo> providerInfos, Map<String, MethodInvokeInfo> methodInvokeInfos) {
        this.providerInfos = providerInfos;
        this.methodInvokeInfos = methodInvokeInfos;
    }

    public List<ProviderInfo> getProviderInfos() {
        return providerInfos;
    }

    public MethodInvokeInfo getMethodInvokeInfo(String methodName) {
        if (methodInvokeInfos == null || !methodInvokeInfos.containsKey(methodName)) {
            return defaultMethodInvokeInfo;
        }
        return methodInvokeInfos.get(methodName);
    }

    @Setter
    @Getter
    public static class MethodInvokeInfo {
        private long timeout = Constants.CONSUMER_GLOBAL_INVOKE_TIMEOUT;
        private int retries = Constants.CONSUMER_GLOBAL_INVOKE_RETIES;
    }
}
