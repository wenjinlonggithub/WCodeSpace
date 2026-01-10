package com.architecture.advanced;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka精确一次语义（Exactly Once Semantics）演示
 * 
 * 演示如何实现端到端的精确一次处理：
 * - 幂等性生产者 + 事务性消息
 * - 事务性消费者
 * - Consume-Transform-Produce模式
 * - 重复数据的处理
 */
public class KafkaExactlyOnceExample {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaExactlyOnceExample.class);
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String INPUT_TOPIC = "raw-events";
    private static final String OUTPUT_TOPIC = "processed-events";
    private static final String CONSUMER_GROUP = "exactly-once-processor";
    private static final String TRANSACTIONAL_ID_PREFIX = "exactly-once-txn";
    
    // 用于跟踪已处理的消息，防止重复处理
    private final Set<String> processedMessageIds = ConcurrentHashMap.newKeySet();
    
    public static void main(String[] args) {
        KafkaExactlyOnceExample example = new KafkaExactlyOnceExample();
        
        logger.info("🚀 Kafka精确一次语义演示开始");
        
        // 启动生产者线程
        Thread producerThread = new Thread(example::runDataProducer);
        producerThread.setName("DataProducer");
        producerThread.start();
        
        // 启动消费-转换-生产处理器
        Thread processorThread = new Thread(example::runExactlyOnceProcessor);
        processorThread.setName("ExactlyOnceProcessor");
        processorThread.start();
        
        // 启动结果验证消费者
        Thread validatorThread = new Thread(example::runResultValidator);
        validatorThread.setName("ResultValidator");
        validatorThread.start();
        
        // 运行30秒后停止
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("🛑 停止所有处理线程");
        producerThread.interrupt();
        processorThread.interrupt();
        validatorThread.interrupt();
        
        logger.info("✅ Kafka精确一次语义演示完成");
    }
    
    /**
     * 数据生产者 - 生成原始事件数据
     */
    private void runDataProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // 启用幂等性，避免重复消息
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            int messageCount = 0;
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String messageId = "msg-" + (messageCount++);
                    String eventData = createRawEvent(messageId, "user-" + (messageCount % 100));
                    
                    ProducerRecord<String, String> record = new ProducerRecord<>(
                        INPUT_TOPIC, messageId, eventData);
                    
                    producer.send(record, (metadata, exception) -> {
                        if (exception == null) {
                            logger.debug("📤 原始事件已发送: {} -> partition={}, offset={}", 
                                       messageId, metadata.partition(), metadata.offset());
                        } else {
                            logger.error("❌ 发送失败: {}", messageId, exception);
                        }
                    });
                    
                    Thread.sleep(1000); // 每秒发送一条消息
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("生产者异常", e);
                }
            }
        }
        
        logger.info("📤 数据生产者已停止");
    }
    
    /**
     * 精确一次处理器 - 消费-转换-生产模式
     */
    private void runExactlyOnceProcessor() {
        // 消费者配置
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        // 生产者配置（事务性）
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, 
                         TRANSACTIONAL_ID_PREFIX + "-" + Thread.currentThread().getId());
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
             KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            
            // 初始化事务
            producer.initTransactions();
            
            // 订阅输入主题
            consumer.subscribe(Collections.singletonList(INPUT_TOPIC));
            logger.info("🔄 精确一次处理器已启动");
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    
                    if (!records.isEmpty()) {
                        // 开始事务
                        producer.beginTransaction();
                        
                        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                        boolean hasValidMessages = false;
                        
                        for (ConsumerRecord<String, String> record : records) {
                            String messageId = record.key();
                            
                            // 检查是否已处理过（幂等性保证）
                            if (!processedMessageIds.contains(messageId)) {
                                // 处理消息
                                String processedData = processEvent(record.value());
                                
                                // 发送处理后的消息
                                ProducerRecord<String, String> outputRecord = 
                                    new ProducerRecord<>(OUTPUT_TOPIC, messageId, processedData);
                                producer.send(outputRecord);
                                
                                // 记录已处理的消息ID
                                processedMessageIds.add(messageId);
                                hasValidMessages = true;
                                
                                logger.info("🔄 处理消息: {} -> {}", messageId, 
                                          processedData.substring(0, Math.min(50, processedData.length())) + "...");
                            } else {
                                logger.debug("⚠️ 跳过重复消息: {}", messageId);
                            }
                            
                            // 收集offset信息
                            TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                            offsets.put(tp, new OffsetAndMetadata(record.offset() + 1));
                        }
                        
                        if (hasValidMessages) {
                            // 将消费者offset作为事务的一部分提交
                            producer.sendOffsetsToTransaction(offsets, CONSUMER_GROUP);
                            
                            // 提交事务
                            producer.commitTransaction();
                            logger.debug("✅ 事务提交成功，处理了 {} 条消息", records.count());
                        } else {
                            // 没有新消息需要处理，中止事务
                            producer.abortTransaction();
                            logger.debug("🔄 无新消息，事务已中止");
                        }
                    }
                    
                } catch (Exception e) {
                    logger.error("处理异常，回滚事务", e);
                    try {
                        producer.abortTransaction();
                    } catch (Exception abortEx) {
                        logger.error("回滚事务失败", abortEx);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("处理器异常", e);
        }
        
        logger.info("🔄 精确一次处理器已停止");
    }
    
    /**
     * 结果验证器 - 验证输出数据的一致性
     */
    private void runResultValidator() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "result-validator");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(OUTPUT_TOPIC));
            
            Set<String> receivedMessages = new HashSet<>();
            int duplicateCount = 0;
            int totalMessages = 0;
            
            logger.info("🔍 结果验证器已启动");
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(2000));
                    
                    for (ConsumerRecord<String, String> record : records) {
                        String messageId = record.key();
                        totalMessages++;
                        
                        if (receivedMessages.contains(messageId)) {
                            duplicateCount++;
                            logger.warn("⚠️ 发现重复消息: {}", messageId);
                        } else {
                            receivedMessages.add(messageId);
                            logger.debug("✅ 收到处理结果: {}", messageId);
                        }
                    }
                    
                    if (totalMessages > 0 && totalMessages % 10 == 0) {
                        logger.info("📊 验证统计: 总消息={}, 唯一消息={}, 重复消息={}", 
                                  totalMessages, receivedMessages.size(), duplicateCount);
                    }
                    
                } catch (Exception e) {
                    logger.error("验证器异常", e);
                }
            }
            
            // 最终统计
            logger.info("📊 最终统计结果:");
            logger.info("   总接收消息数: {}", totalMessages);
            logger.info("   唯一消息数: {}", receivedMessages.size());
            logger.info("   重复消息数: {}", duplicateCount);
            logger.info("   重复率: {:.2f}%", 
                       totalMessages > 0 ? (duplicateCount * 100.0 / totalMessages) : 0);
            
        }
        
        logger.info("🔍 结果验证器已停止");
    }
    
    /**
     * 创建原始事件数据
     */
    private String createRawEvent(String messageId, String userId) {
        return String.format(
            "{\"messageId\":\"%s\",\"userId\":\"%s\",\"eventType\":\"page_view\"," +
            "\"page\":\"/product/%d\",\"timestamp\":%d,\"sessionId\":\"session-%s\"}",
            messageId, userId, (int)(Math.random() * 1000), 
            System.currentTimeMillis(), userId
        );
    }
    
    /**
     * 事件处理逻辑 - 模拟数据转换
     */
    private String processEvent(String rawEvent) {
        try {
            // 模拟复杂的数据处理
            Thread.sleep(50);
            
            // 简单的数据转换：添加处理时间戳和状态
            return rawEvent.replace("}", 
                String.format(",\"processedAt\":%d,\"status\":\"processed\"}", 
                             System.currentTimeMillis()));
                             
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("处理中断", e);
        }
    }
    
    /**
     * 显示精确一次语义的配置和原理
     */
    public static void showExactlyOnceConfiguration() {
        logger.info("\\n--- Exactly Once Semantics 配置 ---");
        
        logger.info("\\n🔧 生产者配置:");
        logger.info("  enable.idempotence=true - 启用幂等性生产者");
        logger.info("  transactional.id - 设置事务ID");
        logger.info("  acks=all - 等待所有副本确认");
        logger.info("  retries=Integer.MAX_VALUE - 最大重试次数");
        
        logger.info("\\n🔧 消费者配置:");
        logger.info("  enable.auto.commit=false - 禁用自动提交offset");
        logger.info("  isolation.level=read_committed - 只读取已提交的消息");
        
        logger.info("\\n📋 实现原理:");
        logger.info("1. 幂等性生产者 - 防止消息重复");
        logger.info("2. 事务性消息 - 原子性发送");
        logger.info("3. 事务性消费 - 将offset提交纳入事务");
        logger.info("4. read_committed - 消费者只看到已提交的数据");
        
        logger.info("\\n🎯 适用场景:");
        logger.info("📌 金融交易处理");
        logger.info("📌 订单状态同步");
        logger.info("📌 用户积分计算");
        logger.info("📌 实时数据分析");
        
        logger.info("\\n⚠️ 注意事项:");
        logger.info("• 性能开销较大，延迟增加");
        logger.info("• 需要合理设置事务超时时间");
        logger.info("• 消费者组需要支持事务");
        logger.info("• 需要处理僵尸事务的清理");
    }
}