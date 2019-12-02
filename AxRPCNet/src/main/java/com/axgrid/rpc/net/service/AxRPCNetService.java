package com.axgrid.rpc.net.service;

import com.axgrid.rpc.dto.AxRPCContext;
import com.axgrid.rpc.exception.AxRPCException;
import com.axgrid.rpc.net.exception.AxRPCNotFoundException;
import com.axgrid.rpc.services.AxRPCContextService;
import com.axgrid.rpc.services.AxRPCService;
import com.axgrid.rpc.net.transport.TCPTransport;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Slf4j
public class AxRPCNetService<T extends GeneratedMessageV3, V extends GeneratedMessageV3, C extends AxRPCContext> extends SimpleChannelInboundHandler implements HealthIndicator {
    private TCPTransport<T, V> transport;

    @Autowired(required = false)
    List<AxRPCService<T, V, C>> services;

    @Autowired
    AxRPCContextService<T, C> contextService;

    @PostConstruct
    void start() {
        transport.start();
    }

    @PreDestroy
    void stop() {
        transport.stop();
    }

    public AxRPCNetService(int port) {
        transport = new TCPTransport<>(port);
        transport.setChannelHandler(this);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext transportContext, Object o) throws Exception {
        T requestProto = (T)o;
        try {
            C ctx = contextService.getContext(requestProto, transportContext);
            if (log.isDebugEnabled()) log.debug("AxRpcRequest {} CTX:{}", requestProto, ctx);
            for(AxRPCService<T, V, C> service : services) {
                V responseProto = service.request(requestProto, ctx);
                if (log.isDebugEnabled()) log.debug("AxRpcResponse {} CTX:{}", responseProto, ctx);
                if (responseProto != null){
                    transportContext.channel().write(responseProto);
                    return;
                }
            }
            throw new AxRPCNotFoundException();
        }catch (AxRPCException exc) {
            log.error("DisconnectException: {}", exc.getMessage());
            if (exc.getCode() >= 500)
                transportContext.channel().disconnect();
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        contextService.createContext(channelHandlerContext);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
        contextService.removeContext(channelHandlerContext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        log.error("AxRPCNetException: {}", throwable.getMessage());
        channelHandlerContext.channel().disconnect();
    }

    @Override
    public Health health() {
        Health.Builder res = transport.isUp() ? Health.up() :  Health.down();
        res.withDetail(transport.getName(), transport.isUp());
        return res.build();
    }}
