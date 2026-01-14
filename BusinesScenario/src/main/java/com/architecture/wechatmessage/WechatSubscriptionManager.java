package com.architecture.wechatmessage;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 微信订阅消息管理服务
 *
 * 核心功能：
 * 1. 管理用户订阅状态（次数、有效期）
 * 2. 发送前检查是否可发送
 * 3. 发送后更新订阅次数
 * 4. 处理43101错误
 *
 * @author 架构师
 * @date 2026-01-14
 */
@Slf4j
@Service
public class WechatSubscriptionManager {

    @Autowired
    private WechatSubscriptionRepository subscriptionRepo;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 用户订阅时调用
     *
     * 场景：前端调用 wx.requestSubscribeMessage 成功后，通知后端记录订阅状态
     *
     * @param userId 用户ID
     * @param openid 用户openid
     * @param templateId 模板ID
     */
    @Transactional
    public void onUserSubscribe(String userId, String openid, String templateId) {
        log.info("用户订阅，userId: {}, templateId: {}", userId, templateId);

        // 查询是否已有订阅记录
        Optional<WechatSubscription> existing = subscriptionRepo
            .findByUserIdAndTemplateIdAndStatus(userId, templateId, "ACTIVE");

        if (existing.isPresent()) {
            // 已有有效订阅，增加次数
            WechatSubscription subscription = existing.get();
            subscription.setRemainingCount(subscription.getRemainingCount() + 1);
            subscription.setSubscribeTime(LocalDateTime.now());
            subscription.setExpireTime(LocalDateTime.now().plusDays(30));
            subscriptionRepo.save(subscription);
            log.info("订阅次数+1，剩余次数: {}", subscription.getRemainingCount());
        } else {
            // 新订阅
            WechatSubscription subscription = new WechatSubscription();
            subscription.setUserId(userId);
            subscription.setOpenid(openid);
            subscription.setTemplateId(templateId);
            subscription.setRemainingCount(1);  // 一次性订阅
            subscription.setSubscribeTime(LocalDateTime.now());
            subscription.setExpireTime(LocalDateTime.now().plusDays(30));
            subscription.setStatus("ACTIVE");
            subscriptionRepo.save(subscription);
            log.info("新建订阅记录，剩余次数: 1");
        }

        // 同步到Redis（用于快速查询）
        String redisKey = getRedisKey(userId, templateId);
        redisTemplate.opsForValue().set(redisKey, "1", 30, TimeUnit.DAYS);
    }

    /**
     * 检查是否可以发送消息
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return true-可以发送, false-不能发送
     */
    public CanSendResult canSend(String userId, String templateId) {
        // 1. 先查Redis（快速判断）
        String redisKey = getRedisKey(userId, templateId);
        String cached = redisTemplate.opsForValue().get(redisKey);
        if (cached == null) {
            return CanSendResult.fail("用户未订阅或订阅已过期");
        }

        // 2. 查数据库（精确判断）
        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateIdAndStatus(userId, templateId, "ACTIVE");

        if (!optional.isPresent()) {
            return CanSendResult.fail("订阅记录不存在");
        }

        WechatSubscription subscription = optional.get();

        // 3. 检查剩余次数
        if (subscription.getRemainingCount() <= 0) {
            return CanSendResult.fail("订阅次数已用完");
        }

        // 4. 检查有效期
        if (subscription.getExpireTime().isBefore(LocalDateTime.now())) {
            return CanSendResult.fail("订阅已过期");
        }

        return CanSendResult.success(subscription);
    }

    /**
     * 发送成功后，扣减次数
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     */
    @Transactional
    public void onMessageSent(String userId, String templateId) {
        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateIdAndStatus(userId, templateId, "ACTIVE");

        if (!optional.isPresent()) {
            log.warn("发送成功但订阅记录不存在，userId: {}, templateId: {}", userId, templateId);
            return;
        }

        WechatSubscription subscription = optional.get();
        subscription.setRemainingCount(subscription.getRemainingCount() - 1);
        subscription.setLastSendTime(LocalDateTime.now());

        // 如果次数用完，标记为已消费
        if (subscription.getRemainingCount() <= 0) {
            subscription.setStatus("CONSUMED");
            // 删除Redis缓存
            String redisKey = getRedisKey(userId, templateId);
            redisTemplate.delete(redisKey);
            log.info("订阅次数已用完，标记为CONSUMED");
        }

        subscriptionRepo.save(subscription);
    }

    /**
     * 处理43101错误
     *
     * 当微信返回43101错误时，同步本地状态
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     */
    @Transactional
    public void handle43101Error(String userId, String templateId) {
        log.warn("收到43101错误，userId: {}, templateId: {}", userId, templateId);

        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (optional.isPresent()) {
            WechatSubscription subscription = optional.get();
            subscription.setStatus("CONSUMED");
            subscription.setRemainingCount(0);
            subscriptionRepo.save(subscription);

            // 删除Redis缓存
            String redisKey = getRedisKey(userId, templateId);
            redisTemplate.delete(redisKey);

            log.info("已同步本地订阅状态为CONSUMED");
        }

        // 记录到异常表，用于运营分析
        recordFailedSend(userId, templateId, "43101");
    }

    /**
     * 获取用户的订阅状态
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return 订阅状态信息
     */
    public SubscriptionStatus getStatus(String userId, String templateId) {
        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (!optional.isPresent()) {
            return SubscriptionStatus.notSubscribed();
        }

        WechatSubscription subscription = optional.get();
        return SubscriptionStatus.builder()
            .subscribed(true)
            .status(subscription.getStatus())
            .remainingCount(subscription.getRemainingCount())
            .expireTime(subscription.getExpireTime())
            .build();
    }

    /**
     * 批量检查用户订阅状态
     *
     * 用于群发消息前的批量校验
     *
     * @param userIds 用户ID列表
     * @param templateId 模板ID
     * @return userId -> 是否可发送
     */
    public Map<String, Boolean> batchCanSend(List<String> userIds, String templateId) {
        Map<String, Boolean> result = new HashMap<>();

        // 批量查询数据库
        List<WechatSubscription> subscriptions = subscriptionRepo
            .findByUserIdInAndTemplateIdAndStatus(userIds, templateId, "ACTIVE");

        // 构建映射
        Map<String, WechatSubscription> subscriptionMap = subscriptions.stream()
            .collect(Collectors.toMap(WechatSubscription::getUserId, s -> s));

        // 逐个判断
        for (String userId : userIds) {
            WechatSubscription subscription = subscriptionMap.get(userId);
            if (subscription != null
                && subscription.getRemainingCount() > 0
                && subscription.getExpireTime().isAfter(LocalDateTime.now())) {
                result.put(userId, true);
            } else {
                result.put(userId, false);
            }
        }

        return result;
    }

    /**
     * 定时清理过期订阅
     *
     * 建议：每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredSubscriptions() {
        log.info("开始清理过期订阅");

        // 清理30天前的已消费订阅
        int consumedCount = subscriptionRepo
            .deleteByStatusAndCreatedAtBefore("CONSUMED", LocalDateTime.now().minusDays(30));
        log.info("清理已消费订阅: {} 条", consumedCount);

        // 标记过期的订阅
        List<WechatSubscription> expired = subscriptionRepo
            .findByStatusAndExpireTimeBefore("ACTIVE", LocalDateTime.now());

        for (WechatSubscription subscription : expired) {
            subscription.setStatus("EXPIRED");
            subscriptionRepo.save(subscription);
        }
        log.info("标记过期订阅: {} 条", expired.size());
    }

    /**
     * 生成Redis Key
     */
    private String getRedisKey(String userId, String templateId) {
        return "wx:subscribe:" + userId + ":" + templateId;
    }

    /**
     * 记录发送失败
     */
    private void recordFailedSend(String userId, String templateId, String errorCode) {
        // 记录到失败表，用于后续分析
        // TODO: 实现失败记录逻辑
    }

    /**
     * 发送检查结果
     */
    @Data
    public static class CanSendResult {
        private boolean canSend;
        private String reason;
        private WechatSubscription subscription;

        public static CanSendResult success(WechatSubscription subscription) {
            CanSendResult result = new CanSendResult();
            result.setCanSend(true);
            result.setSubscription(subscription);
            return result;
        }

        public static CanSendResult fail(String reason) {
            CanSendResult result = new CanSendResult();
            result.setCanSend(false);
            result.setReason(reason);
            return result;
        }
    }

    /**
     * 订阅状态信息
     */
    @Data
    @Builder
    public static class SubscriptionStatus {
        private boolean subscribed;
        private String status;
        private Integer remainingCount;
        private LocalDateTime expireTime;

        public static SubscriptionStatus notSubscribed() {
            return SubscriptionStatus.builder()
                .subscribed(false)
                .build();
        }
    }
}
