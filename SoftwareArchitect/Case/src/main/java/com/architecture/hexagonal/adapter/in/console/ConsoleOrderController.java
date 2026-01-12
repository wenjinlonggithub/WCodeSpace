package com.architecture.hexagonal.adapter.in.console;

import com.architecture.hexagonal.application.port.in.CreateOrderCommand;
import com.architecture.hexagonal.application.port.in.CreateOrderUseCase;
import com.architecture.hexagonal.application.port.in.GetOrderUseCase;
import com.architecture.hexagonal.domain.*;

/**
 * 主适配器 - 控制台适配器
 * 驱动应用(主动方),调用输入端口
 */
public class ConsoleOrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    public ConsoleOrderController(
        CreateOrderUseCase createOrderUseCase,
        GetOrderUseCase getOrderUseCase
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    /**
     * 处理创建订单请求
     */
    public void handleCreateOrder(String customerId, String productId, int quantity) {
        System.out.println("\n>>> 处理创建订单请求");
        System.out.println("客户ID: " + customerId);
        System.out.println("商品ID: " + productId);
        System.out.println("数量: " + quantity);

        try {
            // 构建命令
            CreateOrderCommand command = new CreateOrderCommand(
                new CustomerId(customerId),
                new ProductId(productId),
                quantity
            );

            // 调用用例
            OrderId orderId = createOrderUseCase.execute(command);

            System.out.println("✓ 订单创建成功: " + orderId);

            // 显示订单详情
            displayOrderDetails(orderId);

        } catch (Exception e) {
            System.out.println("✗ 订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 显示订单详情
     */
    private void displayOrderDetails(OrderId orderId) {
        Order order = getOrderUseCase.execute(orderId);

        System.out.println("\n【订单详情】");
        System.out.println("订单ID: " + order.getId());
        System.out.println("客户ID: " + order.getCustomerId());
        System.out.println("订单状态: " + order.getStatus());
        System.out.println("订单金额: " + order.getTotalAmount());
        System.out.println("创建时间: " + order.getCreatedAt());
        System.out.println("\n【订单明细】");
        order.getItems().forEach(item ->
            System.out.println("  - " + item.getProductName() +
                             " x " + item.getQuantity() +
                             " = " + item.getSubtotal())
        );
    }
}
