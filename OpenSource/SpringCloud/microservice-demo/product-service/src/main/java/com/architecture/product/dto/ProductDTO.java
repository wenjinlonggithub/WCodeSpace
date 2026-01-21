package com.architecture.product.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品DTO - 数据传输对象
 *
 * 用于服务间调用，不包含敏感信息
 *
 * @author Architecture Team
 */
@Data
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String brand;
    private String imageUrl;
    private Integer status;
}
