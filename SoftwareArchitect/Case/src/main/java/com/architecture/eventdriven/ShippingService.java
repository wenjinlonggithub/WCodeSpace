package com.architecture.eventdriven;

/**
 * 物流服务 - 事件消费者
 * 监听支付事件并创建物流单
 */
public class ShippingService {

    /**
     * 创建物流单
     */
    public void createShipment(OrderPaidEvent event) {
        System.out.println("  [ShippingService] 创建物流单");
        System.out.println("    订单号: " + event.getOrderId());
        System.out.println("    操作: 生成物流单,准备发货");
    }
}
