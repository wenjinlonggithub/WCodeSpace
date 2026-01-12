package com.architecture.ddd.infrastructure;

import com.architecture.ddd.domain.model.Money;
import com.architecture.ddd.domain.model.Product;
import com.architecture.ddd.domain.model.ProductId;
import com.architecture.ddd.domain.repository.ProductRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DDD - 商品仓储实现（基础设施层）
 */
public class InMemoryProductRepository implements ProductRepository {

    private final Map<ProductId, Product> storage = new ConcurrentHashMap<>();

    public InMemoryProductRepository() {
        // 初始化测试数据
        initTestData();
    }

    private void initTestData() {
        Product product1 = Product.create(
            ProductId.of("P001"),
            "iPhone 15 Pro",
            new Money("7999.00")
        );
        product1.setDescription("Apple iPhone 15 Pro 256GB");

        Product product2 = Product.create(
            ProductId.of("P002"),
            "MacBook Pro",
            new Money("12999.00")
        );
        product2.setDescription("Apple MacBook Pro 14寸");

        Product product3 = Product.create(
            ProductId.of("P003"),
            "AirPods Pro",
            new Money("1999.00")
        );
        product3.setDescription("Apple AirPods Pro 第二代");

        storage.put(product1.getId(), product1);
        storage.put(product2.getId(), product2);
        storage.put(product3.getId(), product3);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return Optional.ofNullable(storage.get(id));
    }
}
