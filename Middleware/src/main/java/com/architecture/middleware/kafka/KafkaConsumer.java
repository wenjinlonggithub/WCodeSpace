package com.architecture.middleware.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = "test-topic", groupId = "middleware-group")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void listenWithMetadata(
            @Payload String message,
            //@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset) {
        System.out.println("Received message from topic: " + topic + 
            //", partition: " + partition +
            ", offset: " + offset + 
            ", message: " + message);
    }

    @KafkaListener(topics = "user-topic", groupId = "user-group")
    public void listenWithRecord(ConsumerRecord<String, String> record) {
        System.out.println("Received record: " + 
            "key=" + record.key() + 
            ", value=" + record.value() + 
            ", partition=" + record.partition() + 
            ", offset=" + record.offset());
    }

    @KafkaListener(
        topicPartitions = @TopicPartition(
            topic = "partition-topic",
            partitions = {"0", "1"}
        ),
        groupId = "partition-group"
    )
    public void listenToPartition(String message) {
        System.out.println("Received message from specific partition: " + message);
    }
}
