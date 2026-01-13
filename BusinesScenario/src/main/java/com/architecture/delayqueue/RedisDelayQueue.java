package com.architecture.delayqueue;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 基于Redis Sorted Set的延迟队列
 *
 * 实现原理：
 * 1. 使用Redis的Sorted Set存储延迟任务
 * 2. Score为任务执行时间戳
 * 3. 定时扫描到期任务（每秒1次）
 * 4. 使用Lua脚本保证原子性
 *
 * 优势：
 * - 实现简单
 * - 性能高（Redis单机10万QPS）
 * - 支持持久化（重启不丢失）
 *
 * 适用场景：
 * - 订单超时取消
 * - 延迟重试
 * - 定时通知
 */
public class RedisDelayQueue<T> {

    private static final Logger logger = LoggerFactory.getLogger(RedisDelayQueue.class);

    private static final String QUEUE_KEY_PREFIX = "delay:queue:";

    private final JedisPool jedisPool;
    private final String queueName;
    private final Class<T> taskClass;
    private final Gson gson = new Gson();

    // 定时扫描线程池
    private final ScheduledExecutorService scheduler;

    // 任务处理器
    private Consumer<T> taskHandler;

    // 是否正在运行
    private volatile boolean running = false;

    public RedisDelayQueue(JedisPool jedisPool, String queueName, Class<T> taskClass) {
        this.jedisPool = jedisPool;
        this.queueName = queueName;
        this.taskClass = taskClass;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "DelayQueue-Scanner-" + queueName);
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 添加延迟任务
     *
     * @param task 任务对象
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    public void offer(T task, long delay, TimeUnit unit) {
        long executeTime = System.currentTimeMillis() + unit.toMillis(delay);
        String taskJson = gson.toJson(task);

        String key = QUEUE_KEY_PREFIX + queueName;

        try (Jedis jedis = jedisPool.getResource()) {
            // ZADD key score member
            jedis.zadd(key, executeTime, taskJson);
            logger.debug("添加延迟任务成功, queueName={}, delay={}ms, task={}",
                    queueName, unit.toMillis(delay), taskJson);
        } catch (Exception e) {
            logger.error("添加延迟任务失败, queueName={}, task={}", queueName, taskJson, e);
            throw new RuntimeException("添加延迟任务失败", e);
        }
    }

    /**
     * 启动消费者（定时扫描到期任务）
     *
     * @param taskHandler 任务处理器
     */
    public void startConsumer(Consumer<T> taskHandler) {
        if (running) {
            logger.warn("消费者已经在运行, queueName={}", queueName);
            return;
        }

        this.taskHandler = taskHandler;
        this.running = true;

        // 每秒扫描一次
        scheduler.scheduleWithFixedDelay(this::pollAndExecute, 0, 1, TimeUnit.SECONDS);

        logger.info("延迟队列消费者启动成功, queueName={}", queueName);
    }

    /**
     * 扫描并执行到期任务
     */
    private void pollAndExecute() {
        String key = QUEUE_KEY_PREFIX + queueName;
        long currentTime = System.currentTimeMillis();

        try (Jedis jedis = jedisPool.getResource()) {
            // 1. 获取到期任务（score <= currentTime）
            // ZRANGEBYSCORE key min max LIMIT 0 100
            Set<String> tasks = jedis.zrangeByScore(key, 0, currentTime, 0, 100);

            if (tasks == null || tasks.isEmpty()) {
                return;
            }

            logger.debug("扫描到{}个到期任务, queueName={}", tasks.size(), queueName);

            // 2. 处理每个任务
            for (String taskJson : tasks) {
                try {
                    // 使用Lua脚本删除任务（原子性）
                    // 只有删除成功的实例才执行任务（解决多实例并发问题）
                    boolean removed = removeTask(taskJson, currentTime);

                    if (removed) {
                        // 反序列化任务
                        T task = gson.fromJson(taskJson, taskClass);

                        // 执行任务
                        taskHandler.accept(task);

                        logger.info("延迟任务执行成功, queueName={}, task={}", queueName, taskJson);
                    }
                } catch (Exception e) {
                    logger.error("延迟任务执行失败, queueName={}, task={}", queueName, taskJson, e);
                }
            }
        } catch (Exception e) {
            logger.error("扫描延迟任务异常, queueName={}", queueName, e);
        }
    }

    /**
     * 删除任务（使用Lua脚本保证原子性）
     *
     * 只有score <= currentTime时才删除，避免并发问题
     *
     * @param taskJson 任务JSON
     * @param currentTime 当前时间戳
     * @return true-删除成功，false-已被其他实例删除
     */
    private boolean removeTask(String taskJson, long currentTime) {
        String key = QUEUE_KEY_PREFIX + queueName;

        // Lua脚本：先检查score，再删除
        String luaScript =
                "local score = redis.call('zscore', KEYS[1], ARGV[1]) " +
                "if score and tonumber(score) <= tonumber(ARGV[2]) then " +
                "    return redis.call('zrem', KEYS[1], ARGV[1]) " +
                "else " +
                "    return 0 " +
                "end";

        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(luaScript, 1, key, taskJson, String.valueOf(currentTime));
            return Long.valueOf(1).equals(result);
        }
    }

    /**
     * 停止消费者
     */
    public void shutdown() {
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("延迟队列消费者已关闭, queueName={}", queueName);
    }

    /**
     * 获取队列当前大小
     */
    public long size() {
        String key = QUEUE_KEY_PREFIX + queueName;
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.zcard(key);
        }
    }

    /**
     * 取消任务
     *
     * @param task 任务对象
     * @return true-成功，false-任务不存在
     */
    public boolean cancel(T task) {
        String key = QUEUE_KEY_PREFIX + queueName;
        String taskJson = gson.toJson(task);

        try (Jedis jedis = jedisPool.getResource()) {
            Long removed = jedis.zrem(key, taskJson);
            boolean success = removed != null && removed > 0;
            if (success) {
                logger.info("取消延迟任务成功, queueName={}, task={}", queueName, taskJson);
            }
            return success;
        }
    }

    /**
     * 使用示例
     */
    public static class DelayTask {
        private String orderId;
        private String action;

        public DelayTask(String orderId, String action) {
            this.orderId = orderId;
            this.action = action;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getAction() {
            return action;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 初始化Redis连接池
        JedisPool jedisPool = new JedisPool("localhost", 6379);

        // 创建延迟队列
        RedisDelayQueue<DelayTask> delayQueue = new RedisDelayQueue<>(
                jedisPool,
                "order:cancel",
                DelayTask.class
        );

        // 启动消费者
        delayQueue.startConsumer(task -> {
            logger.info("处理延迟任务: orderId={}, action={}", task.getOrderId(), task.getAction());
            // 执行业务逻辑：取消订单
        });

        // 添加延迟任务：10秒后执行
        delayQueue.offer(new DelayTask("ORDER_001", "CANCEL"), 10, TimeUnit.SECONDS);
        logger.info("已添加延迟任务，10秒后执行");

        // 等待任务执行
        Thread.sleep(15000);

        // 关闭
        delayQueue.shutdown();
        jedisPool.close();
    }
}
