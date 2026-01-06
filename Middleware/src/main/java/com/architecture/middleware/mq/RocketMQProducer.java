package com.architecture.middleware.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RocketMQProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendMessage(String topic, String message) {
        rocketMQTemplate.convertAndSend(topic, message);
        System.out.println("RocketMQ sent message to " + topic + ": " + message);
    }

    public void sendMessageWithTag(String topic, String tag, String message) {
        rocketMQTemplate.convertAndSend(topic + ":" + tag, message);
        System.out.println("RocketMQ sent message to " + topic + " with tag " + tag + ": " + message);
    }

    public void sendAsyncMessage(String topic, String message) {
        Message<String> msg = MessageBuilder.withPayload(message).build();
        //rocketMQTemplate.asyncSend(topic, msg, result -> {
         //   System.out.println("Async message sent result: " + result);
        //}, Duration.ofSeconds(3));
        System.out.println("RocketMQ async message sent to " + topic + ": " + message);
    }

    public void sendDelayMessage(String topic, String message, int delayLevel) {
        Message<String> msg = MessageBuilder.withPayload(message)
                .setHeader("DELAY", delayLevel)
                .build();
        rocketMQTemplate.syncSend(topic, msg);
        System.out.println("RocketMQ sent delay message to " + topic + ": " + message);
    }

    public void sendOrderlyMessage(String topic, String message, String orderId) {
        rocketMQTemplate.syncSendOrderly(topic, message, orderId);
        System.out.println("RocketMQ sent orderly message to " + topic + " with orderId " + orderId + ": " + message);
    }
}
