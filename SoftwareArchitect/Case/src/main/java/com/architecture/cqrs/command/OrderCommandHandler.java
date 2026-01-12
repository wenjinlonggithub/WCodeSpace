package com.architecture.cqrs.command;

import com.architecture.cqrs.event.OrderCreatedEvent;
import com.architecture.cqrs.event.EventPublisher;
import com.architecture.cqrs.write.OrderWriteModel;
import com.architecture.cqrs.write.OrderWriteRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CQRS - 命令处理器 (写端)
 *
 * 职责:
 * 1. 处理写操作命令
 * 2. 更新写模型
 * 3. 发布事件通知读端
 */
public class OrderCommandHandler {

    private final OrderWriteRepository writeRepository;
    private final EventPublisher eventPublisher;

    public OrderCommandHandler(OrderWriteRepository writeRepository, EventPublisher eventPublisher) {
        this.writeRepository = writeRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 处理创建订单命令
     */
    public String handle(CreateOrderCommand command) {
        System.out.println("\n[CommandHandler] 处理创建订单命令");

        // 1. 验证命令
        validateCommand(command);

        // 2. 计算总金额
        BigDecimal totalAmount = command.getItems().stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 创建写模型
        OrderWriteModel writeModel = new OrderWriteModel();
        writeModel.setId(generateOrderId());
        writeModel.setUserId(command.getUserId());
        writeModel.setTotalAmount(totalAmount);
        writeModel.setStatus("CREATED");
        writeModel.setShippingAddress(command.getShippingAddress());
        writeModel.setCreatedAt(LocalDateTime.now());

        // 4. 保存到写库
        writeRepository.save(writeModel);
        System.out.println("  ✓ 写模型已保存: " + writeModel.getId());

        // 5. 发布事件(通知读端同步)
        OrderCreatedEvent event = new OrderCreatedEvent(
            writeModel.getId(),
            writeModel.getUserId(),
            totalAmount,
            command.getItems(),
            command.getShippingAddress(),
            writeModel.getCreatedAt()
        );
        eventPublisher.publish(event);

        return writeModel.getId();
    }

    private void validateCommand(CreateOrderCommand command) {
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单不能为空");
        }
        if (command.getShippingAddress() == null || command.getShippingAddress().isEmpty()) {
            throw new IllegalArgumentException("收货地址不能为空");
        }
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
