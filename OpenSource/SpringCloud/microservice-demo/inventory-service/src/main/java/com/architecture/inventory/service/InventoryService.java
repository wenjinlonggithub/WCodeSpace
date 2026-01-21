package com.architecture.inventory.service;

import com.architecture.inventory.domain.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存服务实现类
 *
 * 核心业务逻辑:
 * 1. 库存扣减: 创建订单时调用
 * 2. 库存归还: 取消订单时调用
 * 3. 库存查询: 查询商品可用库存
 *
 * 并发控制:
 * - synchronized同步锁保证线程安全
 * - ConcurrentHashMap保证集合并发安全
 *
 * 生产环境优化方案:
 * 1. 使用Redis分布式锁: SETNX + 过期时间
 * 2. 使用数据库行锁: SELECT ... FOR UPDATE
 * 3. 使用乐观锁: WHERE version = ?
 * 4. 异步扣减: 使用消息队列解耦
 *
 * @author Architecture Team
 */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    // 模拟数据库存储
    private final Map<Long, Inventory> inventoryDatabase = new ConcurrentHashMap<>();

    /**
     * 初始化测试数据
     */
    @PostConstruct
    public void initData() {
        log.info("初始化库存测试数据...");

        // 商品100: iPhone 15 Pro - 库存50
        createInventory(100L, 50);

        // 商品101: MacBook Pro - 库存30
        createInventory(101L, 30);

        // 商品102: AirPods Pro - 库存100
        createInventory(102L, 100);

        log.info("库存测试数据初始化完成，共{}个商品库存", inventoryDatabase.size());
    }

    /**
     * 创建库存记录
     */
    private void createInventory(Long productId, Integer quantity) {
        Inventory inventory = new Inventory();
        inventory.setId(productId);
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        inventory.setLockedQuantity(0);
        inventory.setAvailableQuantity(quantity);
        inventory.setVersion(1);
        inventory.setCreatedAt(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryDatabase.put(productId, inventory);
    }

    /**
     * 扣减库存
     *
     * 执行流程:
     * 1. 获取商品库存信息
     * 2. 检查可用库存是否充足
     * 3. 扣减库存（原子操作）
     * 4. 返回扣减结果
     *
     * 并发控制:
     * - 使用synchronized确保同一商品的库存扣减串行化
     * - 防止超卖问题
     *
     * 分布式场景:
     * - 单机锁synchronized只能保证单个JVM内部的并发安全
     * - 多实例部署时需要使用分布式锁（Redis/Zookeeper）
     *
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return true-扣减成功, false-库存不足
     */
    public synchronized Boolean decreaseStock(Long productId, Integer quantity) {
        log.info("扣减库存, productId={}, quantity={}", productId, quantity);

        Inventory inventory = inventoryDatabase.get(productId);
        if (inventory == null) {
            log.error("商品库存不存在, productId={}", productId);
            return false;
        }

        // 检查可用库存
        if (inventory.getAvailableQuantity() < quantity) {
            log.warn("库存不足, productId={}, 可用库存={}, 需要={}",
                    productId, inventory.getAvailableQuantity(), quantity);
            return false;
        }

        // 扣减库存（原子操作）
        // 实际生产中应该使用:
        // UPDATE inventory SET quantity = quantity - #{quantity}, version = version + 1
        // WHERE product_id = #{productId} AND version = #{version} AND quantity >= #{quantity}
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.calculateAvailableQuantity();
        inventory.setVersion(inventory.getVersion() + 1);
        inventory.setUpdatedAt(LocalDateTime.now());

        log.info("库存扣减成功, productId={}, 剩余库存={}", productId, inventory.getQuantity());
        return true;
    }

    /**
     * 归还库存（增加库存）
     *
     * 应用场景:
     * 1. 订单取消
     * 2. 订单超时未支付
     * 3. 扣减失败回滚
     *
     * @param productId 商品ID
     * @param quantity 归还数量
     * @return true-归还成功, false-失败
     */
    public synchronized Boolean increaseStock(Long productId, Integer quantity) {
        log.info("归还库存, productId={}, quantity={}", productId, quantity);

        Inventory inventory = inventoryDatabase.get(productId);
        if (inventory == null) {
            log.error("商品库存不存在, productId={}", productId);
            return false;
        }

        // 增加库存
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.calculateAvailableQuantity();
        inventory.setVersion(inventory.getVersion() + 1);
        inventory.setUpdatedAt(LocalDateTime.now());

        log.info("库存归还成功, productId={}, 当前库存={}", productId, inventory.getQuantity());
        return true;
    }

    /**
     * 查询库存
     *
     * @param productId 商品ID
     * @return 可用库存数量
     */
    public Integer getStock(Long productId) {
        log.info("查询库存, productId={}", productId);

        Inventory inventory = inventoryDatabase.get(productId);
        if (inventory == null) {
            return 0;
        }

        return inventory.getAvailableQuantity();
    }

    /**
     * 锁定库存（预占库存）
     *
     * 应用场景:
     * - 用户下单但未支付，先锁定库存
     * - 支付成功后扣减库存，支付超时后释放锁定
     *
     * @param productId 商品ID
     * @param quantity 锁定数量
     * @return true-锁定成功, false-库存不足
     */
    public synchronized Boolean lockStock(Long productId, Integer quantity) {
        log.info("锁定库存, productId={}, quantity={}", productId, quantity);

        Inventory inventory = inventoryDatabase.get(productId);
        if (inventory == null) {
            return false;
        }

        if (inventory.getAvailableQuantity() < quantity) {
            log.warn("可用库存不足, 无法锁定");
            return false;
        }

        inventory.setLockedQuantity(inventory.getLockedQuantity() + quantity);
        inventory.calculateAvailableQuantity();
        inventory.setVersion(inventory.getVersion() + 1);
        inventory.setUpdatedAt(LocalDateTime.now());

        log.info("库存锁定成功, productId={}, 锁定数量={}", productId, quantity);
        return true;
    }

    /**
     * 释放锁定库存
     *
     * @param productId 商品ID
     * @param quantity 释放数量
     * @return true-释放成功, false-失败
     */
    public synchronized Boolean unlockStock(Long productId, Integer quantity) {
        log.info("释放锁定库存, productId={}, quantity={}", productId, quantity);

        Inventory inventory = inventoryDatabase.get(productId);
        if (inventory == null) {
            return false;
        }

        inventory.setLockedQuantity(inventory.getLockedQuantity() - quantity);
        inventory.calculateAvailableQuantity();
        inventory.setVersion(inventory.getVersion() + 1);
        inventory.setUpdatedAt(LocalDateTime.now());

        log.info("锁定库存释放成功, productId={}", productId);
        return true;
    }
}
