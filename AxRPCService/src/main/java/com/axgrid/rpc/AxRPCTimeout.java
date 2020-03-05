package com.axgrid.rpc;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface AxRPCTimeout {
    int value() default 1000;
    int retry() default 5;
    int retryTimeout() default 50;
}
