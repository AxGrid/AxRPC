package com.axgrid.rpc.services;

import com.axgrid.rpc.dto.AxRPCContext;
import com.google.protobuf.GeneratedMessageV3;


public interface AxRPCContextService<T extends GeneratedMessageV3, C extends AxRPCContext> {
    C getContext(T request, Object requestObject);
    void createContext(Object requestObject);
    void removeContext(Object requestObject);

}
