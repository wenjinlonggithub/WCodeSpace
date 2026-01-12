package com.architecture.ddd.application;

import com.architecture.ddd.domain.event.DomainEvent;
import com.architecture.ddd.domain.model.*;
import com.architecture.ddd.domain.repository.OrderRepository;
import com.architecture.ddd.domain.repository.ProductRepository;
import com.architecture.ddd.domain.service.PricingService;

/**
 * DDD - 应用服务
 *
 * 应用服务职责:
 * 1. 协调领域对象完成用例
 * 2. 不包含业务逻辑
 * 3. 处理事务
 * 4. 发布领域事件
 */
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PricingService pricingService;

    public OrderApplicationService(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        PricingService pricingService
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.pricingService = pricingService;
    }

    /**
     * 创建订单用例
     */
    public OrderId createOrder(CreateOrderCommand command) {
        // 1. 创建订单聚合
        Order order = Order.create(
            command.getCustomerId(),
            command.getShippingAddress()
        );

        // 2. 添加订单项
        for (CreateOrderCommand.OrderItemData itemData : command.getItems()) {
            Product product = productRepository.findById(itemData.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("商品不存在: " + itemData.getProductId()));

            order.addItem(product, itemData.getQuantity());
        }

        // 3. 提交订单（触发业务规则和领域事件）
        order.submit();

        // 4. 持久化
        orderRepository.save(order);

        // 5. 发布领域事件
        publishDomainEvents(order);

        return order.getId();
    }

    /**
     * 支付订单用例
     */
    public void payOrder(OrderId orderId, Money paidAmount) {
        // 1. 加载聚合
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        // 2. 执行业务逻辑（聚合内部）
        order.confirmPayment(paidAmount);

        // 3. 持久化
        orderRepository.save(order);

        // 4. 发布领域事件
        publishDomainEvents(order);
    }

    /**
     * 取消订单用例
     */
    public void cancelOrder(OrderId orderId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        order.cancel(reason);
        orderRepository.save(order);
        publishDomainEvents(order);
    }

    /**
     * 发布领域事件
     */
    private void publishDomainEvents(Order order) {
        for (DomainEvent event : order.getDomainEvents()) {
            System.out.println("[EventPublisher] 发布事件: " + event);
            // 实际应该发布到消息队列
        }
        order.clearDomainEvents();
    }
}
