package com.architecture.business.order;

import com.architecture.business.user.UserService;
import com.architecture.business.payment.PaymentService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * 演示 Dubbo 服务间调用：
 * OrderService -> UserService (验证用户)
 * OrderService -> PaymentService (处理支付)
 *
 * 配置方式：
 * 1. XML 配置：
 *    <dubbo:reference id="userService"
 *                     interface="com.architecture.business.user.UserService"
 *                     version="1.0.0" timeout="3000" />
 *
 * 2. 注解配置：
 *    @DubboReference(version = "1.0.0", timeout = 3000)
 *    private UserService userService;
 *
 * 3. API 配置：
 *    ReferenceConfig<UserService> reference = new ReferenceConfig<>();
 *    reference.setInterface(UserService.class);
 *    UserService userService = reference.get();
 */
public class OrderServiceImpl implements OrderService {

    // 注入其他 Dubbo 服务
    // @DubboReference(version = "1.0.0", timeout = 3000, check = false)
    private UserService userService;

    // @DubboReference(version = "1.0.0", timeout = 3000, check = false)
    private PaymentService paymentService;

    // 模拟数据库
    private static final Map<Long, Order> orderDatabase = new ConcurrentHashMap<>();
    private static final Map<Long, List<Order>> userOrderIndex = new ConcurrentHashMap<>();

    static {
        // 初始化测试数据
        Order order1 = new Order(1L, Arrays.asList(
            new OrderItem(101L, "iPhone 15", new java.math.BigDecimal("5999"), 1),
            new OrderItem(102L, "AirPods Pro", new java.math.BigDecimal("1999"), 1)
        ));
        order1.setOrderId(1L);
        order1.setShippingAddress("北京市朝阳区xxx街道");
        order1.setStatus(1); // 已支付

        orderDatabase.put(1L, order1);
        userOrderIndex.computeIfAbsent(1L, k -> new ArrayList<>()).add(order1);
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Long createOrder(Order order) {
        System.out.println("[OrderService] 创建订单: " + order);

        // 1. 验证用户是否存在（调用 UserService）
        try {
            if (userService != null) {
                System.out.println("[OrderService] 验证用户: userId=" + order.getUserId());
                userService.getUserById(order.getUserId());
            }
        } catch (Exception e) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 2. 验证订单信息
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("订单商品不能为空");
        }

        // 3. 生成订单ID
        Long orderId = generateOrderId();
        order.setOrderId(orderId);
        order.setStatus(0); // 待支付
        order.setCreateTime(System.currentTimeMillis());
        order.setUpdateTime(System.currentTimeMillis());

        // 4. 保存订单
        orderDatabase.put(orderId, order);
        userOrderIndex.computeIfAbsent(order.getUserId(), k -> new ArrayList<>()).add(order);

        System.out.println("[OrderService] 订单创建成功: orderId=" + orderId);
        return orderId;
    }

    @Override
    public Order getOrderById(Long orderId) {
        System.out.println("[OrderService] 查询订单: orderId=" + orderId);

        Order order = orderDatabase.get(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        System.out.println("[OrderService] 查询结果: " + order);
        return order;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        System.out.println("[OrderService] 查询用户订单: userId=" + userId);

        List<Order> orders = userOrderIndex.getOrDefault(userId, Collections.emptyList());
        System.out.println("[OrderService] 查询到 " + orders.size() + " 个订单");
        return new ArrayList<>(orders);
    }

    @Override
    public Boolean updateOrderStatus(Long orderId, Integer status) {
        System.out.println("[OrderService] 更新订单状态: orderId=" + orderId + ", status=" + status);

        Order order = orderDatabase.get(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        order.setStatus(status);
        order.setUpdateTime(System.currentTimeMillis());

        System.out.println("[OrderService] 订单状态更新成功");
        return true;
    }

    @Override
    public Boolean cancelOrder(Long orderId) {
        System.out.println("[OrderService] 取消订单: orderId=" + orderId);

        Order order = orderDatabase.get(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不允许取消");
        }

        order.setStatus(2); // 已取消
        order.setUpdateTime(System.currentTimeMillis());

        System.out.println("[OrderService] 订单取消成功");
        return true;
    }

    @Override
    public PaymentResult payOrder(Long orderId, String paymentMethod) {
        System.out.println("[OrderService] 支付订单: orderId=" + orderId + ", method=" + paymentMethod);

        // 1. 查询订单
        Order order = orderDatabase.get(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不允许支付");
        }

        // 2. 调用支付服务（演示服务间调用）
        PaymentResult result;
        try {
            if (paymentService != null) {
                System.out.println("[OrderService] 调用支付服务");
                result = paymentService.processPayment(
                    orderId,
                    order.getUserId(),
                    order.getTotalAmount(),
                    paymentMethod
                );
            } else {
                // 模拟支付成功
                result = new PaymentResult(true, "支付成功");
                result.setTransactionId("TXN_" + System.currentTimeMillis());
            }
        } catch (Exception e) {
            System.out.println("[OrderService] 支付失败: " + e.getMessage());
            return new PaymentResult(false, "支付失败: " + e.getMessage());
        }

        // 3. 更新订单状态
        if (result.getSuccess()) {
            order.setStatus(1); // 已支付
            order.setUpdateTime(System.currentTimeMillis());
            System.out.println("[OrderService] 订单支付成功");
        }

        return result;
    }

    private Long generateOrderId() {
        return System.currentTimeMillis();
    }
}

/**
 * 服务调用最佳实践：
 *
 * 1. 超时设置：
 *    - 根据实际情况设置合理的超时时间
 *    - 读操作可以短一些，写操作可以长一些
 *    - 避免设置过长导致调用堆积
 *
 * 2. 重试策略：
 *    - 幂等操作可以开启重试
 *    - 非幂等操作应关闭重试
 *    - 合理设置重试次数
 *
 * 3. 容错处理：
 *    - 使用 try-catch 捕获异常
 *    - 提供降级方案
 *    - 记录失败日志
 *
 * 4. 异步调用：
 *    - 不需要立即返回结果的操作使用异步
 *    - CompletableFuture<String> future = RpcContext.getContext().asyncCall(...)
 *
 * 5. 服务分组：
 *    - 使用 group 隔离不同环境
 *    - 使用 version 进行灰度发布
 *
 * 6. 依赖检查：
 *    - 启动时检查依赖服务：check=true
 *    - 允许延迟启动：check=false
 *
 * 7. 调用链追踪：
 *    - 传递 traceId 进行全链路追踪
 *    - 使用 RpcContext 传递隐式参数
 */
