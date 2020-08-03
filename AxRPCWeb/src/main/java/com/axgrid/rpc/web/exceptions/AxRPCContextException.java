package com.axgrid.rpc.web.exceptions;

import com.axgrid.rpc.exception.AxRPCException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
public class AxRPCContextException extends AxRPCException {
    public AxRPCContextException(int code, String message) {
        super(code, message);
    }

}
