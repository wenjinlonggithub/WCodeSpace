package com.architecture.consistency.saga.steps;

import com.architecture.consistency.saga.SagaContext;
import com.architecture.consistency.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扣减库存步骤
 */
@Slf4j
public class DeductInventoryStep implements SagaStep {

    private static final Map<String, Integer> inventoryDatabase = new ConcurrentHashMap<>();

    static {
        // 初始化一些库存
        inventoryDatabase.put("iPhone 15 Pro", 100);
        inventoryDatabase.put("MacBook Pro", 50);
        inventoryDatabase.put("iPad Pro", 200);
    }

    /**
     * 是否模拟失败
     */
    private final boolean simulateFailure;

    public DeductInventoryStep(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    @Override
    public String getName() {
        return "扣减库存";
    }

    @Override
    public StepResult execute(SagaContext context) {
        try {
            String productName = context.get("productName");
            String orderId = context.get("orderId");

            log.info("扣减库存: orderId={}, product={}", orderId, productName);

            // 模拟失败场景
            if (simulateFailure) {
                log.error("库存不足（模拟）: product={}", productName);
                return StepResult.failure("库存不足");
            }

            // 检查库存
            Integer currentInventory = inventoryDatabase.getOrDefault(productName, 0);
            if (currentInventory <= 0) {
                log.error("库存不足: product={}, current={}", productName, currentInventory);
                return StepResult.failure("库存不足");
            }

            // 扣减库存
            inventoryDatabase.put(productName, currentInventory - 1);
            context.put("deductedInventory", 1);

            log.info("库存扣减成功: product={}, remaining={}", productName, currentInventory - 1);
            return StepResult.success("库存扣减成功");

        } catch (Exception e) {
            log.error("库存扣减失败", e);
            return StepResult.failure("库存扣减失败: " + e.getMessage());
        }
    }

    @Override
    public StepResult compensate(SagaContext context) {
        try {
            String productName = context.get("productName");
            Integer deductedInventory = context.get("deductedInventory");

            if (deductedInventory == null) {
                return StepResult.success("无需补偿库存");
            }

            log.warn("补偿：恢复库存: product={}, quantity={}", productName, deductedInventory);

            // 恢复库存
            Integer currentInventory = inventoryDatabase.getOrDefault(productName, 0);
            inventoryDatabase.put(productName, currentInventory + deductedInventory);

            log.info("库存恢复成功: product={}, current={}", productName, currentInventory + deductedInventory);
            return StepResult.success("库存恢复成功");

        } catch (Exception e) {
            log.error("库存恢复失败", e);
            return StepResult.failure("库存恢复失败: " + e.getMessage());
        }
    }
}
