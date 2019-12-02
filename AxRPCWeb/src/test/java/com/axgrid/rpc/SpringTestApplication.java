package com.axgrid.rpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@Slf4j
@ActiveProfiles("test")
@EnableAxRPCWeb
public class SpringTestApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringTestApplication.class, args);
    }

}
