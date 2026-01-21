package com.architecture.inventory.controller;

import com.architecture.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 库存控制器
 *
 * 提供库存相关的REST接口:
 * - GET /inventory/{productId}: 查询库存
 * - POST /inventory/decrease: 扣减库存（Order Service调用）
 * - POST /inventory/increase: 归还库存（Order Service调用）
 * - POST /inventory/lock: 锁定库存
 * - POST /inventory/unlock: 释放锁定库存
 *
 * 接口设计原则:
 * 1. 幂等性: 相同请求多次调用结果一致
 * 2. 原子性: 库存操作要么成功要么失败
 * 3. 可靠性: 失败时需要补偿机制
 *
 * @author Architecture Team
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * 查询库存
     *
     * @param productId 商品ID
     * @return 可用库存数量
     */
    @GetMapping("/{productId}")
    public Integer getStock(@PathVariable Long productId) {
        return inventoryService.getStock(productId);
    }

    /**
     * 扣减库存
     *
     * 此接口会被Order Service调用
     * 调用时机: 创建订单时
     *
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return true-成功, false-失败
     */
    @PostMapping("/decrease")
    public Boolean decreaseStock(@RequestParam Long productId,
                                  @RequestParam Integer quantity) {
        return inventoryService.decreaseStock(productId, quantity);
    }

    /**
     * 归还库存
     *
     * 此接口会被Order Service调用
     * 调用时机: 订单取消或创建失败时
     *
     * @param productId 商品ID
     * @param quantity 归还数量
     * @return true-成功, false-失败
     */
    @PostMapping("/increase")
    public Boolean increaseStock(@RequestParam Long productId,
                                  @RequestParam Integer quantity) {
        return inventoryService.increaseStock(productId, quantity);
    }

    /**
     * 锁定库存
     *
     * @param productId 商品ID
     * @param quantity 锁定数量
     * @return true-成功, false-失败
     */
    @PostMapping("/lock")
    public Boolean lockStock(@RequestParam Long productId,
                             @RequestParam Integer quantity) {
        return inventoryService.lockStock(productId, quantity);
    }

    /**
     * 释放锁定库存
     *
     * @param productId 商品ID
     * @param quantity 释放数量
     * @return true-成功, false-失败
     */
    @PostMapping("/unlock")
    public Boolean unlockStock(@RequestParam Long productId,
                               @RequestParam Integer quantity) {
        return inventoryService.unlockStock(productId, quantity);
    }
}
