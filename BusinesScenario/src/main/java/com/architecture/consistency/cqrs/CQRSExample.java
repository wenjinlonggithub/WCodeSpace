package com.architecture.consistency.cqrs;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CQRS + 缓存预热示例
 *
 * 核心思想：
 * 1. 读写分离：命令（写）和查询（读）使用不同的模型
 * 2. 缓存预热：订单创建时立即写入缓存
 * 3. 查询优化：查询优先从缓存读取，延迟低，性能高
 *
 * 优点：
 * - 解决了订单刚创建查询不到的问题
 * - 查询性能极高（缓存命中率接近100%）
 * - 读写分离，互不影响
 * - 可以针对读和写分别优化
 *
 * 缺点：
 * - 架构复杂度增加
 * - 需要维护缓存一致性
 * - 可能存在短暂的数据不一致
 */
@Slf4j
public class CQRSExample {

    public static void main(String[] args) throws InterruptedException {
        log.info("=== CQRS + 缓存预热示例开始 ===\n");

        // 初始化组件
        OrderCache cache = new OrderCache();
        CacheWarmer cacheWarmer = new CacheWarmer(cache);
        OrderCommandService commandService = new OrderCommandService(cacheWarmer);
        OrderQueryService queryService = new OrderQueryService(cache);

        // 场景1：创建订单并立即查询
        scenario1_CreateAndQueryImmediately(commandService, queryService);

        log.info("\n" + "=".repeat(80) + "\n");

        // 场景2：高并发读写测试
        scenario2_ConcurrentReadWrite(commandService, queryService);

        log.info("\n" + "=".repeat(80) + "\n");

        // 场景3：查看缓存统计
        scenario3_CacheStatistics(cache);

        log.info("\n=== CQRS + 缓存预热示例结束 ===");
    }

    /**
     * 场景1：创建订单并立即查询（零延迟）
     */
    private static void scenario1_CreateAndQueryImmediately(
            OrderCommandService commandService,
            OrderQueryService queryService) {

        log.info("【场景1】创建订单并立即查询");

        // 创建订单
        String orderId = commandService.createOrder(
                "user001",
                "iPhone 15 Pro",
                new BigDecimal("7999.00")
        );

        // 立即查询（不需要延迟，缓存已预热）
        OrderReadModel order = queryService.getOrder(orderId);

        // 验证结果
        if (order != null) {
            log.info("查询成功（零延迟）:");
            log.info("  订单ID: {}", order.getOrderId());
            log.info("  用户: {}", order.getUserName());
            log.info("  商品: {}", order.getProductName());
            log.info("  金额: {}", order.getAmount());
            log.info("  状态: {}", order.getStatusDesc());
        } else {
            log.error("查询失败！");
        }
    }

    /**
     * 场景2：高并发读写测试
     */
    private static void scenario2_ConcurrentReadWrite(
            OrderCommandService commandService,
            OrderQueryService queryService) throws InterruptedException {

        log.info("【场景2】高并发读写测试");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int orderCount = 50;
        CountDownLatch latch = new CountDownLatch(orderCount);

        long startTime = System.currentTimeMillis();

        // 并发创建订单
        for (int i = 0; i < orderCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String orderId = commandService.createOrder(
                            "user" + index,
                            "商品" + index,
                            new BigDecimal(String.valueOf(1000 + index))
                    );

                    // 立即查询
                    OrderReadModel order = queryService.getOrder(orderId);
                    if (order == null) {
                        log.error("查询失败: orderId={}", orderId);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();

        log.info("并发测试完成: orderCount={}, 耗时={}ms",
                orderCount, endTime - startTime);
    }

    /**
     * 场景3：查看缓存统计
     */
    private static void scenario3_CacheStatistics(OrderCache cache) {
        log.info("【场景3】缓存统计");
        cache.printStatistics();
    }
}
