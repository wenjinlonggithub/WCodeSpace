package com.distributed.transaction.twopc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单服务参与者
 * 模拟订单创建操作
 */
public class OrderServiceParticipant implements Participant {

    private final String name = "OrderService";

    // 模拟订单数据库
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    // 模拟待提交的订单（用于准备阶段）
    private final Map<String, Order> pendingOrders = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean prepare(String transactionId) {
        try {
            // 模拟业务逻辑：创建订单（但不提交）
            System.out.println("  [" + name + "] 创建订单...");

            Order order = new Order();
            order.setOrderId(transactionId);
            order.setUserId(1L);
            order.setProductId(1001L);
            order.setQuantity(5);
            order.setStatus("PENDING");

            // 保存到待提交订单
            pendingOrders.put(transactionId, order);
            System.out.println("  [" + name + "] 订单创建成功（待提交）: " + order);

            // 模拟网络延迟
            Thread.sleep(100);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] 准备阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void commit(String transactionId) {
        try {
            Order order = pendingOrders.get(transactionId);
            if (order == null) {
                System.err.println("  [" + name + "] 未找到待提交订单");
                return;
            }

            // 提交订单
            order.setStatus("CONFIRMED");
            orders.put(transactionId, order);

            System.out.println("  [" + name + "] 订单提交成功: " + order);

            // 清除待提交记录
            pendingOrders.remove(transactionId);

            // 模拟网络延迟
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("  [" + name + "] 提交阶段异常: " + e.getMessage());
        }
    }

    @Override
    public void rollback(String transactionId) {
        try {
            Order order = pendingOrders.get(transactionId);
            if (order == null) {
                System.out.println("  [" + name + "] 未找到待提交订单，无需回滚");
                return;
            }

            // 删除待提交订单
            pendingOrders.remove(transactionId);
            System.out.println("  [" + name + "] 订单回滚成功: orderId=" + transactionId);

            // 模拟网络延迟
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("  [" + name + "] 回滚阶段异常: " + e.getMessage());
        }
    }

    /**
     * 获取订单（用于测试）
     */
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    /**
     * 订单实体类
     */
    public static class Order {
        private String orderId;
        private Long userId;
        private Long productId;
        private Integer quantity;
        private String status;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "orderId='" + orderId + '\'' +
                    ", userId=" + userId +
                    ", productId=" + productId +
                    ", quantity=" + quantity +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}
