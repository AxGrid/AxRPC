package com.axgrid.rpc.exception;

public class AxRPCLoginRequiredException extends AxRPCException {
    public AxRPCLoginRequiredException(){
        super(401, "Unauthorized");
    }
}
