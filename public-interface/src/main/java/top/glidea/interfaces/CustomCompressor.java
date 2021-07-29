package top.glidea.interfaces;

import top.glidea.framework.remoting.compress.Compressor;

public class CustomCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        System.out.println("自定义压缩");
        return bytes;
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        System.out.println("自定义解压");
        return bytes;
    }
}
