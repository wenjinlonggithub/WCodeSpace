package com.architecture.consistency.saga;

import com.architecture.consistency.saga.steps.CreateOrderStep;
import com.architecture.consistency.saga.steps.DeductInventoryStep;
import com.architecture.consistency.saga.steps.ProcessPaymentStep;
import com.architecture.consistency.saga.steps.SendNotificationStep;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Saga分布式事务示例
 *
 * 场景：电商下单流程
 * 1. 创建订单
 * 2. 扣减库存
 * 3. 处理支付
 * 4. 发送通知
 *
 * 特点：
 * - 每个步骤都是本地事务
 * - 步骤按顺序执行
 * - 任何步骤失败，触发补偿操作
 * - 按相反顺序回滚已执行的步骤
 *
 * 优点：
 * - 适合长事务和跨服务场景
 * - 支持复杂业务流程
 * - 良好的可观测性
 *
 * 缺点：
 * - 需要设计补偿逻辑
 * - 可能出现中间状态
 * - 实现复杂度较高
 */
@Slf4j
public class SagaExample {

    public static void main(String[] args) {
        log.info("=== Saga分布式事务示例开始 ===\n");

        // 场景1：所有步骤成功
        scenario1_AllStepsSuccess();

        log.info("\n" + "=".repeat(80) + "\n");

        // 场景2：库存扣减失败，触发补偿
        scenario2_InventoryFailure();

        log.info("\n=== Saga分布式事务示例结束 ===");
    }

    /**
     * 场景1：所有步骤成功
     */
    private static void scenario1_AllStepsSuccess() {
        log.info("【场景1】所有步骤成功");

        // 创建Saga编排器
        SagaOrchestrator orchestrator = new SagaOrchestrator()
                .addStep(new CreateOrderStep())
                .addStep(new DeductInventoryStep(false))  // 不模拟失败
                .addStep(new ProcessPaymentStep())
                .addStep(new SendNotificationStep());

        // 创建Saga上下文
        String orderId = "ORD" + System.currentTimeMillis();
        SagaContext context = new SagaContext("SAGA" + System.currentTimeMillis(), orderId);
        context.put("userId", "user001");
        context.put("productName", "iPhone 15 Pro");
        context.put("amount", new BigDecimal("7999.00"));

        // 执行Saga
        SagaOrchestrator.SagaExecutionResult result = orchestrator.execute(context);

        // 打印结果
        log.info("\n【执行结果】");
        log.info("成功: {}", result.isSuccess());
        log.info("消息: {}", result.getMessage());
        log.info("Saga状态: {}", context.getStatus());
    }

    /**
     * 场景2：库存扣减失败，触发补偿
     */
    private static void scenario2_InventoryFailure() {
        log.info("【场景2】库存扣减失败，触发补偿");

        // 创建Saga编排器
        SagaOrchestrator orchestrator = new SagaOrchestrator()
                .addStep(new CreateOrderStep())
                .addStep(new DeductInventoryStep(true))  // 模拟库存扣减失败
                .addStep(new ProcessPaymentStep())
                .addStep(new SendNotificationStep());

        // 创建Saga上下文
        String orderId = "ORD" + System.currentTimeMillis();
        SagaContext context = new SagaContext("SAGA" + System.currentTimeMillis(), orderId);
        context.put("userId", "user002");
        context.put("productName", "MacBook Pro");
        context.put("amount", new BigDecimal("19999.00"));

        // 执行Saga
        SagaOrchestrator.SagaExecutionResult result = orchestrator.execute(context);

        // 打印结果
        log.info("\n【执行结果】");
        log.info("成功: {}", result.isSuccess());
        log.info("消息: {}", result.getMessage());
        log.info("Saga状态: {}", context.getStatus());
    }
}
