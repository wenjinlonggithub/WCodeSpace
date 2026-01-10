package com.architecture.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

public class KafkaConsumerExample {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerExample.class);
    private static final String TOPIC_NAME = "user-events";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "user-events-consumer-group";

    public static void main(String[] args) {
        KafkaConsumerExample example = new KafkaConsumerExample();
        
        Thread autoCommitThread = new Thread(example::runAutoCommitConsumer);
        Thread manualCommitThread = new Thread(example::runManualCommitConsumer);
        Thread seekConsumerThread = new Thread(example::runSeekConsumer);
        
        autoCommitThread.start();
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        manualCommitThread.start();
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        seekConsumerThread.start();
    }

    public void runAutoCommitConsumer() {
        logger.info("Starting auto-commit consumer example");
        
        Properties props = createConsumerProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID + "-auto");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(TOPIC_NAME));
            
            int messageCount = 0;
            while (messageCount < 50) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Auto-commit consumer - Key: {}, Value: {}, Partition: {}, Offset: {}", 
                              record.key(), record.value(), record.partition(), record.offset());
                    messageCount++;
                }
                
                if (records.isEmpty()) {
                    logger.info("No records received, continuing to poll...");
                }
            }
        } catch (Exception e) {
            logger.error("Error in auto-commit consumer", e);
        }
        
        logger.info("Auto-commit consumer example completed");
    }

    public void runManualCommitConsumer() {
        logger.info("Starting manual commit consumer example");
        
        Properties props = createConsumerProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID + "-manual");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(TOPIC_NAME));
            
            int messageCount = 0;
            while (messageCount < 50) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Manual commit consumer - Key: {}, Value: {}, Partition: {}, Offset: {}", 
                              record.key(), record.value(), record.partition(), record.offset());
                    messageCount++;
                    
                    if (messageCount % 10 == 0) {
                        consumer.commitSync();
                        logger.info("Committed offset after processing {} messages", messageCount);
                    }
                }
                
                if (records.isEmpty()) {
                    logger.info("No records received, continuing to poll...");
                }
            }
            
            consumer.commitSync();
            logger.info("Final commit completed");
            
        } catch (Exception e) {
            logger.error("Error in manual commit consumer", e);
        }
        
        logger.info("Manual commit consumer example completed");
    }

    public void runSeekConsumer() {
        logger.info("Starting seek consumer example");
        
        Properties props = createConsumerProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID + "-seek");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(TOPIC_NAME));
            
            consumer.poll(Duration.ofMillis(0));
            
            Set<TopicPartition> assignedPartitions = consumer.assignment();
            logger.info("Assigned partitions: {}", assignedPartitions);
            
            for (TopicPartition partition : assignedPartitions) {
                long currentOffset = consumer.position(partition);
                logger.info("Current offset for partition {}: {}", partition.partition(), currentOffset);
                
                if (currentOffset > 5) {
                    consumer.seek(partition, currentOffset - 5);
                    logger.info("Seeking to offset {} for partition {}", currentOffset - 5, partition.partition());
                }
            }
            
            int messageCount = 0;
            while (messageCount < 20) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Seek consumer - Key: {}, Value: {}, Partition: {}, Offset: {}", 
                              record.key(), record.value(), record.partition(), record.offset());
                    messageCount++;
                }
                
                if (records.isEmpty()) {
                    break;
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in seek consumer", e);
        }
        
        logger.info("Seek consumer example completed");
    }

    private Properties createConsumerProperties() {
        Properties props = new Properties();
        
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        return props;
    }
}