package com.architecture.consistency.eventsourcing;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 订单仓储
 * 负责加载和保存订单聚合根
 */
@Slf4j
public class OrderRepository {

    private final EventStore eventStore;

    public OrderRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    /**
     * 根据ID加载订单（通过重放事件）
     */
    public OrderAggregate load(String orderId) {
        log.info("加载订单聚合根: orderId={}", orderId);

        // 从事件存储加载事件流
        List<DomainEvent> events = eventStore.loadEvents(orderId);

        if (events.isEmpty()) {
            log.warn("订单不存在: orderId={}", orderId);
            return null;
        }

        // 创建新的聚合根实例
        OrderAggregate order = new OrderAggregate();

        // 重放所有事件，恢复聚合根状态
        order.replayEvents(events);

        log.info("订单聚合根加载完成: orderId={}, status={}, version={}",
                orderId, order.getStatus(), order.getVersion());

        return order;
    }

    /**
     * 保存订单（保存未提交的事件）
     */
    public void save(OrderAggregate order) {
        log.info("保存订单聚合根: orderId={}", order.getOrderId());

        // 获取未提交的事件
        List<DomainEvent> uncommittedEvents = order.getUncommittedEvents();

        if (uncommittedEvents.isEmpty()) {
            log.info("没有未提交的事件，跳过保存");
            return;
        }

        // 保存事件到事件存储
        Long expectedVersion = order.getVersion() - uncommittedEvents.size();
        eventStore.saveEvents(order.getOrderId(), uncommittedEvents, expectedVersion);

        // 标记事件已提交
        order.markEventsAsCommitted();

        log.info("订单聚合根保存完成: orderId={}, version={}", order.getOrderId(), order.getVersion());
    }
}
