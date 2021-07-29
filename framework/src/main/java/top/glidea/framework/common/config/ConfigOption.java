package top.glidea.framework.common.config;

/**
 * 配置项
 */
public interface ConfigOption {
    /**
     * Rpc服务端口
     */
    String PROVIDER_PUBLISH_PORT = "provider.publish.port";
    /**
     * 注册中心地址
     * <p>
     * eg: register-address = 159.66.66.66:2181
     */
    String REGISTRY_ADDRESS = "registry.address";
    /**
     * 帧压缩阈值
     * <p>
     * frame length >= COMPRESS_THRESHOLD，触发压缩（单位: byte）
     * 默认值为1400，可以稍作调整，以平衡CPU解压缩和网络传输的消耗
     * 但不建议超过TCP MSS，以减少一个消息拆成两部分到不同包的情况（超过MSS，app frame一定分散在多个tcp包中）
     * TCP MSS默认1460，实际值在TCP连接时协商决定
     */
    String COMPRESS_THRESHOLD = "compress.threshold";
    /**
     * 可拓展组件
     */
    String EXPANSION = "expansion";
    /**
     * 自定义异常
     * <blockquote><pre>
     * eg: custom-exceptions:
     *      - xx.xx.xxException
     *      - .....
     * </pre></blockquote>
     * 表示我作为Provider，已经确保了Consumer也import了上面的自定义RuntimeException，否则序列化会因为ClassNotFound而失败。
     * 如果你什么都不设置，自定义RuntimeException将转换成RuntimeException发送
     */
    String PROVIDER_CUSTOM_EXCEPTIONS = "provider.exception.custom-exceptions";
    /**
     * Provider向Consumer发送异常时，是否清除掉堆栈信息。仅传递异常信息，堆栈信息将通过log记录
     */
    String PROVIDER_EXCEPTION_IGNORE_STACK_TRACE = "provider.exception.ignore-stack-trace";
    /**
     * Provider线程池模型。boss线程数
     */
    String PROVIDER_THREAD_NUM_BOSS = "provider.thread-num.boss";
    /**
     * Provider线程池模型。worker线程数
     */
    String PROVIDER_THREAD_NUM_WORKER = "provider.thread-num.worker";
    /**
     * Provider线程池模型。业务线程数
     * <p>
     * 注：value <= 0时，PROVIDER_ENABLE_ASYNC将失效
     */
    String PROVIDER_THREAD_NUM_SERVICE = "provider.thread-num.service";
    /**
     * 业务是否异步处理
     * <p>
     * CPU密集型业务，建议关闭，反之。
     */
    String PROVIDER_ENABLE_ASYNC = "provider.enable-async";
    /**
     * 负载均衡权重
     * <p>
     * 根据机器相对性能而定，标准默认值为100
     */
    String PROVIDER_LOAD_BALANCE_WEIGHT = "provider.load-balance.weight";
    /**
     * Consumer服务设置
     * <blockquote><pre>
     * eg:
     * consumer:
     *   services:
     *     - interface: com.xxx...
     *       methods:
     *         - name: sayHello
     *           timeout: 2000
     *           retries: 1
     *         - name: sayHi
     *           retries: 1
     *     - interface: com.yyy...
     *       methods:
     *         - name: sya
     *           timeout: 22221
     *           retries: 4
     * </pre></blockquote>
     */
    String CONSUMER_SERVICES = "consumer.services";
}
