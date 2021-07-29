package top.glidea.framework.common.exception;

/**
 * 标识cause是一个业务异常
 */
public class BusinessException extends RuntimeException {
    public BusinessException(Throwable cause) {
        super(cause);
    }
}
