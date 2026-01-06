package com.architecture.middleware.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

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
            System.out.println("Request path: " + path);
            
            return chain.filter(exchange).then(
                org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl(exchange, exchange.getRequest().getURI())
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