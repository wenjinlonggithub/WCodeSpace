package com.architecture.consistency.delay_retry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态: CREATING, CREATED, PAID, CANCELLED
     */
    private OrderStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public enum OrderStatus {
        CREATING("创建中"),
        CREATED("已创建"),
        PAID("已支付"),
        CANCELLED("已取消");

        private final String desc;

        OrderStatus(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
