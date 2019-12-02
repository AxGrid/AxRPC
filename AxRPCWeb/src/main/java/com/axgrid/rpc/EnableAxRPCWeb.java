package com.axgrid.rpc;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AxRPCConfiguration.class})
public @interface EnableAxRPCWeb {
}
