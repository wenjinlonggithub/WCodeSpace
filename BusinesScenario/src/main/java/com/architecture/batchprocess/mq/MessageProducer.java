package com.architecture.batchprocess.mq;

import com.architecture.batchprocess.model.DataRecord;
import com.architecture.batchprocess.repository.DataRepository;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 方案2：消息队列生产者
 * 负责从数据库分页查询数据并推送到队列
 */
public class MessageProducer {

    private final DataRepository dataRepository;
    private final BlockingQueue<DataRecord> messageQueue;
    private static final int PAGE_SIZE = 1000;

    public MessageProducer(DataRepository dataRepository, BlockingQueue<DataRecord> messageQueue) {
        this.dataRepository = dataRepository;
        this.messageQueue = messageQueue;
    }

    /**
     * 启动生产者，将数据推送到队列
     */
    public void produce() {
        System.out.println("生产者启动，开始推送数据到队列...");

        long currentId = 0;
        int totalProduced = 0;

        try {
            while (true) {
                // 分页查询数据
                List<DataRecord> records = dataRepository.findByIdGreaterThan(currentId, PAGE_SIZE);

                if (records.isEmpty()) {
                    System.out.println("生产者完成，共推送 " + totalProduced + " 条数据");
                    break;
                }

                // 推送到队列
                for (DataRecord record : records) {
                    messageQueue.put(record);
                    totalProduced++;
                }

                currentId = records.get(records.size() - 1).getId();

                if (totalProduced % 1000 == 0) {
                    System.out.println("生产者已推送: " + totalProduced + " 条");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("生产者被中断");
        }
    }
}
