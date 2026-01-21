package com.architecture.netflix;

import com.netflix.appinfo.InstanceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Eureka Client - 服务提供者/消费者
 *
 * 核心功能：
 * 1. 服务注册：启动时向Eureka Server注册
 * 2. 续约(心跳)：每30秒发送一次心跳
 * 3. 获取注册表：每30秒从Eureka Server拉取服务列表
 * 4. 服务下线：关闭时通知Eureka Server
 *
 * 注册流程：
 * 1. 读取配置（application.yml）
 * 2. 构建InstanceInfo（实例信息）
 * 3. 发送POST请求到Eureka Server
 * 4. Eureka Server存储到注册表
 *
 * 心跳机制：
 * - 默认30秒一次心跳
 * - 90秒未收到心跳则剔除实例
 * - 自我保护模式：大量实例心跳失败时，暂停剔除
 */
@EnableDiscoveryClient
@SpringBootApplication
public class EurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }
}

/**
 * 服务发现示例Controller
 */
@RestController
class ServiceDiscoveryController {

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * 获取所有服务列表
     *
     * 业务场景：
     * - 服务监控面板：展示所有在线服务
     * - 管理后台：查看服务实例状态
     */
    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }

    /**
     * 获取指定服务的所有实例
     *
     * 业务场景：
     * - 负载均衡：从多个实例中选择一个
     * - 健康检查：过滤掉不健康的实例
     * - 灰度发布：根据版本标签选择实例
     *
     * @param serviceName 服务名（如：user-service）
     */
    @GetMapping("/services/{serviceName}/instances")
    public List<ServiceInstance> getInstances(@PathVariable String serviceName) {
        return discoveryClient.getInstances(serviceName);
    }

    /**
     * 服务实例详细信息
     *
     * 返回示例：
     * {
     *   "instanceId": "192.168.1.100:user-service:8081",
     *   "serviceId": "user-service",
     *   "host": "192.168.1.100",
     *   "port": 8081,
     *   "uri": "http://192.168.1.100:8081",
     *   "metadata": {
     *     "version": "1.0",
     *     "region": "cn-north-1"
     *   }
     * }
     */
    @GetMapping("/services/{serviceName}/detail")
    public String getServiceDetail(@PathVariable String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        if (instances.isEmpty()) {
            return "No instances found for service: " + serviceName;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Service: ").append(serviceName).append("\n");
        sb.append("Total Instances: ").append(instances.size()).append("\n\n");

        for (ServiceInstance instance : instances) {
            sb.append("Instance ID: ").append(instance.getInstanceId()).append("\n");
            sb.append("Host: ").append(instance.getHost()).append("\n");
            sb.append("Port: ").append(instance.getPort()).append("\n");
            sb.append("URI: ").append(instance.getUri()).append("\n");
            sb.append("Metadata: ").append(instance.getMetadata()).append("\n");
            sb.append("---\n");
        }

        return sb.toString();
    }
}

/**
 * 业务使用案例：电商微服务架构
 *
 * 服务架构：
 * ┌──────────────┐
 * │Eureka Server │
 * │  (8761)      │
 * └──────┬───────┘
 *        │
 *    注册/发现
 *        │
 *  ┌─────┴─────────────┬──────────────┬─────────────┐
 *  │                   │              │             │
 *  ▼                   ▼              ▼             ▼
 * ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
 * │  User    │  │  Order   │  │ Product  │  │ Payment  │
 * │ Service  │  │ Service  │  │ Service  │  │ Service  │
 * │ (8081-3) │  │ (8091-3) │  │ (8101-3) │  │ (8111-2) │
 * └──────────┘  └──────────┘  └──────────┘  └──────────┘
 *
 * 配置示例：
 *
 * # application.yml
 * spring:
 *   application:
 *     name: user-service
 *
 * eureka:
 *   client:
 *     service-url:
 *       defaultZone: http://localhost:8761/eureka/
 *     # 是否从Eureka获取注册信息
 *     fetch-registry: true
 *     # 是否注册到Eureka
 *     register-with-eureka: true
 *   instance:
 *     # 实例ID
 *     instance-id: ${spring.cloud.client.ip-address}:${server.port}
 *     # 优先使用IP注册
 *     prefer-ip-address: true
 *     # 心跳间隔（默认30秒）
 *     lease-renewal-interval-in-seconds: 30
 *     # 超时剔除时间（默认90秒）
 *     lease-expiration-duration-in-seconds: 90
 *     # 元数据（自定义信息）
 *     metadata-map:
 *       version: 1.0.0
 *       region: cn-north-1
 *       zone: zone-1
 *
 * 使用场景：
 *
 * 1. 服务注册：
 *    - 用户服务启动 3 个实例（8081, 8082, 8083）
 *    - 订单服务启动 3 个实例（8091, 8092, 8093）
 *    - 所有实例注册到Eureka
 *
 * 2. 服务发现：
 *    - 订单服务需要调用用户服务
 *    - 从Eureka获取user-service的所有实例
 *    - 使用Ribbon进行负载均衡
 *
 * 3. 故障转移：
 *    - user-service的8081实例宕机
 *    - Eureka在90秒后剔除该实例
 *    - 新请求自动路由到8082和8083
 *
 * 4. 灰度发布：
 *    - 部署新版本实例，metadata.version=2.0.0
 *    - 根据版本号选择性路由流量
 *    - 逐步增加新版本流量比例
 */
