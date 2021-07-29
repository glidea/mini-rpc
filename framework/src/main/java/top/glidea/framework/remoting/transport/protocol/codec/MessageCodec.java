package top.glidea.framework.remoting.transport.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import top.glidea.framework.common.factory.SingletonFactory;
import top.glidea.framework.common.config.Config;
import top.glidea.framework.common.config.ConfigOption;
import top.glidea.framework.common.config.YmlConfig;
import top.glidea.framework.common.factory.ExpansionFactory;
import top.glidea.framework.remoting.compress.Compressor;
import top.glidea.framework.remoting.transport.protocol.bodybean.Message;
import top.glidea.framework.remoting.transport.protocol.ProtocolFrameDefinition;
import top.glidea.framework.remoting.serialize.Serializer;

import java.util.List;

/**
 * 消息编解码器
 * Inbound:
 * ProtocolFrameDecoder#decode -> complete frame -> MessageCodec#decode -> Message obj of frame's body -> handler in the back
 * Outbound:
 * Message obj of frame's body -> MessageCodec#encode -> net
 * <p>
 * 必须和LengthFieldBasedFrameDecoder一起使用，确保入站时，MessageSharableCodec#decode接到的ByteBuf消息是完整的
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {
    private final Config config = SingletonFactory.get(YmlConfig.class);

    private Compressor compressor = ExpansionFactory.get(Compressor.class);
    private Serializer serializer = ExpansionFactory.get(Serializer.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) {
        ProtocolFrameDefinition definition = new ProtocolFrameDefinition(msg);
        ByteBuf frame = ctx.alloc().buffer();

        int needCompressMinLen = config.getNotNull(ConfigOption.COMPRESS_THRESHOLD, Integer.class);
        int frameLen = definition.getBodyLength() + ProtocolFrameDefinition.HEADER_LENGTH;
        if (frameLen >= needCompressMinLen) {
            byte[] compress = compressor.compress(definition.getBodyBytes());
            definition.setBodyBytes(compress);
            definition.setIsCompressed((byte) 1);
            definition.setBodyLength(compress.length);
            log.debug("发往 [{}] 的协议包长度：{}, 到达 {} 的压缩阈值，已自动进行压缩，压缩后长度为：{}",
                    ctx.channel().remoteAddress(), frameLen,
                    needCompressMinLen, definition.getBodyLength());
        }

        frame.writeBytes(definition.getMagicNum());
        frame.writeByte(definition.getIsCompressed());
        frame.writeByte(definition.getMessageType());
        frame.writeInt(definition.getBodyLength());
        frame.writeBytes(definition.getBodyBytes());
        outList.add(frame);
        log.debug("协议包封装完成，准备发往 [{}]", ctx.channel().remoteAddress());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf frame, List<Object> out) {
        try {
            ProtocolFrameDefinition definition = new ProtocolFrameDefinition(frame);
            if (definition.getIsCompressed() == 1) {
                byte[] bodyBytes = compressor.decompress(definition.getBodyBytes());
                definition.setBodyBytes(bodyBytes);
                definition.setBodyLength(bodyBytes.length);
                definition.setIsCompressed((byte) 0);
                log.debug("检测到 [{}] 的协议包经过了压缩，已将其解压", ctx.channel().remoteAddress());
            }

            byte messageType = definition.getMessageType();
            byte[] bodyBytes = definition.getBodyBytes();
            Class<?> messageClass = ProtocolFrameDefinition.getMessageClazz(messageType);
            Message message = (Message) serializer.toObj(bodyBytes, messageClass);
            out.add(message);

        } catch (Exception e) {
            throw new RuntimeException("frame decode fail", e);
        }
    }
}
