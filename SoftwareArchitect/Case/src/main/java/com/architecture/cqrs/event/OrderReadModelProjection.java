package com.architecture.cqrs.event;

import com.architecture.cqrs.read.OrderReadModel;
import com.architecture.cqrs.read.OrderReadRepository;

import java.util.stream.Collectors;

/**
 * CQRS - 读模型投影
 *
 * 职责:监听事件并更新读模型
 */
public class OrderReadModelProjection {

    private final OrderReadRepository readRepository;

    public OrderReadModelProjection(OrderReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    /**
     * 处理订单创建事件,更新读模型
     */
    public void onOrderCreated(OrderCreatedEvent event) {
        System.out.println("  [Projection] 同步读模型: " + event.getOrderId());

        // 创建读模型(可以包含冗余数据以优化查询)
        OrderReadModel readModel = new OrderReadModel();
        readModel.setId(event.getOrderId());
        readModel.setUserId(event.getUserId());
        readModel.setTotalAmount(event.getTotalAmount());
        readModel.setStatus("CREATED");
        readModel.setShippingAddress(event.getShippingAddress());

        // 冗余数据:商品数量
        readModel.setItemCount(event.getItems().size());

        // 冗余数据:商品名称列表(避免JOIN查询)
        String productNames = event.getItems().stream()
            .map(item -> "商品" + item.getProductId())
            .collect(Collectors.joining(", "));
        readModel.setProductNames(productNames);

        readModel.setCreatedAt(event.getOccurredOn());

        // 保存到读库
        readRepository.save(readModel);
        System.out.println("  ✓ 读模型已同步");
    }
}
