package com.axgrid.rpc.web.handler;


import com.axgrid.rpc.dto.AxRPCEventContext;
import com.axgrid.rpc.repository.AxRPCEventRepository;
import com.axgrid.rpc.services.AxRPCEventContextService;
import com.axgrid.rpc.services.AxRPCEventService;
import com.axgrid.rpc.web.exceptions.AxRPCContextException;
import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class AxRPCEventWebHandler <V extends GeneratedMessageV3, VC extends GeneratedMessageV3, E extends AxRPCEventRepository<V>, C extends AxRPCEventContext> {

    @Autowired
    AxRPCEventService<V, VC, E> eventService;

    @Autowired
    AxRPCEventContextService<C> eventContextService;

    @Value("${axgrid.rpc.eventTimeout:60000}")
    long eventTimeout;

    final byte[] defaultResult = new byte[0];

    @GetMapping(value = "/ev", produces = { "application/octet-stream" }, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public DeferredResult<byte[]> eventRequest(
            @RequestParam(name = "c", defaultValue = "default", required = false) String channel,
            @RequestParam(name = "l", defaultValue = "-1", required = false) long lastMessage,
            @RequestParam(name = "s", required = false) String session
    ) {
        C context = eventContextService.get(session);
        if (!context.isEventAllowed()) throw new AxRPCContextException(401, "Unauthorized");
        DeferredResult<byte[]> result = new DeferredResult<byte[]>(eventTimeout, defaultResult);
        result.onTimeout(() -> {
            log.debug("Timeout for {} in channel {}.{}", session, channel, lastMessage);
            eventService.removeListener(channel, result);
        });
        eventService.listener(channel, lastMessage, result);
        return result;
    }

}
