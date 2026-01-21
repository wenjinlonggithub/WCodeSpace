package com.architecture.order.service;

import com.architecture.order.client.*;
import com.architecture.order.domain.Order;
import com.architecture.order.domain.OrderStatus;
import com.architecture.order.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单服务 - 核心业务逻辑
 *
 * 这是一个典型的微服务编排场景，展示了:
 * 1. 服务间调用: 使用OpenFeign调用其他微服务
 * 2. 事务管理: 本地事务 + 分布式事务补偿
 * 3. 异常处理: 服务调用失败的处理
 * 4. 熔断降级: Resilience4j保护服务
 *
 * 源码学习要点:
 * - OpenFeign如何构建HTTP请求
 * - Ribbon如何实现负载均衡
 * - Resilience4j如何实现熔断
 * - Spring如何管理分布式事务
 *
 * @author architecture
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    // 模拟数据库存储
    private final Map<Long, Order> orderDatabase = new HashMap<>();
    private Long orderIdSequence = 1L;

    /**
     * 创建订单 - 完整流程演示
     *
     * 业务流程:
     * 1. 验证用户信息 (调用User Service)
     * 2. 查询商品信息 (调用Product Service)
     * 3. 扣减库存 (调用Inventory Service)
     * 4. 创建支付单 (调用Payment Service)
     * 5. 保存订单
     *
     * 技术要点:
     * - 微服务调用链路: Order → User/Product/Inventory/Payment
     * - 事务补偿: 库存扣减失败后需要回滚
     * - 异常处理: 任一服务调用失败的处理
     * - 日志追踪: Sleuth自动生成TraceId
     *
     * 源码分析:
     * 1. Feign调用流程:
     *    - SynchronousMethodHandler.invoke() 拦截方法调用
     *    - RequestTemplate 构建HTTP请求模板
     *    - LoadBalancerFeignClient 集成负载均衡
     *    - Ribbon 选择服务实例
     *    - Client 发送HTTP请求
     *
     * 2. 熔断降级流程:
     *    - CircuitBreakerAspect 拦截方法调用
     *    - CircuitBreaker 判断是否熔断
     *    - 熔断时直接调用fallback方法
     *
     * @param request 创建订单请求
     * @return 订单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("开始创建订单, userId={}, productId={}, quantity={}",
                request.getUserId(), request.getProductId(), request.getQuantity());

        try {
            // ==================== 步骤1: 验证用户 ====================
            // Feign调用链路:
            // 1. userServiceClient.getUserById(1L)
            // 2. Feign动态代理拦截
            // 3. 从Eureka获取user-service实例列表
            // 4. Ribbon负载均衡选择一个实例
            // 5. 发送HTTP请求: GET http://192.168.1.100:8001/users/1
            log.info("调用User Service验证用户...");
            UserDTO user = userServiceClient.getUserById(request.getUserId());
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            log.info("用户验证成功: {}", user.getUsername());

            // ==================== 步骤2: 查询商品 ====================
            log.info("调用Product Service查询商品...");
            ProductDTO product = productServiceClient.getProduct(request.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            log.info("商品查询成功: {}, 价格: {}", product.getName(), product.getPrice());

            // ==================== 步骤3: 扣减库存 ====================
            // 注意: 这是一个关键的非幂等操作
            // 如果失败需要补偿(在catch块中回滚)
            log.info("调用Inventory Service扣减库存...");
            Boolean stockDecreased = inventoryServiceClient.decreaseStock(
                    request.getProductId(),
                    request.getQuantity()
            );
            if (!stockDecreased) {
                throw new BusinessException("库存不足");
            }
            log.info("库存扣减成功");

            // ==================== 步骤4: 创建支付单 ====================
            BigDecimal totalAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(request.getQuantity()));

            log.info("调用Payment Service创建支付单...");
            PaymentServiceClient.CreatePaymentRequest paymentRequest =
                    new PaymentServiceClient.CreatePaymentRequest(
                            user.getId(),
                            "ORD" + System.currentTimeMillis(),
                            totalAmount
                    );
            PaymentDTO payment = paymentServiceClient.createPayment(paymentRequest);
            log.info("支付单创建成功, paymentId={}", payment.getId());

            // ==================== 步骤5: 保存订单 ====================
            Order order = new Order(user.getId(), product.getId(),
                    request.getQuantity(), totalAmount);
            order.setPaymentId(payment.getId());
            order.setId(orderIdSequence++);
            orderDatabase.put(order.getId(), order);

            log.info("订单创建成功, orderId={}, orderNo={}", order.getId(), order.getOrderNo());

            return convertToDTO(order, user, product, payment);

        } catch (Exception e) {
            log.error("订单创建失败", e);

            // 补偿操作: 回滚库存
            try {
                log.info("开始回滚库存...");
                inventoryServiceClient.increaseStock(
                        request.getProductId(),
                        request.getQuantity()
                );
                log.info("库存回滚成功");
            } catch (Exception rollbackEx) {
                log.error("库存回滚失败", rollbackEx);
                // 实际生产环境需要:
                // 1. 记录补偿失败日志
                // 2. 发送告警通知
                // 3. 人工介入处理
            }

            throw new BusinessException("订单创建失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询订单
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    public OrderDTO getOrder(Long orderId) {
        log.info("查询订单, orderId={}", orderId);

        Order order = orderDatabase.get(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 查询关联信息
        UserDTO user = userServiceClient.getUserById(order.getUserId());
        ProductDTO product = productServiceClient.getProduct(order.getProductId());

        return convertToDTO(order, user, product, null);
    }

    /**
     * 取消订单
     *
     * 业务逻辑:
     * 1. 更新订单状态为已取消
     * 2. 归还库存
     * 3. 取消支付单(如果已创建)
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        log.info("取消订单, orderId={}", orderId);

        Order order = orderDatabase.get(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单状态
        if (!order.getStatus().canCancel()) {
            throw new BusinessException("订单状态不允许取消");
        }

        // 归还库存
        inventoryServiceClient.increaseStock(
                order.getProductId(),
                order.getQuantity()
        );

        // 更新订单状态
        order.cancel();
        log.info("订单取消成功, orderId={}", orderId);
    }

    /**
     * 转换为DTO对象
     */
    private OrderDTO convertToDTO(Order order, UserDTO user,
                                   ProductDTO product, PaymentDTO payment) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setProductId(order.getProductId());
        dto.setQuantity(order.getQuantity());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreateTime(order.getCreateTime());

        if (user != null) {
            dto.setUserName(user.getUsername());
        }
        if (product != null) {
            dto.setProductName(product.getName());
        }

        return dto;
    }
}
