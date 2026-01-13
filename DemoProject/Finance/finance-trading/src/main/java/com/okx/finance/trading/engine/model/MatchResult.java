package com.okx.finance.trading.engine.model;

import com.okx.finance.common.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 撮合结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    // 主动单（新订单）
    private Order takerOrder;

    // 被动单（挂单）
    private Order makerOrder;

    // 成交价格
    private BigDecimal matchPrice;

    // 成交数量
    private BigDecimal matchQuantity;

    // 成交金额
    private BigDecimal matchAmount;

    // 成交时间
    private Long matchTime;

    // 交易对
    private String symbol;

    public MatchResult(Order takerOrder, Order makerOrder, BigDecimal matchPrice, BigDecimal matchQuantity) {
        this.takerOrder = takerOrder;
        this.makerOrder = makerOrder;
        this.matchPrice = matchPrice;
        this.matchQuantity = matchQuantity;
        this.matchAmount = matchPrice.multiply(matchQuantity);
        this.matchTime = System.currentTimeMillis();
        this.symbol = takerOrder.getSymbol();
    }
}
