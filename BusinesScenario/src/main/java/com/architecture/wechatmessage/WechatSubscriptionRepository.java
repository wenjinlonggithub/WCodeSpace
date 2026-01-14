package com.architecture.wechatmessage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 微信订阅消息 Repository
 *
 * @author 架构师
 * @date 2026-01-14
 */
@Repository
public interface WechatSubscriptionRepository extends JpaRepository<WechatSubscription, Long> {

    /**
     * 根据用户ID和模板ID查询
     */
    Optional<WechatSubscription> findByUserIdAndTemplateId(String userId, String templateId);

    /**
     * 根据用户ID、模板ID和状态查询
     */
    Optional<WechatSubscription> findByUserIdAndTemplateIdAndStatus(
        String userId,
        String templateId,
        String status
    );

    /**
     * 批量查询用户的有效订阅
     */
    List<WechatSubscription> findByUserIdInAndTemplateIdAndStatus(
        List<String> userIds,
        String templateId,
        String status
    );

    /**
     * 查询即将过期的订阅（用于提醒用户）
     */
    List<WechatSubscription> findByStatusAndExpireTimeBefore(
        String status,
        LocalDateTime expireTime
    );

    /**
     * 删除已消费的订阅（定时清理）
     */
    int deleteByStatusAndCreatedAtBefore(String status, LocalDateTime createdAt);
}
