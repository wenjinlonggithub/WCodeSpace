package com.architecture.microservices;

import com.architecture.microservices.gateway.ApiGateway;
import com.architecture.microservices.infrastructure.ServiceRegistry;
import com.architecture.microservices.order.OrderService;
import com.architecture.microservices.product.ProductService;
import com.architecture.microservices.user.UserService;

import java.math.BigDecimal;

/**
 * 微服务架构演示
 *
 * 微服务架构 = Microservices Architecture
 *
 * 核心概念:
 * 1. 服务拆分: 按业务能力拆分为独立的服务
 *    - OrderService: 订单服务
 *    - ProductService: 产品服务
 *    - UserService: 用户服务
 *
 * 2. 服务自治: 每个服务独立部署、独立数据库
 *    - 独立的数据存储
 *    - 独立的生命周期
 *    - 独立的技术栈
 *
 * 3. 服务注册与发现:
 *    - ServiceRegistry: 服务注册中心
 *    - 服务启动时注册
 *    - 服务调用时发现
 *
 * 4. API网关:
 *    - 统一入口
 *    - 路由转发
 *    - 负载均衡
 *
 * 5. 服务间通信:
 *    - REST API (同步)
 *    - 消息队列 (异步)
 *
 * 架构流程:
 * 客户端 → API网关 → 服务发现 → 具体服务 → 数据库
 */
public class MicroservicesArchitectureDemo {

    public static void main(String[] args) {
        System.out.println("========== 微服务架构演示 ==========\n");

        // ========== 初始化服务注册中心 ==========
        System.out.println("【步骤1: 初始化服务注册中心】");
        System.out.println("=".repeat(50));
        ServiceRegistry registry = ServiceRegistry.getInstance();
        System.out.println("✓ 服务注册中心已启动\n");

        // ========== 启动各个微服务 ==========
        System.out.println("【步骤2: 启动各个微服务】");
        System.out.println("=".repeat(50));

        // 启动用户服务
        UserService userService = new UserService(registry);
        userService.start();

        // 启动产品服务
        ProductService productService = new ProductService(registry);
        productService.start();

        // 启动订单服务
        OrderService orderService = new OrderService(registry);
        orderService.start();

        System.out.println("✓ 所有服务已启动\n");

        // ========== 初始化API网关 ==========
        System.out.println("【步骤3: 初始化API网关】");
        System.out.println("=".repeat(50));
        ApiGateway gateway = new ApiGateway(registry);
        System.out.println("✓ API网关已启动\n");

        // ========== 场景1: 创建用户(通过API网关) ==========
        System.out.println("\n【场景1: 创建用户】");
        System.out.println("=".repeat(50));

        String createUserRequest = "{\"name\":\"张三\",\"email\":\"zhangsan@example.com\"}";
        String userResponse = gateway.route("/api/users", "POST", createUserRequest);
        System.out.println("响应: " + userResponse);

        // ========== 场景2: 查询用户信息 ==========
        System.out.println("\n\n【场景2: 查询用户信息】");
        System.out.println("=".repeat(50));

        String getUserResponse = gateway.route("/api/users/U001", "GET", null);
        System.out.println("响应: " + getUserResponse);

        // ========== 场景3: 创建产品 ==========
        System.out.println("\n\n【场景3: 创建产品】");
        System.out.println("=".repeat(50));

        String createProductRequest = "{\"name\":\"iPhone 15\",\"price\":7999.00}";
        String productResponse = gateway.route("/api/products", "POST", createProductRequest);
        System.out.println("响应: " + productResponse);

        // ========== 场景4: 查询产品信息 ==========
        System.out.println("\n\n【场景4: 查询产品信息】");
        System.out.println("=".repeat(50));

        String getProductResponse = gateway.route("/api/products/P001", "GET", null);
        System.out.println("响应: " + getProductResponse);

        // ========== 场景5: 创建订单(跨服务调用) ==========
        System.out.println("\n\n【场景5: 创建订单 - 跨服务调用】");
        System.out.println("=".repeat(50));
        System.out.println("订单服务需要调用:");
        System.out.println("  1. 用户服务 - 验证用户");
        System.out.println("  2. 产品服务 - 验证产品和库存");

        String createOrderRequest = "{\"userId\":\"U001\",\"productId\":\"P001\",\"quantity\":2}";
        String orderResponse = gateway.route("/api/orders", "POST", createOrderRequest);
        System.out.println("\n响应: " + orderResponse);

        // ========== 场景6: 查询订单信息 ==========
        System.out.println("\n\n【场景6: 查询订单信息】");
        System.out.println("=".repeat(50));

        String getOrderResponse = gateway.route("/api/orders/O001", "GET", null);
        System.out.println("响应: " + getOrderResponse);

        // ========== 场景7: 查看服务注册信息 ==========
        System.out.println("\n\n【场景7: 查看服务注册信息】");
        System.out.println("=".repeat(50));
        registry.printRegisteredServices();

        // ========== 场景8: 演示服务降级 ==========
        System.out.println("\n\n【场景8: 服务降级演示】");
        System.out.println("=".repeat(50));
        System.out.println("模拟产品服务下线...");
        productService.stop();

        System.out.println("\n尝试查询产品(服务已下线):");
        String fallbackResponse = gateway.route("/api/products/P001", "GET", null);
        System.out.println("响应: " + fallbackResponse);

        System.out.println("\n\n========== 演示完成 ==========");
        printMicroservicesSummary();
    }

    private static void printMicroservicesSummary() {
        System.out.println("\n========== 微服务架构总结 ==========");
        System.out.println("【核心概念】");
        System.out.println("1. 服务拆分");
        System.out.println("   - 按业务能力拆分");
        System.out.println("   - 单一职责原则");
        System.out.println("   - 服务边界清晰");
        System.out.println();
        System.out.println("2. 服务自治");
        System.out.println("   - 独立部署");
        System.out.println("   - 独立数据库");
        System.out.println("   - 独立扩展");
        System.out.println();
        System.out.println("3. 服务注册与发现");
        System.out.println("   - 服务注册中心");
        System.out.println("   - 动态服务发现");
        System.out.println("   - 健康检查");
        System.out.println();
        System.out.println("4. API网关");
        System.out.println("   - 统一入口");
        System.out.println("   - 路由转发");
        System.out.println("   - 负载均衡");
        System.out.println("   - 熔断降级");
        System.out.println();
        System.out.println("【架构优势】");
        System.out.println("✓ 技术异构: 每个服务可以使用不同技术栈");
        System.out.println("✓ 独立部署: 服务可以独立发布,不影响其他服务");
        System.out.println("✓ 弹性扩展: 可以针对性地扩展高负载服务");
        System.out.println("✓ 故障隔离: 单个服务故障不会影响整个系统");
        System.out.println();
        System.out.println("【架构挑战】");
        System.out.println("⚠ 分布式复杂性: 网络延迟、部分故障");
        System.out.println("⚠ 数据一致性: 分布式事务、最终一致性");
        System.out.println("⚠ 服务治理: 服务数量增多,管理复杂");
        System.out.println("⚠ 运维成本: 需要完善的DevOps能力");
        System.out.println();
        System.out.println("【适用场景】");
        System.out.println("✓ 大型复杂系统");
        System.out.println("✓ 团队规模较大");
        System.out.println("✓ 不同模块负载差异大");
        System.out.println("✓ 需要快速迭代和部署");
        System.out.println("=====================================");
    }
}
