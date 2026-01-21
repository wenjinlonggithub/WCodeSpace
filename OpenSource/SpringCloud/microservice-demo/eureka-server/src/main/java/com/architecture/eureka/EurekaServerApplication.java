package com.architecture.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server 启动类
 *
 * 核心功能:
 * 1. 服务注册: 微服务启动时向Eureka注册自己的信息
 * 2. 服务发现: 客户端从Eureka获取其他服务的地址信息
 * 3. 健康检查: 定期检查已注册服务的健康状态
 * 4. 自我保护: 网络故障时避免误剔除服务
 *
 * 工作原理:
 * - 服务提供者启动时,向Eureka Server注册,携带服务名、IP、端口等信息
 * - 服务消费者从Eureka Server获取服务列表,缓存到本地
 * - 服务提供者每30秒发送心跳,续约租期
 * - Eureka Server 90秒未收到心跳,剔除该服务实例
 * - 客户端每30秒从服务端同步服务列表
 *
 * 集群模式:
 * - Eureka Server之间通过互相注册形成集群
 * - 每个节点既是服务端也是客户端
 * - 节点间同步注册信息,实现高可用
 *
 * @author Architecture Team
 */
@SpringBootApplication
@EnableEurekaServer // 启用Eureka Server功能
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
        System.out.println("==================================================");
        System.out.println("Eureka Server 启动成功!");
        System.out.println("访问地址: http://localhost:8761");
        System.out.println("==================================================");
    }
}
