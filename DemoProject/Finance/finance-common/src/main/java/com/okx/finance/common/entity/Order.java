package com.okx.finance.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {
    private Long userId;
    private String orderId;
    private String symbol;
    private String orderType;
    private String side;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal executedQuantity;
    private BigDecimal executedAmount;
    private String status;
    private String timeInForce;
}
