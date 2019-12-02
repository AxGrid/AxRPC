package com.axgrid.rpc.net.transport;

import com.axgrid.rpc.net.pipeline.ProtoNettyDecoder;
import com.axgrid.rpc.net.pipeline.ProtoNettyEncoder;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TCPTransport<T extends GeneratedMessageV3, V extends GeneratedMessageV3>  {

    Thread thread;
    final int port;

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    @Setter
    ChannelHandler channelHandler;

    @Getter
    boolean up = false;

    private ProtoNettyDecoder<T> decoder;
    private ProtoNettyEncoder<V> encoder;

    public TCPTransport(int port) {
        this.encoder = new ProtoNettyEncoder<V>() {};
        this.decoder = new ProtoNettyDecoder<T>() {};
        this.port = port;
    }

    public void run() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(10));
                            ch.pipeline().addLast("writeTimeoutHandler", new WriteTimeoutHandler(10));
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3));
                            ch.pipeline().addLast("decoder", decoder);
                            ch.pipeline().addLast(new LengthFieldPrepender(3));
                            ch.pipeline().addLast("encoder", encoder);//кодирует строку в биты при отправке
                            ch.pipeline().addLast(channelHandler);
                        }
                    });
            ChannelFuture f = b.bind(port).sync();
            log.info("TCP start on {} port", port);
            up = true;
            f.channel().closeFuture().sync();
        }catch (InterruptedException ignore) {
        }catch (Exception e) {
            log.error("exception:{}", e.getMessage());
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.info("TCP stopped");
            up = false;
        }
    }

    public void start() {
        if (thread != null)
            stop();
        if (channelHandler == null) return;
        thread = new Thread(this::run);
        thread.start();
    }


    public void stop() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        thread = null;
    }

    public String getName() {
        return "tcp";
    }
}
