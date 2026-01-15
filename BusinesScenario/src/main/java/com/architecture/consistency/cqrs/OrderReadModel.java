package com.architecture.consistency.cqrs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单读模型
 * 针对查询优化的数据结构，可能包含冗余字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadModel {

    /**
     * 订单基本信息
     */
    private String orderId;
    private String userId;
    private String userName;
    private String productName;
    private BigDecimal amount;
    private String status;
    private String statusDesc;

    /**
     * 扩展信息（冗余字段，方便查询）
     */
    private String userPhone;
    private String userAddress;
    private String productImage;

    /**
     * 时间信息
     */
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 统计信息
     */
    private Integer queryCount;
}
