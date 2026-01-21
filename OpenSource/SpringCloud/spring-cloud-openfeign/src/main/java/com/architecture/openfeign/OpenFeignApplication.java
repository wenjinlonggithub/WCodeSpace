package com.architecture.openfeign;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Spring Cloud OpenFeign
 *
 * 核心功能：
 * 1. 声明式HTTP客户端：用注解定义HTTP请求
 * 2. 集成Ribbon：自动负载均衡
 * 3. 集成Hystrix：自动熔断降级
 * 4. 请求/响应压缩
 * 5. 日志记录
 *
 * 核心原理：
 * - 基于动态代理生成HTTP客户端
 * - 扫描@FeignClient注解的接口
 * - 通过Ribbon从Eureka获取服务实例
 * - 使用HTTP客户端（默认HttpURLConnection）发送请求
 *
 * 优势：
 * 1. 简化HTTP调用：不需要手动构建HttpClient
 * 2. 集成服务发现：自动从注册中心获取地址
 * 3. 负载均衡：自动在多个实例间分配请求
 * 4. 容错机制：支持重试和降级
 */
@EnableFeignClients
@SpringBootApplication
public class OpenFeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenFeignApplication.class, args);
    }

    /**
     * Feign日志配置
     * NONE: 不记录
     * BASIC: 记录请求方法、URL、响应状态码、执行时间
     * HEADERS: BASIC + 请求和响应头
     * FULL: HEADERS + 请求和响应体
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 请求超时配置
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            5000,    // 连接超时（毫秒）
            10000    // 读取超时（毫秒）
        );
    }

    /**
     * 重试配置
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
            100,     // 初始重试间隔（毫秒）
            1000,    // 最大重试间隔（毫秒）
            3        // 最大重试次数
        );
    }
}

/**
 * 用户服务Feign客户端
 *
 * @FeignClient 参数说明：
 * - name/value: 服务名（从Eureka获取）
 * - url: 直接指定URL（不使用服务发现）
 * - fallback: 降级处理类
 * - fallbackFactory: 降级工厂类（可获取异常信息）
 * - configuration: 自定义配置类
 */
@FeignClient(
    name = "user-service",
    fallback = UserServiceFallback.class,
    configuration = UserServiceConfig.class
)
interface UserServiceClient {

    /**
     * 根据ID查询用户
     *
     * 实际请求：GET http://user-service/users/{id}
     */
    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    /**
     * 查询所有用户
     */
    @GetMapping("/users")
    List<UserDTO> getAllUsers();

    /**
     * 创建用户
     *
     * 实际请求：POST http://user-service/users
     * Content-Type: application/json
     */
    @PostMapping("/users")
    UserDTO createUser(@RequestBody UserDTO user);

    /**
     * 更新用户
     */
    @PutMapping("/users/{id}")
    UserDTO updateUser(@PathVariable("id") Long id, @RequestBody UserDTO user);

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable("id") Long id);

    /**
     * 带请求头的查询
     */
    @GetMapping("/users/search")
    List<UserDTO> searchUsers(
        @RequestParam("keyword") String keyword,
        @RequestHeader("Authorization") String token
    );
}

/**
 * 用户DTO
 */
class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Integer age;

    // constructors, getters, setters
    public UserDTO() {}

    public UserDTO(Long id, String username, String email, Integer age) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.age = age;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}

/**
 * Feign客户端自定义配置
 */
class UserServiceConfig {
    // 可配置：日志级别、超时、重试等
}
