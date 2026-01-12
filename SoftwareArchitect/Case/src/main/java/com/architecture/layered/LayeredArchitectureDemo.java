package com.architecture.layered;

import com.architecture.layered.controller.OrderController;
import com.architecture.layered.dto.CreateOrderRequest;
import com.architecture.layered.dto.OrderDTO;
import com.architecture.layered.repository.OrderRepository;
import com.architecture.layered.repository.ProductRepository;
import com.architecture.layered.service.InventoryService;
import com.architecture.layered.service.NotificationService;
import com.architecture.layered.service.OrderService;

import java.util.List;

/**
 * 分层架构演示
 *
 * 架构层次:
 * 1. 表现层 (Presentation): OrderController - 处理请求和响应
 * 2. 业务逻辑层 (Business Logic): OrderService - 实现业务规则
 * 3. 持久层 (Persistence): OrderRepository - 数据访问
 * 4. 数据层 (Data): 模拟的内存数据库
 *
 * 特点:
 * - 每层职责清晰
 * - 上层依赖下层
 * - 易于理解和维护
 */
public class LayeredArchitectureDemo {

    public static void main(String[] args) {
        System.out.println("========== 分层架构演示 ==========\n");

        // 初始化各层组件
        // 持久层
        OrderRepository orderRepository = new OrderRepository();
        ProductRepository productRepository = new ProductRepository();

        // 业务层
        InventoryService inventoryService = new InventoryService();
        NotificationService notificationService = new NotificationService();
        OrderService orderService = new OrderService(
            orderRepository,
            productRepository,
            inventoryService,
            notificationService
        );

        // 表现层
        OrderController orderController = new OrderController(orderService);

        // 演示1: 创建订单
        System.out.println("1. 创建订单");
        System.out.println("-----------------------------------");
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1001L);
        request.setProductId(1L);
        request.setQuantity(2);
        request.setShippingAddress("北京市朝阳区xx路xx号");

        OrderDTO createdOrder = orderController.createOrder(request);
        System.out.println("订单创建成功: " + createdOrder.getId());
        System.out.println("订单金额: " + createdOrder.getTotalAmount());
        System.out.println("订单状态: " + createdOrder.getStatus());
        System.out.println();

        // 演示2: 查询订单
        System.out.println("2. 查询订单详情");
        System.out.println("-----------------------------------");
        OrderDTO order = orderController.getOrder(createdOrder.getId());
        System.out.println("订单ID: " + order.getId());
        System.out.println("商品名称: " + order.getProductName());
        System.out.println("数量: " + order.getQuantity());
        System.out.println("总金额: " + order.getTotalAmount());
        System.out.println();

        // 演示3: 再创建几个订单
        System.out.println("3. 创建更多订单");
        System.out.println("-----------------------------------");
        for (int i = 0; i < 3; i++) {
            CreateOrderRequest req = new CreateOrderRequest();
            req.setUserId(1001L);
            req.setProductId((long) (i % 3 + 1));
            req.setQuantity(1);
            req.setShippingAddress("测试地址" + i);
            OrderDTO dto = orderController.createOrder(req);
            System.out.println("订单创建: " + dto.getId() + " - " + dto.getProductName());
        }
        System.out.println();

        // 演示4: 查询用户所有订单
        System.out.println("4. 查询用户订单列表");
        System.out.println("-----------------------------------");
        List<OrderDTO> userOrders = orderController.getUserOrders(1001L);
        System.out.println("用户1001的订单数量: " + userOrders.size());
        userOrders.forEach(o ->
            System.out.println("  订单" + o.getId() + ": " + o.getProductName() +
                             " x " + o.getQuantity() + " = ¥" + o.getTotalAmount())
        );
        System.out.println();

        // 演示5: 取消订单
        System.out.println("5. 取消订单");
        System.out.println("-----------------------------------");
        System.out.println("取消前库存: " + inventoryService.getStock(1L));
        orderController.cancelOrder(createdOrder.getId());
        System.out.println("订单已取消");
        System.out.println("取消后库存: " + inventoryService.getStock(1L));
        System.out.println();

        // 演示6: 支付订单
        System.out.println("6. 支付订单");
        System.out.println("-----------------------------------");
        OrderDTO order2 = userOrders.get(1);
        System.out.println("支付前状态: " + order2.getStatus());
        orderService.payOrder(order2.getId());
        OrderDTO paidOrder = orderController.getOrder(order2.getId());
        System.out.println("支付后状态: " + paidOrder.getStatus());
        System.out.println();

        System.out.println("========== 演示完成 ==========");
        printArchitectureSummary();
    }

    private static void printArchitectureSummary() {
        System.out.println("\n========== 分层架构总结 ==========");
        System.out.println("【架构特点】");
        System.out.println("1. 职责分离: 每层负责特定功能");
        System.out.println("   - Controller: 请求处理和响应");
        System.out.println("   - Service: 业务逻辑和规则");
        System.out.println("   - Repository: 数据访问");
        System.out.println();
        System.out.println("2. 单向依赖: 上层依赖下层");
        System.out.println("   Controller → Service → Repository");
        System.out.println();
        System.out.println("3. 易于理解: 结构清晰,便于新人上手");
        System.out.println();
        System.out.println("【适用场景】");
        System.out.println("✓ 中小型Web应用");
        System.out.println("✓ 传统企业应用");
        System.out.println("✓ 业务逻辑中等复杂度");
        System.out.println("✓ 团队熟悉的经典模式");
        System.out.println();
        System.out.println("【注意事项】");
        System.out.println("⚠ 避免Service层过于臃肿");
        System.out.println("⚠ 防止贫血模型(业务逻辑都在Service)");
        System.out.println("⚠ 合理使用DTO进行数据传输");
        System.out.println("=====================================");
    }
}
