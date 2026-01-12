package com.architecture.hexagonal.application.port.out;

import com.architecture.hexagonal.domain.DomainEvent;

import java.util.List;

/**
 * 输出端口 - 事件发布器接口
 */
public interface EventPublisher {

    void publish(DomainEvent event);

    void publishAll(List<DomainEvent> events);
}
