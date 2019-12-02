package com.axgrid.rpc.net.exception;

import com.axgrid.rpc.exception.AxRPCException;

public class AxRPCNotFoundException extends AxRPCException {
    public AxRPCNotFoundException() {
        super(501);
    }
}
