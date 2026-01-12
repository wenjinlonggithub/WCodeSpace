package com.architecture.ddd.domain.model;

import com.architecture.ddd.domain.event.OrderCreatedEvent;
import com.architecture.ddd.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DDD - 订单聚合根
 *
 * 聚合根特点:
 * 1. 有唯一标识(ID)
 * 2. 封装业务规则
 * 3. 保证聚合内的一致性
 * 4. 对外发布领域事件
 */
public class Order {

    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private Address shippingAddress;
    private LocalDateTime createdAt;

    // 未提交的领域事件
    private List<DomainEvent> domainEvents = new ArrayList<>();

    // 构造函数私有，通过工厂方法创建
    private Order() {
    }

    /**
     * 工厂方法 - 创建订单
     */
    public static Order create(CustomerId customerId, Address shippingAddress) {
        Order order = new Order();
        order.id = OrderId.generate();
        order.customerId = customerId;
        order.items = new ArrayList<>();
        order.totalAmount = Money.ZERO;
        order.status = OrderStatus.DRAFT;
        order.shippingAddress = shippingAddress;
        order.createdAt = LocalDateTime.now();
        return order;
    }

    /**
     * 添加订单项 - 业务规则
     */
    public void addItem(Product product, Quantity quantity) {
        // 规则1: 只能在草稿状态添加商品
        if (!this.status.isDraft()) {
            throw new IllegalStateException("只能在草稿状态添加商品");
        }

        // 规则2: 检查商品是否已存在
        OrderItem existingItem = findItemByProductId(product.getId());
        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
        } else {
            OrderItem item = OrderItem.create(product, quantity);
            this.items.add(item);
        }

        // 重新计算总金额
        recalculateTotalAmount();
    }

    /**
     * 提交订单
     */
    public void submit() {
        // 验证业务规则
        validateCanSubmit();

        // 改变状态
        this.status = OrderStatus.SUBMITTED;

        // 发布领域事件
        registerEvent(new OrderCreatedEvent(
            this.id.getValue(),
            this.customerId.getValue(),
            this.totalAmount.getAmount(),
            this.createdAt
        ));
    }

    /**
     * 确认支付
     */
    public void confirmPayment(Money paidAmount) {
        // 规则: 只有已提交的订单可以支付
        if (!this.status.isSubmitted()) {
            throw new IllegalStateException("订单状态不允许支付");
        }

        // 规则: 支付金额必须等于订单金额
        if (!this.totalAmount.equals(paidAmount)) {
            throw new IllegalArgumentException("支付金额不正确");
        }

        this.status = OrderStatus.PAID;
    }

    /**
     * 取消订单
     */
    public void cancel(String reason) {
        // 规则: 已发货的订单不能取消
        if (this.status.isShipped()) {
            throw new IllegalStateException("已发货订单不能取消");
        }

        this.status = OrderStatus.CANCELLED;
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
        if (shippingAddress == null) {
            throw new IllegalStateException("收货地址不能为空");
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
     * 查找订单项
     */
    private OrderItem findItemByProductId(ProductId productId) {
        return items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
    }

    /**
     * 注册领域事件
     */
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
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

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
