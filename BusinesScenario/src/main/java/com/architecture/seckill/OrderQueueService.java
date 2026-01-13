package com.architecture.seckill;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 订单队列服务
 *
 * 技术要点：
 * 1. 使用阻塞队列实现异步削峰
 * 2. 消费者线程池处理订单创建
 * 3. 实际生产环境使用RabbitMQ/Kafka等消息中间件
 */
public class OrderQueueService {

    private static final Logger logger = LoggerFactory.getLogger(OrderQueueService.class);

    // 订单队列：容量10000，超出则拒绝（保护系统）
    private final BlockingQueue<SeckillService.SeckillMessage> orderQueue;

    // 消费者线程池：处理订单创建
    private final ExecutorService consumerExecutor;

    // 数据库订单服务
    private final OrderService orderService;

    private final Gson gson = new Gson();

    // 队列消费者运行状态
    private volatile boolean running = true;

    public OrderQueueService(OrderService orderService) {
        this(orderService, 10000, 10);
    }

    public OrderQueueService(OrderService orderService, int queueCapacity, int consumerThreads) {
        this.orderService = orderService;
        this.orderQueue = new LinkedBlockingQueue<>(queueCapacity);

        // 创建消费者线程池
        this.consumerExecutor = new ThreadPoolExecutor(
                consumerThreads,
                consumerThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "OrderConsumer-" + count++);
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 启动消费者
        startConsumers(consumerThreads);
    }

    /**
     * 发送订单到队列
     *
     * @param message 秒杀消息
     * @return true-成功，false-队列已满
     */
    public boolean sendOrder(SeckillService.SeckillMessage message) {
        try {
            // offer：非阻塞，队列满时返回false
            boolean success = orderQueue.offer(message, 100, TimeUnit.MILLISECONDS);
            if (success) {
                logger.info("订单入队成功, message={}", gson.toJson(message));
            } else {
                logger.warn("订单队列已满, message={}", gson.toJson(message));
            }
            return success;
        } catch (InterruptedException e) {
            logger.error("订单入队被中断, message={}", gson.toJson(message), e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 启动消费者线程
     */
    private void startConsumers(int consumerCount) {
        for (int i = 0; i < consumerCount; i++) {
            consumerExecutor.submit(() -> {
                while (running) {
                    try {
                        // 从队列中获取订单消息（阻塞）
                        SeckillService.SeckillMessage message = orderQueue.poll(1, TimeUnit.SECONDS);
                        if (message != null) {
                            processOrder(message);
                        }
                    } catch (InterruptedException e) {
                        logger.warn("消费者线程被中断");
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.error("处理订单异常", e);
                    }
                }
            });
        }
        logger.info("订单消费者启动成功, consumerCount={}", consumerCount);
    }

    /**
     * 处理订单创建
     */
    private void processOrder(SeckillService.SeckillMessage message) {
        try {
            logger.info("开始处理订单, userId={}, productId={}", message.getUserId(), message.getProductId());

            // 调用订单服务创建订单（带乐观锁，防止超卖）
            boolean success = orderService.createOrder(message.getUserId(), message.getProductId());

            if (success) {
                logger.info("订单创建成功, userId={}, productId={}", message.getUserId(), message.getProductId());
            } else {
                logger.warn("订单创建失败（库存不足或重复下单）, userId={}, productId={}",
                        message.getUserId(), message.getProductId());
            }
        } catch (Exception e) {
            logger.error("订单创建异常, userId={}, productId={}",
                    message.getUserId(), message.getProductId(), e);
        }
    }

    /**
     * 关闭队列服务
     */
    public void shutdown() {
        running = false;
        consumerExecutor.shutdown();
        try {
            if (!consumerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                consumerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            consumerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("订单队列服务已关闭");
    }

    /**
     * 获取队列当前大小
     */
    public int getQueueSize() {
        return orderQueue.size();
    }
}
