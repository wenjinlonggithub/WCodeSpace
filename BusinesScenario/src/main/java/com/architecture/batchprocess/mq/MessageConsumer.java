package com.architecture.batchprocess.mq;

import com.architecture.batchprocess.model.ApiResponse1;
import com.architecture.batchprocess.model.ApiResponse2;
import com.architecture.batchprocess.model.ApiResponse3;
import com.architecture.batchprocess.model.DataRecord;
import com.architecture.batchprocess.repository.DataRepository;
import com.architecture.batchprocess.service.BatchProcessMonitor;
import com.architecture.batchprocess.service.ExternalApiService;
import com.architecture.batchprocess.service.MultiLevelRateLimiter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

/**
 * 方案2：消息队列消费者
 * 负责从队列中消费数据并处理
 */
public class MessageConsumer implements Runnable {

    private final BlockingQueue<DataRecord> messageQueue;
    private final DataRepository dataRepository;
    private final ExternalApiService apiService;
    private final MultiLevelRateLimiter rateLimiter;
    private final BatchProcessMonitor monitor;
    private final String consumerId;
    private volatile boolean running = true;

    public MessageConsumer(String consumerId,
                          BlockingQueue<DataRecord> messageQueue,
                          DataRepository dataRepository,
                          ExternalApiService apiService,
                          MultiLevelRateLimiter rateLimiter,
                          BatchProcessMonitor monitor) {
        this.consumerId = consumerId;
        this.messageQueue = messageQueue;
        this.dataRepository = dataRepository;
        this.apiService = apiService;
        this.rateLimiter = rateLimiter;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        System.out.println("消费者 " + consumerId + " 启动");

        while (running) {
            try {
                // 从队列中获取数据
                DataRecord record = messageQueue.poll(1, java.util.concurrent.TimeUnit.SECONDS);

                if (record == null) {
                    // 队列为空，检查是否应该退出
                    if (messageQueue.isEmpty() && !running) {
                        break;
                    }
                    continue;
                }

                // 处理数据
                processRecord(record);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("消费者 " + consumerId + " 停止");
    }

    /**
     * 处理单条记录
     */
    private void processRecord(DataRecord record) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            // 限流控制
            rateLimiter.acquire("mq_api");

            // 并行调用API
            CompletableFuture<ApiResponse1> future1 =
                CompletableFuture.supplyAsync(() -> apiService.callApi1(record));
            CompletableFuture<ApiResponse2> future2 =
                CompletableFuture.supplyAsync(() -> apiService.callApi2(record));
            CompletableFuture<ApiResponse3> future3 =
                CompletableFuture.supplyAsync(() -> apiService.callApi3(record));

            CompletableFuture.allOf(future1, future2, future3).join();

            ApiResponse1 resp1 = future1.get();
            ApiResponse2 resp2 = future2.get();
            ApiResponse3 resp3 = future3.get();

            // 计算结果
            BigDecimal result = record.getBaseValue()
                .multiply(resp1.getFactor())
                .add(resp2.getAdjustment())
                .multiply(resp3.getCoefficient());

            record.setResult(result);
            record.setProcessedAt(new Date());
            record.setStatus("SUCCESS");

            success = true;

        } catch (Exception e) {
            record.setStatus("FAILED");
            record.setErrorMsg(e.getMessage());
        } finally {
            // 更新数据库
            dataRepository.batchUpdate(java.util.Collections.singletonList(record));

            // 记录监控指标
            long latency = System.currentTimeMillis() - startTime;
            monitor.recordProcess(success, latency);
        }
    }

    public void stop() {
        this.running = false;
    }
}
