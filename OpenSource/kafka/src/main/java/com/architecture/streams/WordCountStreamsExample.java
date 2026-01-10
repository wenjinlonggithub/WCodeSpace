package com.architecture.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

public class WordCountStreamsExample {
    private static final Logger logger = LoggerFactory.getLogger(WordCountStreamsExample.class);
    private static final String INPUT_TOPIC = "text-input";
    private static final String OUTPUT_TOPIC = "word-count-output";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String APPLICATION_ID = "word-count-streams-app";

    public static void main(String[] args) {
        WordCountStreamsExample example = new WordCountStreamsExample();
        example.runWordCountStream();
    }

    public void runWordCountStream() {
        logger.info("Starting Word Count Kafka Streams application");

        Properties props = createStreamsProperties();
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> textLines = builder.stream(INPUT_TOPIC);

        KTable<String, Long> wordCounts = textLines
                .flatMapValues(textLine -> {
                    logger.debug("Processing line: {}", textLine);
                    return java.util.Arrays.asList(textLine.toLowerCase().split("\\W+"));
                })
                .filter((key, word) -> word.length() > 0)
                .groupBy((key, word) -> word)
                .count(Materialized.as("counts-store"));

        wordCounts.toStream().to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));

        wordCounts.toStream().foreach((word, count) -> 
            logger.info("Word: {} -> Count: {}", word, count));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.setStateListener((newState, oldState) -> 
            logger.info("State transition from {} to {}", oldState, newState));

        streams.setUncaughtExceptionHandler((Thread thread, Throwable exception) -> {
            logger.error("Uncaught exception in thread {}", thread.getName(), exception);
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Word Count Streams application");
            streams.close(Duration.ofSeconds(10));
        }));

        streams.start();
        logger.info("Word Count Streams application started");

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        streams.close();
        logger.info("Word Count Streams application stopped");
    }

    private Properties createStreamsProperties() {
        Properties props = new Properties();
        
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        
        return props;
    }
}