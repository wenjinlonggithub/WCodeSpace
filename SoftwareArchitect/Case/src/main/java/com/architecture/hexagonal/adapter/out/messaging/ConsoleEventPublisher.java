package com.architecture.hexagonal.adapter.out.messaging;

import com.architecture.hexagonal.application.port.out.EventPublisher;
import com.architecture.hexagonal.domain.DomainEvent;

import java.util.List;

/**
 * 次适配器 - 控制台事件发布器
 * 实际应用中可以替换为RabbitMQ、Kafka等
 */
public class ConsoleEventPublisher implements EventPublisher {

    @Override
    public void publish(DomainEvent event) {
        System.out.println("[EventPublisher] 发布事件: " + event);
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
