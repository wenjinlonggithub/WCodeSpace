package com.architecture.consistency.saga.steps;

import com.architecture.consistency.saga.SagaContext;
import com.architecture.consistency.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理支付步骤
 */
@Slf4j
public class ProcessPaymentStep implements SagaStep {

    private static final Map<String, Map<String, Object>> paymentDatabase = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "处理支付";
    }

    @Override
    public StepResult execute(SagaContext context) {
        try {
            String orderId = context.get("orderId");
            String userId = context.get("userId");
            BigDecimal amount = context.get("amount");

            log.info("处理支付: orderId={}, userId={}, amount={}", orderId, userId, amount);

            // 模拟支付处理
            String paymentId = "PAY" + System.currentTimeMillis();

            Map<String, Object> payment = new HashMap<>();
            payment.put("paymentId", paymentId);
            payment.put("orderId", orderId);
            payment.put("userId", userId);
            payment.put("amount", amount);
            payment.put("status", "SUCCESS");

            paymentDatabase.put(paymentId, payment);

            // 将支付ID保存到上下文
            context.put("paymentId", paymentId);

            log.info("支付处理成功: paymentId={}", paymentId);
            return StepResult.success("支付处理成功", paymentId);

        } catch (Exception e) {
            log.error("支付处理失败", e);
            return StepResult.failure("支付处理失败: " + e.getMessage());
        }
    }

    @Override
    public StepResult compensate(SagaContext context) {
        try {
            String paymentId = context.get("paymentId");

            if (paymentId == null) {
                return StepResult.success("无需补偿支付");
            }

            log.warn("补偿：退款: paymentId={}", paymentId);

            // 更新支付状态为已退款
            Map<String, Object> payment = paymentDatabase.get(paymentId);
            if (payment != null) {
                payment.put("status", "REFUNDED");
                log.info("退款成功: paymentId={}", paymentId);
            }

            return StepResult.success("退款成功");

        } catch (Exception e) {
            log.error("退款失败", e);
            return StepResult.failure("退款失败: " + e.getMessage());
        }
    }
}
