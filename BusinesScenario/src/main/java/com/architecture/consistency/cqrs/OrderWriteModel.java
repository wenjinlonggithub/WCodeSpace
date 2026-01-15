package com.architecture.consistency.cqrs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单写模型
 * 用于写操作（命令）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderWriteModel {

    private String orderId;
    private String userId;
    private String productName;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
