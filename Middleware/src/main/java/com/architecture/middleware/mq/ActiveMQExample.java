package com.architecture.middleware.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActiveMQExample {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(String destination, String message) {
        jmsTemplate.convertAndSend(destination, message);
        System.out.println("ActiveMQ sent message to " + destination + ": " + message);
    }

    @JmsListener(destination = "test-queue")
    public void receiveMessage(String message) {
        System.out.println("ActiveMQ received message: " + message);
    }

    @JmsListener(destination = "order-queue")
    public void receiveOrderMessage(String message) {
        System.out.println("ActiveMQ received order message: " + message);
    }
}
