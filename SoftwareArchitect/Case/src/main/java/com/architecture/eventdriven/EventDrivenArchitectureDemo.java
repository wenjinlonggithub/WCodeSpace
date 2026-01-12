package com.architecture.eventdriven;

import java.math.BigDecimal;

/**
 * 事件驱动架构演示
 *
 * 核心组件:
 * 1. 事件(Event): OrderCreatedEvent, OrderPaidEvent
 * 2. 事件总线(EventBus): 发布订阅中心
 * 3. 事件生产者(Publisher): OrderService
 * 4. 事件消费者(Subscriber): EmailService, InventoryService, ShippingService
 *
 * 特点:
 * - 松耦合: 生产者和消费者通过事件解耦
 * - 异步处理: 事件消费者异步处理业务
 * - 易扩展: 可以随时添加新的事件消费者
 * - 业务流程清晰: 通过事件串联业务流程
 */
public class EventDrivenArchitectureDemo {

    public static void main(String[] args) {
        System.out.println("========== 事件驱动架构演示 ==========\n");

        // 1. 创建事件总线
        EventBus eventBus = new EventBus();

        // 2. 创建服务
        OrderService orderService = new OrderService(eventBus);
        EmailService emailService = new EmailService();
        InventoryService inventoryService = new InventoryService();
        ShippingService shippingService = new ShippingService();

        // 3. 订阅事件
        System.out.println("【初始化: 订阅事件】");
        System.out.println("=".repeat(50));

        // 订阅订单创建事件
        eventBus.subscribe(OrderCreatedEvent.class, emailService::sendOrderConfirmation);
        eventBus.subscribe(OrderCreatedEvent.class, inventoryService::reserveStock);

        // 订阅订单支付事件
        eventBus.subscribe(OrderPaidEvent.class, emailService::sendPaymentConfirmation);
        eventBus.subscribe(OrderPaidEvent.class, inventoryService::deductStock);
        eventBus.subscribe(OrderPaidEvent.class, shippingService::createShipment);

        System.out.println();

        // 4. 业务场景演示
        System.out.println("\n【场景1: 创建订单】");
        System.out.println("=".repeat(50));
        String orderId1 = orderService.createOrder("U001", new BigDecimal("999.00"));

        System.out.println("\n【场景2: 支付订单】");
        System.out.println("=".repeat(50));
        orderService.payOrder(orderId1, new BigDecimal("999.00"));

        System.out.println("\n【场景3: 再创建一个订单】");
        System.out.println("=".repeat(50));
        String orderId2 = orderService.createOrder("U002", new BigDecimal("1599.00"));

        System.out.println("\n【场景4: 支付第二个订单】");
        System.out.println("=".repeat(50));
        orderService.payOrder(orderId2, new BigDecimal("1599.00"));

        System.out.println("\n\n========== 演示完成 ==========");
        printArchitectureSummary();
    }

    private static void printArchitectureSummary() {
        System.out.println("\n========== 事件驱动架构总结 ==========");
        System.out.println("【核心组件】");
        System.out.println("1. 事件(Event): 表示系统中发生的重要业务事件");
        System.out.println("   - OrderCreatedEvent: 订单创建事件");
        System.out.println("   - OrderPaidEvent: 订单支付事件");
        System.out.println();
        System.out.println("2. 事件总线(EventBus): 发布订阅的中心");
        System.out.println("   - publish(): 发布事件");
        System.out.println("   - subscribe(): 订阅事件");
        System.out.println();
        System.out.println("3. 事件生产者(Publisher): 产生事件的服务");
        System.out.println("   - OrderService: 创建订单后发布事件");
        System.out.println();
        System.out.println("4. 事件消费者(Subscriber): 处理事件的服务");
        System.out.println("   - EmailService: 发送邮件通知");
        System.out.println("   - InventoryService: 处理库存");
        System.out.println("   - ShippingService: 创建物流单");
        System.out.println();
        System.out.println("【架构特点】");
        System.out.println("✓ 松耦合: 生产者和消费者通过事件解耦");
        System.out.println("✓ 异步处理: 提高系统响应速度");
        System.out.println("✓ 易扩展: 可以随时添加新的事件监听器");
        System.out.println("✓ 流程清晰: 业务流程通过事件串联");
        System.out.println("✓ 故障隔离: 某个消费者失败不影响其他消费者");
        System.out.println();
        System.out.println("【事件流程】");
        System.out.println("1. 订单创建");
        System.out.println("   OrderService → OrderCreatedEvent");
        System.out.println("   ├─> EmailService: 发送确认邮件");
        System.out.println("   └─> InventoryService: 预留库存");
        System.out.println();
        System.out.println("2. 订单支付");
        System.out.println("   OrderService → OrderPaidEvent");
        System.out.println("   ├─> EmailService: 发送支付成功邮件");
        System.out.println("   ├─> InventoryService: 扣减库存");
        System.out.println("   └─> ShippingService: 创建物流单");
        System.out.println();
        System.out.println("【适用场景】");
        System.out.println("✓ 高并发系统(秒杀、抢票)");
        System.out.println("✓ 实时通知(消息推送)");
        System.out.println("✓ 复杂业务流程(订单处理)");
        System.out.println("✓ 数据同步(跨系统)");
        System.out.println("✓ 审计日志(操作记录)");
        System.out.println("=====================================");
    }
}
