package com.axgrid.rpc.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AxRPCEventRepository {
    final Map<String, Queue<byte[]>> data;

    public AxRPCEventRepository() {
        this.data = new ConcurrentHashMap<>();
    }
}
