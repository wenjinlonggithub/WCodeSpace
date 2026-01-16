package com.architecture.business.demo;

import com.architecture.business.order.*;
import com.architecture.business.payment.*;
import com.architecture.business.user.*;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 业务场景演示
 *
 * 完整演示一个电商订单流程：
 * 1. 用户登录
 * 2. 创建订单
 * 3. 支付订单
 * 4. 查询订单状态
 *
 * 展示 Dubbo 服务间调用的完整流程
 */
public class BusinessDemo {

    public static void main(String[] args) {
        System.out.println("========== Dubbo 业务场景演示 ==========\n");

        // 创建服务实例（实际环境中通过 Dubbo 注入）
        UserService userService = new UserServiceImpl();
        OrderService orderService = new OrderServiceImpl();
        PaymentService paymentService = new PaymentServiceImpl();

        // 设置服务依赖
        ((OrderServiceImpl) orderService).setUserService(userService);
        ((OrderServiceImpl) orderService).setPaymentService(paymentService);

        try {
            // 场景1：用户登录
            System.out.println("【场景1】用户登录");
            System.out.println("----------------------------------------");
            String token = userService.login("alice", "123456");
            System.out.println("登录成功，Token: " + token);

            Thread.sleep(500);

            // 场景2：查询用户信息
            System.out.println("\n【场景2】查询用户信息");
            System.out.println("----------------------------------------");
            User user = userService.getUserById(1L);
            System.out.println("用户信息: " + user);

            Thread.sleep(500);

            // 场景3：创建订单
            System.out.println("\n【场景3】创建订单");
            System.out.println("----------------------------------------");
            Order order = new Order(1L, Arrays.asList(
                new OrderItem(201L, "MacBook Pro", new BigDecimal("18999"), 1),
                new OrderItem(202L, "Magic Mouse", new BigDecimal("799"), 1)
            ));
            order.setShippingAddress("北京市朝阳区xxx街道xxx号");

            Long orderId = orderService.createOrder(order);
            System.out.println("订单创建成功，订单ID: " + orderId);

            Thread.sleep(500);

            // 场景4：查询订单详情
            System.out.println("\n【场景4】查询订单详情");
            System.out.println("----------------------------------------");
            Order createdOrder = orderService.getOrderById(orderId);
            System.out.println("订单信息: " + createdOrder);
            System.out.println("订单金额: " + createdOrder.getTotalAmount());
            System.out.println("订单状态: " + getOrderStatusText(createdOrder.getStatus()));

            Thread.sleep(500);

            // 场景5：支付订单
            System.out.println("\n【场景5】支付订单");
            System.out.println("----------------------------------------");
            PaymentResult paymentResult = orderService.payOrder(orderId, "alipay");
            System.out.println("支付结果: " + paymentResult);

            if (paymentResult.getSuccess()) {
                System.out.println("支付成功！交易号: " + paymentResult.getTransactionId());
            } else {
                System.out.println("支付失败: " + paymentResult.getMessage());
            }

            Thread.sleep(500);

            // 场景6：再次查询订单状态
            System.out.println("\n【场景6】再次查询订单状态");
            System.out.println("----------------------------------------");
            createdOrder = orderService.getOrderById(orderId);
            System.out.println("订单状态: " + getOrderStatusText(createdOrder.getStatus()));

            Thread.sleep(500);

            // 场景7：查询用户所有订单
            System.out.println("\n【场景7】查询用户所有订单");
            System.out.println("----------------------------------------");
            var userOrders = orderService.getOrdersByUserId(1L);
            System.out.println("用户订单数量: " + userOrders.size());
            userOrders.forEach(o ->
                System.out.println("  - 订单" + o.getOrderId() + ": " +
                                 o.getTotalAmount() + "元, " +
                                 getOrderStatusText(o.getStatus()))
            );

            Thread.sleep(500);

            // 场景8：查询支付记录
            System.out.println("\n【场景8】查询支付记录");
            System.out.println("----------------------------------------");
            var paymentRecords = paymentService.getPaymentRecords(1L);
            System.out.println("支付记录数量: " + paymentRecords.size());
            /**paymentRecords.forEach(record ->
                System.out.println("  - " + record)
            );*/

            // 场景9：测试异常情况 - 创建不存在用户的订单
            System.out.println("\n【场景9】异常场景测试");
            System.out.println("----------------------------------------");
            try {
                Order invalidOrder = new Order(999L, Arrays.asList(
                    new OrderItem(301L, "Test Product", new BigDecimal("100"), 1)
                ));
                orderService.createOrder(invalidOrder);
            } catch (Exception e) {
                System.out.println("预期的异常: " + e.getMessage());
            }

            // 场景10：用户登出
            System.out.println("\n【场景10】用户登出");
            System.out.println("----------------------------------------");
            boolean logoutResult = userService.logout(token);
            System.out.println("登出结果: " + (logoutResult ? "成功" : "失败"));

            System.out.println("\n========== 演示完成 ==========");

        } catch (Exception e) {
            System.err.println("演示过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getOrderStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已取消";
            case 3: return "已完成";
            default: return "未知状态";
        }
    }
}

/**
 * Dubbo 服务调用链路说明：
 *
 * 1. 用户登录流程：
 *    Consumer -> UserService.login()
 *    └─> Provider: UserServiceImpl.login()
 *
 * 2. 创建订单流程：
 *    Consumer -> OrderService.createOrder()
 *    └─> Provider: OrderServiceImpl.createOrder()
 *        └─> Dubbo调用: UserService.getUserById() (验证用户)
 *
 * 3. 支付订单流程：
 *    Consumer -> OrderService.payOrder()
 *    └─> Provider: OrderServiceImpl.payOrder()
 *        └─> Dubbo调用: PaymentService.processPayment()
 *            └─> Provider: PaymentServiceImpl.processPayment()
 *                └─> 调用第三方支付接口
 *
 * 4. 服务依赖关系：
 *    OrderService -> UserService (用户验证)
 *    OrderService -> PaymentService (支付处理)
 *
 * 5. 关键技术点：
 *    - 服务注册与发现
 *    - 远程方法调用
 *    - 负载均衡
 *    - 服务降级和容错
 *    - 分布式事务（补偿机制）
 *    - 异常传播和处理
 *
 * 6. 实际部署架构：
 *    ┌─────────────┐
 *    │   Gateway   │
 *    └──────┬──────┘
 *           │
 *    ┌──────┴──────────────────┐
 *    │   Order Service (N)     │
 *    └─────┬────────────┬──────┘
 *          │            │
 *    ┌─────┴─────┐  ┌──┴──────────┐
 *    │User Service│  │Payment Service│
 *    │    (N)     │  │      (N)      │
 *    └────────────┘  └───────────────┘
 *           │              │
 *    ┌──────┴──────────────┴──────┐
 *    │   Registry (Zookeeper)     │
 *    └────────────────────────────┘
 */
