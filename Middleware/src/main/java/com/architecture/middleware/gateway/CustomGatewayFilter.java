package com.architecture.middleware.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CustomGatewayFilter extends AbstractGatewayFilterFactory<CustomGatewayFilter.Config> {

    public CustomGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("Gateway filter: " + config.getName() + " - Processing request");
            
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().name();
            System.out.println("Request: " + method + " " + path);
            
            long startTime = System.currentTimeMillis();
            
            return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    System.out.println("Request processed in " + (endTime - startTime) + "ms");
                })
            );
        };
    }

    public static class Config {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}