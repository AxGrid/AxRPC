package com.axgrid.rpc.web.handler;

import com.axgrid.rpc.exception.AxRPCException;
import com.axgrid.rpc.exception.AxRPCInitializeException;
import com.google.protobuf.GeneratedMessageV3;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

@RestControllerAdvice
public abstract class AxRPCErrorHandler<V extends GeneratedMessageV3> {

    private Class<V> persistentResponseClass;
    private Method newBuilderMethod;
    private Method setSuccessMethod;
    private Method setErrorTextMethod;
    private Method setErrorCodeMethod;

    private Method getMethod(V.Builder builder, String name) {
        return Arrays.stream(builder.getClass().getMethods()).filter(item -> item.getName().equals("set"+ StringUtils.capitalize(name)) && item.getParameterTypes().length == 1).findFirst().orElse(null);
    }

    @ExceptionHandler(AxRPCException.class)
    @ResponseStatus(HttpStatus.OK)
    public void handleCustomException(AxRPCException axe, HttpServletResponse response) throws Exception {

        V.Builder builder = (V.Builder)newBuilderMethod.invoke(null);
        setSuccessMethod.invoke(builder, false);
        setErrorTextMethod.invoke(builder, axe.getMessage());
        setErrorCodeMethod.invoke(builder, axe.getCode());
    }


    public AxRPCErrorHandler() throws Exception {
        this.persistentResponseClass = (Class<V>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];

        newBuilderMethod = persistentResponseClass.getMethod("newBuilder");
        V.Builder builder = (V.Builder)newBuilderMethod.invoke(null);
        setSuccessMethod = getMethod(builder, "success");
        setErrorTextMethod = getMethod(builder, "errorText");
        setErrorCodeMethod = getMethod(builder, "errorCode");

    }

}
