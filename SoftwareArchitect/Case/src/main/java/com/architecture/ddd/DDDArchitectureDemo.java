package com.architecture.ddd;

import com.architecture.ddd.application.CreateOrderCommand;
import com.architecture.ddd.application.OrderApplicationService;
import com.architecture.ddd.domain.model.*;
import com.architecture.ddd.domain.repository.OrderRepository;
import com.architecture.ddd.domain.repository.ProductRepository;
import com.architecture.ddd.domain.service.PricingService;
import com.architecture.ddd.infrastructure.InMemoryOrderRepository;
import com.architecture.ddd.infrastructure.InMemoryProductRepository;

import java.util.Arrays;
import java.util.List;

/**
 * DDD架构演示
 *
 * 核心概念:
 * 1. 实体(Entity): Order, Product - 有唯一标识
 * 2. 值对象(Value Object): Money, OrderId, Address - 不可变
 * 3. 聚合(Aggregate): Order聚合根 + OrderItem实体
 * 4. 领域服务(Domain Service): PricingService - 跨聚合逻辑
 * 5. 仓储(Repository): OrderRepository - 持久化
 * 6. 领域事件(Domain Event): OrderCreatedEvent - 业务事件
 * 7. 应用服务(Application Service): 协调用例
 *
 * DDD分层:
 * - 表现层(Presentation): 省略
 * - 应用层(Application): OrderApplicationService
 * - 领域层(Domain): 实体、值对象、聚合、领域服务
 * - 基础设施层(Infrastructure): 仓储实现
 */
public class DDDArchitectureDemo {

    public static void main(String[] args) {
        System.out.println("========== DDD架构演示 ==========\n");

        // 基础设施层
        OrderRepository orderRepository = new InMemoryOrderRepository();
        ProductRepository productRepository = new InMemoryProductRepository();

        // 领域层
        PricingService pricingService = new PricingService();

        // 应用层
        OrderApplicationService orderService = new OrderApplicationService(
            orderRepository,
            productRepository,
            pricingService
        );

        // ========== 场景1: 创建订单 ==========
        System.out.println("【场景1: 创建订单】");
        System.out.println("=".repeat(50));

        // 构建命令
        CustomerId customerId = CustomerId.of("C001");
        Address shippingAddress = Address.of(
            "北京市",
            "朝阳区",
            "望京街道",
            "阜通东大街",
            "xx号楼xx室"
        );

        List<CreateOrderCommand.OrderItemData> items = Arrays.asList(
            new CreateOrderCommand.OrderItemData(
                ProductId.of("P001"),
                Quantity.of(2)
            ),
            new CreateOrderCommand.OrderItemData(
                ProductId.of("P003"),
                Quantity.of(1)
            )
        );

        CreateOrderCommand command = new CreateOrderCommand(
            customerId,
            shippingAddress,
            items
        );

        // 执行用例
        OrderId orderId = orderService.createOrder(command);
        System.out.println("✓ 订单创建成功: " + orderId);

        // 查询订单详情
        Order order = orderRepository.findById(orderId).orElseThrow();
        displayOrderDetail(order);

        // ========== 场景2: 支付订单 ==========
        System.out.println("\n\n【场景2: 支付订单】");
        System.out.println("=".repeat(50));
        System.out.println("订单ID: " + orderId);
        System.out.println("支付前状态: " + order.getStatus());

        orderService.payOrder(orderId, order.getTotalAmount());

        order = orderRepository.findById(orderId).orElseThrow();
        System.out.println("✓ 支付成功");
        System.out.println("支付后状态: " + order.getStatus());

        // ========== 场景3: 再创建一个订单 ==========
        System.out.println("\n\n【场景3: 创建第二个订单】");
        System.out.println("=".repeat(50));

        CreateOrderCommand command2 = new CreateOrderCommand(
            customerId,
            shippingAddress,
            Arrays.asList(
                new CreateOrderCommand.OrderItemData(
                    ProductId.of("P002"),
                    Quantity.of(1)
                )
            )
        );

        OrderId orderId2 = orderService.createOrder(command2);
        Order order2 = orderRepository.findById(orderId2).orElseThrow();
        System.out.println("✓ 订单创建成功: " + orderId2);
        displayOrderDetail(order2);

        // ========== 场景4: 取消订单 ==========
        System.out.println("\n\n【场景4: 取消订单】");
        System.out.println("=".repeat(50));
        System.out.println("取消订单: " + orderId2);

        orderService.cancelOrder(orderId2, "不想要了");

        order2 = orderRepository.findById(orderId2).orElseThrow();
        System.out.println("✓ 订单已取消");
        System.out.println("取消后状态: " + order2.getStatus());

        // ========== 场景5: 错误处理 - 支付金额不正确 ==========
        System.out.println("\n\n【场景5: 错误处理 - 支付金额不正确】");
        System.out.println("=".repeat(50));

        CreateOrderCommand command3 = new CreateOrderCommand(
            customerId,
            shippingAddress,
            Arrays.asList(
                new CreateOrderCommand.OrderItemData(
                    ProductId.of("P003"),
                    Quantity.of(1)
                )
            )
        );

        OrderId orderId3 = orderService.createOrder(command3);
        Order order3 = orderRepository.findById(orderId3).orElseThrow();
        System.out.println("订单总额: " + order3.getTotalAmount());

        try {
            orderService.payOrder(orderId3, new Money("1000.00")); // 错误的金额
        } catch (Exception e) {
            System.out.println("✗ 支付失败: " + e.getMessage());
        }

        System.out.println("\n\n========== 演示完成 ==========");
        printDDDSummary();
    }

    private static void displayOrderDetail(Order order) {
        System.out.println("\n【订单详情】");
        System.out.println("订单ID: " + order.getId());
        System.out.println("客户ID: " + order.getCustomerId());
        System.out.println("订单状态: " + order.getStatus().getDescription());
        System.out.println("收货地址: " + order.getShippingAddress());
        System.out.println("订单总额: " + order.getTotalAmount());
        System.out.println("创建时间: " + order.getCreatedAt());
        System.out.println("\n【订单明细】");
        order.getItems().forEach(item ->
            System.out.println("  - " + item.getProductName() +
                             " x " + item.getQuantity() +
                             " @ " + item.getUnitPrice() +
                             " = " + item.getSubtotal())
        );
    }

    private static void printDDDSummary() {
        System.out.println("\n========== DDD架构总结 ==========");
        System.out.println("【核心概念】");
        System.out.println("1. 实体(Entity)");
        System.out.println("   - 有唯一标识(ID)");
        System.out.println("   - 通过ID判断相等性");
        System.out.println("   - 示例: Order, Product");
        System.out.println();
        System.out.println("2. 值对象(Value Object)");
        System.out.println("   - 没有唯一标识");
        System.out.println("   - 通过属性值判断相等性");
        System.out.println("   - 不可变(Immutable)");
        System.out.println("   - 示例: Money, OrderId, Address");
        System.out.println();
        System.out.println("3. 聚合(Aggregate)");
        System.out.println("   - 一组相关对象的集合");
        System.out.println("   - 有聚合根(Aggregate Root)");
        System.out.println("   - 保证聚合内的一致性");
        System.out.println("   - 示例: Order聚合根 + OrderItem实体");
        System.out.println();
        System.out.println("4. 领域服务(Domain Service)");
        System.out.println("   - 不属于任何实体的业务逻辑");
        System.out.println("   - 协调多个聚合");
        System.out.println("   - 无状态");
        System.out.println("   - 示例: PricingService");
        System.out.println();
        System.out.println("5. 仓储(Repository)");
        System.out.println("   - 封装聚合的持久化");
        System.out.println("   - 接口定义在领域层");
        System.out.println("   - 实现在基础设施层");
        System.out.println("   - 示例: OrderRepository");
        System.out.println();
        System.out.println("6. 领域事件(Domain Event)");
        System.out.println("   - 表示领域中发生的重要事件");
        System.out.println("   - 不可变");
        System.out.println("   - 示例: OrderCreatedEvent");
        System.out.println();
        System.out.println("【DDD分层】");
        System.out.println("用户接口层 (省略)");
        System.out.println("   ↓");
        System.out.println("应用层: OrderApplicationService");
        System.out.println("   - 协调领域对象");
        System.out.println("   - 不包含业务逻辑");
        System.out.println("   ↓");
        System.out.println("领域层: 实体、值对象、聚合、领域服务");
        System.out.println("   - 核心业务逻辑");
        System.out.println("   - 不依赖外部");
        System.out.println("   ↓");
        System.out.println("基础设施层: 仓储实现");
        System.out.println("   - 技术实现");
        System.out.println("   - 依赖倒置");
        System.out.println();
        System.out.println("【架构优势】");
        System.out.println("✓ 业务逻辑清晰: 封装在领域对象中");
        System.out.println("✓ 高度内聚: 相关逻辑聚集在一起");
        System.out.println("✓ 易于测试: 领域逻辑可独立测试");
        System.out.println("✓ 灵活演进: 适应业务变化");
        System.out.println();
        System.out.println("【适用场景】");
        System.out.println("✓ 复杂业务领域");
        System.out.println("✓ 长期维护的项目");
        System.out.println("✓ 业务规则复杂多变");
        System.out.println("✓ 需要领域专家参与");
        System.out.println("=====================================");
    }
}
