package com.architecture.consistency.eventsourcing;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 事件存储
 * 存储所有领域事件
 */
@Slf4j
public class EventStore {

    /**
     * 事件存储：aggregateId -> 事件列表
     */
    private final Map<String, List<DomainEvent>> eventStreams = new ConcurrentHashMap<>();

    /**
     * 保存事件
     */
    public void saveEvents(String aggregateId, List<DomainEvent> events, Long expectedVersion) {
        log.info("保存事件: aggregateId={}, eventCount={}, expectedVersion={}",
                aggregateId, events.size(), expectedVersion);

        // 获取或创建事件流
        List<DomainEvent> eventStream = eventStreams.computeIfAbsent(
                aggregateId,
                k -> Collections.synchronizedList(new ArrayList<>())
        );

        // 乐观锁检查
        long currentVersion = eventStream.size();
        if (expectedVersion != null && currentVersion != expectedVersion) {
            throw new ConcurrentModificationException(
                    String.format("版本冲突: expected=%d, current=%d", expectedVersion, currentVersion)
            );
        }

        // 保存事件
        synchronized (eventStream) {
            eventStream.addAll(events);
        }

        log.info("事件保存成功: aggregateId={}, newVersion={}", aggregateId, eventStream.size());

        // 发布事件到事件总线（用于通知其他服务）
        for (DomainEvent event : events) {
            publishEvent(event);
        }
    }

    /**
     * 加载事件流
     */
    public List<DomainEvent> loadEvents(String aggregateId) {
        List<DomainEvent> events = eventStreams.getOrDefault(aggregateId, new ArrayList<>());
        log.info("加载事件流: aggregateId={}, eventCount={}", aggregateId, events.size());
        return new ArrayList<>(events);
    }

    /**
     * 加载指定版本之后的事件
     */
    public List<DomainEvent> loadEventsAfterVersion(String aggregateId, Long afterVersion) {
        List<DomainEvent> events = eventStreams.getOrDefault(aggregateId, new ArrayList<>());
        return events.stream()
                .filter(event -> event.getVersion() > afterVersion)
                .collect(Collectors.toList());
    }

    /**
     * 发布事件到事件总线
     */
    private void publishEvent(DomainEvent event) {
        log.info("发布事件: eventId={}, eventType={}, aggregateId={}",
                event.getEventId(), event.getEventType(), event.getAggregateId());

        // 这里可以集成消息队列，如Kafka、RabbitMQ等
        // 通知其他微服务处理该事件
    }

    /**
     * 获取事件总数
     */
    public int getTotalEventCount() {
        return eventStreams.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * 获取聚合根数量
     */
    public int getAggregateCount() {
        return eventStreams.size();
    }
}
