package top.glidea.framework.remoting.transport.protocol.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import top.glidea.framework.remoting.transport.protocol.ProtocolFrameDefinition;

/**
 * 协议帧解码器
 * get complete frame from TCP stream
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolFrameDecoder() {
        super(ProtocolFrameDefinition.MAX_FRAME_LENGTH,
                ProtocolFrameDefinition.LENGTH_FIELD_OFFSET,
                ProtocolFrameDefinition.LENGTH_FIELD_LENGTH,
                ProtocolFrameDefinition.LENGTH_ADJUSTMENT,
                0
        );
    }
}
