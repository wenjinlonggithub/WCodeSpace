package com.architecture.middleware.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/user/**")
                .uri("http://localhost:8081"))
            .route("order-service", r -> r.path("/order/**")
                .uri("http://localhost:8082"))
            .route("product-service", r -> r.path("/product/**")
                .filters(f -> f.addRequestHeader("X-Gateway", "Spring-Cloud-Gateway"))
                .uri("http://localhost:8083"))
            .build();
    }
}