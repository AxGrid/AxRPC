package com.axgrid.rpc.services;

import com.axgrid.rpc.dto.AxRPCContext;
import com.google.protobuf.GeneratedMessageV3;

public abstract class AxRPCContextServiceImpl<T extends GeneratedMessageV3, C extends AxRPCContext>
        implements AxRPCContextService<T, C> {

    public void createContext(Object requestObject) { }

    public void removeContext(Object requestObject) { }

}
