package com.axgrid.rpc.net.pipeline;

import com.axgrid.rpc.exception.AxRPCInitializeException;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
public class ProtoNettyDecoder<T extends GeneratedMessageV3> extends MessageToMessageDecoder<ByteBuf> {

    private final Class<T> persistentClass;
    private final Method parseFrom;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Object o = parseFrom.invoke(null, byteBuf.nioBuffer());
        list.add(o);
        byteBuf.release();
    }

    public ProtoNettyDecoder() {
        persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        try {
            parseFrom = this.persistentClass.getMethod("parseFrom", ByteBuffer.class);
        }catch (NoSuchMethodException e) {
            log.error("Protobuf parseFrom(InputStream) not found");
            throw new AxRPCInitializeException();
        }
    }


}
