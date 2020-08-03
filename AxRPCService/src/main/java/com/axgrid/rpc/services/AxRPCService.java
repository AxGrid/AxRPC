package com.axgrid.rpc.services;

import com.axgrid.metrics.service.AxMetricService;
import com.axgrid.rpc.*;
import com.axgrid.rpc.dto.*;
import com.axgrid.rpc.exception.AxRPCException;
import com.axgrid.rpc.exception.AxRPCLoginRequiredException;
import com.axgrid.rpc.exception.AxRPCTrxRequiredException;
import com.axgrid.rpc.repository.AxRPCCache;
import com.google.protobuf.GeneratedMessageV3;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public abstract class AxRPCService<T extends GeneratedMessageV3, V extends GeneratedMessageV3, C extends AxRPCContext> implements AxRPCDescription {

    private final List<MethodHolder> methods;

    public int methodsCount() {
        return methods.size();
    }

    private Class<T> persistentResponseClass;
    private Class<T> persistentRequestClass;
    private Class<T> persistentContextClass;

    @Autowired(required = false)
    List<AxRPCEntryPoint<T>> entryPoints;

    private Method getTrx = null;

    private String errorCodeFieldName = "errorCode";

    private String correlationIdFieldName = "correlationId";

    private String successFieldName = "success";

    private String errorTextFieldName = "errorText";

    private String sessionFieldName = "session";

    private String trxFieldName = "trx";

    public String getSessionFieldName() {
        try {
            if (persistentRequestClass.getMethod("get" + StringUtils.capitalize(sessionFieldName)) != null) {
                return sessionFieldName;
            } else return null;
        }catch(NoSuchMethodException ignore) {
            return null;
        }
    }

    @Value("${axgrid.metrics.enabled:false}")
    public boolean metricsEnabled;

    @Value("${axgrid.rpc.timeout:2000}")
    public int timeout;

    @Autowired
    private AxMetricService metricService;

    public String getErrorCodeFieldName() {
        try {
            if (persistentResponseClass.getMethod("get" + StringUtils.capitalize(errorCodeFieldName)) != null) {
                return errorCodeFieldName;
            } else return null;
        }catch(NoSuchMethodException ignore) {
            return null;
        }
    }

    public String getSuccessFieldName() {
        try {
            if (persistentResponseClass.getMethod("get" + StringUtils.capitalize(successFieldName)) != null) {
                return successFieldName;
            } else return null;
        }catch(NoSuchMethodException ignore) {
            return null;
        }
    }

    public String getErrorTextFieldName() {
        try {
            if (persistentResponseClass.getMethod("get" + StringUtils.capitalize(errorTextFieldName)) != null) {
                return errorTextFieldName;
            } else return null;
        }catch(NoSuchMethodException ignore) {
            return null;
        }
    }

    public String getCorrelationIdFieldName() {
        try {
            if (persistentRequestClass.getMethod("get" + StringUtils.capitalize(correlationIdFieldName)) != null) {
                return correlationIdFieldName;
            } else return null;
        }catch(NoSuchMethodException ignore) {
            return null;
        }
    }

    public String getTrxFieldName() {
        try {
            if (persistentRequestClass.getMethod("get" + StringUtils.capitalize(trxFieldName)) != null) {
                return trxFieldName;
            } else return null;
        }catch(NoSuchMethodException ignore) {
            return null;
        }
    }

    public String getHttpEntryPoint() {
        try {
            AxRPCEntryPoint<T> ep = entryPoints.stream().filter(item -> item.getType() == EntryPointTypes.HTTP).findFirst().orElse(null);
            if (ep == null) return "";
            RequestMapping rm =  ep.getClass().getAnnotation(RequestMapping.class);
            if (rm == null) return "";
            if (rm.path().length > 0)
                return rm.path()[0];
            if (rm.value().length > 0)
                return rm.value()[0];
        }catch (Exception e) {

        }
        return "";
    }

    @Autowired
    AxRPCCache<V> cacheService;

    private MethodHolder getRPCMethod(Class<?> type) {
        return methods.stream().filter(item -> Arrays.stream(item.innerMethod.getParameterTypes()).anyMatch(mt -> mt.isAssignableFrom(type))).findFirst().orElse(null);
    }

    public String getRequestObject() { return persistentRequestClass.getSimpleName(); }
    public String getResponseObject() { return persistentResponseClass.getSimpleName();}

    public String getRequestObjectFullName() { return persistentRequestClass.getName();}
    public String getResponseObjectFullName() { return persistentResponseClass.getName();}

    final Pattern patternHas = Pattern.compile("^hasOp(.*)$");
    final Pattern patternGet = Pattern.compile("^getOp(.*)$");

    private String name;

    public String getName() {
        if (this.name == null)
            return this.getClass().getSimpleName();
        else
            return this.name;
    }

    @Getter
    private String description;

    public String getFullName() {
        return this.getClass().getName();
    }

    public List<AxRPCDescriptionMethod> getMethods() {
        return methods.stream().map(item -> new AxRPCDescriptionMethod(
                item.getInnerMethod().getName(),
                item.getGetMethod().getReturnType().getName(),
                item.getGetMethod().getReturnType().getSimpleName(),
                item.getSetMethod().getParameterTypes()[0].getName(),
                item.getSetMethod().getParameterTypes()[0].getSimpleName(),
                item.rpc.description(),
                item.loginRequired,
                item.trxRequired,
                item.isEmptyRequest,
                item.timeout == null ? new AxRPCTimeoutHolder(timeout) : item.timeout
                )).collect(Collectors.toList());
    }

    public AxRPCService() {
        if (metricsEnabled) log.info("AxMetric Enable");
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

        AxRPCServiceDescription description = this.getClass().getAnnotation(AxRPCServiceDescription.class);
        if (description != null) {
            this.correlationIdFieldName = description.correlationIdFieldName();
            this.errorCodeFieldName = description.errorCodeFieldName();
            this.sessionFieldName = description.sessionFieldName();
            this.errorTextFieldName = description.errorTextFieldName();
            this.successFieldName = description.successFieldName();
            this.trxFieldName = description.trxFieldName();
            this.name = (description.name() == null || description.name().equals("")) ? this.getClass().getSimpleName() : description.name();
            this.description = description.description();
        }

        try {
            getTrx = this.persistentRequestClass.getMethod("get" + StringUtils.capitalize(trxFieldName));
        }catch (NoSuchMethodException ignore) {
        }

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
                    } catch (NoSuchMethodException ignore) {}
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
        Date startDate = new Date();
        String trx = null;
        if (getTrx != null)
            try {
                trx = (String)getTrx.invoke(request);
                V cachedResponse = cacheService.get(trx);

                if (cachedResponse != null) {
                    if (log.isDebugEnabled()) log.info("AxRPC Response from cache {} {}", trx, cachedResponse);
                    return cachedResponse;
                }
            }catch (IllegalAccessException | InvocationTargetException ignore) { }

        for(MethodHolder holder : methods) {
            try {
                V response = holder.Invoke(request, context, trx != null && trx.length() > 5);
                if (response != null) {
                    return cacheService.put(trx, response);
                }
            } catch (IllegalAccessException | InvocationTargetException ignore) {
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

        String description;

        final Method innerMethod;
        final Object target;

        boolean loginRequired = false;
        boolean trxRequired = false;
        boolean isEmptyRequest = false;
        AxRPCTimeoutHolder timeout = null;

        boolean hasContext = false;

        String getRPCMethodName() {
            return innerMethod.getName();
        }

        boolean notReady() {
            return hasMethod == null || getMethod == null || setMethod == null;
        }

        final Map<String, Method> setMethodMap = new ConcurrentHashMap<>();

        private void setField(V.Builder builder, String name, Object value) {
            try {
                Method m = setMethodMap.compute(name, (k,v) -> v != null ? v :
                        Arrays.stream(builder.getClass().getMethods()).filter(item -> item.getName().equals("set"+StringUtils.capitalize(name)) && item.getParameterTypes().length == 1).findFirst().orElse(null)
                );
                //Method m = Arrays.stream(builder.getClass().getMethods()).filter(item -> item.getName().equals("set"+StringUtils.capitalize(name)) && item.getParameterTypes().length == 1).findFirst().orElse(null);
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

        V Invoke(T request, C context, boolean trxPresent) throws InvocationTargetException, IllegalAccessException {
            if (!(boolean)hasMethod.invoke(request)) return null;
            if (metricsEnabled) metricService.increment("axrpc.rpc.request", 1, "method:" + getRPCMethodName());
            if (!trxPresent && trxRequired) throw new AxRPCTrxRequiredException(this.getRPCMethodName());
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
                if (metricsEnabled) metricService.increment("axrpc.rpc.ok", 1, "method:" + getRPCMethodName());
            }catch (AxRPCException ex) {
                if (metricsEnabled) metricService.increment("axrpc.rpc.error", 1, "method:" + getRPCMethodName()+";code:"+ex.getCode());
                setField(builder, successFieldName, false);
                log.error("Invoke Error in method {}.{}(): {}", target.getClass().getName(), innerMethod.getName(), ex.getMessage());
                setField(builder, errorTextFieldName, ex.getMessage() == null ? "NULL" : ex.getMessage());
                setField(builder, errorCodeFieldName, ex.getCode());
            }catch (InvocationTargetException ex) {
                setField(builder, successFieldName, false);
                if (ex.getCause() != null) {
                    if (ex.getCause() instanceof AxRPCException) {
                        if (metricsEnabled) { metricService.increment("axrpc.rpc.error", 1, "method:" + getRPCMethodName()+";code:"+((AxRPCException)ex.getCause()).getCode()); }
                        String message = ex.getCause().getMessage();
                        try {
                            log.info("BYTES:{}", message.getBytes("UTF-8"));
                        }catch (UnsupportedEncodingException e) {
                            log.info("ERROR:{}", e.getMessage());
                        }
                        setField(builder, errorTextFieldName, message);
                        setField(builder, errorCodeFieldName, ((AxRPCException)ex.getCause()).getCode());
                    }else {
                        if (metricsEnabled) { metricService.increment("axrpc.rpc.error", 1, "method:" + getRPCMethodName()+";code:500"); }
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
            this.trxRequired = this.innerMethod.getAnnotation(AxRPCTrx.class) != null;
            this.isEmptyRequest = this.innerMethod.getAnnotation(AxRPCEmpty.class) != null;
            this.timeout = this.innerMethod.getAnnotation(AxRPCTimeout.class) != null ? new AxRPCTimeoutHolder(this.innerMethod.getAnnotation(AxRPCTimeout.class)) : null;
        }
    }
}
