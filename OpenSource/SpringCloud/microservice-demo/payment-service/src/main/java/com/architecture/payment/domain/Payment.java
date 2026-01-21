package com.architecture.payment.domain;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付实体类
 *
 * 数据库表: payments
 *
 * 字段说明:
 * - id: 支付ID
 * - paymentNo: 支付单号（唯一）
 * - orderNo: 订单号
 * - userId: 用户ID
 * - amount: 支付金额
 * - payMethod: 支付方式（ALIPAY, WECHAT, BALANCE）
 * - status: 支付状态
 * - transactionId: 第三方交易流水号
 * - createdAt: 创建时间
 * - payTime: 支付时间
 *
 * @author Architecture Team
 */
@Data
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付ID
     */
    private Long id;

    /**
     * 支付单号（系统生成，唯一）
     */
    private String paymentNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式: ALIPAY-支付宝, WECHAT-微信, BALANCE-余额
     */
    private String payMethod;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 第三方交易流水号
     */
    private String transactionId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 支付状态枚举
     */
    public enum PaymentStatus {
        PENDING("待支付"),
        PAYING("支付中"),
        SUCCESS("支付成功"),
        FAILED("支付失败"),
        REFUNDING("退款中"),
        REFUNDED("已退款");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
