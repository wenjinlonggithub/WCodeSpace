package com.architecture.ddd.domain.event;

import java.time.LocalDateTime;

/**
 * DDD - 领域事件基类
 */
public abstract class DomainEvent {

    private final LocalDateTime occurredOn;

    public DomainEvent() {
        this.occurredOn = LocalDateTime.now();
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
