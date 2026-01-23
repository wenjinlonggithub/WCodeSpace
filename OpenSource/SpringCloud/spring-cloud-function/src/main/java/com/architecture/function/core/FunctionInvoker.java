package com.architecture.function.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

/**
 * 函数执行引擎（核心）
 *
 * 这是 Spring Cloud Function 的核心执行引擎
 * 负责函数的调用、类型转换、异常处理等
 *
 * 核心职责：
 * 1. 函数查找：从 FunctionCatalog 中查找函数
 * 2. 类型转换：将输入数据转换为函数所需的类型
 * 3. 函数执行：调用函数并获取结果
 * 4. 结果转换：将函数结果转换为输出格式
 * 5. 异常处理：捕获并处理执行过程中的异常
 */
public class FunctionInvoker {

    private final SimpleFunctionCatalog functionCatalog;
    private final ObjectMapper objectMapper;

    public FunctionInvoker(SimpleFunctionCatalog functionCatalog) {
        this.functionCatalog = functionCatalog;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 执行函数（核心方法）
     *
     * 完整的执行流程：
     * 1. 查找函数
     * 2. 类型转换（输入）
     * 3. 执行函数
     * 4. 类型转换（输出）
     * 5. 返回结果
     *
     * @param functionName 函数名称
     * @param input 输入数据
     * @param inputType 输入类型
     * @param outputType 输出类型
     * @return 执行结果
     */
    public <I, O> InvocationResult<O> invoke(
            String functionName,
            Object input,
            Class<I> inputType,
            Class<O> outputType) {

        System.out.println("\n========== 函数执行引擎启动 ==========");
        System.out.println("[FunctionInvoker] 函数名称: " + functionName);
        System.out.println("[FunctionInvoker] 输入数据: " + input);
        System.out.println("[FunctionInvoker] 输入类型: " + inputType.getSimpleName());
        System.out.println("[FunctionInvoker] 输出类型: " + outputType.getSimpleName());

        try {
            // 步骤1: 查找函数
            System.out.println("\n--- 步骤1: 查找函数 ---");
            Function<I, O> function = functionCatalog.lookup(functionName);
            System.out.println("[FunctionInvoker] ✓ 函数查找成功");

            // 步骤2: 类型转换（输入）
            System.out.println("\n--- 步骤2: 输入类型转换 ---");
            I convertedInput = convertInput(input, inputType);
            System.out.println("[FunctionInvoker] ✓ 输入转换成功: " + convertedInput);

            // 步骤3: 执行函数
            System.out.println("\n--- 步骤3: 执行函数 ---");
            long startTime = System.currentTimeMillis();
            O result = function.apply(convertedInput);
            long endTime = System.currentTimeMillis();
            System.out.println("[FunctionInvoker] ✓ 函数执行成功");
            System.out.println("[FunctionInvoker] 执行时间: " + (endTime - startTime) + "ms");
            System.out.println("[FunctionInvoker] 执行结果: " + result);

            // 步骤4: 返回结果
            System.out.println("\n--- 步骤4: 返回结果 ---");
            System.out.println("========== 函数执行完成 ==========\n");

            return InvocationResult.success(result, endTime - startTime);

        } catch (Exception e) {
            // 步骤5: 异常处理
            System.err.println("\n--- 异常处理 ---");
            System.err.println("[FunctionInvoker] ✗ 执行失败: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========== 函数执行失败 ==========\n");

            return InvocationResult.failure(e);
        }
    }

    /**
     * 输入类型转换
     *
     * 核心转换逻辑：
     * 1. 如果输入已经是目标类型，直接返回
     * 2. 如果输入是 String，尝试 JSON 反序列化
     * 3. 如果输入是其他类型，尝试对象映射
     *
     * @param input 原始输入
     * @param targetType 目标类型
     * @return 转换后的输入
     */
    @SuppressWarnings("unchecked")
    private <I> I convertInput(Object input, Class<I> targetType) throws Exception {
        System.out.println("[TypeConverter] 开始类型转换");
        System.out.println("[TypeConverter] 原始类型: " + input.getClass().getSimpleName());
        System.out.println("[TypeConverter] 目标类型: " + targetType.getSimpleName());

        // 情况1: 类型已匹配
        if (targetType.isInstance(input)) {
            System.out.println("[TypeConverter] 类型已匹配，无需转换");
            return (I) input;
        }

        // 情况2: String 类型
        if (targetType == String.class) {
            System.out.println("[TypeConverter] 转换为 String");
            return (I) input.toString();
        }

        // 情况3: 基本类型
        if (targetType == Integer.class || targetType == int.class) {
            System.out.println("[TypeConverter] 转换为 Integer");
            if (input instanceof Number) {
                return (I) Integer.valueOf(((Number) input).intValue());
            }
            return (I) Integer.valueOf(input.toString());
        }

        if (targetType == Double.class || targetType == double.class) {
            System.out.println("[TypeConverter] 转换为 Double");
            if (input instanceof Number) {
                return (I) Double.valueOf(((Number) input).doubleValue());
            }
            return (I) Double.valueOf(input.toString());
        }

        // 情况4: JSON 反序列化
        if (input instanceof String) {
            System.out.println("[TypeConverter] JSON 反序列化");
            return objectMapper.readValue((String) input, targetType);
        }

        // 情况5: 对象映射
        System.out.println("[TypeConverter] 对象映射转换");
        String json = objectMapper.writeValueAsString(input);
        return objectMapper.readValue(json, targetType);
    }

    /**
     * 执行结果封装
     *
     * 包含执行结果、执行时间、异常信息等
     */
    public static class InvocationResult<O> {
        private final boolean success;
        private final O result;
        private final long executionTime;
        private final Exception exception;

        private InvocationResult(boolean success, O result, long executionTime, Exception exception) {
            this.success = success;
            this.result = result;
            this.executionTime = executionTime;
            this.exception = exception;
        }

        public static <O> InvocationResult<O> success(O result, long executionTime) {
            return new InvocationResult<>(true, result, executionTime, null);
        }

        public static <O> InvocationResult<O> failure(Exception exception) {
            return new InvocationResult<>(false, null, 0, exception);
        }

        public boolean isSuccess() {
            return success;
        }

        public O getResult() {
            return result;
        }

        public long getExecutionTime() {
            return executionTime;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public String toString() {
            if (success) {
                return "InvocationResult{success=true, result=" + result +
                       ", executionTime=" + executionTime + "ms}";
            } else {
                return "InvocationResult{success=false, exception=" +
                       exception.getMessage() + "}";
            }
        }
    }
}
