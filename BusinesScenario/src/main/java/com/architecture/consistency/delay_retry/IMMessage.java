package com.architecture.consistency.delay_retry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IM消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IMMessage {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 接收用户ID
     */
    private String userId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 发送状态
     */
    private SendStatus status;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 最大重试次数
     */
    private int maxRetryCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    public enum MessageType {
        ORDER_CREATED("订单创建通知"),
        ORDER_PAID("订单支付通知"),
        ORDER_SHIPPED("订单发货通知");

        private final String desc;

        MessageType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum SendStatus {
        PENDING("待发送"),
        SUCCESS("发送成功"),
        FAILED("发送失败"),
        DEAD_LETTER("进入死信队列");

        private final String desc;

        SendStatus(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
