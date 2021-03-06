package com.axgrid.rpc;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface AxRPCServiceDescription {
    String name() default "";
    String description() default "";
    String correlationIdFieldName() default "correlationId";
    String errorCodeFieldName() default "errorCode";
    String errorTextFieldName() default "errorText";
    String successFieldName() default "success";
    String sessionFieldName() default "session";
    String trxFieldName() default "trx";
}

