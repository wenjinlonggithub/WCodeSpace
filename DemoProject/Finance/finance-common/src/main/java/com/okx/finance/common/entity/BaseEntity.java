package com.okx.finance.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 实体基类
 * 所有数据库实体类的父类，包含通用字段
 *
 * @author OKX Finance Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
public class BaseEntity implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 使用雪花算法生成的全局唯一ID
     */
    private Long id;

    /**
     * 创建时间
     * 记录数据首次创建的时间，由数据库自动生成
     */
    private Date createTime;

    /**
     * 更新时间
     * 记录数据最后一次更新的时间，由数据库自动更新
     */
    private Date updateTime;

    /**
     * 删除标记
     * 0-未删除，1-已删除（软删除）
     * 默认值为0，采用软删除机制保证数据安全
     */
    private Integer deleted = 0;
}
