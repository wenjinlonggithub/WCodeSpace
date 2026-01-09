package com.architecture.infrastructure.repository;

import com.architecture.domain.entity.Product;
import com.architecture.domain.entity.ProductCategory;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Long nextId();
    void save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findByCategory(ProductCategory category);
    List<Product> findAll();
    void delete(Long id);
}
