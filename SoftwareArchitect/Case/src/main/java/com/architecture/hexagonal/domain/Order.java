package com.architecture.hexagonal.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 领域模型 - 订单聚合根
 * 六边形架构的核心,不依赖任何外部框架
 */
public class Order {

    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 领域事件(未提交)
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();

    public Order(OrderId id, CustomerId customerId) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.totalAmount = Money.ZERO;
        this.status = OrderStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 添加订单项 - 业务逻辑
     */
    public void addItem(Product product, int quantity) {
        if (this.status != OrderStatus.DRAFT) {
            throw new IllegalStateException("只能在草稿状态添加商品");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }

        OrderItem item = new OrderItem(
            product.getId(),
            product.getName(),
            quantity,
            product.getPrice()
        );

        this.items.add(item);
        recalculateTotalAmount();
    }

    /**
     * 提交订单
     */
    public void submit() {
        validateCanSubmit();
        this.status = OrderStatus.SUBMITTED;
        this.updatedAt = LocalDateTime.now();

        // 发布领域事件
        registerEvent(new OrderSubmittedEvent(this.id, this.customerId, this.totalAmount));
    }

    /**
     * 取消订单
     */
    public void cancel(String reason) {
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("订单已取消");
        }
        if (this.status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("已发货订单不能取消");
        }

        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        registerEvent(new OrderCancelledEvent(this.id, reason));
    }

    /**
     * 验证是否可以提交
     */
    private void validateCanSubmit() {
        if (items.isEmpty()) {
            throw new IllegalStateException("订单不能为空");
        }
        if (totalAmount.isZero()) {
            throw new IllegalStateException("订单金额不能为0");
        }
    }

    /**
     * 重新计算总金额
     */
    private void recalculateTotalAmount() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    /**
     * 注册领域事件
     */
    private void registerEvent(DomainEvent event) {
        this.uncommittedEvents.add(event);
    }

    // Getters
    public OrderId getId() {
        return id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }
}
