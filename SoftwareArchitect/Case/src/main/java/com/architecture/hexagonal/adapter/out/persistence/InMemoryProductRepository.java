package com.architecture.hexagonal.adapter.out.persistence;

import com.architecture.hexagonal.application.port.out.ProductRepository;
import com.architecture.hexagonal.domain.Money;
import com.architecture.hexagonal.domain.Product;
import com.architecture.hexagonal.domain.ProductId;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 次适配器 - 内存商品仓储实现
 */
public class InMemoryProductRepository implements ProductRepository {

    private final Map<ProductId, Product> storage = new ConcurrentHashMap<>();

    public InMemoryProductRepository() {
        // 初始化测试数据
        storage.put(
            new ProductId("P001"),
            new Product(
                new ProductId("P001"),
                "iPhone 15 Pro",
                new Money(new BigDecimal("7999.00"))
            )
        );
        storage.put(
            new ProductId("P002"),
            new Product(
                new ProductId("P002"),
                "MacBook Pro",
                new Money(new BigDecimal("12999.00"))
            )
        );
        storage.put(
            new ProductId("P003"),
            new Product(
                new ProductId("P003"),
                "AirPods Pro",
                new Money(new BigDecimal("1999.00"))
            )
        );
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return Optional.ofNullable(storage.get(id));
    }
}
