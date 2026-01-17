package com.architecture.batchprocess.service;

import com.architecture.batchprocess.model.ApiResponse1;
import com.architecture.batchprocess.model.ApiResponse2;
import com.architecture.batchprocess.model.ApiResponse3;
import com.architecture.batchprocess.model.DataRecord;

import java.math.BigDecimal;
import java.util.Random;
import java.util.function.Supplier;

/**
 * 外部API服务
 * 模拟调用外部API，带重试和降级机制
 */
public class ExternalApiService {

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private final Random random = new Random();

    /**
     * 调用API1 - 获取因子
     */
    public ApiResponse1 callApi1(DataRecord record) {
        return callWithRetry(() -> {
            simulateApiCall(150); // 模拟150ms延迟

            // 模拟10%失败率
            if (random.nextInt(100) < 10) {
                throw new RuntimeException("API1调用失败");
            }

            BigDecimal factor = new BigDecimal("1.1");
            return new ApiResponse1(factor, "SUCCESS", "调用成功");
        }, "API1", ApiResponse1.defaultValue());
    }

    /**
     * 调用API2 - 获取调整值
     */
    public ApiResponse2 callApi2(DataRecord record) {
        return callWithRetry(() -> {
            simulateApiCall(200); // 模拟200ms延迟

            // 模拟10%失败率
            if (random.nextInt(100) < 10) {
                throw new RuntimeException("API2调用失败");
            }

            BigDecimal adjustment = new BigDecimal("10.0");
            return new ApiResponse2(adjustment, "SUCCESS");
        }, "API2", ApiResponse2.defaultValue());
    }

    /**
     * 调用API3 - 获取系数
     */
    public ApiResponse3 callApi3(DataRecord record) {
        return callWithRetry(() -> {
            simulateApiCall(180); // 模拟180ms延迟

            // 模拟10%失败率
            if (random.nextInt(100) < 10) {
                throw new RuntimeException("API3调用失败");
            }

            BigDecimal coefficient = new BigDecimal("0.95");
            return new ApiResponse3(coefficient, null);
        }, "API3", ApiResponse3.defaultValue());
    }

    /**
     * 带重试的API调用
     */
    private <T> T callWithRetry(Supplier<T> apiCall, String apiName, T fallbackValue) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY) {
            try {
                return apiCall.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;

                if (attempt < MAX_RETRY) {
                    System.out.println(apiName + " 调用失败，第" + attempt + "次重试");
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // 所有重试都失败，使用降级值
        System.out.println(apiName + " 调用失败，已重试" + MAX_RETRY + "次，使用降级值");
        return fallbackValue;
    }

    /**
     * 模拟API调用延迟
     */
    private void simulateApiCall(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
