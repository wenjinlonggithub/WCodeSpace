package com.architecture.inventory.domain;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存实体类
 *
 * 数据库表: inventory
 *
 * 字段说明:
 * - id: 库存ID
 * - productId: 商品ID
 * - quantity: 库存数量
 * - lockedQuantity: 锁定数量（预占库存）
 * - availableQuantity: 可用库存 = quantity - lockedQuantity
 * - version: 乐观锁版本号
 * - createdAt: 创建时间
 * - updatedAt: 更新时间
 *
 * 并发控制方案:
 * 1. 乐观锁: 使用version字段，更新时WHERE version = oldVersion
 * 2. 悲观锁: SELECT ... FOR UPDATE
 * 3. 分布式锁: Redis SETNX
 *
 * @author Architecture Team
 */
@Data
public class Inventory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 库存ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 总库存数量
     */
    private Integer quantity;

    /**
     * 锁定数量（待支付订单占用）
     */
    private Integer lockedQuantity;

    /**
     * 可用库存 = quantity - lockedQuantity
     */
    private Integer availableQuantity;

    /**
     * 乐观锁版本号
     * 每次更新+1，用于防止并发更新冲突
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 计算可用库存
     */
    public void calculateAvailableQuantity() {
        this.availableQuantity = this.quantity - this.lockedQuantity;
    }
}
