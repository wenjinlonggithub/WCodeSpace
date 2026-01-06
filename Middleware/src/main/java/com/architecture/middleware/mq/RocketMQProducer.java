package com.architecture.middleware.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

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
        rocketMQTemplate.asyncSend(topic, MessageBuilder.withPayload(message).build(), 
            new org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener() {
                @Override
                public org.apache.rocketmq.spring.support.RocketMQHeaders executeLocalTransaction(
                    org.springframework.messaging.Message msg, Object arg) {
                    System.out.println("Async message sent: " + message);
                    return null;
                }

                @Override
                public org.apache.rocketmq.spring.support.RocketMQHeaders checkLocalTransaction(
                    org.springframework.messaging.Message msg) {
                    return null;
                }
            });
    }

    public void sendDelayMessage(String topic, String message, int delayLevel) {
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(message).build(), 3000, delayLevel);
        System.out.println("RocketMQ sent delay message to " + topic + ": " + message);
    }
}
