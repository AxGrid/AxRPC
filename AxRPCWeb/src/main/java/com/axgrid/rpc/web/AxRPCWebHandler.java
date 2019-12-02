package com.axgrid.rpc.web;


import com.axgrid.rpc.AxRPCContext;
import com.axgrid.rpc.AxRPCService;
import com.axgrid.rpc.Request;
import com.axgrid.rpc.exceptions.AxRPCNotFoundException;
import com.axgrid.rpc.service.AxRPCContextService;
import com.axgrid.rpc.service.AxRPCWebService;
import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

@Slf4j
public abstract class AxRPCWebHandler<T extends GeneratedMessageV3, V extends GeneratedMessageV3, C extends AxRPCContext> {

    private Class<T> persistentResponseClass;
    private Class<T> persistentRequestClass;
    private Class<T> persistentContextClass;

    private Method parseFrom;

    @Autowired(required = false)
    List<AxRPCService<T, V, C>> services;

    @Autowired
    AxRPCContextService<T, C> contextService;

    @PostMapping("/")
    public void protoRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {

            T requestProto = (T)parseFrom.invoke(null, request.getInputStream());
            C ctx = contextService.getContext(requestProto, request);
            if (log.isDebugEnabled()) log.debug("AxRpcRequest {} CTX:{}", requestProto, ctx);
            for(AxRPCService<T, V, C> service : services) {
                V responseProto = service.request(requestProto, ctx);
                if (log.isDebugEnabled()) log.debug("AxRpcResponse {} CTX:{}", responseProto, ctx);
                if (responseProto != null){
                    responseProto.writeTo(response.getOutputStream());
                    return;
                }
            }
            if (log.isDebugEnabled()) log.debug("AxRpcResponse: Not found");
            throw new AxRPCNotFoundException();

        }catch (InvocationTargetException | IllegalAccessException ignore) {
        }
    }

    public AxRPCWebHandler() throws NoSuchMethodException {
        this.persistentResponseClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];

        this.persistentRequestClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

        this.persistentContextClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[2];

        parseFrom = this.persistentRequestClass.getMethod("parseFrom", InputStream.class);
    }

}
