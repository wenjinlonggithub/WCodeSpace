package com.architecture.hexagonal.application.port.out;

import com.architecture.hexagonal.domain.Product;
import com.architecture.hexagonal.domain.ProductId;

import java.util.Optional;

/**
 * 输出端口 - 商品仓储接口
 */
public interface ProductRepository {

    Optional<Product> findById(ProductId id);
}
