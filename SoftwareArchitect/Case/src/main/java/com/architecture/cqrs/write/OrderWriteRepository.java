package com.architecture.cqrs.write;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CQRS - 写仓储
 */
public class OrderWriteRepository {

    private final Map<String, OrderWriteModel> storage = new ConcurrentHashMap<>();

    public void save(OrderWriteModel model) {
        storage.put(model.getId(), model);
    }

    public OrderWriteModel findById(String id) {
        return storage.get(id);
    }
}
