package com.architecture.order.client;

import com.architecture.order.dto.PaymentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

/**
 * 支付服务Feign客户端
 *
 * 支付服务职责:
 * 1. 创建支付单
 * 2. 处理支付回调
 * 3. 查询支付状态
 * 4. 退款处理
 *
 * @author architecture
 */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    /**
     * 创建支付单
     *
     * 支付流程:
     * 1. 订单服务创建支付单
     * 2. 返回支付单ID和支付链接
     * 3. 用户完成支付
     * 4. 支付网关回调
     * 5. 更新订单状态
     *
     * @param request 支付请求
     * @return 支付信息
     */
    @PostMapping("/payment/create")
    PaymentDTO createPayment(@RequestBody CreatePaymentRequest request);

    /**
     * 创建支付请求
     */
    class CreatePaymentRequest {
        private Long userId;
        private String orderNo;
        private BigDecimal amount;
        private String paymentMethod; // 支付方式: ALIPAY, WECHAT, BANK_CARD

        public CreatePaymentRequest() {
        }

        public CreatePaymentRequest(Long userId, String orderNo, BigDecimal amount) {
            this.userId = userId;
            this.orderNo = orderNo;
            this.amount = amount;
            this.paymentMethod = "ALIPAY"; // 默认支付宝
        }

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }
}
