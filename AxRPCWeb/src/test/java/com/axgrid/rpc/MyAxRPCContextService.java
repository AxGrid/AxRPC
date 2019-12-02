package com.axgrid.rpc;


import com.axgrid.rpc.services.AxRPCContextServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MyAxRPCContextService extends AxRPCContextServiceImpl<Request, MyAxRPCContext> {
    @Override
    public MyAxRPCContext getContext(Request request, Object httpRequest) {
        return new MyAxRPCContext();
    }

}
