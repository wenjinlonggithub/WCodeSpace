package com.architecture.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;

public class JsonProducerExample {
    private static final Logger logger = LoggerFactory.getLogger(JsonProducerExample.class);
    private static final String TOPIC_NAME = "user-events-json";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public static void main(String[] args) {
        JsonProducerExample example = new JsonProducerExample();
        example.sendUserEvents();
    }

    public void sendUserEvents() {
        logger.info("Starting JSON producer example");
        
        Properties props = createProducerProperties();
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < 20; i++) {
                UserEvent event = createRandomUserEvent(i);
                String key = "user-" + event.getUserId();
                String jsonValue = objectMapper.writeValueAsString(event);
                
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, key, jsonValue);
                
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        logger.info("Sent JSON event: userId={}, eventType={}, partition={}, offset={}", 
                                  event.getUserId(), event.getEventType(), metadata.partition(), metadata.offset());
                    } else {
                        logger.error("Error sending JSON event for userId: {}", event.getUserId(), exception);
                    }
                });
                
                Thread.sleep(100);
            }
            
            producer.flush();
        } catch (Exception e) {
            logger.error("Error in JSON producer", e);
        }
        
        logger.info("JSON producer example completed");
    }

    private UserEvent createRandomUserEvent(int id) {
        String[] eventTypes = {"login", "logout", "purchase", "view_product", "add_to_cart", "search"};
        String[] products = {"laptop", "phone", "tablet", "headphones", "keyboard", "mouse"};
        
        UserEvent event = new UserEvent();
        event.setUserId("user-" + id);
        event.setEventType(eventTypes[random.nextInt(eventTypes.length)]);
        event.setTimestamp(System.currentTimeMillis());
        event.setSessionId("session-" + random.nextInt(1000));
        
        if ("purchase".equals(event.getEventType()) || "view_product".equals(event.getEventType())) {
            event.setProductId(products[random.nextInt(products.length)]);
            event.setAmount(random.nextDouble() * 1000);
        }
        
        return event;
    }

    private Properties createProducerProperties() {
        Properties props = new Properties();
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        
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