package com.architecture.consistency.local_message;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 本地消息表数据访问层
 * 模拟数据库操作
 */
@Slf4j
public class LocalMessageDAO {

    /**
     * 模拟数据库存储
     */
    private final Map<String, LocalMessage> messageTable = new ConcurrentHashMap<>();

    /**
     * 插入消息记录
     */
    public void insert(LocalMessage message) {
        messageTable.put(message.getMessageId(), message);
        log.info("本地消息表插入成功: messageId={}, businessId={}, status={}",
                message.getMessageId(), message.getBusinessId(), message.getStatus());
    }

    /**
     * 更新消息状态
     */
    public void updateStatus(String messageId, LocalMessage.MessageStatus status, String errorMessage) {
        LocalMessage message = messageTable.get(messageId);
        if (message != null) {
            message.setStatus(status);
            message.setUpdateTime(LocalDateTime.now());
            if (errorMessage != null) {
                message.setErrorMessage(errorMessage);
            }
            log.info("本地消息表更新成功: messageId={}, status={}", messageId, status);
        }
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount(String messageId, LocalDateTime nextExecuteTime) {
        LocalMessage message = messageTable.get(messageId);
        if (message != null) {
            message.setRetryCount(message.getRetryCount() + 1);
            message.setNextExecuteTime(nextExecuteTime);
            message.setUpdateTime(LocalDateTime.now());
            log.info("本地消息表重试次数+1: messageId={}, retryCount={}, nextExecuteTime={}",
                    messageId, message.getRetryCount(), nextExecuteTime);
        }
    }

    /**
     * 查询待发送的消息
     * 包括：状态为PENDING或FAILED，且到达执行时间的消息
     */
    public List<LocalMessage> queryPendingMessages(int limit) {
        LocalDateTime now = LocalDateTime.now();

        return messageTable.values().stream()
                .filter(msg -> {
                    boolean isPending = msg.getStatus() == LocalMessage.MessageStatus.PENDING ||
                                       msg.getStatus() == LocalMessage.MessageStatus.FAILED;
                    boolean isTimeToExecute = msg.getNextExecuteTime() == null ||
                                             msg.getNextExecuteTime().isBefore(now);
                    boolean canRetry = msg.getRetryCount() < msg.getMaxRetryCount();
                    return isPending && isTimeToExecute && canRetry;
                })
                .sorted(Comparator.comparing(LocalMessage::getCreateTime))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 查询消息
     */
    public LocalMessage queryById(String messageId) {
        return messageTable.get(messageId);
    }

    /**
     * 查询业务关联的消息
     */
    public List<LocalMessage> queryByBusinessId(String businessId) {
        return messageTable.values().stream()
                .filter(msg -> msg.getBusinessId().equals(businessId))
                .collect(Collectors.toList());
    }

    /**
     * 获取统计信息
     */
    public Map<LocalMessage.MessageStatus, Long> getStatistics() {
        return messageTable.values().stream()
                .collect(Collectors.groupingBy(
                        LocalMessage::getStatus,
                        Collectors.counting()
                ));
    }
}
