package com.architecture.ddd.domain.service;

import com.architecture.ddd.domain.model.Money;
import com.architecture.ddd.domain.model.Order;

import java.math.BigDecimal;

/**
 * DDD - 定价领域服务
 *
 * 领域服务特点:
 * 1. 不属于任何实体或值对象的领域逻辑
 * 2. 协调多个聚合
 * 3. 无状态
 */
public class PricingService {

    /**
     * 计算折扣后的价格
     */
    public Money calculateDiscountedPrice(Order order, BigDecimal discountRate) {
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 ||
            discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("折扣率必须在0-1之间");
        }

        Money originalAmount = order.getTotalAmount();
        BigDecimal discountAmount = originalAmount.getAmount().multiply(discountRate);
        return originalAmount.subtract(new Money(discountAmount));
    }

    /**
     * 计算运费
     */
    public Money calculateShippingFee(Order order) {
        Money totalAmount = order.getTotalAmount();

        // 业务规则: 满100免运费，否则10元
        if (totalAmount.isGreaterThan(new Money("100.00"))) {
            return Money.ZERO;
        } else {
            return new Money("10.00");
        }
    }
}
