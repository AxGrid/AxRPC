package com.axgrid.rpc;

import com.axgrid.rpc.repository.AxRPCEventRepository;
import com.axgrid.rpc.services.AxRPCEventService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyEventService extends AxRPCEventService<Event, EventCollection, MyEventRepository> {
    @Override
    protected EventCollection getResponseMessage(List<Event> message) {
        return EventCollection.newBuilder()
                .addAllEvents(message)
                .build();
    }
}
