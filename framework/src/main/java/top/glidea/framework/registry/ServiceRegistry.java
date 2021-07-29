package top.glidea.framework.registry;

import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.common.pojo.ServiceKey;

public interface ServiceRegistry {
    String NAME = "service-registry";

    /**
     * 服务注册
     *
     * @param serviceKey   你想注册啥服务
     * @param providerInfo 能提供这服务的Provider的信息
     */
    void register(ServiceKey serviceKey, ProviderInfo providerInfo);

    /**
     * 注销所有自己注册过的服务
     * <p>
     * 当Provider应用停止后，注册中心一般能自动注销。
     * 但不会很及时，因为心跳要容忍一段timeout来确认Provider的存活。
     * 所以为了及时注销，进而提高Consumer的调用成功率，正常停止时手动注销也是有必要的。
     */
    void unRegisterAll();
}
