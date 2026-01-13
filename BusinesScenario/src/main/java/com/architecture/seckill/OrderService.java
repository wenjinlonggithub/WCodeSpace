package com.architecture.seckill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务（模拟数据库操作）
 *
 * 核心技术点：
 * 1. 使用乐观锁（版本号）防止超卖
 * 2. 数据库操作SQL示例：
 *    UPDATE stock SET count=count-1, version=version+1
 *    WHERE product_id=? AND version=? AND count>0
 * 3. 如果更新影响行数=0，说明库存不足或版本冲突
 */
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    // 模拟数据库中的商品库存表
    // 实际生产中这是数据库表，这里用内存Map模拟
    private final Map<Long, ProductStock> stockTable = new ConcurrentHashMap<>();

    // 模拟订单表：userId_productId -> orderId
    private final Map<String, Long> orderTable = new ConcurrentHashMap<>();

    // 订单ID生成器
    private final AtomicLong orderIdGenerator = new AtomicLong(1000000);

    /**
     * 初始化商品库存（模拟数据库初始化）
     */
    public void initProductStock(Long productId, int stock) {
        stockTable.put(productId, new ProductStock(productId, stock, 0));
        logger.info("初始化商品库存, productId={}, stock={}", productId, stock);
    }

    /**
     * 创建订单（带乐观锁）
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return true-成功，false-失败（库存不足或重复下单）
     */
    public boolean createOrder(Long userId, Long productId) {

        // 1. 检查是否重复下单（幂等性）
        String orderKey = userId + "_" + productId;
        if (orderTable.containsKey(orderKey)) {
            logger.warn("重复下单, userId={}, productId={}", userId, productId);
            return false;
        }

        // 2. 查询商品库存（模拟SELECT）
        ProductStock stock = stockTable.get(productId);
        if (stock == null) {
            logger.error("商品不存在, productId={}", productId);
            return false;
        }

        // 3. 使用乐观锁扣减库存（模拟UPDATE）
        // SQL: UPDATE stock SET count=count-1, version=version+1
        //      WHERE product_id=? AND version=? AND count>0
        boolean success = stock.decrementStock();

        if (!success) {
            logger.warn("库存不足或并发冲突, userId={}, productId={}, version={}",
                    userId, productId, stock.getVersion());
            return false;
        }

        // 4. 创建订单（模拟INSERT）
        long orderId = orderIdGenerator.incrementAndGet();
        orderTable.put(orderKey, orderId);

        logger.info("订单创建成功, orderId={}, userId={}, productId={}, remainStock={}",
                orderId, userId, productId, stock.getCount());

        return true;
    }

    /**
     * 查询订单
     */
    public Long queryOrder(Long userId, Long productId) {
        String orderKey = userId + "_" + productId;
        return orderTable.get(orderKey);
    }

    /**
     * 查询库存
     */
    public ProductStock queryStock(Long productId) {
        return stockTable.get(productId);
    }

    /**
     * 商品库存实体（模拟数据库表）
     *
     * 字段说明：
     * - productId: 商品ID（主键）
     * - count: 库存数量
     * - version: 版本号（乐观锁）
     */
    public static class ProductStock {
        private final Long productId;
        private final AtomicInteger count;
        private final AtomicInteger version;

        public ProductStock(Long productId, int count, int version) {
            this.productId = productId;
            this.count = new AtomicInteger(count);
            this.version = new AtomicInteger(version);
        }

        /**
         * 原子性扣减库存（模拟乐观锁UPDATE）
         *
         * 实现逻辑：
         * 1. 读取当前库存和版本号
         * 2. 判断库存是否充足
         * 3. CAS更新库存和版本号
         * 4. 失败则重试
         *
         * @return true-成功，false-库存不足
         */
        public boolean decrementStock() {
            // 自旋重试（模拟乐观锁的重试机制）
            for (int i = 0; i < 10; i++) {
                int currentCount = count.get();

                // 库存不足
                if (currentCount <= 0) {
                    return false;
                }

                // CAS扣减库存
                if (count.compareAndSet(currentCount, currentCount - 1)) {
                    // 版本号+1
                    version.incrementAndGet();
                    return true;
                }

                // CAS失败，说明有并发修改，重试
            }

            // 重试10次仍失败，返回失败
            return false;
        }

        public Long getProductId() {
            return productId;
        }

        public int getCount() {
            return count.get();
        }

        public int getVersion() {
            return version.get();
        }
    }
}
