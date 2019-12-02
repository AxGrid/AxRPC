package com.axgrid.rpc;

import com.google.protobuf.GeneratedMessageV3;
import com.oracle.tools.packager.Log;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public abstract class AxRPCService<T extends GeneratedMessageV3, V extends GeneratedMessageV3, C extends AxRPCContext> {

    private final List<MethodHolder> methods;

    public int methodsCount() {
        return methods.size();
    }

    private Class<T> persistentResponseClass;
    private Class<T> persistentRequestClass;
    private Class<T> persistentContextClass;

    private String errorCodeFieldName = "errorCode";
    private String correlationIdFieldName = "correlationId";
    private String successFieldName = "success";
    private String errorTextFieldName = "errorText";


    private MethodHolder getRPCMethod(Class<?> type) {
        return methods.stream().filter(item -> Arrays.stream(item.innerMethod.getParameterTypes()).anyMatch(mt -> mt.isAssignableFrom(type))).findFirst().orElse(null);
    }

    final Pattern patternHas = Pattern.compile("^hasOp(.*)$");
    final Pattern patternGet = Pattern.compile("^getOp(.*)$");

    public AxRPCService() {

        this.persistentResponseClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];

        this.persistentRequestClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

        this.persistentContextClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[2];

        methods = getAccessibleMethods(this.getClass()).stream()
                .filter(item -> item.getAnnotation(AxRPC.class) != null)
                .map(item -> new MethodHolder(this, item.getAnnotation(AxRPC.class), item))
                .collect(Collectors.toList());



        for(Method method : persistentRequestClass.getMethods()) {
            Matcher matcher = patternGet.matcher(method.getName());
            if (matcher.find()) {
                String name = matcher.group(1);
                MethodHolder holder = getRPCMethod(method.getReturnType());
                if (holder != null) {
                    try {
                        holder.hasMethod = persistentRequestClass.getMethod("hasOp" + name);
                        holder.getMethod = method;
                        holder.newBuilderMethod = persistentResponseClass.getMethod("newBuilder");
                        holder.setMethod = holder.newBuilderMethod.getReturnType().getMethod("setRp" + name, holder.innerMethod.getReturnType());
                    }catch (NoSuchMethodException ignore) {}
                }
            }
        }

        methods.stream().filter(MethodHolder::notReady).collect(Collectors.toList()).forEach(item -> {
            log.warn("Internal Method {} in {} not ready for RPC ({} / {} / {})", item.innerMethod.getName(), this.getClass().getName(), item.hasMethod, item.getMethod, item.setMethod);
            methods.remove(item);
        });

        methods.forEach(item -> {
            log.info("Add RPCMethod {}.{}({})", this.getClass().getName(), item.innerMethod.getName(), item.getMethod.getName());
        });
    }

    public V request(T request, C context) {
        for(MethodHolder holder : methods) {
            try {
                V response = holder.Invoke(request, context);
                if (response != null) {
                    return response;
                }
            }catch (IllegalAccessException | InvocationTargetException ignore) {
                return null;
            }
        }
        return null;
    }


    private static List<Method> getAccessibleMethods(Class clazz) {
        List<Method> result = new ArrayList<>();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers) || Modifier.isPrivate(modifiers)) {
                    result.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }

    @Data
    class MethodHolder {
        final AxRPC rpc;
        Method hasMethod;
        Method getMethod;
        Method setMethod;
        Method newBuilderMethod;

        final Method innerMethod;
        final Object target;

        boolean loginRequired = false;

        boolean hasContext = false;

        String getRPCMethodName() {
            return innerMethod.getName();
        }

        boolean notReady() {
            return hasMethod == null || getMethod == null || setMethod == null;
        }

        private void setField(V.Builder builder, String name, Object value) {
            try {
                Method m = Arrays.stream(builder.getClass().getMethods()).filter(item -> item.getName().equals("set"+StringUtils.capitalize(name)) && item.getParameterTypes().length == 1).findFirst().orElse(null);
                if (m != null)
                    m.invoke(builder, value);
            } catch (InvocationTargetException | IllegalAccessException ignore) {
                return;
            }
        }

        private long getCorrelationId(T request) {
            try {
                Method m = request.getClass().getMethod("get"+StringUtils.capitalize(correlationIdFieldName));
                return (long)m.invoke(request);
            }catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException ignore) {
                return 0;
            }
        }

        V Invoke(T request, C context) throws InvocationTargetException, IllegalAccessException {
            if (!(boolean)hasMethod.invoke(request)) return null;
            V.Builder builder = (V.Builder)newBuilderMethod.invoke(null);
            try {
                long correlationId = getCorrelationId(request);
                setField(builder, correlationIdFieldName, correlationId);
                if (this.loginRequired && (context == null || !context.isLoggedIn()))
                    throw new AxRPCLoginRequiredException();
                Object res = getMethod.invoke(request);
                setField(builder, successFieldName, true);
                Object result = hasContext ? innerMethod.invoke(target, res, context) : innerMethod.invoke(target, res);
                setMethod.invoke(builder, result);
            }catch (AxRPCException ex) {
                setField(builder, successFieldName, false);
                log.error("Invoke Error in method {}.{}(): {}", target.getClass().getName(), innerMethod.getName(), ex.getMessage());
                setField(builder, errorTextFieldName, ex.getMessage() == null ? "NULL" : ex.getMessage());
                setField(builder, errorCodeFieldName, ex.code);
            }catch (InvocationTargetException ex) {
                setField(builder, successFieldName, false);
                if (ex.getCause() != null) {
                    if (ex.getCause() instanceof AxRPCException) {
                        String message = ex.getCause().getMessage();
                        try {
                            log.info("BYTES:{}", message.getBytes("UTF-8"));
                        }catch (UnsupportedEncodingException e) {
                            log.info("ERROR:{}", e.getMessage());
                        }
                        setField(builder, errorTextFieldName, message);
                        setField(builder, errorCodeFieldName, ((AxRPCException)ex.getCause()).getCode());
                    }else {
                        log.error("Invoke Error in method " + innerMethod.getName(), ex);
                        setField(builder, errorTextFieldName, ex.getMessage() == null ? "NULL" : ex.getMessage());
                        setField(builder, errorCodeFieldName, 500);
                    }
                }
            }
            return (V)builder.build();
        }

        MethodHolder(Object target, AxRPC rpc, Method innerMethod) {
            this.target = target;
            this.rpc = rpc;
            this.innerMethod = innerMethod;
            this.hasContext = Arrays.stream(innerMethod.getParameterTypes()).anyMatch(i -> i.equals(persistentContextClass));
            this.loginRequired = this.innerMethod.getAnnotation(AxRPCLoginRequired.class) != null;
        }
    }
}
