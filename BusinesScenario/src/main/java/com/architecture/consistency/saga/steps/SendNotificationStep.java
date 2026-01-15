package com.architecture.consistency.saga.steps;

import com.architecture.consistency.saga.SagaContext;
import com.architecture.consistency.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

/**
 * 发送通知步骤
 */
@Slf4j
public class SendNotificationStep implements SagaStep {

    @Override
    public String getName() {
        return "发送通知";
    }

    @Override
    public StepResult execute(SagaContext context) {
        try {
            String orderId = context.get("orderId");
            String userId = context.get("userId");
            String productName = context.get("productName");

            log.info("发送通知: orderId={}, userId={}", orderId, userId);

            // 模拟发送IM消息
            String message = String.format("您的订单【%s】已创建成功，商品：%s", orderId, productName);
            log.info("IM消息: userId={}, message={}", userId, message);

            context.put("notificationSent", true);

            return StepResult.success("通知发送成功");

        } catch (Exception e) {
            log.error("通知发送失败", e);
            return StepResult.failure("通知发送失败: " + e.getMessage());
        }
    }

    @Override
    public StepResult compensate(SagaContext context) {
        try {
            Boolean notificationSent = context.get("notificationSent");

            if (notificationSent == null || !notificationSent) {
                return StepResult.success("无需补偿通知");
            }

            String orderId = context.get("orderId");
            String userId = context.get("userId");

            log.warn("补偿：发送取消通知: orderId={}, userId={}", orderId, userId);

            // 发送取消通知
            String message = String.format("您的订单【%s】已取消", orderId);
            log.info("IM取消消息: userId={}, message={}", userId, message);

            return StepResult.success("取消通知发送成功");

        } catch (Exception e) {
            log.error("取消通知发送失败", e);
            return StepResult.failure("取消通知发送失败: " + e.getMessage());
        }
    }
}
