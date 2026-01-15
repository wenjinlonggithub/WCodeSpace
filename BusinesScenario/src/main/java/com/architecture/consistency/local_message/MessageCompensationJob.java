package com.architecture.consistency.local_message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消息补偿定时任务
 * 定期扫描本地消息表，发送未成功的消息
 */
@Slf4j
public class MessageCompensationJob {

    private final LocalMessageDAO localMessageDAO;
    private final ScheduledExecutorService scheduler;
    private final Gson gson = new Gson();

    /**
     * 扫描间隔（秒）
     */
    private final int scanIntervalSeconds;

    /**
     * 每次扫描处理的消息数量
     */
    private final int batchSize;

    /**
     * 运行标志
     */
    private volatile boolean running = false;

    public MessageCompensationJob(LocalMessageDAO localMessageDAO, int scanIntervalSeconds, int batchSize) {
        this.localMessageDAO = localMessageDAO;
        this.scanIntervalSeconds = scanIntervalSeconds;
        this.batchSize = batchSize;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 启动定时任务
     */
    public void start() {
        if (running) {
            log.warn("补偿任务已在运行中");
            return;
        }

        running = true;
        log.info("启动消息补偿定时任务: 扫描间隔={}秒, 批次大小={}", scanIntervalSeconds, batchSize);

        scheduler.scheduleWithFixedDelay(
                this::compensateMessages,
                0,
                scanIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    /**
     * 停止定时任务
     */
    public void stop() {
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
        log.info("消息补偿定时任务已停止");
    }

    /**
     * 补偿消息
     */
    private void compensateMessages() {
        try {
            log.info("开始扫描本地消息表...");

            // 查询待发送的消息
            List<LocalMessage> pendingMessages = localMessageDAO.queryPendingMessages(batchSize);

            if (pendingMessages.isEmpty()) {
                log.info("没有待补偿的消息");
                printStatistics();
                return;
            }

            log.info("查询到{}条待补偿消息", pendingMessages.size());

            // 处理每条消息
            for (LocalMessage message : pendingMessages) {
                processMessage(message);
            }

            printStatistics();

        } catch (Exception e) {
            log.error("消息补偿任务执行异常", e);
        }
    }

    /**
     * 处理单条消息
     */
    private void processMessage(LocalMessage message) {
        log.info("处理消息: messageId={}, businessId={}, retryCount={}/{}",
                message.getMessageId(), message.getBusinessId(),
                message.getRetryCount(), message.getMaxRetryCount());

        try {
            // 更新状态为发送中
            localMessageDAO.updateStatus(message.getMessageId(),
                    LocalMessage.MessageStatus.SENDING, null);

            // 发送消息到目标服务
            boolean success = sendToTargetService(message);

            if (success) {
                // 发送成功，更新状态
                localMessageDAO.updateStatus(message.getMessageId(),
                        LocalMessage.MessageStatus.SUCCESS, null);
                log.info("消息发送成功: messageId={}", message.getMessageId());

            } else {
                // 发送失败，准备重试
                handleSendFailure(message, "目标服务返回失败");
            }

        } catch (Exception e) {
            // 发送异常，准备重试
            handleSendFailure(message, e.getMessage());
        }
    }

    /**
     * 发送消息到目标服务
     * 模拟HTTP调用或消息队列发送
     */
    private boolean sendToTargetService(LocalMessage message) {
        log.info("发送消息到目标服务: targetService={}, content={}",
                message.getTargetService(), message.getContent());

        // 解析消息内容
        Map<String, Object> content = gson.fromJson(
                message.getContent(),
                new TypeToken<Map<String, Object>>(){}.getType()
        );

        // 模拟IM服务发送消息
        String orderId = (String) content.get("orderId");
        String userId = (String) content.get("userId");
        String productName = (String) content.get("productName");

        log.info("IM消息: 用户[{}]的订单[{}]已创建，商品：{}", userId, orderId, productName);

        // 模拟发送成功（实际应该调用HTTP接口或MQ）
        return true;
    }

    /**
     * 处理发送失败
     */
    private void handleSendFailure(LocalMessage message, String errorMessage) {
        log.warn("消息发送失败: messageId={}, error={}, retryCount={}",
                message.getMessageId(), errorMessage, message.getRetryCount());

        if (message.getRetryCount() >= message.getMaxRetryCount()) {
            // 超过最大重试次数，标记为失败
            localMessageDAO.updateStatus(message.getMessageId(),
                    LocalMessage.MessageStatus.FAILED,
                    "超过最大重试次数: " + errorMessage);
            log.error("消息发送失败（超过最大重试次数）: messageId={}", message.getMessageId());

        } else {
            // 计算下次重试时间（指数退避）
            LocalDateTime nextExecuteTime = calculateNextRetryTime(message.getRetryCount());

            // 更新重试次数和下次执行时间
            localMessageDAO.incrementRetryCount(message.getMessageId(), nextExecuteTime);
            localMessageDAO.updateStatus(message.getMessageId(),
                    LocalMessage.MessageStatus.PENDING, errorMessage);

            log.info("消息将在{}重试", nextExecuteTime);
        }
    }

    /**
     * 计算下次重试时间（指数退避）
     */
    private LocalDateTime calculateNextRetryTime(int retryCount) {
        // 1分钟、3分钟、10分钟、30分钟、60分钟
        int[] delayMinutes = {1, 3, 10, 30, 60};
        int delay = delayMinutes[Math.min(retryCount, delayMinutes.length - 1)];
        return LocalDateTime.now().plusMinutes(delay);
    }

    /**
     * 打印统计信息
     */
    private void printStatistics() {
        Map<LocalMessage.MessageStatus, Long> stats = localMessageDAO.getStatistics();
        log.info("消息统计: {}", stats);
    }
}
