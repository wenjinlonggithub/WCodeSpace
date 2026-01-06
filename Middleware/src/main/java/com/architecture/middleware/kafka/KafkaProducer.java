package com.architecture.middleware.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Message sent to topic " + topic + ": " + message);
    }

    public void sendMessageWithKey(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
        System.out.println("Message sent to topic " + topic + " with key " + key + ": " + message);
    }

    public void sendMessageWithCallback(String topic, String message) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
        
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.err.println("Failed to send message: " + message + ", error: " + throwable.getMessage());
            } else {
                System.out.println("Message sent successfully: " + message + 
                    " with offset: " + result.getRecordMetadata().offset());
            }
        });
    }
}
