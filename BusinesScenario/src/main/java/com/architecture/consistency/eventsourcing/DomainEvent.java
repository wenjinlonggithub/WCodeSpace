package com.architecture.consistency.eventsourcing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 领域事件基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 聚合根ID
     */
    private String aggregateId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件版本号
     */
    private Long version;

    /**
     * 事件发生时间
     */
    private LocalDateTime occurredAt;

    /**
     * 应用事件到聚合根
     */
    public abstract void applyTo(Object aggregate);
}
