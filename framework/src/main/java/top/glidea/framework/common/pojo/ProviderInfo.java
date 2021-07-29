package top.glidea.framework.common.pojo;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ProviderInfo {
    private Address address;
    private int weight;

    public ProviderInfo(String infoStr) {
        String[] split = infoStr.split("-");
        try {
            this.address = new Address(split[0]);
            int weight = Integer.parseInt(split[1]);
            this.weight = Math.max(weight, 0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public ProviderInfo(Address address, int weight) {
        this.address = address;
        this.weight = weight;
    }

    public static List<ProviderInfo> castAll(List<String> infoStrs) {
        List<ProviderInfo> infos = new ArrayList<>();
        for (String infoStr : infoStrs) {
            infos.add(new ProviderInfo(infoStr));
        }
        return infos;
    }

    @Override
    public String toString() {
        return address.toString() + "-" + weight;
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ProviderInfo) {
            ProviderInfo objProviderInfo = (ProviderInfo) obj;
            if (this.address.equals(objProviderInfo.address)
                    && this.weight == objProviderInfo.weight) {
                return true;
            }
        }
        return false;
    }
}
