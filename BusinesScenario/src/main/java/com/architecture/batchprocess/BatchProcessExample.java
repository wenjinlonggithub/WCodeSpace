package com.architecture.batchprocess;

import com.architecture.batchprocess.repository.DataRepository;
import com.architecture.batchprocess.service.BatchProcessMonitor;
import com.architecture.batchprocess.service.BatchProcessService;
import com.architecture.batchprocess.service.ExternalApiService;
import com.architecture.batchprocess.service.MultiLevelRateLimiter;

/**
 * 方案1：多线程并发批处理示例
 * 演示如何使用批处理服务处理大量数据
 */
public class BatchProcessExample {

    public static void main(String[] args) {
        System.out.println("========== 大数据批处理示例 ==========\n");

        // 1. 初始化组件
        DataRepository dataRepository = new DataRepository();
        ExternalApiService apiService = new ExternalApiService();
        MultiLevelRateLimiter rateLimiter = new MultiLevelRateLimiter(100.0, 30.0);
        BatchProcessMonitor monitor = new BatchProcessMonitor();

        // 2. 创建批处理服务
        BatchProcessService batchService = new BatchProcessService(
            dataRepository,
            apiService,
            rateLimiter,
            monitor
        );

        // 3. 初始化测试数据（模拟10000条数据）
        dataRepository.initTestData(10000);

        // 4. 启动批处理任务
        System.out.println("开始处理10000条数据...\n");
        long startTime = System.currentTimeMillis();

        batchService.startBatchProcess();

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        // 5. 打印最终结果
        System.out.println("\n========== 处理完成 ==========");
        System.out.println("总耗时: " + elapsedTime + "ms (" + (elapsedTime / 1000) + "秒)");
        System.out.println("总数据量: " + dataRepository.count());
        System.out.println("成功数量: " + dataRepository.countByStatus("SUCCESS"));
        System.out.println("失败数量: " + dataRepository.countByStatus("FAILED"));
        System.out.println("================================\n");
    }
}
