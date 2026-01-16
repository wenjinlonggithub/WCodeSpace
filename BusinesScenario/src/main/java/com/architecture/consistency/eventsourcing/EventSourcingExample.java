package com.architecture.consistency.eventsourcing;

import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.math.BigDecimal;

/**
 * 事件溯源示例
 *
 * 核心思想：
 * 1. 不存储实体的当前状态，而是存储导致状态变化的所有事件
 * 2. 通过重放事件流来恢复实体的当前状态
 * 3. 事件是不可变的，天然支持审计和历史追溯
 *
 * 优点：
 * - 完整的历史记录，可以回溯任意时间点的状态
 * - 天然支持审计和调试
 * - 事件可以作为集成的数据源
 * - 支持时间旅行（查看历史状态）
 *
 * 缺点：
 * - 查询复杂，需要重放事件
 * - 事件结构变更需要迁移
 * - 存储成本较高
 * - 学习曲线陡峭
 */
@Slf4j
public class EventSourcingExample {

    public static void main(String[] args) {
        log.info("=== 事件溯源示例开始 ===\n");

        // 初始化组件
        EventStore eventStore = new EventStore();
        OrderRepository orderRepository = new OrderRepository(eventStore);

        String orderId = "ORD" + System.currentTimeMillis();

        // 场景1：创建订单
        log.info("【场景1】创建订单");
        OrderAggregate order = new OrderAggregate();
        order.createOrder(orderId, "user001", "iPhone 15 Pro", new BigDecimal("7999.00"));
        orderRepository.save(order);

        log.info("\n" + "=".repeat(80) + "\n");

        // 场景2：支付订单
        log.info("【场景2】支付订单");
        OrderAggregate loadedOrder = orderRepository.load(orderId);
        loadedOrder.payOrder();
        orderRepository.save(loadedOrder);

        log.info("\n" + "=".repeat(80) + "\n");

        // 场景3：查看订单当前状态（通过重放事件）
        log.info("【场景3】查看订单当前状态");
        OrderAggregate currentOrder = orderRepository.load(orderId);
        log.info("订单ID: {}", currentOrder.getOrderId());
        log.info("用户ID: {}", currentOrder.getUserId());
        log.info("商品: {}", currentOrder.getProductName());
        log.info("金额: {}", currentOrder.getAmount());
        log.info("状态: {}", currentOrder.getStatus());
        log.info("版本: {}", currentOrder.getVersion());

        log.info("\n" + "=".repeat(80) + "\n");

        // 场景4：创建另一个订单并取消
        log.info("【场景4】创建订单并取消");
        String orderId2 = "ORD" + (System.currentTimeMillis() + 1);
        OrderAggregate order2 = new OrderAggregate();
        order2.createOrder(orderId2, "user002", "MacBook Pro", new BigDecimal("19999.00"));
        orderRepository.save(order2);

        OrderAggregate loadedOrder2 = orderRepository.load(orderId2);
        loadedOrder2.cancelOrder("用户主动取消");
        orderRepository.save(loadedOrder2);

        log.info("\n" + "=".repeat(80) + "\n");

        // 场景5：查看事件存储统计
        log.info("【场景5】事件存储统计");
        log.info("聚合根数量: {}", eventStore.getAggregateCount());
        log.info("事件总数: {}", eventStore.getTotalEventCount());

        log.info("\n" + "=".repeat(80) + "\n");

        // 场景6：查看完整事件流
        log.info("【场景6】查看订单1的完整事件流");
        var events = eventStore.loadEvents(orderId);
        for (DomainEvent event : events) {
            log.info("事件: type={}, version={}, occurredAt={}",
                    event.getEventType(), event.getVersion(), event.getOccurredAt());
        }

        log.info("\n=== 事件溯源示例结束 ===");
    }
}
