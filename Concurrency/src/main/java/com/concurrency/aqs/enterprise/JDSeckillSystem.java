package com.concurrency.aqs.enterprise;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 京东 - 秒杀系统
 *
 * 业务场景：
 * 京东秒杀系统需要处理百万级并发请求，精确控制商品库存，防止超卖。
 * 典型场景：iPhone新品首发、茅台酒抢购等。
 *
 * 技术方案：
 * - 使用Semaphore控制库存数量
 * - 许可数量代表库存，acquire代表扣减库存
 * - 结合预扣库存和订单确认机制
 *
 * 业务价值：
 * - 处理百万级并发秒杀请求
 * - 防止超卖，保证库存准确性
 * - 提供订单超时自动释放库存能力
 */
public class JDSeckillSystem {

    private final String productId;
    private final String productName;
    private final int totalStock;
    private final Semaphore stockSemaphore;
    private final AtomicInteger successCount;
    private final AtomicInteger failCount;
    private final ConcurrentHashMap<String, SeckillOrder> orders;

    public JDSeckillSystem(String productId, String productName, int totalStock) {
        this.productId = productId;
        this.productName = productName;
        this.totalStock = totalStock;
        this.stockSemaphore = new Semaphore(totalStock);
        this.successCount = new AtomicInteger(0);
        this.failCount = new AtomicInteger(0);
        this.orders = new ConcurrentHashMap<>();
    }

    /**
     * 秒杀下单（核心方法）
     *
     * @param userId 用户ID
     * @return 订单ID，null表示秒杀失败
     */
    public String seckill(String userId) {
        // 尝试扣减库存（非阻塞）
        boolean acquired = stockSemaphore.tryAcquire();

        if (!acquired) {
            // 库存不足，秒杀失败
            failCount.incrementAndGet();
            System.out.printf("[秒杀失败] 用户%s - 商品已售罄%n", userId);
            return null;
        }

        try {
            // 生成订单
            String orderId = generateOrderId();
            SeckillOrder order = new SeckillOrder(
                    orderId,
                    userId,
                    productId,
                    productName,
                    System.currentTimeMillis()
            );

            // 保存订单
            orders.put(orderId, order);
            successCount.incrementAndGet();

            System.out.printf("[秒杀成功] 用户%s 抢到商品 - 订单号: %s (剩余库存: %d)%n",
                    userId, orderId, stockSemaphore.availablePermits());

            return orderId;

        } catch (Exception e) {
            // 异常情况，释放库存
            stockSemaphore.release();
            failCount.incrementAndGet();
            System.err.printf("[秒杀异常] 用户%s - %s%n", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 取消订单（释放库存）
     */
    public boolean cancelOrder(String orderId) {
        SeckillOrder order = orders.remove(orderId);
        if (order == null) {
            return false;
        }

        // 释放库存
        stockSemaphore.release();
        successCount.decrementAndGet();

        System.out.printf("[订单取消] 订单%s已取消，库存已释放 (当前库存: %d)%n",
                orderId, stockSemaphore.availablePermits());

        return true;
    }

    /**
     * 订单超时自动取消（模拟）
     */
    public void autoCancel(String orderId, long timeoutMs) {
        new Thread(() -> {
            try {
                Thread.sleep(timeoutMs);
                SeckillOrder order = orders.get(orderId);
                if (order != null && !order.isPaid()) {
                    cancelOrder(orderId);
                    System.out.printf("[订单超时] 订单%s超时未支付，已自动取消%n", orderId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "AutoCancel-" + orderId).start();
    }

    /**
     * 支付订单
     */
    public boolean payOrder(String orderId) {
        SeckillOrder order = orders.get(orderId);
        if (order == null) {
            return false;
        }

        order.setPaid(true);
        System.out.printf("[订单支付] 订单%s支付成功%n", orderId);
        return true;
    }

    /**
     * 获取秒杀统计信息
     */
    public SeckillStats getStats() {
        return new SeckillStats(
                productId,
                productName,
                totalStock,
                stockSemaphore.availablePermits(),
                successCount.get(),
                failCount.get()
        );
    }

    private String generateOrderId() {
        return "JD" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    // 秒杀订单
    public static class SeckillOrder {
        private final String orderId;
        private final String userId;
        private final String productId;
        private final String productName;
        private final long createTime;
        private volatile boolean paid;

        public SeckillOrder(String orderId, String userId, String productId,
                            String productName, long createTime) {
            this.orderId = orderId;
            this.userId = userId;
            this.productId = productId;
            this.productName = productName;
            this.createTime = createTime;
            this.paid = false;
        }

        public boolean isPaid() {
            return paid;
        }

        public void setPaid(boolean paid) {
            this.paid = paid;
        }
    }

    // 秒杀统计
    public static class SeckillStats {
        private final String productId;
        private final String productName;
        private final int totalStock;
        private final int remainStock;
        private final int successCount;
        private final int failCount;

        public SeckillStats(String productId, String productName, int totalStock,
                            int remainStock, int successCount, int failCount) {
            this.productId = productId;
            this.productName = productName;
            this.totalStock = totalStock;
            this.remainStock = remainStock;
            this.successCount = successCount;
            this.failCount = failCount;
        }

        public void print() {
            System.out.println("\n=== 秒杀统计 ===");
            System.out.printf("商品ID: %s%n", productId);
            System.out.printf("商品名称: %s%n", productName);
            System.out.printf("总库存: %d%n", totalStock);
            System.out.printf("剩余库存: %d%n", remainStock);
            System.out.printf("成功订单: %d%n", successCount);
            System.out.printf("失败请求: %d%n", failCount);
            System.out.printf("总请求数: %d%n", successCount + failCount);
            System.out.printf("成功率: %.2f%%%n", (double) successCount / (successCount + failCount) * 100);
        }
    }

    // 测试示例
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 京东秒杀系统演示 ===\n");

        // 创建秒杀活动：iPhone 16 Pro，库存10台
        JDSeckillSystem seckill = new JDSeckillSystem(
                "PROD-001",
                "iPhone 16 Pro 256GB",
                10
        );

        // 模拟1000个用户抢购
        Thread[] threads = new Thread[1000];
        for (int i = 0; i < 1000; i++) {
            final String userId = "User-" + (i + 1);
            threads[i] = new Thread(() -> {
                String orderId = seckill.seckill(userId);
                if (orderId != null) {
                    // 模拟部分用户支付，部分用户超时
                    if (Math.random() < 0.7) {
                        // 70%的用户会支付
                        try {
                            Thread.sleep((long) (Math.random() * 100));
                            seckill.payOrder(orderId);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        // 30%的用户超时未支付，5秒后自动取消
                        seckill.autoCancel(orderId, 5000);
                    }
                }
            }, userId);
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 打印统计信息
        seckill.getStats().print();

        // 等待超时订单取消
        System.out.println("\n等待超时订单自动取消...");
        Thread.sleep(6000);

        // 再次打印统计信息
        System.out.println("\n超时订单取消后:");
        seckill.getStats().print();
    }
}
