package com.axgrid.rpc;

import com.axgrid.rpc.repository.AxRPCEventRepository;
import lombok.var;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class MyEventRepository implements AxRPCEventRepository<Event>  {
    Map<String, List<Event>> events = new ConcurrentHashMap<>();
    AtomicLong ids = new AtomicLong();

    @Override
    public List<Event> getAll(String channel, Long lastId) {
        if (lastId == -1) {
            var list = events.getOrDefault(channel, Collections.emptyList());
            if (list.size() > 0) return Collections.singletonList(list.get(list.size() -1));
        }

        return events.getOrDefault(channel, Collections.emptyList()).stream().filter(item -> item.getId() > lastId).collect(Collectors.toList());
    }

    @Override
    public Event add(String channel,Event message) {
        var newMessage = message.toBuilder().setId(ids.incrementAndGet()).build();
        events.compute(channel, (k,v) -> {
            if (v == null) v = new ArrayList<>();
            v.add(newMessage);
            return v;
        });
        return newMessage;
    }
}
