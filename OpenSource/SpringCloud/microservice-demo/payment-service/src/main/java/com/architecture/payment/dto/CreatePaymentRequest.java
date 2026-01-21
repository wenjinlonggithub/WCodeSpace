package com.architecture.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建支付单请求
 *
 * @author Architecture Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式: ALIPAY, WECHAT, BALANCE
     */
    private String payMethod;
}
