package com.axgrid.rpc;

import com.axgrid.rpc.dto.AxRPCContext;

public class MyAxRPCContext implements AxRPCContext {
    @Override
    public boolean isLoggedIn() {
        return true;
    }
}
