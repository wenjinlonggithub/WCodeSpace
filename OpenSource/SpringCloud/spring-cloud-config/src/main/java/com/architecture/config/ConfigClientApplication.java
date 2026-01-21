package com.architecture.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Cloud Config Client
 *
 * 核心功能：
 * 1. 从Config Server获取配置
 * 2. 支持配置动态刷新(@RefreshScope)
 * 3. 快速失败与重试机制
 *
 * 配置加载顺序：
 * 1. bootstrap.yml 先加载
 * 2. 连接Config Server
 * 3. 获取远程配置
 * 4. 合并本地配置
 */
@SpringBootApplication
public class ConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }
}

/**
 * 配置刷新示例Controller
 *
 * @RefreshScope 注解说明：
 * - 标记的Bean会在配置更新时重新创建
 * - 需要调用 /actuator/refresh 端点触发刷新
 * - 配合Spring Cloud Bus可实现自动刷新
 */
@RefreshScope
@RestController
class ConfigController {

    @Value("${config.info:default}")
    private String configInfo;

    @Value("${server.port}")
    private String port;

    /**
     * 获取配置信息
     * 业务场景：实时获取最新配置，无需重启服务
     */
    @GetMapping("/config")
    public String getConfig() {
        return "Config Info: " + configInfo + ", Port: " + port;
    }
}
