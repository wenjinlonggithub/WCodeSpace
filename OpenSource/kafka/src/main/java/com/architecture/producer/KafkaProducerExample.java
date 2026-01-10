package com.architecture.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducerExample {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerExample.class);
    private static final String TOPIC_NAME = "user-events";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        KafkaProducerExample example = new KafkaProducerExample();
        example.runSyncProducer();
        example.runAsyncProducer();
        example.runProducerWithCallback();
    }

    public void runSyncProducer() {
        logger.info("Starting synchronous producer example");
        
        Properties props = createProducerProperties();
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < 10; i++) {
                String key = "user-" + i;
                String value = "User event data for user " + i;
                
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, key, value);
                
                try {
                    RecordMetadata metadata = producer.send(record).get();
                    logger.info("Sent record with key: {} to partition: {} at offset: {}", 
                              key, metadata.partition(), metadata.offset());
                } catch (Exception e) {
                    logger.error("Error sending record with key: {}", key, e);
                }
            }
        }
        logger.info("Synchronous producer example completed");
    }

    public void runAsyncProducer() {
        logger.info("Starting asynchronous producer example");
        
        Properties props = createProducerProperties();
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < 10; i++) {
                String key = "async-user-" + i;
                String value = "Async user event data for user " + i;
                
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, key, value);
                
                Future<RecordMetadata> future = producer.send(record);
                logger.info("Sent async record with key: {}", key);
            }
            
            producer.flush();
        }
        logger.info("Asynchronous producer example completed");
    }

    public void runProducerWithCallback() {
        logger.info("Starting producer with callback example");
        
        Properties props = createProducerProperties();
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < 10; i++) {
                String key = "callback-user-" + i;
                String value = "Callback user event data for user " + i;
                
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, key, value);
                
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        if (exception == null) {
                            logger.info("Callback: Record sent successfully - Key: {}, Partition: {}, Offset: {}", 
                                      key, metadata.partition(), metadata.offset());
                        } else {
                            logger.error("Callback: Error sending record with key: {}", key, exception);
                        }
                    }
                });
            }
            
            producer.flush();
        }
        logger.info("Producer with callback example completed");
    }

    private Properties createProducerProperties() {
        Properties props = new Properties();
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        return props;
    }
}