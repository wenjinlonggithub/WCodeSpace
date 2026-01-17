package com.architecture.infrastructure.repository.impl;

import com.architecture.domain.entity.Customer;
import com.architecture.infrastructure.repository.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCustomerRepository implements CustomerRepository {
    
    private final Map<Long, Customer> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Long nextId() {
        return idGenerator.getAndIncrement();
    }

    @Override
    public void save(Customer customer) {
        storage.put(customer.getId(), customer);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }
}
