package com.architecture.middleware.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    @RabbitListener(queues = RabbitMQConfig.DIRECT_QUEUE)
    public void receiveDirectMessage(String message) {
        System.out.println("Direct queue received: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE_1)
    public void receiveTopicMessage1(String message) {
        System.out.println("Topic queue 1 received: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE_2)
    public void receiveTopicMessage2(String message) {
        System.out.println("Topic queue 2 received: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.FANOUT_QUEUE_1)
    public void receiveFanoutMessage1(String message) {
        System.out.println("Fanout queue 1 received: " + message);
    }

    @RabbitListener(queues = RabbitMQConfig.FANOUT_QUEUE_2)
    public void receiveFanoutMessage2(String message) {
        System.out.println("Fanout queue 2 received: " + message);
    }
}
