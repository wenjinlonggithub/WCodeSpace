package com.architecture.batchprocess.sharding;

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
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 方案3：分布式任务调度 - 分片处理器
 * 模拟XXL-JOB或ElasticJob的分片功能
 */
public class ShardingTaskHandler {

    private final DataRepository dataRepository;
    private final ExternalApiService apiService;
    private final MultiLevelRateLimiter rateLimiter;
    private final BatchProcessMonitor monitor;

    private static final int PAGE_SIZE = 1000;

    public ShardingTaskHandler(DataRepository dataRepository,
                               ExternalApiService apiService,
                               MultiLevelRateLimiter rateLimiter,
                               BatchProcessMonitor monitor) {
        this.dataRepository = dataRepository;
        this.apiService = apiService;
        this.rateLimiter = rateLimiter;
        this.monitor = monitor;
    }

    /**
     * 执行分片任务
     * @param shardIndex 当前分片索引（从0开始）
     * @param shardTotal 总分片数
     */
    public void executeSharding(int shardIndex, int shardTotal) {
        System.out.println("分片 " + shardIndex + "/" + shardTotal + " 开始处理");

        long totalCount = dataRepository.count();
        long shardSize = totalCount / shardTotal;
        long startId = shardIndex * shardSize;
        long endId = (shardIndex == shardTotal - 1) ? totalCount : (shardIndex + 1) * shardSize;

        System.out.println("分片 " + shardIndex + " 处理范围: ID " + startId + " - " + endId);

        long currentId = startId;
        int processed = 0;

        while (currentId < endId) {
            // 分页查询
            List<DataRecord> records = dataRepository.findByIdGreaterThan(currentId, PAGE_SIZE);

            if (records.isEmpty()) {
                break;
            }

            // 处理每条记录
            for (DataRecord record : records) {
                if (record.getId() > endId) {
                    break;
                }
                processRecord(record);
                processed++;
            }

            currentId = records.get(records.size() - 1).getId();

            if (processed % 500 == 0) {
                System.out.println("分片 " + shardIndex + " 已处理: " + processed + " 条");
            }
        }

        System.out.println("分片 " + shardIndex + " 完成，共处理: " + processed + " 条");
    }

    /**
     * 处理单条记录
     */
    private void processRecord(DataRecord record) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            rateLimiter.acquire("sharding_api");

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
            dataRepository.batchUpdate(java.util.Collections.singletonList(record));

            long latency = System.currentTimeMillis() - startTime;
            monitor.recordProcess(success, latency);
        }
    }
}
