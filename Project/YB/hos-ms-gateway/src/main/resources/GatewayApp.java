package com.hospital.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * @author lvzeqiang
 * @date 2022/6/30 16:58
 * @description
 **/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class GatewayApp {
    public static void main(String[] args) {
        //try {
        //    System.setProperty("reactor.netty.pool.leasingStrategy", "lifo");
        //} catch (Exception e) {
        //    System.out.println("set reactor error");
        //}

        SpringApplication.run(GatewayApp.class, args);

    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }
}
