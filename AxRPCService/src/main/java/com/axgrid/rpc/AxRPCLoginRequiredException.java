package com.axgrid.rpc;

public class AxRPCLoginRequiredException extends AxRPCException {
    public AxRPCLoginRequiredException(){
        super(401, "Unauthorized");
    }
}
