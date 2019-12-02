package com.axgrid.rpc.net.pipeline;


import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class ProtoNettyEncoder<T extends GeneratedMessageV3> extends MessageToMessageEncoder<T> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, T t, List<Object> list) {
        list.add(Unpooled.wrappedBuffer(t.toByteArray()));
    }
}
