package com.architecture.consistency.delay_retry;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

/**
 * IM消息服务
 * 负责发送IM消息，支持延迟消费和重试机制
 */
@Slf4j
public class IMMessageService {

    private final OrderService orderService;
    private final RetryStrategy retryStrategy;

    /**
     * 延迟队列：存储待发送的消息
     */
    private final DelayQueue<DelayedMessage> delayQueue = new DelayQueue<>();

    /**
     * 死信队列：存储超过最大重试次数的消息
     */
    private final Map<String, IMMessage> deadLetterQueue = new ConcurrentHashMap<>();

    /**
     * 消息存储
     */
    private final Map<String, IMMessage> messageStore = new ConcurrentHashMap<>();

    /**
     * 工作线程池
     */
    private final ExecutorService executorService;

    /**
     * 服务运行标志
     */
    private volatile boolean running = true;

    public IMMessageService(OrderService orderService, RetryStrategy retryStrategy) {
        this.orderService = orderService;
        this.retryStrategy = retryStrategy;
        this.executorService = Executors.newFixedThreadPool(3);

        // 启动消息消费线程
        startMessageConsumer();
    }

    /**
     * 发送订单创建通知
     * 使用延迟队列，延迟一段时间后再发送，给订单落库时间
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param initialDelaySeconds 初始延迟时间（秒）
     */
    public void sendOrderCreatedNotification(String orderId, String userId, int initialDelaySeconds) {
        String messageId = "MSG" + System.currentTimeMillis();

        IMMessage message = IMMessage.builder()
                .messageId(messageId)
                .orderId(orderId)
                .userId(userId)
                .type(IMMessage.MessageType.ORDER_CREATED)
                .status(IMMessage.SendStatus.PENDING)
                .retryCount(0)
                .maxRetryCount(retryStrategy.getMaxRetryCount())
                .createTime(LocalDateTime.now())
                .nextRetryTime(LocalDateTime.now().plusSeconds(initialDelaySeconds))
                .build();

        messageStore.put(messageId, message);

        // 加入延迟队列
        delayQueue.offer(new DelayedMessage(message, initialDelaySeconds * 1000));

        log.info("IM消息已加入延迟队列: messageId={}, orderId={}, delay={}s",
                messageId, orderId, initialDelaySeconds);
    }

    /**
     * 启动消息消费线程
     */
    private void startMessageConsumer() {
        executorService.submit(() -> {
            log.info("IM消息消费线程已启动");

            while (running) {
                try {
                    // 从延迟队列获取到期的消息
                    DelayedMessage delayedMessage = delayQueue.poll(1, TimeUnit.SECONDS);

                    if (delayedMessage != null) {
                        IMMessage message = delayedMessage.getMessage();
                        processMessage(message);
                    }
                } catch (InterruptedException e) {
                    log.warn("消息消费线程被中断", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            log.info("IM消息消费线程已停止");
        });
    }

    /**
     * 处理消息
     */
    private void processMessage(IMMessage message) {
        log.info("开始处理IM消息: messageId={}, orderId={}, retryCount={}",
                message.getMessageId(), message.getOrderId(), message.getRetryCount());

        // 查询订单信息
        Order order = orderService.getOrder(message.getOrderId());

        if (order == null) {
            // 订单不存在，需要重试
            handleSendFailure(message, "订单不存在或未落库");
        } else if (order.getStatus() == Order.OrderStatus.CREATING) {
            // 订单还在创建中，需要重试
            handleSendFailure(message, "订单还在创建中");
        } else {
            // 订单已创建，发送消息
            sendMessage(message, order);
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(IMMessage message, Order order) {
        String content = buildMessageContent(order);
        message.setContent(content);
        message.setStatus(IMMessage.SendStatus.SUCCESS);

        log.info("IM消息发送成功: messageId={}, orderId={}, content={}",
                message.getMessageId(), order.getOrderId(), content);
    }

    /**
     * 处理发送失败
     */
    private void handleSendFailure(IMMessage message, String reason) {
        log.warn("IM消息发送失败: messageId={}, orderId={}, reason={}, retryCount={}",
                message.getMessageId(), message.getOrderId(), reason, message.getRetryCount());

        message.setRetryCount(message.getRetryCount() + 1);

        if (retryStrategy.canRetry(message.getRetryCount())) {
            // 计算下次重试时间
            LocalDateTime nextRetryTime = retryStrategy.calculateNextRetryTime(message.getRetryCount());
            message.setNextRetryTime(nextRetryTime);

            // 重新加入延迟队列
            long delayMs = java.time.Duration.between(LocalDateTime.now(), nextRetryTime).toMillis();
            delayQueue.offer(new DelayedMessage(message, delayMs));

            log.info("IM消息已重新加入延迟队列: messageId={}, retryCount={}, nextRetryTime={}",
                    message.getMessageId(), message.getRetryCount(), nextRetryTime);
        } else {
            // 超过最大重试次数，进入死信队列
            message.setStatus(IMMessage.SendStatus.DEAD_LETTER);
            deadLetterQueue.put(message.getMessageId(), message);

            log.error("IM消息超过最大重试次数，进入死信队列: messageId={}, orderId={}",
                    message.getMessageId(), message.getOrderId());
        }
    }

    /**
     * 构建消息内容
     */
    private String buildMessageContent(Order order) {
        return String.format("您的订单【%s】已创建成功，商品：%s，金额：%.2f元",
                order.getOrderId(), order.getProductName(), order.getAmount());
    }

    /**
     * 获取消息状态
     */
    public IMMessage getMessageStatus(String messageId) {
        return messageStore.get(messageId);
    }

    /**
     * 获取死信队列大小
     */
    public int getDeadLetterQueueSize() {
        return deadLetterQueue.size();
    }

    /**
     * 停止服务
     */
    public void shutdown() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("IM消息服务已关闭");
    }

    /**
     * 延迟消息包装类
     */
    private static class DelayedMessage implements Delayed {
        private final IMMessage message;
        private final long executeTime;

        public DelayedMessage(IMMessage message, long delayMs) {
            this.message = message;
            this.executeTime = System.currentTimeMillis() + delayMs;
        }

        public IMMessage getMessage() {
            return message;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = executeTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.executeTime, ((DelayedMessage) o).executeTime);
        }
    }
}
