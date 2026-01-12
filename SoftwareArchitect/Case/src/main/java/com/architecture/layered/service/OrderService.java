package com.architecture.layered.service;

import com.architecture.layered.entity.Order;
import com.architecture.layered.entity.OrderStatus;
import com.architecture.layered.entity.Product;
import com.architecture.layered.repository.OrderRepository;
import com.architecture.layered.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务逻辑层 - 订单服务
 * 职责: 实现业务规则、协调多个仓储、处理事务
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                       ProductRepository productRepository,
                       InventoryService inventoryService,
                       NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
    }

    /**
     * 创建订单
     * 事务方法: 订单创建、库存扣减、通知发送要在同一事务中
     */
    public Order createOrder(Long userId, Long productId, Integer quantity, String shippingAddress) {
        // 1. 获取商品信息
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        // 2. 检查库存
        if (!inventoryService.checkStock(productId, quantity)) {
            throw new IllegalStateException("库存不足");
        }

        // 3. 计算金额
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));

        // 4. 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setQuantity(quantity);
        order.setUnitPrice(product.getPrice());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setCreatedAt(LocalDateTime.now());

        // 5. 保存订单
        Order savedOrder = orderRepository.save(order);

        // 6. 扣减库存
        inventoryService.reduceStock(productId, quantity);

        // 7. 发送通知
        notificationService.sendOrderConfirmation(userId, savedOrder.getId());

        return savedOrder;
    }

    /**
     * 获取订单详情
     */
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    /**
     * 获取用户订单列表
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * 取消订单
     */
    public void cancelOrder(Long orderId) {
        // 1. 获取订单
        Order order = getOrderById(orderId);

        // 2. 业务规则: 只有待支付状态可以取消
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("订单状态不允许取消");
        }

        // 3. 更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.update(order);

        // 4. 恢复库存
        inventoryService.restoreStock(order.getProductId(), order.getQuantity());

        // 5. 发送取消通知
        notificationService.sendOrderCancellation(order.getUserId(), orderId);
    }

    /**
     * 订单支付
     */
    public void payOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("订单状态不允许支付");
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.update(order);

        // 通知发货
        notificationService.sendShippingNotification(order.getId());
    }
}
