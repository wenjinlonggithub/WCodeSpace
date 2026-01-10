package com.architecture.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

public class UserEventStreamsExample {
    private static final Logger logger = LoggerFactory.getLogger(UserEventStreamsExample.class);
    private static final String INPUT_TOPIC = "user-events-json";
    private static final String PURCHASE_TOPIC = "purchase-events";
    private static final String USER_ACTIVITY_TOPIC = "user-activity-summary";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String APPLICATION_ID = "user-event-streams-app";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        UserEventStreamsExample example = new UserEventStreamsExample();
        example.runUserEventStream();
    }

    public void runUserEventStream() {
        logger.info("Starting User Event Kafka Streams application");

        Properties props = createStreamsProperties();
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> userEvents = builder.stream(INPUT_TOPIC);

        KStream<String, UserEvent> parsedEvents = userEvents
                .mapValues(jsonValue -> {
                    try {
                        return objectMapper.readValue(jsonValue, UserEvent.class);
                    } catch (Exception e) {
                        logger.error("Error parsing JSON: {}", jsonValue, e);
                        return null;
                    }
                })
                .filter((key, event) -> event != null);

        KStream<String, UserEvent> purchaseEvents = parsedEvents
                .filter((key, event) -> "purchase".equals(event.getEventType()));

        purchaseEvents
                .mapValues(event -> {
                    try {
                        return objectMapper.writeValueAsString(event);
                    } catch (Exception e) {
                        logger.error("Error serializing purchase event", e);
                        return null;
                    }
                })
                .filter((key, value) -> value != null)
                .to(PURCHASE_TOPIC);

        KTable<String, Long> userActivityCount = parsedEvents
                .groupByKey()
                .count(Materialized.as("user-activity-counts"));

        KTable<Windowed<String>, Long> windowedActivityCount = parsedEvents
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)))
                .count(Materialized.as("windowed-user-activity"));

        windowedActivityCount
                .toStream()
                .map((windowedKey, count) -> {
                    String key = windowedKey.key();
                    long windowStart = windowedKey.window().start();
                    long windowEnd = windowedKey.window().end();
                    
                    UserActivitySummary summary = new UserActivitySummary();
                    summary.setUserId(key);
                    summary.setActivityCount(count);
                    summary.setWindowStart(windowStart);
                    summary.setWindowEnd(windowEnd);
                    
                    try {
                        String value = objectMapper.writeValueAsString(summary);
                        logger.info("User activity summary: {}", value);
                        return KeyValue.pair(key, value);
                    } catch (Exception e) {
                        logger.error("Error serializing activity summary", e);
                        return KeyValue.pair(key, null);
                    }
                })
                .filter((key, value) -> value != null)
                .to(USER_ACTIVITY_TOPIC);

        parsedEvents.foreach((key, event) -> 
            logger.debug("Processed event: userId={}, eventType={}, timestamp={}", 
                       event.getUserId(), event.getEventType(), event.getTimestamp()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.setStateListener((newState, oldState) -> 
            logger.info("State transition from {} to {}", oldState, newState));

        streams.setUncaughtExceptionHandler((Thread thread, Throwable exception) -> {
            logger.error("Uncaught exception in thread {}", thread.getName(), exception);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down User Event Streams application");
            streams.close(Duration.ofSeconds(10));
        }));

        streams.start();
        logger.info("User Event Streams application started");

        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        streams.close();
        logger.info("User Event Streams application stopped");
    }

    private Properties createStreamsProperties() {
        Properties props = new Properties();
        
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        
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

    public static class UserActivitySummary {
        private String userId;
        private Long activityCount;
        private Long windowStart;
        private Long windowEnd;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public Long getActivityCount() { return activityCount; }
        public void setActivityCount(Long activityCount) { this.activityCount = activityCount; }

        public Long getWindowStart() { return windowStart; }
        public void setWindowStart(Long windowStart) { this.windowStart = windowStart; }

        public Long getWindowEnd() { return windowEnd; }
        public void setWindowEnd(Long windowEnd) { this.windowEnd = windowEnd; }
    }
}