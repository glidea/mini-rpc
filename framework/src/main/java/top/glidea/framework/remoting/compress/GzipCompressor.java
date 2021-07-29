package top.glidea.framework.remoting.compress;

import top.glidea.framework.remoting.transport.protocol.ProtocolFrameDefinition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompressor implements Compressor {
    private static final int BUFFER_SIZE = ProtocolFrameDefinition.MAX_FRAME_LENGTH;
    private static ThreadLocal<byte[]> localBuffer = ThreadLocal.withInitial(() -> new byte[BUFFER_SIZE]);

    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("压缩粗戳辣", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        byte[] buffer = localBuffer.get();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {

            int n;
            while ((n = gunzip.read(buffer)) > -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("解压粗戳辣", e);
        }
    }
}
