package com.axgrid.rpc;

public class MyAxRPCContext implements AxRPCContext {
    @Override
    public boolean isLoggedIn() {
        return true;
    }
}
