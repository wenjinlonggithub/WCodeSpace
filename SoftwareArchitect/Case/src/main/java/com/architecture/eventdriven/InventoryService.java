package com.architecture.eventdriven;

/**
 * 库存服务 - 事件消费者
 * 监听订单事件并更新库存
 */
public class InventoryService {

    /**
     * 预留库存
     */
    public void reserveStock(OrderCreatedEvent event) {
        System.out.println("  [InventoryService] 预留库存");
        System.out.println("    订单号: " + event.getOrderId());
        System.out.println("    操作: 锁定库存");
    }

    /**
     * 扣减库存
     */
    public void deductStock(OrderPaidEvent event) {
        System.out.println("  [InventoryService] 扣减库存");
        System.out.println("    订单号: " + event.getOrderId());
        System.out.println("    操作: 实际扣减库存");
    }
}
