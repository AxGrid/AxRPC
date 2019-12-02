package com.axgrid.rpc;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded=true)
@Data
public class AxRPCException extends RuntimeException {

    @EqualsAndHashCode.Include
    final int code;

    public AxRPCException() {
        this(500);
    }

    public AxRPCException(int code) {
        this(code, "AxRPCException "+code);
    }

    public AxRPCException(int code, String message) {
        super(message);
        this.code = code;
    }

}
