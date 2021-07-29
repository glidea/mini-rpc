package top.glidea.framework.cluster.loadbalance;

import top.glidea.framework.common.pojo.ProviderInfo;
import top.glidea.framework.common.pojo.ServiceKey;
import top.glidea.framework.remoting.transport.protocol.bodybean.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性哈希负载均衡
 * <p>
 * 参考Dubbo实现
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private final Map<ServiceKey, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected ProviderInfo doSelect(List<ProviderInfo> providerInfos, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(providerInfos);
        ServiceKey key = rpcRequest.getServiceKey();
        ConsistentHashSelector selector = selectors.get(key);

        // selector为空，或Providers中有上线下线的情况
        if (selector == null || selector.identityHashCode != identityHashCode) {
            /*
                这里可能会重复new对象，但不会有线程安全问题
                dubbo没加锁，个人认为原因应该是：
                在并发非常高，并且new耗时不长的情况下，
                并发地new的耗时小于串行加解重量级锁（这里用不了cas，因为后面一定并且马上要用到新new的Selector）
             */
            selectors.put(key, new ConsistentHashSelector(providerInfos, identityHashCode));
            selector = selectors.get(key);
        }
        return selector.select(rpcRequest);
    }

    private static final class ConsistentHashSelector {
        private static final int VIRTUAL_REPLICA_NUMBER = 160;
        private static final int DIGEST16_SPLIT_SIZE = 4;

        private final TreeMap<Long, ProviderInfo> virtualNodes;
        /**
         * List<ProviderInfo> providerInfos的原始hashcode
         * 用于判断virtualNodes和providerInfos对应的Providers是否一致的
         */
        private final int identityHashCode;

        ConsistentHashSelector(List<ProviderInfo> providerInfos, int identityHashCode) {
            this.virtualNodes = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            // mount virtual node for every provider
            for (ProviderInfo providerInfo : providerInfos) {
                String address = providerInfo.getAddress().toString();
                for (int i = 0; i < VIRTUAL_REPLICA_NUMBER / DIGEST16_SPLIT_SIZE; i++) {
                    byte[] digest16 = md5(address + i);
                    for (int h = 0; h < DIGEST16_SPLIT_SIZE; h++) {
                        long m = hash(digest16, h);
                        virtualNodes.put(m, providerInfo);
                    }
                }
            }
        }

        /**
         * select one node for req
         */
        public ProviderInfo select(RpcRequest rpcRequest) {
            String key = toKey(rpcRequest.getParameterValues());
            byte[] digest16 = md5(key);
            long hash = hash(digest16, 0);
            return selectForKey(hash);
        }

        /**
         * make key by first req param. hash(md5(key)) to select one node on hash circle
         */
        private String toKey(Object[] args) {
            if (args == null || args.length == 1) {
                return "";
            }
            return String.valueOf(args[0]);
        }

        /**
         * select one node for req hash
         */
        private ProviderInfo selectForKey(long hash) {
            // 环上找到第一个大于req hash的node
            Map.Entry<Long, ProviderInfo> entry = virtualNodes.tailMap(hash, true).firstEntry();
            // 没找到的话，循环从头找第一个
            if (entry == null) {
                entry = virtualNodes.firstEntry();
            }
            return entry.getValue();
        }

        /**
         * @return 取digest16[number*4...number*4+3]四位进行hash运算后的hash值
         */
        private long hash(byte[] digest16, int number) {
            return (((long) (digest16[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest16[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest16[1 + number * 4] & 0xFF) << 8)
                    | (digest16[number * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }

        /**
         * @return 长度固定16位的摘要
         */
        private byte[] md5(String value) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            md5.reset();
            md5.update(value.getBytes(StandardCharsets.UTF_8));
            return md5.digest();
        }
    }
}
