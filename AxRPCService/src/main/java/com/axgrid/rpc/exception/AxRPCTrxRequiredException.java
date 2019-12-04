package com.axgrid.rpc.exception;

public class AxRPCTrxRequiredException extends AxRPCException {
    public AxRPCTrxRequiredException(){
        super(400, "Trx required");
    }
    public AxRPCTrxRequiredException(String method){
        super(400, String.format("Method %s required Trx", method));
    }
}
