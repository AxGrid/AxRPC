package com.axgrid.rpc;

import com.axgrid.rpc.dto.AxRPCContext;
import com.axgrid.rpc.dto.AxRPCEventContext;

public class MyAxRPCContext implements AxRPCContext, AxRPCEventContext {
    @Override
    public boolean isLoggedIn() {
        return true;
    }

    @Override
    public boolean isEventAllowed() {
        return true;
    }
}
