package com.architecture.consistency.saga;

/**
 * Saga步骤接口
 * 每个步骤包含正向操作和补偿操作
 */
public interface SagaStep {

    /**
     * 获取步骤名称
     */
    String getName();

    /**
     * 执行正向操作
     *
     * @param context Saga上下文
     * @return 执行结果
     */
    StepResult execute(SagaContext context);

    /**
     * 执行补偿操作（回滚）
     *
     * @param context Saga上下文
     * @return 补偿结果
     */
    StepResult compensate(SagaContext context);

    /**
     * 步骤执行结果
     */
    class StepResult {
        private final boolean success;
        private final String message;
        private final Object data;

        public StepResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static StepResult success(String message) {
            return new StepResult(true, message, null);
        }

        public static StepResult success(String message, Object data) {
            return new StepResult(true, message, data);
        }

        public static StepResult failure(String message) {
            return new StepResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}
