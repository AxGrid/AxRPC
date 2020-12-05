package com.axgrid.rpc.services;

import com.axgrid.rpc.dto.AxRPCEventContext;

public interface AxRPCEventContextService<C extends AxRPCEventContext> {
    C get(String session);
}
