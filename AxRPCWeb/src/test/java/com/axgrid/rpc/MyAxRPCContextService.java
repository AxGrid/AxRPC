package com.axgrid.rpc;


import com.axgrid.rpc.services.AxRPCContextServiceImpl;
import com.axgrid.rpc.services.AxRPCEventContextService;
import org.springframework.stereotype.Service;

@Service
public class MyAxRPCContextService extends AxRPCContextServiceImpl<Request, MyAxRPCContext> implements AxRPCEventContextService<MyAxRPCContext> {
    @Override
    public MyAxRPCContext getContext(Request request, Object httpRequest) {
        return new MyAxRPCContext();
    }

    @Override
    public MyAxRPCContext get(String session) {
        return new MyAxRPCContext();
    }
}
