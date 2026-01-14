package com.architecture.wechatmessage;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 微信订阅消息记录
 *
 * 用途：本地化管理用户的订阅状态，不依赖微信接口查询
 *
 * @author 架构师
 * @date 2026-01-14
 */
@Data
public class WechatSubscription {

    private Long id;

    /**
     * 用户ID（业务系统的用户ID）
     */
    private String userId;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 剩余可发送次数
     * 一次性订阅：每次授权 +1，每次发送 -1
     * 长期订阅：固定为 999（表示不限次数）
     */
    private Integer remainingCount;

    /**
     * 订阅时间（用户授权的时间）
     */
    private LocalDateTime subscribeTime;

    /**
     * 过期时间（授权后30天过期）
     */
    private LocalDateTime expireTime;

    /**
     * 最后发送时间
     */
    private LocalDateTime lastSendTime;

    /**
     * 状态
     * ACTIVE - 有效（可发送）
     * CONSUMED - 已消费（次数用完）
     * EXPIRED - 已过期
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 备注（记录特殊情况，如43101错误）
     */
    private String remark;

    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
