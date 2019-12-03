package com.axgrid.rpc.web.handler;

import com.axgrid.rpc.dto.AxRPCContext;
import com.axgrid.rpc.services.AxRPCService;
import com.axgrid.rpc.exception.AxRPCInitializeException;
import com.axgrid.rpc.web.exceptions.AxRPCNotFoundException;
import com.axgrid.rpc.services.AxRPCContextService;
import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    private Method parseFromBytes;

    @Autowired(required = false)
    List<AxRPCService<T, V, C>> services;

    @Autowired
    AxRPCContextService<T, C> contextService;

    @PostMapping(value = "/", produces = { "application/octet-stream" }, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void protoRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException {
        log.info("Incoming request");
        try {

            byte[] getBytes = IOUtils.toByteArray(request.getInputStream());
            log.debug("Read {} bytes", getBytes.length);
            //T requestProto = (T)parseFrom.invoke(null, request.getInputStream());
            T requestProto = (T)parseFromBytes.invoke(null, getBytes);
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

        }catch (InvocationTargetException | IllegalAccessException e) {
            log.error("InvocationError:{}", e.getMessage());
            throw e;
        }
    }

    public AxRPCWebHandler()  {
        this.persistentResponseClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];

        this.persistentRequestClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

        this.persistentContextClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[2];
        try {
            parseFrom = this.persistentRequestClass.getMethod("parseFrom", InputStream.class);
            parseFromBytes = this.persistentRequestClass.getMethod("parseFrom", byte[].class);
        }catch (NoSuchMethodException e) {
            log.error("Protobuf parseFrom(InputStream) not found");
            throw new AxRPCInitializeException();
        }
    }

}
