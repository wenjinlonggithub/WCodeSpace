package com.architecture.batchprocess.sharding;

import com.architecture.batchprocess.repository.DataRepository;
import com.architecture.batchprocess.service.BatchProcessMonitor;
import com.architecture.batchprocess.service.ExternalApiService;
import com.architecture.batchprocess.service.MultiLevelRateLimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 方案3：分布式任务调度方案示例
 * 模拟多机分片并行处理
 */
public class ShardingExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 分布式任务调度示例 ==========\n");

        // 1. 初始化组件
        DataRepository dataRepository = new DataRepository();
        ExternalApiService apiService = new ExternalApiService();
        MultiLevelRateLimiter rateLimiter = new MultiLevelRateLimiter(100.0, 30.0);
        BatchProcessMonitor monitor = new BatchProcessMonitor();

        // 2. 初始化测试数据
        dataRepository.initTestData(10000);

        // 3. 启动监控
        monitor.start();

        // 4. 配置分片参数（模拟3台机器，每台处理1/3数据）
        int shardTotal = 3;
        ExecutorService executor = Executors.newFixedThreadPool(shardTotal);

        System.out.println("启动 " + shardTotal + " 个分片任务（模拟" + shardTotal + "台机器）\n");

        long startTime = System.currentTimeMillis();

        // 5. 提交分片任务
        List<ShardingTaskHandler> handlers = new ArrayList<>();
        for (int i = 0; i < shardTotal; i++) {
            ShardingTaskHandler handler = new ShardingTaskHandler(
                dataRepository,
                apiService,
                rateLimiter,
                monitor
            );
            handlers.add(handler);

            final int shardIndex = i;
            executor.submit(() -> handler.executeSharding(shardIndex, shardTotal));
        }

        // 6. 等待所有分片完成
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        long endTime = System.currentTimeMillis();

        // 7. 打印结果
        System.out.println("\n========== 处理完成 ==========");
        System.out.println("总耗时: " + (endTime - startTime) + "ms");
        System.out.println("分片数量: " + shardTotal);
        monitor.printStats();
        System.out.println("成功数量: " + dataRepository.countByStatus("SUCCESS"));
        System.out.println("失败数量: " + dataRepository.countByStatus("FAILED"));
        System.out.println("================================\n");
    }
}
