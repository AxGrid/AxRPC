package com.axgrid.rpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AxTestRPC extends AxRPCService<Request, Response, MyAxRPCContext> {

    @AxRPC
    public RpPing.Builder ping(OpPing ping) {
        return RpPing.newBuilder().setTime(ping.getTime());
    }

    @AxRPC
    @AxRPCLoginRequired
    public RpHelloWorld.Builder hw(OpHelloWorld m, MyAxRPCContext ctx) {
        return RpHelloWorld.newBuilder().setResult(String.format("Hello %s", m.getName()));
    }
}
