package com.architecture.consistency.local_message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 本地消息表实体
 * 与业务表在同一个数据库，保证事务一致性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalMessage {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 业务ID（如订单ID）
     */
    private String businessId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 消息内容（JSON格式）
     */
    private String content;

    /**
     * 目标服务
     */
    private String targetService;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 已重试次数
     */
    private int retryCount;

    /**
     * 最大重试次数
     */
    private int maxRetryCount;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    public enum MessageStatus {
        PENDING("待发送"),
        SENDING("发送中"),
        SUCCESS("发送成功"),
        FAILED("发送失败");

        private final String desc;

        MessageStatus(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
