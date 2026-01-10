package com.architecture.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaConfigurationExample {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConfigurationExample.class);
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        KafkaConfigurationExample example = new KafkaConfigurationExample();
        example.demonstrateConfigurations();
    }

    public void demonstrateConfigurations() {
        logger.info("Demonstrating Kafka configurations");
        
        Properties producerProps = createHighThroughputProducerConfig();
        logConfiguration("High Throughput Producer", producerProps);
        
        Properties reliableProducerProps = createReliableProducerConfig();
        logConfiguration("Reliable Producer", reliableProducerProps);
        
        Properties consumerProps = createOptimizedConsumerConfig();
        logConfiguration("Optimized Consumer", consumerProps);
        
        Properties streamsProps = createStreamsConfig();
        logConfiguration("Kafka Streams", streamsProps);
    }

    public Properties createHighThroughputProducerConfig() {
        Properties props = new Properties();
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 10);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
        
        return props;
    }

    public Properties createReliableProducerConfig() {
        Properties props = new Properties();
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
        
        return props;
    }

    public Properties createOptimizedConsumerConfig() {
        Properties props = new Properties();
        
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "optimized-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 50000);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1048576);
        
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 40000);
        
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        return props;
    }

    public Properties createStreamsConfig() {
        Properties props = new Properties();
        
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "configuration-example-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, org.apache.kafka.common.serialization.Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, org.apache.kafka.common.serialization.Serdes.String().getClass());
        
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10485760);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
        
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.WINDOW_STORE_CHANGE_LOG_ADDITIONAL_RETENTION_MS_CONFIG, 86400000);
        
        props.put(StreamsConfig.producerPrefix(ProducerConfig.BATCH_SIZE_CONFIG), 32768);
        props.put(StreamsConfig.producerPrefix(ProducerConfig.LINGER_MS_CONFIG), 10);
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_RECORDS_CONFIG), 1000);
        
        return props;
    }

    public Properties createSecureProducerConfig() {
        Properties props = createReliableProducerConfig();
        
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config", 
                 "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                 "username=\"your-username\" password=\"your-password\";");
        
        props.put("ssl.truststore.location", "/path/to/kafka.client.truststore.jks");
        props.put("ssl.truststore.password", "truststore-password");
        props.put("ssl.keystore.location", "/path/to/kafka.client.keystore.jks");
        props.put("ssl.keystore.password", "keystore-password");
        props.put("ssl.key.password", "key-password");
        
        return props;
    }

    public Properties createAvroProducerConfig() {
        Properties props = createReliableProducerConfig();
        
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
                 "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put("schema.registry.url", "http://localhost:8081");
        
        return props;
    }

    private void logConfiguration(String configName, Properties props) {
        logger.info("=== {} Configuration ===", configName);
        props.forEach((key, value) -> logger.info("  {}: {}", key, value));
        logger.info("");
    }
}