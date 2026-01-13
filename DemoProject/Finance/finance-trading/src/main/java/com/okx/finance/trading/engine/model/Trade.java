package com.okx.finance.trading.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 成交记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private Long id;
    private String tradeId;
    private String symbol;
    private String takerOrderId;
    private String makerOrderId;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String side;
    private Long timestamp;
    private BigDecimal takerFee;
    private BigDecimal makerFee;
}
