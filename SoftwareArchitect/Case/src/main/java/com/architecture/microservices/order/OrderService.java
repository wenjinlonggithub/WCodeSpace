package com.architecture.microservices.order;

import com.architecture.microservices.infrastructure.ServiceInstance;
import com.architecture.microservices.infrastructure.ServiceRegistry;
import com.architecture.microservices.product.ProductService;
import com.architecture.microservices.user.UserService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单服务
 *
 * 职责:
 * 1. 订单管理
 * 2. 订单创建(需要调用用户服务和产品服务)
 * 3. 订单查询
 *
 * 依赖:
 * - 用户服务: 验证用户
 * - 产品服务: 验证产品和库存
 */
public class OrderService {

    private static OrderService instance;
    private final ServiceRegistry registry;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private ServiceInstance serviceInstance;

    public OrderService(ServiceRegistry registry) {
        this.registry = registry;
        instance = this;
    }

    public static OrderService getInstance() {
        return instance;
    }

    /**
     * 启动服务并注册到注册中心
     */
    public void start() {
        serviceInstance = new ServiceInstance(
            "order-service-001",
            "order-service",
            "localhost",
            8083
        );

        registry.register("order-service", serviceInstance);
        System.out.println("✓ 订单服务已启动: " + serviceInstance.getUrl());
    }

    /**
     * 停止服务并从注册中心注销
     */
    public void stop() {
        if (serviceInstance != null) {
            registry.deregister("order-service", serviceInstance.getInstanceId());
            System.out.println("✓ 订单服务已停止");
        }
    }

    /**
     * 创建订单(跨服务调用)
     */
    public String createOrder(String requestBody) {
        System.out.println("  [OrderService] 处理创建订单请求");

        String userId = extractValue(requestBody, "userId");
        String productId = extractValue(requestBody, "productId");
        int quantity = Integer.parseInt(extractValue(requestBody, "quantity"));

        // ========== 步骤1: 调用用户服务验证用户 ==========
        System.out.println("\n  → 调用用户服务验证用户");
        UserService userService = UserService.getInstance();
        if (!userService.validateUser(userId)) {
            System.out.println("  ✗ 用户验证失败");
            return "{\"error\":\"用户不存在\"}";
        }
        System.out.println("  ✓ 用户验证通过");

        // ========== 步骤2: 调用产品服务验证库存 ==========
        System.out.println("\n  → 调用产品服务验证库存");
        ProductService productService = ProductService.getInstance();
        if (!productService.checkStock(productId, quantity)) {
            System.out.println("  ✗ 库存不足");
            return "{\"error\":\"库存不足\"}";
        }
        System.out.println("  ✓ 库存验证通过");

        // ========== 步骤3: 获取产品价格 ==========
        System.out.println("\n  → 调用产品服务获取价格");
        BigDecimal price = productService.getPrice(productId);
        BigDecimal totalAmount = price.multiply(new BigDecimal(quantity));
        System.out.println("  ✓ 价格: " + price + ", 总额: " + totalAmount);

        // ========== 步骤4: 扣减库存 ==========
        System.out.println("\n  → 调用产品服务扣减库存");
        productService.reduceStock(productId, quantity);
        System.out.println("  ✓ 库存已扣减");

        // ========== 步骤5: 创建订单 ==========
        System.out.println("\n  → 创建订单记录");
        String orderId = "O" + String.format("%03d", idGenerator.getAndIncrement());
        Order order = new Order(orderId, userId, productId, quantity, totalAmount);
        orders.put(orderId, order);

        System.out.println("  [OrderService] ✓ 订单创建成功: " + orderId);
        return String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"productId\":\"%s\",\"quantity\":%d,\"totalAmount\":%s,\"status\":\"%s\"}",
                           orderId, userId, productId, quantity, totalAmount, "CREATED");
    }

    /**
     * 查询订单
     */
    public String getOrder(String orderId) {
        System.out.println("  [OrderService] 查询订单: " + orderId);

        Order order = orders.get(orderId);
        if (order == null) {
            return "{\"error\":\"订单不存在\"}";
        }

        return String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"productId\":\"%s\",\"quantity\":%d,\"totalAmount\":%s,\"status\":\"%s\"}",
                           order.getId(), order.getUserId(), order.getProductId(),
                           order.getQuantity(), order.getTotalAmount(), order.getStatus());
    }

    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            // 尝试不带引号的值(数字)
            searchKey = "\"" + key + "\":";
            start = json.indexOf(searchKey);
            if (start == -1) return "";
            start += searchKey.length();
            int end = Math.min(
                json.indexOf(",", start) == -1 ? json.length() : json.indexOf(",", start),
                json.indexOf("}", start) == -1 ? json.length() : json.indexOf("}", start)
            );
            return json.substring(start, end).trim();
        }
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    /**
     * 订单实体
     */
    private static class Order {
        private final String id;
        private final String userId;
        private final String productId;
        private final int quantity;
        private final BigDecimal totalAmount;
        private String status = "CREATED";

        public Order(String id, String userId, String productId, int quantity, BigDecimal totalAmount) {
            this.id = id;
            this.userId = userId;
            this.productId = productId;
            this.quantity = quantity;
            this.totalAmount = totalAmount;
        }

        public String getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public String getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public String getStatus() {
            return status;
        }
    }
}
