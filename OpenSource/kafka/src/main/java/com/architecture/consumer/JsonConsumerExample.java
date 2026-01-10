package com.architecture.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class JsonConsumerExample {
    private static final Logger logger = LoggerFactory.getLogger(JsonConsumerExample.class);
    private static final String TOPIC_NAME = "user-events-json";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "json-consumer-group";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        JsonConsumerExample example = new JsonConsumerExample();
        example.consumeUserEvents();
    }

    public void consumeUserEvents() {
        logger.info("Starting JSON consumer example");
        
        Properties props = createConsumerProperties();
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(TOPIC_NAME));
            
            int messageCount = 0;
            while (messageCount < 100) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        UserEvent event = objectMapper.readValue(record.value(), UserEvent.class);
                        processUserEvent(event, record);
                        messageCount++;
                    } catch (Exception e) {
                        logger.error("Error deserializing JSON message: {}", record.value(), e);
                    }
                }
                
                if (records.isEmpty()) {
                    logger.info("No records received, continuing to poll...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in JSON consumer", e);
        }
        
        logger.info("JSON consumer example completed");
    }

    private void processUserEvent(UserEvent event, ConsumerRecord<String, String> record) {
        logger.info("Processing event - UserId: {}, EventType: {}, Timestamp: {}, Partition: {}, Offset: {}", 
                  event.getUserId(), event.getEventType(), event.getTimestamp(), 
                  record.partition(), record.offset());
        
        switch (event.getEventType()) {
            case "login":
                handleLoginEvent(event);
                break;
            case "logout":
                handleLogoutEvent(event);
                break;
            case "purchase":
                handlePurchaseEvent(event);
                break;
            case "view_product":
                handleProductViewEvent(event);
                break;
            case "add_to_cart":
                handleAddToCartEvent(event);
                break;
            case "search":
                handleSearchEvent(event);
                break;
            default:
                logger.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleLoginEvent(UserEvent event) {
        logger.info("User {} logged in at {}", event.getUserId(), event.getTimestamp());
    }

    private void handleLogoutEvent(UserEvent event) {
        logger.info("User {} logged out at {}", event.getUserId(), event.getTimestamp());
    }

    private void handlePurchaseEvent(UserEvent event) {
        logger.info("User {} purchased {} for ${}", 
                  event.getUserId(), event.getProductId(), event.getAmount());
    }

    private void handleProductViewEvent(UserEvent event) {
        logger.info("User {} viewed product {}", event.getUserId(), event.getProductId());
    }

    private void handleAddToCartEvent(UserEvent event) {
        logger.info("User {} added item to cart", event.getUserId());
    }

    private void handleSearchEvent(UserEvent event) {
        logger.info("User {} performed search", event.getUserId());
    }

    private Properties createConsumerProperties() {
        Properties props = new Properties();
        
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        
        return props;
    }

    public static class UserEvent {
        private String userId;
        private String eventType;
        private long timestamp;
        private String sessionId;
        private String productId;
        private Double amount;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }
}