package com.axgrid.rpc;

import com.axgrid.rpc.service.AxRPCContextService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class MyAxRPCContextService implements AxRPCContextService<Request, MyAxRPCContext> {
    @Override
    public MyAxRPCContext getContext(Request request, HttpServletRequest httpRequest) {
        return new MyAxRPCContext();
    }
}
