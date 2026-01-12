package com.architecture.layered.repository;

import com.architecture.layered.entity.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商品仓储
 */
public class ProductRepository {

    private final Map<Long, Product> database = new ConcurrentHashMap<>();

    public ProductRepository() {
        // 初始化一些测试数据
        database.put(1L, new Product(1L, "iPhone 15 Pro", new BigDecimal("7999.00"), 100));
        database.put(2L, new Product(2L, "MacBook Pro", new BigDecimal("12999.00"), 50));
        database.put(3L, new Product(3L, "AirPods Pro", new BigDecimal("1999.00"), 200));
    }

    public Product findById(Long id) {
        return database.get(id);
    }

    public List<Product> findAll() {
        return new ArrayList<>(database.values());
    }

    public Product save(Product product) {
        database.put(product.getId(), product);
        return product;
    }
}
