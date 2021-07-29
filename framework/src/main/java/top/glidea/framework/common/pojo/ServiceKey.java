package top.glidea.framework.common.pojo;

import lombok.Getter;
import lombok.Setter;


/**
 * 目前该类的目的仅在于预留拓展点，比如引入多版本时
 * ServiceKey === interfaceName -> ServiceKey === interfaceName + version
 * 做出上面的变化会容易些
 */
@Getter
@Setter
public class ServiceKey {
    private String interfaceName;

    public ServiceKey(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public String toString() {
        return interfaceName;
    }

    @Override
    public int hashCode() {
        return interfaceName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ServiceKey) {
            ServiceKey objServiceKey = (ServiceKey) obj;
            if (this.interfaceName.equals(objServiceKey.interfaceName)) {
                return true;
            }
        }
        return false;
    }
}
