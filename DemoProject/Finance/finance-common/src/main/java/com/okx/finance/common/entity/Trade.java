package com.okx.finance.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Trade extends BaseEntity {
    private String tradeId;
    private String orderId;
    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
    private String side;
    private BigDecimal fee;
    private String feeCurrency;
}
