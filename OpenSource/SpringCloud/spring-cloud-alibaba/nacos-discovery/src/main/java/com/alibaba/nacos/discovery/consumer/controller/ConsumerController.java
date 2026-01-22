package com.alibaba.nacos.discovery.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * 服务消费者Controller
 * 演示服务发现和负载均衡调用
 *
 * @author architecture
 */
@Slf4j
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    private final RestTemplate restTemplate;
    private final LoadBalancerClient loadBalancerClient;

    public ConsumerController(RestTemplate restTemplate, LoadBalancerClient loadBalancerClient) {
        this.restTemplate = restTemplate;
        this.loadBalancerClient = loadBalancerClient;
    }

    /**
     * 方式1: 使用@LoadBalanced RestTemplate调用
     * 自动实现负载均衡
     */
    @GetMapping("/call/hello")
    public String callHello(@RequestParam(value = "name", defaultValue = "Consumer") String name) {
        log.info("Consumer calling provider with name: {}", name);

        // 使用服务名调用,自动负载均衡
        String url = "http://nacos-provider/provider/hello?name=" + name;
        String result = restTemplate.getForObject(url, String.class);

        log.info("Provider response: {}", result);
        return result;
    }

    /**
     * 方式2: 使用LoadBalancerClient手动选择实例
     * 更灵活的负载均衡控制
     */
    @GetMapping("/call/info")
    public String callInfo() {
        log.info("Consumer calling provider info");

        // 手动选择服务实例
        ServiceInstance instance = loadBalancerClient.choose("nacos-provider");
        if (instance == null) {
            return "No available instance";
        }

        log.info("Selected instance: {}:{}", instance.getHost(), instance.getPort());

        // 构造URL并调用
        String url = String.format("http://%s:%d/provider/info",
                instance.getHost(), instance.getPort());
        String result = restTemplate.getForObject(url, String.class);

        log.info("Provider response: {}", result);
        return result;
    }

    /**
     * 批量调用演示
     * 验证负载均衡效果
     */
    @GetMapping("/call/batch")
    public String batchCall(@RequestParam(value = "count", defaultValue = "5") int count) {
        log.info("Batch calling provider {} times", count);

        StringBuilder results = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String url = "http://nacos-provider/provider/hello?name=Batch-" + i;
            String result = restTemplate.getForObject(url, String.class);
            results.append(i).append(": ").append(result).append("\n");

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return results.toString();
    }

    /**
     * 调用服务处理数据
     */
    @PostMapping("/call/process")
    public String callProcess(@RequestBody String data) {
        log.info("Consumer calling provider to process data");

        String url = "http://nacos-provider/provider/process";
        String result = restTemplate.postForObject(url, data, String.class);

        log.info("Provider response: {}", result);
        return result;
    }
}
