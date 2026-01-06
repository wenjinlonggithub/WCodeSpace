package com.architecture.middleware.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendDirectMessage(String message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.DIRECT_EXCHANGE,
            RabbitMQConfig.DIRECT_ROUTING_KEY,
            message
        );
        System.out.println("Direct message sent: " + message);
    }

    public void sendTopicMessage(String routingKey, String message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TOPIC_EXCHANGE,
            routingKey,
            message
        );
        System.out.println("Topic message sent with routing key " + routingKey + ": " + message);
    }

    public void sendFanoutMessage(String message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.FANOUT_EXCHANGE,
            "",
            message
        );
        System.out.println("Fanout message sent: " + message);
    }
}
