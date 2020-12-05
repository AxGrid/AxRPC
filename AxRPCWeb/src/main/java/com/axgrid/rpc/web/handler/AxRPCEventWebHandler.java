package com.axgrid.rpc.web.handler;


import com.axgrid.rpc.dto.AxRPCEventContext;
import com.axgrid.rpc.repository.AxRPCEventRepository;
import com.axgrid.rpc.services.AxRPCEventContextService;
import com.axgrid.rpc.services.AxRPCEventService;
import com.axgrid.rpc.web.exceptions.AxRPCContextException;
import com.google.protobuf.GeneratedMessageV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.async.DeferredResult;

public abstract class AxRPCEventWebHandler <V extends GeneratedMessageV3, E extends AxRPCEventRepository<V>, C extends AxRPCEventContext> {

    @Autowired
    AxRPCEventService<V, E> eventService;

    @Autowired
    AxRPCEventContextService<C> eventContextService;

    @Value("${axgrid.rpc.eventTimeout:60_000}")
    long eventTimeout;

    @GetMapping(value = "/ev", produces = { "application/octet-stream" }, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public DeferredResult<byte[]> eventRequest(
            @RequestParam(name = "c", defaultValue = "default", required = false) String channel,
            @RequestParam(name = "l", defaultValue = "-1", required = false) long lastMessage,
            @RequestParam(name = "s", required = false) String session
    ) {
        C context = eventContextService.get(session);
        if (!context.isEventAllowed()) throw new AxRPCContextException();
        DeferredResult<byte[]> result = new DeferredResult<>(eventTimeout , new byte[0]);
        eventService.listener(channel, lastMessage, result);
        return result;
    }

}
