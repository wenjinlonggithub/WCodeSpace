package com.architecture.consistency.cqrs;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单命令服务（写服务）
 * 处理所有写操作：创建、更新、删除
 */
@Slf4j
public class OrderCommandService {

    /**
     * 写库（模拟数据库）
     */
    private final Map<String, OrderWriteModel> writeDatabase = new ConcurrentHashMap<>();

    /**
     * 缓存预热器
     */
    private final CacheWarmer cacheWarmer;

    public OrderCommandService(CacheWarmer cacheWarmer) {
        this.cacheWarmer = cacheWarmer;
    }

    /**
     * 创建订单
     * 1. 写入数据库
     * 2. 立即预热缓存
     * 3. 发布事件
     */
    public String createOrder(String userId, String productName, BigDecimal amount) {
        String orderId = "ORD" + System.currentTimeMillis();

        log.info("开始创建订单: orderId={}, userId={}", orderId, userId);

        try {
            // 1. 写入数据库
            OrderWriteModel writeModel = OrderWriteModel.builder()
                    .orderId(orderId)
                    .userId(userId)
                    .productName(productName)
                    .amount(amount)
                    .status("CREATED")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            writeDatabase.put(orderId, writeModel);
            log.info("订单写入数据库成功: orderId={}", orderId);

            // 2. 立即预热缓存（关键步骤！）
            // 构建读模型并写入缓存，这样后续查询就能立即命中
            OrderReadModel readModel = buildReadModel(writeModel);
            cacheWarmer.warmup(orderId, readModel);

            // 3. 发布订单创建事件（通知其他服务）
            publishOrderCreatedEvent(orderId);

            log.info("订单创建成功: orderId={}", orderId);
            return orderId;

        } catch (Exception e) {
            log.error("订单创建失败: orderId={}", orderId, e);
            throw new RuntimeException("订单创建失败", e);
        }
    }

    /**
     * 更新订单状态
     */
    public void updateOrderStatus(String orderId, String status) {
        log.info("更新订单状态: orderId={}, status={}", orderId, status);

        OrderWriteModel writeModel = writeDatabase.get(orderId);
        if (writeModel == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        // 更新数据库
        writeModel.setStatus(status);
        writeModel.setUpdateTime(LocalDateTime.now());

        // 更新缓存
        OrderReadModel readModel = buildReadModel(writeModel);
        cacheWarmer.warmup(orderId, readModel);

        log.info("订单状态更新成功: orderId={}, status={}", orderId, status);
    }

    /**
     * 构建读模型
     * 从写模型转换为读模型，并添加冗余字段
     */
    private OrderReadModel buildReadModel(OrderWriteModel writeModel) {
        // 这里可以查询用户信息、商品信息等，构建完整的读模型
        return OrderReadModel.builder()
                .orderId(writeModel.getOrderId())
                .userId(writeModel.getUserId())
                .userName("用户" + writeModel.getUserId())
                .productName(writeModel.getProductName())
                .amount(writeModel.getAmount())
                .status(writeModel.getStatus())
                .statusDesc(getStatusDesc(writeModel.getStatus()))
                .userPhone("138****1234")
                .userAddress("北京市朝阳区")
                .productImage("https://example.com/product.jpg")
                .createTime(writeModel.getCreateTime())
                .updateTime(writeModel.getUpdateTime())
                .queryCount(0)
                .build();
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(String status) {
        switch (status) {
            case "CREATED":
                return "已创建";
            case "PAID":
                return "已支付";
            case "SHIPPED":
                return "已发货";
            case "COMPLETED":
                return "已完成";
            case "CANCELLED":
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 发布订单创建事件
     */
    private void publishOrderCreatedEvent(String orderId) {
        log.info("发布订单创建事件: orderId={}", orderId);
        // 这里可以发送到消息队列，通知其他服务
    }
}
