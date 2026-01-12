package com.architecture.hexagonal.application.service;

import com.architecture.hexagonal.application.port.in.CreateOrderCommand;
import com.architecture.hexagonal.application.port.in.CreateOrderUseCase;
import com.architecture.hexagonal.application.port.out.EventPublisher;
import com.architecture.hexagonal.application.port.out.OrderRepository;
import com.architecture.hexagonal.application.port.out.ProductRepository;
import com.architecture.hexagonal.domain.*;

/**
 * 应用服务 - 创建订单用例实现
 * 协调领域对象和端口,实现用例流程
 */
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    public CreateOrderService(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        EventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderId execute(CreateOrderCommand command) {
        // 1. 获取商品
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("商品不存在: " + command.getProductId()));

        // 2. 创建订单聚合
        Order order = new Order(OrderId.generate(), command.getCustomerId());

        // 3. 添加订单项(领域逻辑)
        order.addItem(product, command.getQuantity());

        // 4. 提交订单(领域逻辑)
        order.submit();

        // 5. 持久化
        orderRepository.save(order);

        // 6. 发布领域事件
        eventPublisher.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();

        return order.getId();
    }
}
