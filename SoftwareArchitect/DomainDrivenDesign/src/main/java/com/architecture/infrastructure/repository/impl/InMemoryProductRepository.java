package com.architecture.infrastructure.repository.impl;

import com.architecture.domain.entity.Product;
import com.architecture.domain.entity.ProductCategory;
import com.architecture.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryProductRepository implements ProductRepository {
    
    private final Map<Long, Product> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Long nextId() {
        return idGenerator.getAndIncrement();
    }

    @Override
    public void save(Product product) {
        storage.put(product.getId(), product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Product> findByCategory(ProductCategory category) {
        return storage.values().stream()
            .filter(product -> product.getCategory() == category)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }
}
