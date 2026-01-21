package com.architecture.payment.dto;

import com.architecture.payment.domain.Payment;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付DTO - 数据传输对象
 *
 * @author Architecture Team
 */
@Data
public class PaymentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String paymentNo;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String payMethod;
    private Payment.PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime payTime;
}
