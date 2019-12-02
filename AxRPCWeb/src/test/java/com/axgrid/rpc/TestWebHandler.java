package com.axgrid.rpc;

import com.axgrid.rpc.web.AxRPCWebHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ax-rpc/v1/")
public class TestWebHandler extends AxRPCWebHandler<Request, Response, MyAxRPCContext> {

    public TestWebHandler() throws NoSuchMethodException {
    }
}
