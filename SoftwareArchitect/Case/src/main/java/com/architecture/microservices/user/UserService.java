package com.architecture.microservices.user;

import com.architecture.microservices.infrastructure.ServiceInstance;
import com.architecture.microservices.infrastructure.ServiceRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户服务
 *
 * 职责:
 * 1. 用户管理
 * 2. 用户认证
 * 3. 用户信息查询
 */
public class UserService {

    private static UserService instance;
    private final ServiceRegistry registry;
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private ServiceInstance serviceInstance;

    public UserService(ServiceRegistry registry) {
        this.registry = registry;
        instance = this;
    }

    public static UserService getInstance() {
        return instance;
    }

    /**
     * 启动服务并注册到注册中心
     */
    public void start() {
        serviceInstance = new ServiceInstance(
            "user-service-001",
            "user-service",
            "localhost",
            8081
        );

        registry.register("user-service", serviceInstance);
        System.out.println("✓ 用户服务已启动: " + serviceInstance.getUrl());
    }

    /**
     * 停止服务并从注册中心注销
     */
    public void stop() {
        if (serviceInstance != null) {
            registry.deregister("user-service", serviceInstance.getInstanceId());
            System.out.println("✓ 用户服务已停止");
        }
    }

    /**
     * 创建用户
     */
    public String createUser(String requestBody) {
        System.out.println("  [UserService] 处理创建用户请求");

        // 简化的JSON解析(实际应用使用Jackson等)
        String userId = "U" + String.format("%03d", idGenerator.getAndIncrement());
        String name = extractValue(requestBody, "name");
        String email = extractValue(requestBody, "email");

        User user = new User(userId, name, email);
        users.put(userId, user);

        System.out.println("  [UserService] 用户创建成功: " + userId);
        return String.format("{\"userId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\"}",
                           userId, name, email);
    }

    /**
     * 查询用户
     */
    public String getUser(String userId) {
        System.out.println("  [UserService] 查询用户: " + userId);

        User user = users.get(userId);
        if (user == null) {
            return "{\"error\":\"用户不存在\"}";
        }

        return String.format("{\"userId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\"}",
                           user.getId(), user.getName(), user.getEmail());
    }

    /**
     * 验证用户是否存在(供其他服务调用)
     */
    public boolean validateUser(String userId) {
        System.out.println("  [UserService] 验证用户: " + userId);
        return users.containsKey(userId);
    }

    private String extractValue(String json, String key) {
        // 简化的JSON解析
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return "";
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    /**
     * 用户实体
     */
    private static class User {
        private final String id;
        private final String name;
        private final String email;

        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}
