package com.alibaba.nacos.discovery.controller;

import com.alibaba.nacos.discovery.model.ServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务提供者Controller
 * 演示服务注册和服务信息查询
 *
 * @author architecture
 */
@Slf4j
@RestController
@RequestMapping("/provider")
public class ProviderController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String port;

    private final DiscoveryClient discoveryClient;

    public ProviderController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * 提供服务接口
     */
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        log.info("Provider received request: name={}", name);
        return String.format("Hello %s, from %s:%s", name, applicationName, port);
    }

    /**
     * 获取服务信息
     */
    @GetMapping("/info")
    public ServiceInfo getServiceInfo() {
        ServiceInfo info = new ServiceInfo();
        info.setServiceName(applicationName);
        info.setPort(port);
        info.setServices(discoveryClient.getServices());

        log.info("Service info: {}", info);
        return info;
    }

    /**
     * 获取指定服务的所有实例
     */
    @GetMapping("/instances/{serviceName}")
    public List<ServiceInstance> getInstances(@PathVariable String serviceName) {
        log.info("Query instances for service: {}", serviceName);
        return discoveryClient.getInstances(serviceName);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    /**
     * 模拟服务调用
     */
    @PostMapping("/process")
    public String processData(@RequestBody String data) {
        log.info("Processing data: {}", data);
        // 模拟业务处理
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Processed: " + data;
    }
}
