package com.axgrid.rpc.services;

import com.axgrid.rpc.dto.AxRPCContext;
import com.axgrid.rpc.repository.AxRPCEventRepository;
import com.google.protobuf.GeneratedMessageV3;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AxRPCEventService<T extends GeneratedMessageV3, C extends AxRPCContext> {

    @Autowired
    AxRPCEventRepository eventRepository;

    public void invoke(T message, C context) {

    }

}
