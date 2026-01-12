package com.architecture.microservices.gateway;

import com.architecture.microservices.infrastructure.ServiceInstance;
import com.architecture.microservices.infrastructure.ServiceRegistry;
import com.architecture.microservices.order.OrderService;
import com.architecture.microservices.product.ProductService;
import com.architecture.microservices.user.UserService;

/**
 * API网关
 *
 * 职责:
 * 1. 路由转发 - 根据请求路径转发到对应的服务
 * 2. 负载均衡 - 从服务注册中心获取可用实例
 * 3. 熔断降级 - 服务不可用时返回降级响应
 * 4. 统一鉴权 - (简化实现中省略)
 * 5. 限流控制 - (简化实现中省略)
 */
public class ApiGateway {

    private final ServiceRegistry registry;

    public ApiGateway(ServiceRegistry registry) {
        this.registry = registry;
    }

    /**
     * 路由请求
     */
    public String route(String path, String method, String body) {
        System.out.println("\n[Gateway] 收到请求: " + method + " " + path);

        // 路由规则
        if (path.startsWith("/api/users")) {
            return routeToUserService(path, method, body);
        } else if (path.startsWith("/api/products")) {
            return routeToProductService(path, method, body);
        } else if (path.startsWith("/api/orders")) {
            return routeToOrderService(path, method, body);
        } else {
            return "{\"error\":\"路径不存在\"}";
        }
    }

    private String routeToUserService(String path, String method, String body) {
        ServiceInstance instance = registry.discover("user-service");
        if (instance == null) {
            System.out.println("[Gateway] 用户服务不可用,返回降级响应");
            return "{\"error\":\"用户服务暂时不可用\"}";
        }

        System.out.println("[Gateway] 转发到: " + instance.getUrl() + path);

        // 实际应用中这里会发起HTTP请求
        // 这里简化为直接调用服务方法
        UserService userService = UserService.getInstance();
        if (method.equals("POST")) {
            return userService.createUser(body);
        } else if (method.equals("GET")) {
            String userId = extractId(path);
            return userService.getUser(userId);
        }

        return "{\"error\":\"不支持的方法\"}";
    }

    private String routeToProductService(String path, String method, String body) {
        ServiceInstance instance = registry.discover("product-service");
        if (instance == null) {
            System.out.println("[Gateway] 产品服务不可用,返回降级响应");
            return "{\"error\":\"产品服务暂时不可用\",\"data\":{\"name\":\"商品信息暂时无法获取\",\"price\":0}}";
        }

        System.out.println("[Gateway] 转发到: " + instance.getUrl() + path);

        ProductService productService = ProductService.getInstance();
        if (method.equals("POST")) {
            return productService.createProduct(body);
        } else if (method.equals("GET")) {
            String productId = extractId(path);
            return productService.getProduct(productId);
        }

        return "{\"error\":\"不支持的方法\"}";
    }

    private String routeToOrderService(String path, String method, String body) {
        ServiceInstance instance = registry.discover("order-service");
        if (instance == null) {
            System.out.println("[Gateway] 订单服务不可用,返回降级响应");
            return "{\"error\":\"订单服务暂时不可用\"}";
        }

        System.out.println("[Gateway] 转发到: " + instance.getUrl() + path);

        OrderService orderService = OrderService.getInstance();
        if (method.equals("POST")) {
            return orderService.createOrder(body);
        } else if (method.equals("GET")) {
            String orderId = extractId(path);
            return orderService.getOrder(orderId);
        }

        return "{\"error\":\"不支持的方法\"}";
    }

    /**
     * 从路径中提取ID
     * 例如: /api/users/U001 -> U001
     */
    private String extractId(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
