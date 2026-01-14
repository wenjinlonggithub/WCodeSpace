package com.architecture.scenario;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis分布式锁实战案例集合
 *
 * 包含以下案例：
 * 1. 秒杀场景 - 防止库存超卖
 * 2. 定时任务 - 防止重复执行
 * 3. 接口幂等性 - 防止重复提交
 * 4. 分段锁优化 - 提高并发性能
 * 5. 缓存更新 - 防止缓存击穿
 *
 * @author Redis Architect
 * @date 2026-01-14
 */
public class DistributedLockCases {

    private static JedisPool jedisPool;

    static {
        // 初始化连接池
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMinIdle(10);
        jedisPool = new JedisPool(config, "localhost", 6379);
    }

    // ==================== 案例1：秒杀防止超卖 ====================

    /**
     * 案例1：秒杀场景 - 防止库存超卖
     *
     * 场景描述：
     * - 商品库存1000件
     * - 10000人同时抢购
     * - 必须保证不超卖
     *
     * 方案对比：
     * 1. 方案A：单一锁（简单但性能差）
     * 2. 方案B：分段锁（推荐，性能提升10倍）
     * 3. 方案C：无锁方案（性能最优）
     */
    public static class SeckillCase {

        /**
         * 方案A：单一锁方案
         */
        public static boolean seckillWithSingleLock(String productId, String userId) {
            try (Jedis jedis = jedisPool.getResource()) {
                String lockKey = "lock:seckill:" + productId;
                DistributedLock lock = new DistributedLock(jedis, lockKey, 5);

                if (lock.tryLock()) {
                    try {
                        // 1. 查询库存
                        String stockStr = jedis.get("stock:" + productId);
                        if (stockStr == null) {
                            return false;
                        }

                        int stock = Integer.parseInt(stockStr);
                        if (stock <= 0) {
                            System.out.println("❌ " + userId + " 抢购失败：库存不足");
                            return false;
                        }

                        // 2. 扣减库存
                        jedis.decrBy("stock:" + productId, 1);

                        // 3. 创建订单
                        String orderId = createOrder(productId, userId);

                        System.out.println("✅ " + userId + " 抢购成功！订单号: " + orderId);
                        return true;

                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("❌ " + userId + " 抢购失败：获取锁失败");
                    return false;
                }
            }
        }

        /**
         * 方案B：分段锁方案（推荐）
         *
         * 原理：
         * - 将1000件库存分成10段，每段100件
         * - 每段独立加锁
         * - 并发能力提升10倍
         */
        public static boolean seckillWithSegmentLock(String productId, String userId) {
            try (Jedis jedis = jedisPool.getResource()) {
                int segmentCount = 10;
                int segment = ThreadLocalRandom.current().nextInt(segmentCount);

                String lockKey = "lock:seckill:" + productId + ":" + segment;
                String stockKey = "stock:" + productId + ":" + segment;

                DistributedLock lock = new DistributedLock(jedis, lockKey, 5);

                if (lock.tryLock()) {
                    try {
                        // 1. 查询分段库存
                        String stockStr = jedis.get(stockKey);
                        if (stockStr == null) {
                            return false;
                        }

                        int stock = Integer.parseInt(stockStr);
                        if (stock <= 0) {
                            System.out.println("❌ " + userId + " 抢购失败：分段" + segment + "库存不足");
                            return false;
                        }

                        // 2. 扣减分段库存
                        jedis.decrBy(stockKey, 1);

                        // 3. 创建订单
                        String orderId = createOrder(productId, userId);

                        System.out.println("✅ " + userId + " 抢购成功（分段" + segment + "）！订单号: " + orderId);
                        return true;

                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("❌ " + userId + " 抢购失败：分段" + segment + "获取锁失败");
                    return false;
                }
            }
        }

        /**
         * 方案C：无锁方案（性能最优）
         *
         * 原理：
         * - 直接使用Redis原子操作DECR
         * - 无锁开销，性能最高
         * - 适用于简单扣库存场景
         */
        public static boolean seckillWithoutLock(String productId, String userId) {
            try (Jedis jedis = jedisPool.getResource()) {
                String stockKey = "stock:" + productId;

                // 直接原子扣减
                Long stock = jedis.decr(stockKey);

                if (stock >= 0) {
                    // 扣减成功，创建订单
                    String orderId = createOrder(productId, userId);
                    System.out.println("✅ " + userId + " 抢购成功！订单号: " + orderId);
                    return true;
                } else {
                    // 扣减失败，回滚
                    jedis.incr(stockKey);
                    System.out.println("❌ " + userId + " 抢购失败：库存不足");
                    return false;
                }
            }
        }

        /**
         * 创建订单（模拟）
         */
        private static String createOrder(String productId, String userId) {
            return "ORD-" + productId + "-" + userId + "-" + System.currentTimeMillis();
        }

        /**
         * 性能测试
         */
        public static void performanceTest() throws InterruptedException {
            System.out.println("=== 秒杀场景性能测试 ===\n");

            String productId = "product_1001";
            int userCount = 100;  // 100个用户
            int stock = 50;       // 50件库存

            // 初始化库存
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.set("stock:" + productId, String.valueOf(stock));
            }

            ExecutorService executor = Executors.newFixedThreadPool(50);
            CountDownLatch latch = new CountDownLatch(userCount);
            AtomicInteger successCount = new AtomicInteger(0);

            long startTime = System.currentTimeMillis();

            // 模拟用户并发抢购
            for (int i = 0; i < userCount; i++) {
                final String userId = "user_" + i;
                executor.submit(() -> {
                    try {
                        boolean success = seckillWithSingleLock(productId, userId);
                        if (success) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            long elapsed = System.currentTimeMillis() - startTime;

            System.out.println("\n=== 测试结果 ===");
            System.out.println("总用户数: " + userCount);
            System.out.println("库存数量: " + stock);
            System.out.println("成功抢购: " + successCount.get());
            System.out.println("耗时: " + elapsed + "ms");
            System.out.println("TPS: " + (userCount * 1000 / elapsed));

            // 验证库存
            try (Jedis jedis = jedisPool.getResource()) {
                String finalStock = jedis.get("stock:" + productId);
                System.out.println("剩余库存: " + finalStock);
                System.out.println("是否超卖: " + (Integer.parseInt(finalStock) < 0 ? "是 ❌" : "否 ✅"));
            }
        }
    }

    // ==================== 案例2：定时任务防重 ====================

    /**
     * 案例2：分布式定时任务 - 防止重复执行
     *
     * 场景描述：
     * - 3台服务器部署同一个定时任务
     * - 每天凌晨2点执行数据同步
     * - 只允许一个实例执行
     */
    public static class ScheduledTaskCase {

        /**
         * 定时任务执行方法
         */
        public static void executeTask(String taskName, String instanceId) {
            try (Jedis jedis = jedisPool.getResource()) {
                String lockKey = "lock:task:" + taskName;
                int taskTimeout = 3600;  // 任务预计1小时完成

                DistributedLock lock = new DistributedLock(jedis, lockKey, taskTimeout);

                if (lock.tryLock()) {
                    Thread watchDog = null;
                    try {
                        System.out.println("✅ 实例 [" + instanceId + "] 获取锁成功，开始执行任务");

                        // 启动看门狗
                        watchDog = lock.startWatchDog();

                        // 执行任务（模拟耗时操作）
                        doTask(taskName);

                        System.out.println("✅ 实例 [" + instanceId + "] 任务执行完成");

                    } catch (Exception e) {
                        System.err.println("❌ 实例 [" + instanceId + "] 任务执行失败: " + e.getMessage());
                    } finally {
                        // 停止看门狗
                        if (watchDog != null) {
                            watchDog.interrupt();
                        }

                        // 释放锁
                        lock.unlock();
                        System.out.println("🔓 实例 [" + instanceId + "] 释放锁");
                    }
                } else {
                    System.out.println("⏭️  实例 [" + instanceId + "] 未获取锁，跳过任务");
                }
            }
        }

        /**
         * 执行任务（模拟）
         */
        private static void doTask(String taskName) throws InterruptedException {
            System.out.println("🔄 正在执行任务: " + taskName);
            Thread.sleep(2000);  // 模拟耗时2秒
            System.out.println("✅ 任务完成: " + taskName);
        }

        /**
         * 多实例测试
         */
        public static void multiInstanceTest() throws InterruptedException {
            System.out.println("=== 定时任务多实例测试 ===\n");

            String taskName = "syncDataTask";
            int instanceCount = 3;

            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < instanceCount; i++) {
                final String instanceId = "instance-" + (i + 1);
                Thread thread = new Thread(() -> {
                    executeTask(taskName, instanceId);
                });
                threads.add(thread);
            }

            // 启动所有实例
            for (Thread thread : threads) {
                thread.start();
            }

            // 等待所有实例完成
            for (Thread thread : threads) {
                thread.join();
            }

            System.out.println("\n=== 测试完成 ===");
        }
    }

    // ==================== 案例3：接口幂等性 ====================

    /**
     * 案例3：接口幂等性 - 防止重复提交
     *
     * 场景描述：
     * - 用户提交订单
     * - 网络抖动导致重复请求
     * - 必须保证同一个请求只处理一次
     */
    public static class IdempotentCase {

        /**
         * 创建订单（幂等）
         */
        public static String createOrder(OrderRequest request) {
            try (Jedis jedis = jedisPool.getResource()) {
                String lockKey = "lock:order:idempotent:" + request.getRequestId();
                String resultKey = "order:result:" + request.getRequestId();

                DistributedLock lock = new DistributedLock(jedis, lockKey, 60);

                if (lock.tryLock()) {
                    try {
                        // 1. 检查是否已经处理过
                        String existingOrderId = jedis.get(resultKey);
                        if (existingOrderId != null) {
                            System.out.println("⚠️  请求已处理，返回已有订单号: " + existingOrderId);
                            return existingOrderId;
                        }

                        // 2. 处理订单
                        String orderId = processOrder(request);

                        // 3. 缓存结果（24小时过期）
                        jedis.setex(resultKey, 86400, orderId);

                        System.out.println("✅ 订单创建成功: " + orderId);
                        return orderId;

                    } finally {
                        lock.unlock();
                    }
                } else {
                    throw new RuntimeException("订单正在处理中，请勿重复提交");
                }
            }
        }

        /**
         * 处理订单（模拟）
         */
        private static String processOrder(OrderRequest request) {
            try {
                System.out.println("🔄 正在处理订单: " + request.getRequestId());
                Thread.sleep(1000);  // 模拟处理时间
                return "ORD-" + UUID.randomUUID().toString().substring(0, 8);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 重复提交测试
         */
        public static void duplicateSubmitTest() throws InterruptedException {
            System.out.println("=== 接口幂等性测试 ===\n");

            String requestId = "REQ-" + System.currentTimeMillis();
            OrderRequest request = new OrderRequest(requestId, "user_123", "product_456", 2);

            int submitCount = 5;  // 模拟重复提交5次
            ExecutorService executor = Executors.newFixedThreadPool(submitCount);
            CountDownLatch latch = new CountDownLatch(submitCount);

            Set<String> orderIds = Collections.synchronizedSet(new HashSet<>());

            // 模拟并发提交
            for (int i = 0; i < submitCount; i++) {
                final int index = i + 1;
                executor.submit(() -> {
                    try {
                        System.out.println("提交 #" + index + ": " + requestId);
                        String orderId = createOrder(request);
                        orderIds.add(orderId);
                    } catch (Exception e) {
                        System.err.println("提交 #" + index + " 失败: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            System.out.println("\n=== 测试结果 ===");
            System.out.println("提交次数: " + submitCount);
            System.out.println("生成订单数: " + orderIds.size());
            System.out.println("是否幂等: " + (orderIds.size() == 1 ? "是 ✅" : "否 ❌"));
            System.out.println("订单号: " + orderIds);
        }
    }

    // ==================== 案例4：缓存更新 ====================

    /**
     * 案例4：缓存更新 - 防止缓存击穿
     *
     * 场景描述：
     * - 热点数据缓存过期
     * - 大量请求同时查询数据库
     * - 使用分布式锁控制只有一个请求查询数据库
     */
    public static class CacheUpdateCase {

        /**
         * 查询用户信息（带缓存）
         */
        public static User getUser(String userId) {
            try (Jedis jedis = jedisPool.getResource()) {
                String cacheKey = "cache:user:" + userId;

                // 1. 查询缓存
                String cached = jedis.get(cacheKey);
                if (cached != null) {
                    System.out.println("✅ 缓存命中: " + userId);
                    return deserializeUser(cached);
                }

                // 2. 缓存未命中，使用分布式锁
                String lockKey = "lock:cache:user:" + userId;
                DistributedLock lock = new DistributedLock(jedis, lockKey, 10);

                if (lock.tryLock()) {
                    try {
                        // 双重检查：再次查询缓存（可能其他线程已更新）
                        cached = jedis.get(cacheKey);
                        if (cached != null) {
                            System.out.println("✅ 二次缓存命中: " + userId);
                            return deserializeUser(cached);
                        }

                        // 3. 查询数据库
                        System.out.println("🔄 缓存未命中，查询数据库: " + userId);
                        User user = queryDatabase(userId);

                        // 4. 更新缓存（TTL=60秒）
                        if (user != null) {
                            jedis.setex(cacheKey, 60, serializeUser(user));
                            System.out.println("✅ 缓存已更新: " + userId);
                        }

                        return user;

                    } finally {
                        lock.unlock();
                    }
                } else {
                    // 获取锁失败，等待并重试
                    System.out.println("⏳ 等待缓存更新: " + userId);
                    Thread.sleep(100);
                    return getUser(userId);  // 递归重试
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 查询数据库（模拟）
         */
        private static User queryDatabase(String userId) {
            try {
                Thread.sleep(500);  // 模拟数据库查询耗时
                return new User(userId, "User-" + userId, "user" + userId + "@example.com");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private static String serializeUser(User user) {
            return user.getId() + "," + user.getName() + "," + user.getEmail();
        }

        private static User deserializeUser(String str) {
            String[] parts = str.split(",");
            return new User(parts[0], parts[1], parts[2]);
        }

        /**
         * 缓存击穿测试
         */
        public static void cacheBreakdownTest() throws InterruptedException {
            System.out.println("=== 缓存击穿测试 ===\n");

            String userId = "user_999";
            int requestCount = 10;  // 10个并发请求

            ExecutorService executor = Executors.newFixedThreadPool(requestCount);
            CountDownLatch latch = new CountDownLatch(requestCount);

            long startTime = System.currentTimeMillis();

            // 模拟并发请求
            for (int i = 0; i < requestCount; i++) {
                final int index = i + 1;
                executor.submit(() -> {
                    try {
                        System.out.println("请求 #" + index + " 开始");
                        User user = getUser(userId);
                        System.out.println("请求 #" + index + " 完成: " + user);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            long elapsed = System.currentTimeMillis() - startTime;

            System.out.println("\n=== 测试结果 ===");
            System.out.println("并发请求数: " + requestCount);
            System.out.println("总耗时: " + elapsed + "ms");
            System.out.println("说明: 只有第一个请求查询数据库，其他请求命中缓存");
        }
    }

    // ==================== 辅助类 ====================

    /**
     * 订单请求
     */
    static class OrderRequest {
        private String requestId;
        private String userId;
        private String productId;
        private int quantity;

        public OrderRequest(String requestId, String userId, String productId, int quantity) {
            this.requestId = requestId;
            this.userId = userId;
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getRequestId() {
            return requestId;
        }
    }

    /**
     * 用户实体
     */
    static class User {
        private String id;
        private String name;
        private String email;

        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String toString() {
            return "User{id='" + id + "', name='" + name + "', email='" + email + "'}";
        }
    }

    // ==================== 主函数 ====================

    public static void main(String[] args) {
        try {
            // 案例1：秒杀性能测试
            System.out.println("=== 案例1：秒杀场景 ===\n");
            SeckillCase.performanceTest();
            System.out.println("\n" + "=".repeat(60) + "\n");

            // 案例2：定时任务测试
            System.out.println("=== 案例2：定时任务 ===\n");
            ScheduledTaskCase.multiInstanceTest();
            System.out.println("\n" + "=".repeat(60) + "\n");

            // 案例3：幂等性测试
            System.out.println("=== 案例3：接口幂等性 ===\n");
            IdempotentCase.duplicateSubmitTest();
            System.out.println("\n" + "=".repeat(60) + "\n");

            // 案例4：缓存击穿测试
            System.out.println("=== 案例4：缓存更新 ===\n");
            CacheUpdateCase.cacheBreakdownTest();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            jedisPool.close();
        }
    }
}
