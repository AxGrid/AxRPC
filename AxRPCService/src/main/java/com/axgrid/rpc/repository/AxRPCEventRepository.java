package com.axgrid.rpc.repository;

import com.axgrid.rpc.dto.AxRPCEvent;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AxRPCEventRepository {
    final Map<String, Queue<AxRPCEvent>> data;

    

    public AxRPCEventRepository() {
        this.data = new ConcurrentHashMap<>();
    }
}
