package top.glidea.framework.common.util;

import top.glidea.framework.common.exception.RpcException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ExceptionUtils {

    public static String toString(Throwable e) {
        Writer w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    public static void clearStackTraceRecursive(Throwable e) {
        while (e != null) {
            e.setStackTrace(new StackTraceElement[]{});
            e = e.getCause();
        }
    }

    public static RpcException ensureIsRpcException(Throwable e) {
        if (e instanceof RpcException) {
            return (RpcException) e;
        }
        return new RpcException(e.getMessage(), e);
    }
}
