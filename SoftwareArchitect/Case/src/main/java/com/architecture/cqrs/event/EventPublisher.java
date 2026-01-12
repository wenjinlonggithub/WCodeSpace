package com.architecture.cqrs.event;

/**
 * CQRS - 事件发布器
 */
public interface EventPublisher {

    void publish(OrderCreatedEvent event);
}
