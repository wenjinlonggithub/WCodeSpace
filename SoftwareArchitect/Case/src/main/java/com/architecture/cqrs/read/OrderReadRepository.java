package com.architecture.cqrs.read;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * CQRS - 读仓储
 */
public class OrderReadRepository {

    private final Map<String, OrderReadModel> storage = new ConcurrentHashMap<>();

    public void save(OrderReadModel model) {
        storage.put(model.getId(), model);
    }

    public Optional<OrderReadModel> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<OrderReadModel> findByUserId(String userId) {
        return storage.values().stream()
            .filter(model -> model.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
}
