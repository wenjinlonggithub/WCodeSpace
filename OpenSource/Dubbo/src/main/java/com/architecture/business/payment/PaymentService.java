package com.architecture.business.payment;

import com.architecture.business.order.PaymentResult;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 支付服务接口
 *
 * 演示第三方服务集成场景
 */
public interface PaymentService {

    /**
     * 处理支付
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param amount 支付金额
     * @param paymentMethod 支付方式（alipay, wechat, credit_card）
     * @return 支付结果
     */
    PaymentResult processPayment(Long orderId, Long userId, BigDecimal amount, String paymentMethod);

    /**
     * 查询支付状态
     * @param transactionId 交易ID
     * @return 支付状态
     */
    PaymentStatus queryPaymentStatus(String transactionId);

    /**
     * 退款
     * @param transactionId 交易ID
     * @param amount 退款金额
     * @return 退款结果
     */
    RefundResult refund(String transactionId, BigDecimal amount);

    /**
     * 查询用户支付记录
     * @param userId 用户ID
     * @return 支付记录列表
     */
    List<PaymentRecord> getPaymentRecords(Long userId);
}

/**
 * 支付状态
 */
class PaymentStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private Integer status; // 0-待支付, 1-支付成功, 2-支付失败, 3-已退款
    private BigDecimal amount;
    private Long paymentTime;

    public PaymentStatus() {
    }

    public PaymentStatus(String transactionId, Integer status, BigDecimal amount) {
        this.transactionId = transactionId;
        this.status = status;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Long paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getStatusText() {
        switch (status) {
            case 0: return "待支付";
            case 1: return "支付成功";
            case 2: return "支付失败";
            case 3: return "已退款";
            default: return "未知";
        }
    }
}

/**
 * 退款结果
 */
class RefundResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean success;
    private String refundId;
    private BigDecimal refundAmount;
    private String message;
    private Long refundTime;

    public RefundResult(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.refundTime = System.currentTimeMillis();
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Long refundTime) {
        this.refundTime = refundTime;
    }

    @Override
    public String toString() {
        return "RefundResult{" +
                "success=" + success +
                ", refundId='" + refundId + '\'' +
                ", refundAmount=" + refundAmount +
                ", message='" + message + '\'' +
                '}';
    }
}

/**
 * 支付记录
 */
class PaymentRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private Integer status;
    private Long createTime;

    public PaymentRecord() {
    }

    public PaymentRecord(String transactionId, Long orderId, Long userId,
                         BigDecimal amount, String paymentMethod) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = 1;
        this.createTime = System.currentTimeMillis();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "PaymentRecord{" +
                "transactionId='" + transactionId + '\'' +
                ", orderId=" + orderId +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", status=" + status +
                '}';
    }
}
