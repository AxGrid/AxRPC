package com.axgrid.rpc;

import com.axgrid.rpc.services.AxRPCEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MyEventService extends AxRPCEventService<Event, EventCollection, MyEventRepository> {
    @Override
    protected EventCollection getResponseMessage(List<Event> message) {
        return EventCollection.newBuilder()
                .addAllEvents(message)
                .build();
    }
}
