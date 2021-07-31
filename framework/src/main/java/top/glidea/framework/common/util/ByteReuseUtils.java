package top.glidea.framework.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * byte复用工具
 * 起因是想给解压缩分配内存，写完才想到ThreadLocal...
 */
@Deprecated
public class ByteReuseUtils {
    private static List<byte[]> bufferPool = new ArrayList<>();
    private static int allocedCap = 0;
    private static final int MAX_CAP = 30;

    public static synchronized byte[] alloc(int size) {
        for (byte[] bytes : bufferPool) {
            if (bytes.length >= size) {
                bufferPool.remove(bytes);
                return bytes;
            }
        }

        if (allocedCap < MAX_CAP) {
            allocedCap++;
            return new byte[size];
        }

        /*
          bufferPool在此仅供Netty NIO线程使用，线程数一般不会超过MAX_CAP
          所以一般不会走到这一步，{@link LinkedBufferReuseUtil}也是一个道理
         */
        while (bufferPool.size() == 0) {
            try {
                ByteReuseUtils.class.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException("buffer分配失败", e);
            }
        }
        return bufferPool.remove(0);
    }

    public static synchronized void givenBack(byte[] bytes) {
        bufferPool.add(bytes);
        ByteReuseUtils.class.notifyAll();
    }
}
