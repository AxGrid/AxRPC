package com.axgrid.rpc;

import com.axgrid.rpc.web.handler.AxRPCWebHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ax-rpc/v1/test")
public class TestWebHandler extends AxRPCWebHandler<Request, Response, MyAxRPCContext> {
}
