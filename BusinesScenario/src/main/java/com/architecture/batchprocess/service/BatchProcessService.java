package com.architecture.batchprocess.service;

import com.architecture.batchprocess.model.ApiResponse1;
import com.architecture.batchprocess.model.ApiResponse2;
import com.architecture.batchprocess.model.ApiResponse3;
import com.architecture.batchprocess.model.DataRecord;
import com.architecture.batchprocess.repository.DataRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 方案1：多线程并发批处理服务
 * 核心特性：分页查询 + 多线程并发 + 限流控制 + 断点续传
 */
public class BatchProcessService {

    private final DataRepository dataRepository;
    private final ExternalApiService apiService;
    private final MultiLevelRateLimiter rateLimiter;
    private final BatchProcessMonitor monitor;

    // 线程池配置
    private final ExecutorService executor;
    private static final int CORE_POOL_SIZE = 50;
    private static final int MAX_POOL_SIZE = 200;
    private static final int QUEUE_CAPACITY = 1000;

    // 批处理配置
    private static final int PAGE_SIZE = 1000;
    private static final int BATCH_UPDATE_SIZE = 100;

    // 批量更新缓冲区
    private final List<DataRecord> updateBuffer = new CopyOnWriteArrayList<>();

    // 进度记录
    private long lastProcessedId = 0;

    public BatchProcessService(DataRepository dataRepository,
                                ExternalApiService apiService,
                                MultiLevelRateLimiter rateLimiter,
                                BatchProcessMonitor monitor) {
        this.dataRepository = dataRepository;
        this.apiService = apiService;
        this.rateLimiter = rateLimiter;
        this.monitor = monitor;

        // 初始化线程池
        this.executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 启动批处理任务
     */
    public void startBatchProcess() {
        System.out.println("========== 开始批处理任务 ==========");
        monitor.start();

        long currentId = lastProcessedId;
        int totalProcessed = 0;

        try {
            while (true) {
                // 1. 分页查询
                List<DataRecord> records = dataRepository.findByIdGreaterThan(currentId, PAGE_SIZE);

                if (records.isEmpty()) {
                    System.out.println("所有数据处理完成！总计: " + totalProcessed);
                    break;
                }

                // 2. 提交到线程池并发处理
                CountDownLatch latch = new CountDownLatch(records.size());

                for (DataRecord record : records) {
                    executor.submit(() -> {
                        try {
                            processRecord(record);
                        } catch (Exception e) {
                            System.err.println("处理记录失败: " + record.getId() + ", 错误: " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                // 3. 等待当前批次处理完成
                try {
                    latch.await(5, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    System.err.println("等待超时");
                }

                // 4. 批量更新数据库
                flushUpdateBuffer();

                // 5. 更新进度
                currentId = records.get(records.size() - 1).getId();
                lastProcessedId = currentId;

                totalProcessed += records.size();

                // 6. 打印进度
                if (totalProcessed % 1000 == 0) {
                    System.out.println("已处理: " + totalProcessed + " 条，当前ID: " + currentId);
                    monitor.printStats();
                }
            }
        } finally {
            // 7. 关闭线程池
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

            // 8. 打印最终统计
            System.out.println("\n========== 批处理完成 ==========");
            monitor.printStats();
        }
    }

    /**
     * 处理单条记录
     */
    private void processRecord(DataRecord record) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            // 1. 限流控制
            rateLimiter.acquire("batch_api");

            // 2. 并行调用3个外部API
            CompletableFuture<ApiResponse1> future1 =
                CompletableFuture.supplyAsync(() -> apiService.callApi1(record));
            CompletableFuture<ApiResponse2> future2 =
                CompletableFuture.supplyAsync(() -> apiService.callApi2(record));
            CompletableFuture<ApiResponse3> future3 =
                CompletableFuture.supplyAsync(() -> apiService.callApi3(record));

            // 3. 等待所有API返回
            CompletableFuture.allOf(future1, future2, future3).join();

            // 4. 获取结果
            ApiResponse1 resp1 = future1.get();
            ApiResponse2 resp2 = future2.get();
            ApiResponse3 resp3 = future3.get();

            // 5. 内存计算
            BigDecimal result = calculateResult(record, resp1, resp2, resp3);

            // 6. 更新记录
            record.setResult(result);
            record.setProcessedAt(new Date());
            record.setStatus("SUCCESS");

            success = true;

        } catch (Exception e) {
            record.setStatus("FAILED");
            record.setErrorMsg(e.getMessage());
        } finally {
            // 7. 加入批量更新缓冲区
            updateBuffer.add(record);

            // 8. 缓冲区满了就批量更新
            if (updateBuffer.size() >= BATCH_UPDATE_SIZE) {
                flushUpdateBuffer();
            }

            // 9. 记录监控指标
            long latency = System.currentTimeMillis() - startTime;
            monitor.recordProcess(success, latency);
        }
    }

    /**
     * 业务计算逻辑
     */
    private BigDecimal calculateResult(DataRecord record,
                                       ApiResponse1 resp1,
                                       ApiResponse2 resp2,
                                       ApiResponse3 resp3) {
        // 示例：result = (baseValue * factor + adjustment) * coefficient
        return record.getBaseValue()
            .multiply(resp1.getFactor())
            .add(resp2.getAdjustment())
            .multiply(resp3.getCoefficient());
    }

    /**
     * 批量更新数据库
     */
    private synchronized void flushUpdateBuffer() {
        if (updateBuffer.isEmpty()) {
            return;
        }

        List<DataRecord> toUpdate = new ArrayList<>(updateBuffer);
        updateBuffer.clear();

        try {
            dataRepository.batchUpdate(toUpdate);
        } catch (Exception e) {
            System.err.println("批量更新失败: " + e.getMessage());
            // 失败的记录重新加回缓冲区
            updateBuffer.addAll(toUpdate);
        }
    }

    /**
     * 获取最后处理的ID（用于断点续传）
     */
    public long getLastProcessedId() {
        return lastProcessedId;
    }

    /**
     * 设置最后处理的ID（用于断点续传）
     */
    public void setLastProcessedId(long lastProcessedId) {
        this.lastProcessedId = lastProcessedId;
    }
}
