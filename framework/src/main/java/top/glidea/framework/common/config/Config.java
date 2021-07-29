package top.glidea.framework.common.config;

import java.util.Map;

public interface Config {
    /**
     * get value of option
     * @param option     eg: provider.service.port
     * @param valueClass What is the type of value. eg: Integer.class
     * @return option's value.
     */
    <T> T get(String option, Class<T> valueClass);
    /**
     * get value of option
     * @return a value of type String
     */
    String get(String option);

    /**
     * get value of option
     * @return value. impossible to be Null
     */
    <T> T getNotNull(String option, Class<T> valueClass);
    /**
     * get value of option
     * @return a value of type String. impossible to be Null
     */
    String getNotNull(String option);

    /**
     * get prop value from optionOfList
     * <blockquote><pre>
     * eg:
     * consumer:
     *   services:
     *     - interface: com.xxx.A
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
     *  you want to get methods of interface: com.xxx.A.
     *  by:
     *  matchArgs.put("interface", "com.xxx.A")
     *  getFromMapList(ConfigOption.CONSUMER_SERVICES, "methods", List.class, matchArgs)
     *  </pre></blockquote>
     * @return value
     */
    <T> T getFromMapList(String optionOfList, String targetProp, Class<T> valueClass, Map<String, Object> matchKvs);
}
