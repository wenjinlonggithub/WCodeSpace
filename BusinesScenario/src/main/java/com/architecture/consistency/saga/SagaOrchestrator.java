package com.architecture.consistency.saga;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Saga编排器
 * 负责编排和执行Saga步骤
 */
@Slf4j
public class SagaOrchestrator {

    /**
     * Saga步骤列表
     */
    private final List<SagaStep> steps = new ArrayList<>();

    /**
     * 已执行的步骤栈（用于回滚）
     */
    private final Stack<SagaStep> executedSteps = new Stack<>();

    /**
     * 添加步骤
     */
    public SagaOrchestrator addStep(SagaStep step) {
        steps.add(step);
        return this;
    }

    /**
     * 执行Saga
     *
     * @param context Saga上下文
     * @return 执行结果
     */
    public SagaExecutionResult execute(SagaContext context) {
        log.info("开始执行Saga: sagaId={}, businessId={}, steps={}",
                context.getSagaId(), context.getBusinessId(), steps.size());

        context.setStatus(SagaContext.SagaStatus.RUNNING);
        context.setUpdateTime(LocalDateTime.now());

        try {
            // 依次执行每个步骤
            for (SagaStep step : steps) {
                log.info("执行Saga步骤: {}", step.getName());

                SagaStep.StepResult result = step.execute(context);

                if (result.isSuccess()) {
                    log.info("Saga步骤执行成功: {}, message={}", step.getName(), result.getMessage());
                    executedSteps.push(step);
                    context.setUpdateTime(LocalDateTime.now());
                } else {
                    log.error("Saga步骤执行失败: {}, message={}", step.getName(), result.getMessage());
                    // 执行失败，触发补偿
                    compensate(context);
                    return SagaExecutionResult.failure(
                            "Saga执行失败: " + step.getName() + " - " + result.getMessage()
                    );
                }
            }

            // 所有步骤执行成功
            context.setStatus(SagaContext.SagaStatus.COMPLETED);
            context.setUpdateTime(LocalDateTime.now());

            log.info("Saga执行成功: sagaId={}", context.getSagaId());
            return SagaExecutionResult.success("Saga执行成功");

        } catch (Exception e) {
            log.error("Saga执行异常: sagaId={}", context.getSagaId(), e);
            compensate(context);
            return SagaExecutionResult.failure("Saga执行异常: " + e.getMessage());
        }
    }

    /**
     * 补偿（回滚已执行的步骤）
     */
    private void compensate(SagaContext context) {
        log.warn("开始执行Saga补偿: sagaId={}, 需补偿步骤数={}",
                context.getSagaId(), executedSteps.size());

        context.setStatus(SagaContext.SagaStatus.COMPENSATING);
        context.setUpdateTime(LocalDateTime.now());

        // 按照相反的顺序执行补偿操作
        while (!executedSteps.isEmpty()) {
            SagaStep step = executedSteps.pop();
            log.info("执行补偿操作: {}", step.getName());

            try {
                SagaStep.StepResult result = step.compensate(context);

                if (result.isSuccess()) {
                    log.info("补偿操作成功: {}, message={}", step.getName(), result.getMessage());
                } else {
                    log.error("补偿操作失败: {}, message={}", step.getName(), result.getMessage());
                    // 补偿失败，需要人工介入
                }
            } catch (Exception e) {
                log.error("补偿操作异常: {}", step.getName(), e);
            }

            context.setUpdateTime(LocalDateTime.now());
        }

        context.setStatus(SagaContext.SagaStatus.COMPENSATED);
        log.warn("Saga补偿完成: sagaId={}", context.getSagaId());
    }

    /**
     * Saga执行结果
     */
    public static class SagaExecutionResult {
        private final boolean success;
        private final String message;

        public SagaExecutionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SagaExecutionResult success(String message) {
            return new SagaExecutionResult(true, message);
        }

        public static SagaExecutionResult failure(String message) {
            return new SagaExecutionResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
