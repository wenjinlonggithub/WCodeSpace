package com.architecture.delayqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于时间轮算法的延迟队列
 *
 * 实现原理：
 * 1. 将时间划分为槽位（Slot），类似时钟的刻度
 * 2. 每个槽位存储该时刻需要执行的任务链表
 * 3. 指针每隔一个tick移动一格，执行该槽位的所有任务
 * 4. 支持多层时间轮，处理长延迟任务
 *
 * 时间复杂度：
 * - 添加任务：O(1)
 * - 删除任务：O(1)
 * - 执行任务：O(1)
 *
 * 优势：
 * - 性能极高（O(1)时间复杂度）
 * - 精度高（毫秒级）
 * - 内存占用少
 *
 * 劣势：
 * - 内存存储，重启丢失
 * - 单机方案
 *
 * 应用场景：
 * - Netty的HashedWheelTimer
 * - Kafka的TimingWheel
 * - 单机高性能延迟任务
 */
public class TimeWheelDelayQueue {

    private static final Logger logger = LoggerFactory.getLogger(TimeWheelDelayQueue.class);

    // 时间轮配置
    private final int tickDuration;  // 每个刻度的时长（毫秒）
    private final int wheelSize;     // 时间轮大小（槽位数量）

    // 时间轮（数组）
    private final Slot[] wheel;

    // 当前指针位置
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    // 任务执行线程池
    private final ExecutorService executorService;

    // 时间轮驱动线程
    private final ScheduledExecutorService tickExecutor;

    // 任务ID生成器
    private final AtomicInteger taskIdGenerator = new AtomicInteger(0);

    // 是否运行中
    private volatile boolean running = false;

    /**
     * 构造函数
     *
     * @param tickDuration 每个刻度的时长（毫秒），例如100ms
     * @param wheelSize 时间轮大小（槽位数量），例如60
     *                  总时长 = tickDuration * wheelSize，例如100ms * 60 = 6秒
     */
    public TimeWheelDelayQueue(int tickDuration, int wheelSize) {
        this.tickDuration = tickDuration;
        this.wheelSize = wheelSize;
        this.wheel = new Slot[wheelSize];

        // 初始化所有槽位
        for (int i = 0; i < wheelSize; i++) {
            wheel[i] = new Slot();
        }

        // 创建任务执行线程池
        this.executorService = new ThreadPoolExecutor(
                4, 8,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "TimeWheel-Worker-" + count.incrementAndGet());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 创建时间轮驱动线程
        this.tickExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "TimeWheel-Ticker");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 启动时间轮
     */
    public void start() {
        if (running) {
            logger.warn("时间轮已经在运行");
            return;
        }

        running = true;

        // 启动定时任务，每隔tickDuration执行一次
        tickExecutor.scheduleAtFixedRate(this::tick, tickDuration, tickDuration, TimeUnit.MILLISECONDS);

        logger.info("时间轮启动成功, tickDuration={}ms, wheelSize={}", tickDuration, wheelSize);
    }

    /**
     * 添加延迟任务
     *
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     * @return 任务ID（用于取消任务）
     */
    public int addTask(Runnable task, long delay, TimeUnit unit) {
        long delayMs = unit.toMillis(delay);

        if (delayMs <= 0) {
            // 立即执行
            executorService.submit(task);
            return -1;
        }

        // 计算任务应该放在哪个槽位
        // rounds：需要转几圈
        // slotIndex：槽位索引
        long totalTicks = delayMs / tickDuration;
        int rounds = (int) (totalTicks / wheelSize);
        int slotIndex = (currentIndex.get() + (int) (totalTicks % wheelSize)) % wheelSize;

        // 创建任务包装
        int taskId = taskIdGenerator.incrementAndGet();
        Task delayTask = new Task(taskId, task, rounds);

        // 添加到对应槽位
        wheel[slotIndex].addTask(delayTask);

        logger.debug("添加延迟任务, taskId={}, delay={}ms, rounds={}, slotIndex={}",
                taskId, delayMs, rounds, slotIndex);

        return taskId;
    }

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return true-成功，false-任务不存在
     */
    public boolean cancelTask(int taskId) {
        // 遍历所有槽位查找任务
        for (Slot slot : wheel) {
            if (slot.removeTask(taskId)) {
                logger.debug("取消任务成功, taskId={}", taskId);
                return true;
            }
        }
        return false;
    }

    /**
     * 时间轮滴答（指针前进一格）
     */
    private void tick() {
        try {
            // 获取当前槽位
            int index = currentIndex.get();
            Slot slot = wheel[index];

            // 执行该槽位的所有到期任务
            slot.executeExpiredTasks(executorService);

            // 指针前进
            currentIndex.set((index + 1) % wheelSize);

        } catch (Exception e) {
            logger.error("时间轮滴答异常", e);
        }
    }

    /**
     * 关闭时间轮
     */
    public void shutdown() {
        running = false;

        // 关闭定时器
        tickExecutor.shutdown();

        // 关闭任务执行线程池
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("时间轮已关闭");
    }

    /**
     * 槽位（Slot）
     *
     * 存储该时刻需要执行的任务链表
     */
    private static class Slot {
        private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();

        /**
         * 添加任务
         */
        void addTask(Task task) {
            tasks.offer(task);
        }

        /**
         * 删除任务
         */
        boolean removeTask(int taskId) {
            return tasks.removeIf(task -> task.taskId == taskId);
        }

        /**
         * 执行到期任务（rounds=0的任务）
         */
        void executeExpiredTasks(ExecutorService executorService) {
            // 遍历槽位中的所有任务
            for (Task task : tasks) {
                if (task.rounds == 0) {
                    // 任务到期，提交执行
                    executorService.submit(task.runnable);
                    // 从槽位中移除
                    tasks.remove(task);
                } else {
                    // rounds减1
                    task.rounds--;
                }
            }
        }
    }

    /**
     * 延迟任务
     */
    private static class Task {
        final int taskId;
        final Runnable runnable;
        int rounds;  // 还需要转几圈

        Task(int taskId, Runnable runnable, int rounds) {
            this.taskId = taskId;
            this.runnable = runnable;
            this.rounds = rounds;
        }
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) throws InterruptedException {
        // 创建时间轮：每100ms一个刻度，60个槽位（总时长6秒）
        TimeWheelDelayQueue timeWheel = new TimeWheelDelayQueue(100, 60);

        // 启动时间轮
        timeWheel.start();

        // 添加延迟任务
        timeWheel.addTask(() -> {
            logger.info("任务1执行：1秒延迟");
        }, 1, TimeUnit.SECONDS);

        timeWheel.addTask(() -> {
            logger.info("任务2执行：3秒延迟");
        }, 3, TimeUnit.SECONDS);

        int taskId = timeWheel.addTask(() -> {
            logger.info("任务3执行：5秒延迟");
        }, 5, TimeUnit.SECONDS);

        // 等待2秒后取消任务3
        Thread.sleep(2000);
        timeWheel.cancelTask(taskId);
        logger.info("已取消任务3");

        // 等待任务执行
        Thread.sleep(5000);

        // 关闭时间轮
        timeWheel.shutdown();
    }
}
