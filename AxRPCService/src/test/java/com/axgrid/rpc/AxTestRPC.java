package com.axgrid.rpc;

import com.axgrid.rpc.services.AxRPCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AxTestRPC extends AxRPCService<Request, Response, AxContext> {

    int counter = 0;

    @AxRPC
    public RpPing.Builder ping(OpPing ping) {
        log.info("test Method request: {}", ping.getTime());
        return RpPing.newBuilder().setTime(ping.getTime());
    }

    @AxRPC
    @AxRPCLoginRequired
    public RpHelloWorld.Builder hw(OpHelloWorld m, AxContext ctx) {
        ctx.counter ++;
        return RpHelloWorld.newBuilder().setResult(String.format("Hello %s", m.getName()));
    }

    @AxRPC
    @AxRPCTrx
    public RpCounter.Builder counter(OpCounter request) {
        counter += request.getIncrement();
        return RpCounter.newBuilder().setValue(counter);
    }
}
