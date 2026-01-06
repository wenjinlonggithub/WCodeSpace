package com.architecture.middleware.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DIRECT_QUEUE = "direct.queue";
    public static final String DIRECT_EXCHANGE = "direct.exchange";
    public static final String DIRECT_ROUTING_KEY = "direct.routing.key";

    public static final String TOPIC_QUEUE_1 = "topic.queue.1";
    public static final String TOPIC_QUEUE_2 = "topic.queue.2";
    public static final String TOPIC_EXCHANGE = "topic.exchange";

    public static final String FANOUT_QUEUE_1 = "fanout.queue.1";
    public static final String FANOUT_QUEUE_2 = "fanout.queue.2";
    public static final String FANOUT_EXCHANGE = "fanout.exchange";

    @Bean
    public Queue directQueue() {
        return new Queue(DIRECT_QUEUE, true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    @Bean
    public Binding directBinding() {
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(DIRECT_ROUTING_KEY);
    }

    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE_1, true);
    }

    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE_2, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Binding topicBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.#");
    }

    @Bean
    public Binding topicBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.message.*");
    }

    @Bean
    public Queue fanoutQueue1() {
        return new Queue(FANOUT_QUEUE_1, true);
    }

    @Bean
    public Queue fanoutQueue2() {
        return new Queue(FANOUT_QUEUE_2, true);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
    }
}
