package com.architecture.product.domain;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * 数据库表: products
 *
 * 字段说明:
 * - id: 商品ID
 * - name: 商品名称
 * - description: 商品描述
 * - price: 商品价格
 * - category: 商品分类
 * - brand: 品牌
 * - status: 状态(0-下架,1-上架)
 * - createdAt: 创建时间
 * - updatedAt: 更新时间
 *
 * @author Architecture Team
 */
@Data
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品分类
     */
    private String category;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 商品图片URL
     */
    private String imageUrl;

    /**
     * 状态: 0-下架, 1-上架
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
