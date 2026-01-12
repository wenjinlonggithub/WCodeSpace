package com.architecture.layered.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存服务
 */
public class InventoryService {

    // 模拟库存数据
    private final Map<Long, Integer> inventory = new ConcurrentHashMap<>();

    public InventoryService() {
        // 初始化一些商品库存
        inventory.put(1L, 100);
        inventory.put(2L, 50);
        inventory.put(3L, 200);
    }

    /**
     * 检查库存
     */
    public boolean checkStock(Long productId, Integer quantity) {
        Integer currentStock = inventory.getOrDefault(productId, 0);
        return currentStock >= quantity;
    }

    /**
     * 扣减库存
     */
    public void reduceStock(Long productId, Integer quantity) {
        Integer currentStock = inventory.get(productId);
        if (currentStock == null || currentStock < quantity) {
            throw new IllegalStateException("库存不足");
        }
        inventory.put(productId, currentStock - quantity);
        System.out.println("商品 " + productId + " 库存扣减 " + quantity + ", 剩余: " + (currentStock - quantity));
    }

    /**
     * 恢复库存
     */
    public void restoreStock(Long productId, Integer quantity) {
        Integer currentStock = inventory.getOrDefault(productId, 0);
        inventory.put(productId, currentStock + quantity);
        System.out.println("商品 " + productId + " 库存恢复 " + quantity + ", 当前: " + (currentStock + quantity));
    }

    /**
     * 获取库存数量
     */
    public Integer getStock(Long productId) {
        return inventory.getOrDefault(productId, 0);
    }
}
