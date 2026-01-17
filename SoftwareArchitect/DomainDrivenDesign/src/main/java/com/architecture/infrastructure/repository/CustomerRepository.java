package com.architecture.infrastructure.repository;

import com.architecture.domain.entity.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    Long nextId();
    void save(Customer customer);
    Optional<Customer> findById(Long id);
    List<Customer> findAll();
    void delete(Long id);
}
