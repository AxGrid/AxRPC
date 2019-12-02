package com.axgrid.rpc.web.service;

import com.axgrid.rpc.AxRPCContext;
import com.google.protobuf.GeneratedMessageV3;

import javax.servlet.http.HttpServletRequest;

public interface AxRPCContextService<T extends GeneratedMessageV3, C extends AxRPCContext> {

    C getContext(T request, HttpServletRequest httpRequest);
}
