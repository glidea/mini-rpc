package top.glidea.framework.common.exception;

/**
 * Rpc调用过程中的非业务异常
 */
public class RpcException extends RuntimeException {

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}

