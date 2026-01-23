package com.architecture.function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * 业务场景函数示例
 *
 * 展示 Spring Cloud Function 在实际业务中的应用
 */
@Configuration
public class BusinessFunctions {

    /**
     * 场景1: 订单金额计算
     * 输入订单信息，计算总金额（含税）
     */
    @Bean
    public Function<Order, OrderResult> calculateOrder() {
        return order -> {
            double subtotal = order.getPrice() * order.getQuantity();
            double tax = subtotal * 0.1; // 10% 税率
            double total = subtotal + tax;

            return new OrderResult(
                order.getOrderId(),
                subtotal,
                tax,
                total
            );
        };
    }

    /**
     * 场景2: 用户积分计算
     * 根据消费金额计算积分
     */
    @Bean
    public Function<Double, Integer> calculatePoints() {
        return amount -> {
            // 每消费1元获得1积分，满100元额外赠送10积分
            int basePoints = amount.intValue();
            int bonusPoints = (amount.intValue() / 100) * 10;
            return basePoints + bonusPoints;
        };
    }

    /**
     * 场景3: 数据验证
     * 验证邮箱格式
     */
    @Bean
    public Function<String, ValidationResult> validateEmail() {
        return email -> {
            boolean isValid = email != null &&
                            email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
            return new ValidationResult(isValid,
                isValid ? "邮箱格式正确" : "邮箱格式错误");
        };
    }

    // ========== 数据类定义 ==========

    public static class Order {
        private String orderId;
        private double price;
        private int quantity;

        public Order() {}

        public Order(String orderId, double price, int quantity) {
            this.orderId = orderId;
            this.price = price;
            this.quantity = quantity;
        }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class OrderResult {
        private String orderId;
        private double subtotal;
        private double tax;
        private double total;

        public OrderResult() {}

        public OrderResult(String orderId, double subtotal, double tax, double total) {
            this.orderId = orderId;
            this.subtotal = subtotal;
            this.tax = tax;
            this.total = total;
        }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public double getSubtotal() { return subtotal; }
        public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
        public double getTax() { return tax; }
        public void setTax(double tax) { this.tax = tax; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
    }

    public static class ValidationResult {
        private boolean valid;
        private String message;

        public ValidationResult() {}

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
