package com.architecture.middleware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.architecture.middleware.database.mapper")
public class MiddlewareApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiddlewareApplication.class, args);
    }
}
