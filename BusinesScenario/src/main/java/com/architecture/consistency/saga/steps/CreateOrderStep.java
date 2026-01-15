package com.architecture.consistency.saga.steps;

import com.architecture.consistency.saga.SagaContext;
import com.architecture.consistency.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建订单步骤
 */
@Slf4j
public class CreateOrderStep implements SagaStep {

    private static final Map<String, Map<String, Object>> orderDatabase = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "创建订单";
    }

    @Override
    public StepResult execute(SagaContext context) {
        try {
            String orderId = context.getBusinessId();
            String userId = context.get("userId");
            String productName = context.get("productName");
            BigDecimal amount = context.get("amount");

            log.info("创建订单: orderId={}, userId={}, product={}, amount={}",
                    orderId, userId, productName, amount);

            // 模拟创建订单
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", orderId);
            order.put("userId", userId);
            order.put("productName", productName);
            order.put("amount", amount);
            order.put("status", "CREATED");

            orderDatabase.put(orderId, order);

            // 将订单ID保存到上下文
            context.put("orderId", orderId);

            log.info("订单创建成功: orderId={}", orderId);
            return StepResult.success("订单创建成功", orderId);

        } catch (Exception e) {
            log.error("订单创建失败", e);
            return StepResult.failure("订单创建失败: " + e.getMessage());
        }
    }

    @Override
    public StepResult compensate(SagaContext context) {
        try {
            String orderId = context.get("orderId");
            log.warn("补偿：取消订单: orderId={}", orderId);

            // 删除订单或更新状态为已取消
            Map<String, Object> order = orderDatabase.get(orderId);
            if (order != null) {
                order.put("status", "CANCELLED");
                log.info("订单已取消: orderId={}", orderId);
            }

            return StepResult.success("订单取消成功");

        } catch (Exception e) {
            log.error("订单取消失败", e);
            return StepResult.failure("订单取消失败: " + e.getMessage());
        }
    }
}
