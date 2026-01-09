package com.architecture.domain.aggregate;

import com.architecture.domain.entity.Customer;
import com.architecture.domain.entity.Product;
import com.architecture.domain.valueobject.Address;
import com.architecture.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private Long id;
    private Customer customer;
    private List<OrderItem> orderItems;
    private OrderStatus status;
    private Address shippingAddress;
    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;
    private Money totalAmount;

    public Order(Long id, Customer customer, Address shippingAddress) {
        this.id = id;
        this.customer = customer;
        this.shippingAddress = shippingAddress;
        this.orderItems = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
        this.totalAmount = new Money(BigDecimal.ZERO, "CNY");
    }

    public void addOrderItem(Product product, int quantity) {
        if (!product.isAvailable()) {
            throw new IllegalStateException("商品不可用: " + product.getName());
        }
        if (product.getStock() < quantity) {
            throw new IllegalStateException("库存不足: " + product.getName());
        }

        OrderItem orderItem = new OrderItem(product, quantity);
        this.orderItems.add(orderItem);
        
        product.decreaseStock(quantity);
        
        calculateTotalAmount();
    }

    public void removeOrderItem(Product product) {
        orderItems.removeIf(item -> item.getProduct().equals(product));
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        BigDecimal total = orderItems.stream()
            .map(item -> item.getSubTotal().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double discount = customer.getLevel().getDiscount();
        total = total.multiply(BigDecimal.valueOf(discount));
        
        this.totalAmount = new Money(total, "CNY");
    }

    public void confirm() {
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("订单不能为空");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("只有已确认的订单才能发货");
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("只有已发货的订单才能确认收货");
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveryTime = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("已送达的订单不能取消");
        }
        
        for (OrderItem item : orderItems) {
            item.getProduct().increaseStock(item.getQuantity());
        }
        
        this.status = OrderStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<OrderItem> getOrderItems() {
        return new ArrayList<>(orderItems);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}