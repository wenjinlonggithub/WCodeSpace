package com.distributed.transaction.tcc;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单服务 TCC 参与者
 * 实现订单创建的 TCC 模式
 */
public class OrderServiceTCCParticipant implements TCCParticipant {

    private final String name = "OrderService";

    // 模拟订单数据库
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    // 记录已处理的事务（用于幂等性）
    private final Set<String> processedTransactions = ConcurrentHashMap.newKeySet();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean tryExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (orders.containsKey(transactionId)) {
                System.out.println("  [" + name + "] Try 阶段已执行，跳过（幂等性）");
                return true;
            }

            System.out.println("  [" + name + "] Try 阶段：创建订单（待确认状态）");

            // 创建订单
            Order order = new Order();
            order.setOrderId(transactionId);
            order.setUserId(1L);
            order.setProductId(1001L);
            order.setQuantity(5);
            order.setStatus("PENDING"); // 待确认状态

            orders.put(transactionId, order);

            System.out.println("  [" + name + "] 订单创建成功（待确认）: " + order);

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Try 阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean confirmExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (processedTransactions.contains(transactionId + ":CONFIRM")) {
                System.out.println("  [" + name + "] Confirm 阶段已执行，跳过（幂等性）");
                return true;
            }

            System.out.println("  [" + name + "] Confirm 阶段：确认订单");

            // 获取订单
            Order order = orders.get(transactionId);
            if (order == null) {
                System.err.println("  [" + name + "] 未找到订单");
                return false;
            }

            // 更新订单状态为已确认
            order.setStatus("CONFIRMED");

            System.out.println("  [" + name + "] 订单确认成功: " + order);

            // 记录已处理（幂等性）
            processedTransactions.add(transactionId + ":CONFIRM");

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Confirm 阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean cancelExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (processedTransactions.contains(transactionId + ":CANCEL")) {
                System.out.println("  [" + name + "] Cancel 阶段已执行，跳过（幂等性）");
                return true;
            }

            System.out.println("  [" + name + "] Cancel 阶段：取消订单");

            // 获取订单
            Order order = orders.get(transactionId);
            if (order == null) {
                System.out.println("  [" + name + "] 未找到订单，无需取消");
                return true;
            }

            // 更新订单状态为已取消
            order.setStatus("CANCELLED");

            System.out.println("  [" + name + "] 订单取消成功: orderId=" + transactionId);

            // 记录已处理（幂等性）
            processedTransactions.add(transactionId + ":CANCEL");

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Cancel 阶段异常: " + e.getMessage());
            return false;
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
