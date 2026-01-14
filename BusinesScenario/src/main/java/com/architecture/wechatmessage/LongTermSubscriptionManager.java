package com.architecture.wechatmessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 长期订阅消息管理服务
 *
 * 核心区别：
 * 1. 长期订阅不限制发送次数
 * 2. 用户可以随时在设置中开关
 * 3. 43101主要原因是用户主动关闭，而非次数用完
 * 4. "总是保持选择"BUG影响更大
 *
 * @author 架构师
 * @date 2026-01-14
 */
@Slf4j
@Service
public class LongTermSubscriptionManager {

    @Autowired
    private WechatSubscriptionRepository subscriptionRepo;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 用户授权长期订阅
     *
     * 与一次性订阅的区别：
     * - 不限次数（设为999表示不限）
     * - 无过期时间
     * - 用户可以随时开关
     *
     * @param userId 用户ID
     * @param openid 用户openid
     * @param templateId 模板ID
     */
    @Transactional
    public void onUserSubscribe(String userId, String openid, String templateId) {
        log.info("用户授权长期订阅，userId: {}, templateId: {}", userId, templateId);

        Optional<WechatSubscription> existing = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (existing.isPresent()) {
            // 已有订阅记录，重新激活
            WechatSubscription subscription = existing.get();
            subscription.setStatus("ACTIVE");
            subscription.setRemainingCount(999); // 长期订阅，设为999表示不限
            subscription.setSubscribeTime(LocalDateTime.now());
            subscription.setExpireTime(null); // 长期订阅无过期时间
            subscription.setRemark("长期订阅重新激活");
            subscriptionRepo.save(subscription);

            log.info("长期订阅重新激活，userId: {}", userId);
        } else {
            // 新建长期订阅
            WechatSubscription subscription = new WechatSubscription();
            subscription.setUserId(userId);
            subscription.setOpenid(openid);
            subscription.setTemplateId(templateId);
            subscription.setRemainingCount(999); // 不限次数
            subscription.setSubscribeTime(LocalDateTime.now());
            subscription.setExpireTime(null); // 无过期时间
            subscription.setStatus("ACTIVE");
            subscription.setRemark("长期订阅");
            subscriptionRepo.save(subscription);

            log.info("新建长期订阅记录，userId: {}", userId);
        }

        // 写入Redis（永久有效，无TTL）
        String redisKey = getRedisKey(userId, templateId);
        redisTemplate.opsForValue().set(redisKey, "ACTIVE");
    }

    /**
     * 用户关闭长期订阅
     *
     * 场景：用户在小程序设置中关闭了订阅
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     */
    @Transactional
    public void onUserUnsubscribe(String userId, String templateId) {
        log.info("用户关闭长期订阅，userId: {}, templateId: {}", userId, templateId);

        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (optional.isPresent()) {
            WechatSubscription subscription = optional.get();
            subscription.setStatus("UNSUBSCRIBED"); // 用户主动取消
            subscription.setRemark("用户在设置中关闭了订阅");
            subscriptionRepo.save(subscription);

            // 删除Redis缓存
            String redisKey = getRedisKey(userId, templateId);
            redisTemplate.delete(redisKey);

            log.info("长期订阅已关闭，userId: {}", userId);
        }
    }

    /**
     * 检查是否可以发送
     *
     * 长期订阅只需检查：
     * 1. 是否有ACTIVE状态的订阅记录
     * 2. 不需要检查次数（永远为999）
     * 3. 不需要检查过期时间（永久有效）
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return true-可以发送, false-不能发送
     */
    public CanSendResult canSend(String userId, String templateId) {
        // 1. 先查Redis
        String redisKey = getRedisKey(userId, templateId);
        String cached = redisTemplate.opsForValue().get(redisKey);

        if (cached == null || !"ACTIVE".equals(cached)) {
            return CanSendResult.fail("用户未订阅或已关闭订阅");
        }

        // 2. 查数据库确认
        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateIdAndStatus(userId, templateId, "ACTIVE");

        if (!optional.isPresent()) {
            // Redis和数据库不一致，删除Redis缓存
            redisTemplate.delete(redisKey);
            return CanSendResult.fail("订阅记录不存在");
        }

        return CanSendResult.success(optional.get());
    }

    /**
     * 处理43101错误
     *
     * 长期订阅的43101主要原因：
     * 1. 用户在设置中关闭了订阅（70%）
     * 2. "总是保持选择"BUG（20%）
     * 3. 其他异常（10%）
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     */
    @Transactional
    public void handle43101Error(String userId, String templateId) {
        log.warn("长期订阅收到43101错误，userId: {}, templateId: {}", userId, templateId);

        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (optional.isPresent()) {
            WechatSubscription subscription = optional.get();
            subscription.setStatus("ERROR_43101");
            subscription.setRemark("43101错误：可能原因 - (1)用户在设置中关闭了订阅 (2)'总是保持选择'BUG (3)系统异常");
            subscriptionRepo.save(subscription);

            // 删除Redis缓存
            String redisKey = getRedisKey(userId, templateId);
            redisTemplate.delete(redisKey);

            log.info("已标记为ERROR_43101状态，需要引导用户重新授权");
        }

        // 记录到待引导表
        recordResubscribeGuide(userId, templateId, "43101");
    }

    /**
     * 同步用户订阅状态
     *
     * 用途：前端定期调用，同步用户在设置中的订阅状态
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @param frontendStatus 前端获取的状态（accept/reject）
     */
    @Transactional
    public void syncSubscriptionStatus(String userId, String templateId, String frontendStatus) {
        log.info("同步订阅状态，userId: {}, templateId: {}, status: {}",
            userId, templateId, frontendStatus);

        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (!optional.isPresent()) {
            // 数据库没有记录，但前端显示已订阅
            if ("accept".equals(frontendStatus)) {
                log.warn("数据库无记录但前端显示已订阅，可能是数据不同步");
                // 不做处理，等待下次授权
            }
            return;
        }

        WechatSubscription subscription = optional.get();

        if ("accept".equals(frontendStatus)) {
            // 前端显示已订阅，激活状态
            if (!"ACTIVE".equals(subscription.getStatus())) {
                subscription.setStatus("ACTIVE");
                subscription.setRemark("用户在设置中重新开启了订阅");
                subscriptionRepo.save(subscription);

                // 更新Redis
                String redisKey = getRedisKey(userId, templateId);
                redisTemplate.opsForValue().set(redisKey, "ACTIVE");

                log.info("订阅状态已同步为ACTIVE");
            }
        } else if ("reject".equals(frontendStatus)) {
            // 前端显示已拒绝，取消状态
            if (!"UNSUBSCRIBED".equals(subscription.getStatus())) {
                subscription.setStatus("UNSUBSCRIBED");
                subscription.setRemark("用户在设置中关闭了订阅");
                subscriptionRepo.save(subscription);

                // 删除Redis
                String redisKey = getRedisKey(userId, templateId);
                redisTemplate.delete(redisKey);

                log.info("订阅状态已同步为UNSUBSCRIBED");
            }
        }
    }

    /**
     * 检测"总是保持选择"BUG
     *
     * 特征：
     * 1. 前端显示"accept"
     * 2. 后端发送返回43101
     * 3. 用户投诉收不到消息
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @param frontendStatus 前端状态
     * @return true-可能是BUG, false-正常
     */
    public boolean detectAlwaysKeepBug(String userId, String templateId, String frontendStatus) {
        if (!"accept".equals(frontendStatus)) {
            return false;
        }

        // 检查最近是否有43101错误
        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (optional.isPresent()) {
            WechatSubscription subscription = optional.get();
            if ("ERROR_43101".equals(subscription.getStatus())) {
                log.warn("检测到'总是保持选择'BUG，userId: {}, templateId: {}",
                    userId, templateId);
                return true;
            }
        }

        return false;
    }

    /**
     * 获取订阅状态详情
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @return 订阅状态
     */
    public SubscriptionStatusDetail getStatusDetail(String userId, String templateId) {
        Optional<WechatSubscription> optional = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (!optional.isPresent()) {
            return SubscriptionStatusDetail.notSubscribed();
        }

        WechatSubscription subscription = optional.get();

        SubscriptionStatusDetail detail = new SubscriptionStatusDetail();
        detail.setSubscribed(true);
        detail.setStatus(subscription.getStatus());
        detail.setSubscribeTime(subscription.getSubscribeTime());
        detail.setLastSendTime(subscription.getLastSendTime());
        detail.setRemark(subscription.getRemark());

        // 判断是否需要引导重新授权
        if ("ERROR_43101".equals(subscription.getStatus()) ||
            "UNSUBSCRIBED".equals(subscription.getStatus())) {
            detail.setNeedResubscribe(true);
            detail.setResubscribeReason(getResubscribeReason(subscription.getStatus()));
        }

        return detail;
    }

    /**
     * 生成Redis Key
     */
    private String getRedisKey(String userId, String templateId) {
        return "wx:long_term_subscribe:" + userId + ":" + templateId;
    }

    /**
     * 记录待引导重新订阅
     */
    private void recordResubscribeGuide(String userId, String templateId, String reason) {
        // TODO: 记录到待引导表，用于后续引导用户重新授权
        log.info("记录待引导，userId: {}, templateId: {}, reason: {}",
            userId, templateId, reason);
    }

    /**
     * 获取重新订阅原因说明
     */
    private String getResubscribeReason(String status) {
        switch (status) {
            case "ERROR_43101":
                return "订阅状态异常，可能是您在设置中关闭了订阅，或存在'总是保持选择'BUG，建议重新授权";
            case "UNSUBSCRIBED":
                return "您已关闭了订阅，重新开启后可以接收重要通知";
            default:
                return "订阅状态异常，建议重新授权";
        }
    }

    /**
     * 发送检查结果
     */
    @lombok.Data
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
     * 订阅状态详情
     */
    @lombok.Data
    public static class SubscriptionStatusDetail {
        private boolean subscribed;
        private String status;
        private LocalDateTime subscribeTime;
        private LocalDateTime lastSendTime;
        private String remark;
        private boolean needResubscribe;
        private String resubscribeReason;

        public static SubscriptionStatusDetail notSubscribed() {
            SubscriptionStatusDetail detail = new SubscriptionStatusDetail();
            detail.setSubscribed(false);
            detail.setNeedResubscribe(true);
            detail.setResubscribeReason("您尚未订阅，订阅后可以接收重要通知");
            return detail;
        }
    }
}
