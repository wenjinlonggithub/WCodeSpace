package com.architecture.batchprocess.mq;

import com.architecture.batchprocess.model.DataRecord;
import com.architecture.batchprocess.repository.DataRepository;
import com.architecture.batchprocess.service.BatchProcessMonitor;
import com.architecture.batchprocess.service.ExternalApiService;
import com.architecture.batchprocess.service.MultiLevelRateLimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 方案2：消息队列方案示例
 * 演示生产者-消费者模式的批处理
 */
public class MessageQueueExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 消息队列批处理示例 ==========\n");

        // 1. 初始化组件
        DataRepository dataRepository = new DataRepository();
        ExternalApiService apiService = new ExternalApiService();
        MultiLevelRateLimiter rateLimiter = new MultiLevelRateLimiter(100.0, 30.0);
        BatchProcessMonitor monitor = new BatchProcessMonitor();

        // 2. 创建消息队列（容量10000）
        BlockingQueue<DataRecord> messageQueue = new LinkedBlockingQueue<>(10000);

        // 3. 初始化测试数据
        dataRepository.initTestData(5000);

        // 4. 启动监控
        monitor.start();

        // 5. 创建生产者
        MessageProducer producer = new MessageProducer(dataRepository, messageQueue);

        // 6. 创建多个消费者（模拟集群）
        int consumerCount = 5;
        List<MessageConsumer> consumers = new ArrayList<>();
        List<Thread> consumerThreads = new ArrayList<>();

        for (int i = 0; i < consumerCount; i++) {
            MessageConsumer consumer = new MessageConsumer(
                "Consumer-" + i,
                messageQueue,
                dataRepository,
                apiService,
                rateLimiter,
                monitor
            );
            consumers.add(consumer);

            Thread thread = new Thread(consumer);
            consumerThreads.add(thread);
            thread.start();
        }

        // 7. 启动生产者（在单独线程中）
        long startTime = System.currentTimeMillis();
        Thread producerThread = new Thread(producer::produce);
        producerThread.start();

        // 8. 等待生产者完成
        producerThread.join();

        // 9. 等待队列清空
        while (!messageQueue.isEmpty()) {
            Thread.sleep(100);
        }

        // 10. 停止所有消费者
        for (MessageConsumer consumer : consumers) {
            consumer.stop();
        }

        for (Thread thread : consumerThreads) {
            thread.join(5000);
        }

        long endTime = System.currentTimeMillis();

        // 11. 打印结果
        System.out.println("\n========== 处理完成 ==========");
        System.out.println("总耗时: " + (endTime - startTime) + "ms");
        System.out.println("消费者数量: " + consumerCount);
        monitor.printStats();
        System.out.println("成功数量: " + dataRepository.countByStatus("SUCCESS"));
        System.out.println("失败数量: " + dataRepository.countByStatus("FAILED"));
        System.out.println("================================\n");
    }
}
