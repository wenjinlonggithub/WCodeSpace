package com.architecture.cqrs;

import com.architecture.cqrs.command.CreateOrderCommand;
import com.architecture.cqrs.command.OrderCommandHandler;
import com.architecture.cqrs.event.EventPublisher;
import com.architecture.cqrs.event.OrderCreatedEvent;
import com.architecture.cqrs.event.OrderReadModelProjection;
import com.architecture.cqrs.query.OrderDTO;
import com.architecture.cqrs.query.OrderQuery;
import com.architecture.cqrs.query.OrderQueryHandler;
import com.architecture.cqrs.query.UserOrdersQuery;
import com.architecture.cqrs.read.OrderReadRepository;
import com.architecture.cqrs.write.OrderWriteRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * CQRS架构演示
 *
 * CQRS = Command Query Responsibility Segregation (命令查询职责分离)
 *
 * 核心概念:
 * 1. 命令端(Command Side): 处理写操作
 *    - Command: CreateOrderCommand
 *    - CommandHandler: OrderCommandHandler
 *    - WriteModel: 规范化设计,保证一致性
 *
 * 2. 查询端(Query Side): 处理读操作
 *    - Query: OrderQuery
 *    - QueryHandler: OrderQueryHandler
 *    - ReadModel: 非规范化设计,优化查询
 *
 * 3. 事件同步: 写端通过事件通知读端
 *    - Event: OrderCreatedEvent
 *    - Projection: OrderReadModelProjection
 *
 * 架构流程:
 * 写操作: Command → CommandHandler → WriteModel → Event
 * 读操作: Query → QueryHandler → ReadModel
 * 同步: Event → Projection → ReadModel
 */
public class CQRSArchitectureDemo {

    public static void main(String[] args) {
        System.out.println("========== CQRS架构演示 ==========\n");

        // 初始化仓储
        OrderWriteRepository writeRepository = new OrderWriteRepository();
        OrderReadRepository readRepository = new OrderReadRepository();

        // 初始化投影(用于同步读模型)
        OrderReadModelProjection projection = new OrderReadModelProjection(readRepository);

        // 初始化事件发布器(简化实现,直接调用投影)
        EventPublisher eventPublisher = new EventPublisher() {
            @Override
            public void publish(OrderCreatedEvent event) {
                System.out.println("  [EventPublisher] 发布事件: " + event);
                projection.onOrderCreated(event);
            }
        };

        // 初始化命令处理器(写端)
        OrderCommandHandler commandHandler = new OrderCommandHandler(
            writeRepository,
            eventPublisher
        );

        // 初始化查询处理器(读端)
        OrderQueryHandler queryHandler = new OrderQueryHandler(readRepository);

        // ========== 场景1: 创建订单(写操作) ==========
        System.out.println("【场景1: 创建订单 - 写操作】");
        System.out.println("=".repeat(50));

        CreateOrderCommand command1 = new CreateOrderCommand(
            "U001",
            Arrays.asList(
                new CreateOrderCommand.OrderItemData("P001", 2, new BigDecimal("7999.00")),
                new CreateOrderCommand.OrderItemData("P003", 1, new BigDecimal("1999.00"))
            ),
            "北京市朝阳区xx路xx号"
        );

        String orderId1 = commandHandler.handle(command1);
        System.out.println("\n✓ 订单创建成功: " + orderId1);

        // ========== 场景2: 查询订单(读操作) ==========
        System.out.println("\n\n【场景2: 查询订单 - 读操作】");
        System.out.println("=".repeat(50));

        OrderDTO orderDTO = queryHandler.handle(new OrderQuery(orderId1));
        displayOrderDTO(orderDTO);

        // ========== 场景3: 创建更多订单 ==========
        System.out.println("\n\n【场景3: 创建更多订单】");
        System.out.println("=".repeat(50));

        String orderId2 = commandHandler.handle(new CreateOrderCommand(
            "U001",
            Arrays.asList(
                new CreateOrderCommand.OrderItemData("P002", 1, new BigDecimal("12999.00"))
            ),
            "北京市海淀区yy路yy号"
        ));
        System.out.println("\n✓ 订单2创建成功: " + orderId2);

        String orderId3 = commandHandler.handle(new CreateOrderCommand(
            "U002",
            Arrays.asList(
                new CreateOrderCommand.OrderItemData("P001", 1, new BigDecimal("7999.00"))
            ),
            "上海市浦东新区zz路zz号"
        ));
        System.out.println("\n✓ 订单3创建成功: " + orderId3);

        // ========== 场景4: 查询用户订单列表 ==========
        System.out.println("\n\n【场景4: 查询用户订单列表】");
        System.out.println("=".repeat(50));

        List<OrderDTO> userOrders = queryHandler.handleUserOrders(
            new UserOrdersQuery("U001")
        );

        System.out.println("\n用户U001的订单:");
        userOrders.forEach(dto -> {
            System.out.println("  - 订单" + dto.getOrderId() +
                             ": " + dto.getProductNames() +
                             ", 金额: ¥" + dto.getTotalAmount());
        });

        // ========== 场景5: 演示读写分离的优势 ==========
        System.out.println("\n\n【场景5: 读写分离的优势】");
        System.out.println("=".repeat(50));
        System.out.println("✓ 写端(WriteModel): 规范化设计,保证一致性");
        System.out.println("✓ 读端(ReadModel): 非规范化,包含冗余数据");
        System.out.println("✓ 查询性能: 无需JOIN,直接返回聚合数据");
        System.out.println("✓ 独立扩展: 读写可以使用不同的数据库");

        System.out.println("\n\n========== 演示完成 ==========");
        printCQRSSummary();
    }

    private static void displayOrderDTO(OrderDTO dto) {
        System.out.println("\n【订单详情】");
        System.out.println("订单ID: " + dto.getOrderId());
        System.out.println("用户ID: " + dto.getUserId());
        System.out.println("订单状态: " + dto.getStatus());
        System.out.println("订单金额: ¥" + dto.getTotalAmount());
        System.out.println("商品数量: " + dto.getItemCount());
        System.out.println("商品列表: " + dto.getProductNames());
        System.out.println("收货地址: " + dto.getShippingAddress());
        System.out.println("创建时间: " + dto.getCreatedAt());
    }

    private static void printCQRSSummary() {
        System.out.println("\n========== CQRS架构总结 ==========");
        System.out.println("【核心概念】");
        System.out.println("1. 命令查询职责分离");
        System.out.println("   - 写操作: Command → CommandHandler → WriteModel");
        System.out.println("   - 读操作: Query → QueryHandler → ReadModel");
        System.out.println();
        System.out.println("2. 写模型(WriteModel)");
        System.out.println("   - 规范化设计");
        System.out.println("   - 保证数据一致性");
        System.out.println("   - 支持事务");
        System.out.println();
        System.out.println("3. 读模型(ReadModel)");
        System.out.println("   - 非规范化设计");
        System.out.println("   - 冗余数据优化查询");
        System.out.println("   - 可以有多个读模型");
        System.out.println();
        System.out.println("4. 事件同步");
        System.out.println("   - 写端发布事件");
        System.out.println("   - 投影(Projection)监听事件");
        System.out.println("   - 异步更新读模型");
        System.out.println();
        System.out.println("【架构优势】");
        System.out.println("✓ 读写分离: 读写操作独立优化");
        System.out.println("✓ 性能优化: 读端无需JOIN,写端无需考虑查询");
        System.out.println("✓ 独立扩展: 读写可以独立扩展");
        System.out.println("✓ 灵活性: 可以有多个读模型");
        System.out.println();
        System.out.println("【适用场景】");
        System.out.println("✓ 读多写少的系统");
        System.out.println("✓ 复杂查询需求");
        System.out.println("✓ 读写性能要求差异大");
        System.out.println("✓ 需要审计日志");
        System.out.println();
        System.out.println("【注意事项】");
        System.out.println("⚠ 最终一致性: 读写之间有延迟");
        System.out.println("⚠ 复杂度增加: 需要维护两套模型");
        System.out.println("⚠ 事件管理: 需要可靠的事件机制");
        System.out.println("=====================================");
    }
}
