package top.glidea.framework.remoting.compress;


public interface Compressor {
    String NAME = "compressor";

    /**
     * 压缩
     * @param bytes byte of 1.txt
     * @return bytes of 1.zip
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压
     * @param bytes bytes of 1.zip
     * @return bytes byte of 1.txt
     */
    byte[] decompress(byte[] bytes);
}
