package com.architecture.advanced;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Kafka事务性生产者演示
 * 
 * 演示Kafka的事务特性：
 * - 幂等性生产者
 * - 事务性消息发送
 * - 原子性操作
 * - 一致性保证
 */
public class KafkaTransactionalProducerExample {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaTransactionalProducerExample.class);
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC_ORDERS = "orders";
    private static final String TOPIC_PAYMENTS = "payments";
    private static final String TRANSACTIONAL_ID = "order-payment-transaction";
    
    public static void main(String[] args) {
        KafkaTransactionalProducerExample example = new KafkaTransactionalProducerExample();
        
        logger.info("🚀 Kafka事务性生产者演示开始");
        
        // 1. 演示基本事务操作
        example.demonstrateBasicTransaction();
        
        // 2. 演示事务回滚
        example.demonstrateTransactionRollback();
        
        // 3. 演示跨Topic事务
        example.demonstrateMultiTopicTransaction();
        
        logger.info("✅ Kafka事务性生产者演示完成");
    }
    
    /**
     * 创建事务性生产者
     */
    private KafkaProducer<String, String> createTransactionalProducer() {
        Properties props = new Properties();
        
        // 基础配置
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // 事务配置
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, TRANSACTIONAL_ID);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // 性能优化配置
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        return new KafkaProducer<>(props);
    }
    
    /**
     * 演示基本事务操作
     */
    private void demonstrateBasicTransaction() {
        logger.info("\\n--- 基本事务操作演示 ---");
        
        KafkaProducer<String, String> producer = createTransactionalProducer();
        
        try {
            // 初始化事务
            producer.initTransactions();
            logger.info("📝 事务初始化完成");
            
            // 开始事务
            producer.beginTransaction();
            logger.info("📝 开始事务");
            
            // 发送消息
            String orderId = "order-" + System.currentTimeMillis();
            ProducerRecord<String, String> record = new ProducerRecord<>(
                TOPIC_ORDERS, 
                orderId, 
                createOrderJson(orderId, "用户001", "商品A", 99.99)
            );
            
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("✅ 消息发送成功: topic={}, partition={}, offset={}", 
                              metadata.topic(), metadata.partition(), metadata.offset());
                } else {
                    logger.error("❌ 消息发送失败", exception);
                }
            });
            
            // 提交事务
            producer.commitTransaction();
            logger.info("✅ 事务提交成功");
            
        } catch (Exception e) {
            logger.error("❌ 事务执行失败", e);
            try {
                producer.abortTransaction();
                logger.info("🔄 事务已回滚");
            } catch (Exception abortException) {
                logger.error("❌ 事务回滚失败", abortException);
            }
        } finally {
            producer.close();
        }
    }
    
    /**
     * 演示事务回滚
     */
    private void demonstrateTransactionRollback() {
        logger.info("\\n--- 事务回滚演示 ---");
        
        KafkaProducer<String, String> producer = createTransactionalProducer();
        
        try {
            producer.initTransactions();
            producer.beginTransaction();
            logger.info("📝 开始事务（故意回滚）");
            
            // 发送第一条消息
            String orderId1 = "order-rollback-1-" + System.currentTimeMillis();
            producer.send(new ProducerRecord<>(
                TOPIC_ORDERS, 
                orderId1, 
                createOrderJson(orderId1, "用户002", "商品B", 199.99)
            ));
            
            // 发送第二条消息
            String orderId2 = "order-rollback-2-" + System.currentTimeMillis();
            producer.send(new ProducerRecord<>(
                TOPIC_ORDERS, 
                orderId2, 
                createOrderJson(orderId2, "用户003", "商品C", 299.99)
            ));
            
            logger.info("📝 发送了两条消息");
            
            // 模拟业务异常，触发回滚
            if (Math.random() > 0.5) { // 随机触发异常
                throw new RuntimeException("模拟业务异常");
            }
            
            producer.commitTransaction();
            logger.info("✅ 事务提交成功");
            
        } catch (Exception e) {
            logger.info("💥 发生异常: {}", e.getMessage());
            try {
                producer.abortTransaction();
                logger.info("🔄 事务回滚完成 - 两条消息都不会被消费者看到");
            } catch (Exception abortException) {
                logger.error("❌ 事务回滚失败", abortException);
            }
        } finally {
            producer.close();
        }
    }
    
    /**
     * 演示跨Topic事务
     */
    private void demonstrateMultiTopicTransaction() {
        logger.info("\\n--- 跨Topic事务演示 ---");
        
        KafkaProducer<String, String> producer = createTransactionalProducer();
        
        try {
            producer.initTransactions();
            producer.beginTransaction();
            logger.info("📝 开始跨Topic事务");
            
            String transactionId = "txn-" + System.currentTimeMillis();
            
            // 发送订单消息到orders topic
            String orderId = "order-multi-" + System.currentTimeMillis();
            producer.send(new ProducerRecord<>(
                TOPIC_ORDERS,
                orderId,
                createOrderJson(orderId, "用户004", "商品D", 399.99)
            )).get(); // 同步等待确保发送成功
            
            logger.info("📦 订单消息已发送: {}", orderId);
            
            // 发送支付消息到payments topic  
            String paymentId = "payment-" + transactionId;
            producer.send(new ProducerRecord<>(
                TOPIC_PAYMENTS,
                paymentId,
                createPaymentJson(paymentId, orderId, 399.99, "COMPLETED")
            )).get(); // 同步等待确保发送成功
            
            logger.info("💳 支付消息已发送: {}", paymentId);
            
            // 模拟额外的业务逻辑
            Thread.sleep(100);
            
            // 提交事务 - 确保两个Topic的消息要么都成功，要么都失败
            producer.commitTransaction();
            logger.info("✅ 跨Topic事务提交成功 - 订单和支付消息都已确认");
            
        } catch (Exception e) {
            logger.error("❌ 跨Topic事务失败", e);
            try {
                producer.abortTransaction();
                logger.info("🔄 跨Topic事务回滚 - 订单和支付消息都被撤销");
            } catch (Exception abortException) {
                logger.error("❌ 事务回滚失败", abortException);
            }
        } finally {
            producer.close();
        }
    }
    
    /**
     * 创建订单JSON
     */
    private String createOrderJson(String orderId, String userId, String productName, double amount) {
        return String.format(
            "{\"orderId\":\"%s\",\"userId\":\"%s\",\"productName\":\"%s\",\"amount\":%.2f,\"timestamp\":%d,\"status\":\"CREATED\"}",
            orderId, userId, productName, amount, System.currentTimeMillis()
        );
    }
    
    /**
     * 创建支付JSON
     */
    private String createPaymentJson(String paymentId, String orderId, double amount, String status) {
        return String.format(
            "{\"paymentId\":\"%s\",\"orderId\":\"%s\",\"amount\":%.2f,\"status\":\"%s\",\"timestamp\":%d}",
            paymentId, orderId, amount, status, System.currentTimeMillis()
        );
    }
    
    /**
     * 演示事务配置和最佳实践
     */
    public static void showTransactionConfiguration() {
        logger.info("\\n--- Kafka事务配置说明 ---");
        logger.info("1. transactional.id - 事务ID，用于标识生产者实例");
        logger.info("2. enable.idempotence - 启用幂等性，确保消息不重复");
        logger.info("3. acks=all - 等待所有副本确认");
        logger.info("4. retries - 重试次数设置");
        logger.info("5. max.in.flight.requests.per.connection - 控制并发请求数");
        
        logger.info("\\n--- 事务API使用步骤 ---");
        logger.info("1. initTransactions() - 初始化事务");
        logger.info("2. beginTransaction() - 开始事务");
        logger.info("3. send() - 发送消息");
        logger.info("4. commitTransaction() - 提交事务");
        logger.info("5. abortTransaction() - 回滚事务（异常情况）");
        
        logger.info("\\n--- 事务特性 ---");
        logger.info("✅ 原子性 - 事务中的所有操作要么全部成功，要么全部失败");
        logger.info("✅ 一致性 - 事务执行前后数据状态一致");
        logger.info("✅ 隔离性 - 事务执行过程中的中间状态对其他事务不可见");
        logger.info("✅ 持久性 - 事务提交后数据持久化存储");
        
        logger.info("\\n--- 使用场景 ---");
        logger.info("🎯 跨Topic原子写入");
        logger.info("🎯 消息去重处理");
        logger.info("🎯 端到端的一致性保证");
        logger.info("🎯 复杂业务流程的可靠性");
    }
}