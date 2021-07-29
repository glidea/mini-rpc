package top.glidea.framework.common.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * socket网络地址
 * ip with port
 * 由于重写了equals方法，并且URL会作为Map的Key Type，所以又重写了hashCode方法
 * 保证了equals相等，hashcode一定相等
 */
@Getter
@Setter
public class Address {
    private String host;
    private int port;

    public Address(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Address(String addressStr) {
        String[] split = addressStr.split(":");
        try {
            this.host = split[0];
            this.port = Integer.parseInt(split[1]);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("addressStr's format should be host:port. eg: 152.1.4.5:8080'");
        }
    }

    /**
     * @return eg: 152.1.4.5:8080
     */
    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Override
    public int hashCode() {
        // 直接拿String host的hashcode，省事
        return host.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Address) {
            Address objAddress = (Address) obj;
            if (this.host.equals(objAddress.host)
                    && this.port == objAddress.port) {
                return true;
            }
        }
        return false;
    }
}

