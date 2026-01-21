package com.architecture.order.client;

import com.architecture.order.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 *
 * 核心原理:
 * 1. @FeignClient注解会被Spring扫描
 * 2. Spring会为该接口创建动态代理对象
 * 3. 当调用接口方法时，代理对象会:
 *    - 从Eureka获取user-service的实例列表
 *    - 使用Ribbon/LoadBalancer进行负载均衡选择实例
 *    - 构建HTTP请求
 *    - 发送请求到选中的实例
 *    - 解析响应并返回
 *
 * 源码链接:
 * - FeignClientFactoryBean: 创建Feign客户端的工厂
 * - SynchronousMethodHandler: 处理同步方法调用
 * - LoadBalancerFeignClient: 集成负载均衡的Feign客户端
 *
 * @author architecture
 */
@FeignClient(
    name = "user-service",                    // 服务名(在Eureka中注册的名称)
    fallback = UserServiceFallback.class      // 降级处理类
)
public interface UserServiceClient {

    /**
     * 根据ID查询用户
     *
     * HTTP请求:
     * GET http://user-service/users/{id}
     *
     * 实际执行流程:
     * 1. Feign构建RequestTemplate
     * 2. Ribbon从Eureka获取user-service实例列表
     * 3. Ribbon使用负载均衡策略选择一个实例
     * 4. 发送HTTP请求: GET http://192.168.1.100:8001/users/1
     * 5. 接收响应并解码为UserDTO对象
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    /**
     * 验证用户是否存在
     *
     * @param id 用户ID
     * @return true-存在, false-不存在
     */
    @GetMapping("/users/{id}/exists")
    Boolean userExists(@PathVariable("id") Long id);
}
