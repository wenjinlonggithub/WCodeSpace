package com.architecture.hexagonal;

import com.architecture.hexagonal.adapter.in.console.ConsoleOrderController;
import com.architecture.hexagonal.adapter.out.messaging.ConsoleEventPublisher;
import com.architecture.hexagonal.adapter.out.persistence.InMemoryOrderRepository;
import com.architecture.hexagonal.adapter.out.persistence.InMemoryProductRepository;
import com.architecture.hexagonal.application.port.in.CreateOrderUseCase;
import com.architecture.hexagonal.application.port.in.GetOrderUseCase;
import com.architecture.hexagonal.application.port.out.EventPublisher;
import com.architecture.hexagonal.application.port.out.OrderRepository;
import com.architecture.hexagonal.application.port.out.ProductRepository;
import com.architecture.hexagonal.application.service.CreateOrderService;
import com.architecture.hexagonal.application.service.GetOrderService;

/**
 * 六边形架构演示
 *
 * 架构层次:
 * 1. 领域层 (Domain): Order, Money等 - 核心业务逻辑
 * 2. 应用层 (Application): CreateOrderService - 用例编排
 * 3. 端口层 (Ports): 输入端口(UseCase)和输出端口(Repository)
 * 4. 适配器层 (Adapters):
 *    - 主适配器(In): ConsoleOrderController - 驱动应用
 *    - 次适配器(Out): InMemoryOrderRepository - 被应用驱动
 *
 * 核心特点:
 * - 依赖倒置: 核心不依赖外部,外部依赖核心
 * - 端口适配器: 通过接口隔离外部依赖
 * - 高可测试性: 可以Mock所有外部依赖
 * - 技术无关: 核心业务不依赖任何框架
 */
public class HexagonalArchitectureDemo {

    public static void main(String[] args) {
        System.out.println("========== 六边形架构演示 ==========\n");

        // 组装应用 - 依赖注入
        // 1. 次适配器(输出适配器) - 实现输出端口
        OrderRepository orderRepository = new InMemoryOrderRepository();
        ProductRepository productRepository = new InMemoryProductRepository();
        EventPublisher eventPublisher = new ConsoleEventPublisher();

        // 2. 应用服务 - 实现输入端口
        CreateOrderUseCase createOrderUseCase = new CreateOrderService(
            orderRepository,
            productRepository,
            eventPublisher
        );
        GetOrderUseCase getOrderUseCase = new GetOrderService(orderRepository);

        // 3. 主适配器(输入适配器) - 使用输入端口
        ConsoleOrderController controller = new ConsoleOrderController(
            createOrderUseCase,
            getOrderUseCase
        );

        // 演示场景
        System.out.println("【场景1: 创建订单 - iPhone 15 Pro】");
        System.out.println("=".repeat(50));
        controller.handleCreateOrder("C001", "P001", 2);

        System.out.println("\n\n【场景2: 创建订单 - MacBook Pro】");
        System.out.println("=".repeat(50));
        controller.handleCreateOrder("C001", "P002", 1);

        System.out.println("\n\n【场景3: 创建订单 - AirPods Pro】");
        System.out.println("=".repeat(50));
        controller.handleCreateOrder("C002", "P003", 3);

        System.out.println("\n\n【场景4: 错误处理 - 商品不存在】");
        System.out.println("=".repeat(50));
        controller.handleCreateOrder("C001", "P999", 1);

        System.out.println("\n\n【场景5: 错误处理 - 数量无效】");
        System.out.println("=".repeat(50));
        controller.handleCreateOrder("C001", "P001", -1);

        System.out.println("\n\n========== 演示完成 ==========");
        printArchitectureSummary();
    }

    private static void printArchitectureSummary() {
        System.out.println("\n========== 六边形架构总结 ==========");
        System.out.println("【核心概念】");
        System.out.println("1. 端口(Ports): 定义接口");
        System.out.println("   - 输入端口(Primary Port): UseCase接口");
        System.out.println("   - 输出端口(Secondary Port): Repository接口");
        System.out.println();
        System.out.println("2. 适配器(Adapters): 实现接口");
        System.out.println("   - 主适配器(Primary Adapter): Controller, REST API");
        System.out.println("   - 次适配器(Secondary Adapter): Database, Message Queue");
        System.out.println();
        System.out.println("3. 依赖方向:");
        System.out.println("   外部 → 端口 → 应用核心");
        System.out.println("   核心定义接口,外部实现接口");
        System.out.println();
        System.out.println("【架构优势】");
        System.out.println("✓ 依赖倒置: 核心不依赖外部技术");
        System.out.println("✓ 高可测试性: 可以Mock所有端口");
        System.out.println("✓ 技术无关: 易于更换技术实现");
        System.out.println("✓ 业务清晰: 核心领域逻辑纯粹");
        System.out.println("✓ 灵活扩展: 可以添加不同适配器");
        System.out.println();
        System.out.println("【与传统分层架构的区别】");
        System.out.println("传统分层: Controller → Service → DAO → DB");
        System.out.println("           (上层依赖下层,业务依赖技术)");
        System.out.println();
        System.out.println("六边形: Adapter → Port → Application Core");
        System.out.println("        (外部依赖核心,技术依赖业务)");
        System.out.println();
        System.out.println("【适用场景】");
        System.out.println("✓ 复杂业务领域");
        System.out.println("✓ 需要高可测试性");
        System.out.println("✓ 多渠道接入(Web/Mobile/API)");
        System.out.println("✓ 技术栈不稳定");
        System.out.println("=====================================");
    }
}
