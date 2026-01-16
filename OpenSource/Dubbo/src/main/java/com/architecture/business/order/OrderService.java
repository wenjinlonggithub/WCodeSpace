package com.architecture.business.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务接口
 *
 * 演示 Dubbo 服务间调用的场景
 */
public interface OrderService {

    /**
     * 创建订单
     * @param order 订单信息
     * @return 订单ID
     */
    Long createOrder(Order order);

    /**
     * 查询订单
     * @param orderId 订单ID
     * @return 订单信息
     */
    Order getOrderById(Long orderId);

    /**
     * 查询用户订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> getOrdersByUserId(Long userId);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 订单状态
     * @return 是否成功
     */
    Boolean updateOrderStatus(Long orderId, Integer status);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @return 是否成功
     */
    Boolean cancelOrder(Long orderId);

    /**
     * 支付订单
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @return 支付结果
     */
    PaymentResult payOrder(Long orderId, String paymentMethod);
}

/**
 * 订单项实体类
 */
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;

    public OrderItem() {
    }

    public OrderItem(Long productId, String productName, BigDecimal price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

/**
 * 支付结果
 */
public class PaymentResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean success;
    private String transactionId;
    private String message;
    private Long paymentTime;

    public PaymentResult(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.paymentTime = System.currentTimeMillis();
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Long paymentTime) {
        this.paymentTime = paymentTime;
    }

    @Override
    public String toString() {
        return "PaymentResult{" +
                "success=" + success +
                ", transactionId='" + transactionId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
