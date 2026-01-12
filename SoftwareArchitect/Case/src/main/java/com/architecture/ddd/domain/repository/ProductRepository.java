package com.architecture.ddd.domain.repository;

import com.architecture.ddd.domain.model.Product;
import com.architecture.ddd.domain.model.ProductId;

import java.util.Optional;

/**
 * DDD - 商品仓储接口
 */
public interface ProductRepository {

    Optional<Product> findById(ProductId id);
}
