package com.architecture.cqrs.query;

import com.architecture.cqrs.read.OrderReadModel;
import com.architecture.cqrs.read.OrderReadRepository;

import java.util.List;

/**
 * CQRS - 查询处理器 (读端)
 *
 * 职责:
 * 1. 处理读操作查询
 * 2. 从读模型获取数据
 * 3. 返回优化的DTO
 */
public class OrderQueryHandler {

    private final OrderReadRepository readRepository;

    public OrderQueryHandler(OrderReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    /**
     * 处理订单查询
     */
    public OrderDTO handle(OrderQuery query) {
        System.out.println("\n[QueryHandler] 处理订单查询: " + query.getOrderId());

        OrderReadModel readModel = readRepository.findById(query.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        // 转换为DTO
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(readModel.getId());
        dto.setUserId(readModel.getUserId());
        dto.setTotalAmount(readModel.getTotalAmount());
        dto.setStatus(readModel.getStatus());
        dto.setShippingAddress(readModel.getShippingAddress());
        dto.setItemCount(readModel.getItemCount());
        dto.setProductNames(readModel.getProductNames());
        dto.setCreatedAt(readModel.getCreatedAt());

        System.out.println("  ✓ 从读模型查询完成");
        return dto;
    }

    /**
     * 查询用户的所有订单
     */
    public List<OrderDTO> handleUserOrders(UserOrdersQuery query) {
        System.out.println("\n[QueryHandler] 查询用户订单列表: " + query.getUserId());

        List<OrderReadModel> readModels = readRepository.findByUserId(query.getUserId());

        List<OrderDTO> dtos = readModels.stream()
            .map(model -> {
                OrderDTO dto = new OrderDTO();
                dto.setOrderId(model.getId());
                dto.setUserId(model.getUserId());
                dto.setTotalAmount(model.getTotalAmount());
                dto.setStatus(model.getStatus());
                dto.setItemCount(model.getItemCount());
                dto.setCreatedAt(model.getCreatedAt());
                return dto;
            })
            .toList();

        System.out.println("  ✓ 找到 " + dtos.size() + " 个订单");
        return dtos;
    }
}
