package com.architecture.medicalpayment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 库存补偿服务
 *
 * 职责：
 * 1. 定期扫描超时未支付的预扣库存记录
 * 2. 自动释放超时库存
 * 3. 处理异常订单（支付成功但库存确认失败）
 * 4. 监控和告警
 *
 * 补偿机制是分布式系统中保证最终一致性的重要手段
 *
 * @author architecture
 */
@Slf4j
@Service
public class InventoryCompensationService {

    @Autowired
    private InventoryPreDeductionService inventoryService;

    @Autowired
    private InventoryPreDeductionRepositoryV2 preDeductionRepository;

    @Autowired
    private OrderRepositoryV2 orderRepositoryV2;

    @Autowired
    private RefundService refundService;

    @Autowired
    private MonitorService monitorService;

    // 使用线程池异步处理补偿任务
    private final ExecutorService compensationExecutor = Executors.newFixedThreadPool(5);

    /**
     * 定时任务：释放超时的预扣库存
     *
     * 执行频率：每5分钟执行一次
     * 业务场景：
     * - 用户下单后未支付
     * - 用户支付超时
     * - 支付过程中系统异常
     *
     * 防止库存长期被占用，影响其他用户购买
     */
    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    public void releaseExpiredInventory() {
        log.info("开始执行库存补偿任务：释放超时预扣库存");

        try {
            // 1. 查询所有超时且状态为LOCKED的预扣记录
            List<InventoryPreDeduction> expiredRecords =
                    preDeductionRepository.findExpiredLockedRecords(LocalDateTime.now());

            if (expiredRecords.isEmpty()) {
                log.info("无超时预扣库存记录");
                return;
            }

            log.info("发现{}条超时预扣库存记录", expiredRecords.size());

            // 2. 批量处理超时记录
            int successCount = 0;
            int failCount = 0;

            for (InventoryPreDeduction record : expiredRecords) {
                try {
                    // 异步处理每条记录
                    compensationExecutor.submit(() -> processExpiredRecord(record));
                    successCount++;
                } catch (Exception e) {
                    log.error("提交补偿任务失败, orderNo={}", record.getOrderNo(), e);
                    failCount++;
                }
            }

            log.info("库存补偿任务提交完成, 成功={}, 失败={}", successCount, failCount);

            // 3. 上报监控指标
            monitorService.recordCompensationMetrics(expiredRecords.size(), successCount, failCount);

        } catch (Exception e) {
            log.error("库存补偿任务执行异常", e);
            monitorService.sendAlert("库存补偿任务执行异常", e.getMessage());
        }
    }

    /**
     * 处理单条超时记录
     */
    private void processExpiredRecord(InventoryPreDeduction record) {
        String orderNo = record.getOrderNo();
        log.info("处理超时预扣记录, orderNo={}, expireTime={}", orderNo, record.getExpireTime());

        try {
            // 1. 查询订单状态
            Order order = orderRepositoryV2.findByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单不存在, orderNo={}", orderNo);
                return;
            }

            // 2. 根据订单状态决定处理策略
            switch (order.getStatus()) {
                case CREATED:
                case PAYING:
                    // 订单未支付，释放库存
                    releaseInventoryForUnpaidOrder(record, order);
                    break;

                case PAID:
                    // 订单已支付，但预扣记录仍是LOCKED状态，说明确认扣库存失败
                    handlePaidOrderWithLockedInventory(record, order);
                    break;

                case PAYMENT_FAILED:
                    // 支付失败，释放库存
                    releaseInventoryForFailedOrder(record, order);
                    break;

                default:
                    log.warn("订单状态异常, orderNo={}, status={}", orderNo, order.getStatus());
            }

        } catch (Exception e) {
            log.error("处理超时预扣记录异常, orderNo={}", orderNo, e);
        }
    }

    /**
     * 释放未支付订单的库存
     */
    private void releaseInventoryForUnpaidOrder(InventoryPreDeduction record, Order order) {
        String orderNo = record.getOrderNo();
        log.info("释放未支付订单的库存, orderNo={}", orderNo);

        // 1. 释放库存
        ReleaseInventoryResult releaseResult = inventoryService.releaseInventory(orderNo);

        if (releaseResult.isSuccess()) {
            log.info("释放库存成功, orderNo={}", orderNo);

            // 2. 关闭订单
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setRemark("支付超时，已自动关闭");
            order.setUpdatedTime(LocalDateTime.now());
            orderRepositoryV2.save(order);

        } else {
            log.error("释放库存失败, orderNo={}, message={}",
                    orderNo, releaseResult.getMessage());

            // 发送告警
            monitorService.sendAlert("释放库存失败", String.format("orderNo=%s", orderNo));
        }
    }

    /**
     * 释放支付失败订单的库存
     */
    private void releaseInventoryForFailedOrder(InventoryPreDeduction record, Order order) {
        String orderNo = record.getOrderNo();
        log.info("释放支付失败订单的库存, orderNo={}", orderNo);

        ReleaseInventoryResult releaseResult = inventoryService.releaseInventory(orderNo);

        if (!releaseResult.isSuccess()) {
            log.error("释放库存失败, orderNo={}, message={}",
                    orderNo, releaseResult.getMessage());
        }
    }

    /**
     * 处理已支付但库存仍为锁定状态的订单
     *
     * 这是最严重的异常场景：
     * - 用户已经付款
     * - 但库存确认失败，导致订单无法完成
     *
     * 处理策略：
     * 1. 重试确认扣库存
     * 2. 如果重试失败，发起退款
     */
    private void handlePaidOrderWithLockedInventory(InventoryPreDeduction record, Order order) {
        String orderNo = record.getOrderNo();
        log.error("发现已支付但库存未确认的订单, orderNo={}", orderNo);

        // 发送高优先级告警
        monitorService.sendAlert("【严重】已支付订单库存未确认",
                String.format("orderNo=%s, amount=%d", orderNo, order.getAmount()));

        // 1. 重试确认扣库存
        ConfirmDeductionResult confirmResult = inventoryService.confirmDeduction(orderNo);

        if (confirmResult.isSuccess()) {
            log.info("重试确认扣库存成功, orderNo={}", orderNo);

            // 尝试创建HIS订单
            retryCreateHisOrder(order);

        } else {
            log.error("重试确认扣库存失败, orderNo={}, message={}",
                    orderNo, confirmResult.getMessage());

            // 2. 确认失败，发起退款
            initiateRefundForFailedOrder(order, "库存确认失败");
        }
    }

    /**
     * 重试创建HIS订单
     */
    private void retryCreateHisOrder(Order order) {
        // TODO: 实现HIS订单创建重试逻辑
        log.info("重试创建HIS订单, orderNo={}", order.getOrderNo());
    }

    /**
     * 为失败订单发起退款
     */
    private void initiateRefundForFailedOrder(Order order, String reason) {
        String orderNo = order.getOrderNo();
        log.info("为失败订单发起退款, orderNo={}, reason={}", orderNo, reason);

        try {
            RefundRequest refundRequest = RefundRequest.builder()
                    .orderNo(orderNo)
                    .transactionId(order.getTransactionId())
                    .refundAmount(order.getAmount())
                    .reason(reason)
                    .build();

            RefundResult refundResult = refundService.refund(refundRequest);

            if (refundResult.isSuccess()) {
                log.info("自动退款成功, orderNo={}, refundNo={}",
                        orderNo, refundResult.getRefundNo());

                // 更新订单状态
                order.setStatus(OrderStatus.REFUNDED);
                order.setRefundNo(refundResult.getRefundNo());
                order.setRefundTime(LocalDateTime.now());
                order.setRemark("补偿任务自动退款：" + reason);
                orderRepositoryV2.save(order);

                // 释放库存
                inventoryService.releaseInventory(orderNo);

            } else {
                log.error("自动退款失败, orderNo={}, message={}",
                        orderNo, refundResult.getMessage());

                // 推送到人工处理队列
                pushToManualProcessQueue(order, "自动退款失败");
            }

        } catch (Exception e) {
            log.error("退款异常, orderNo={}", orderNo, e);
            pushToManualProcessQueue(order, "退款异常：" + e.getMessage());
        }
    }

    /**
     * 推送到人工处理队列
     */
    private void pushToManualProcessQueue(Order order, String reason) {
        log.error("【需人工处理】订单异常, orderNo={}, reason={}", order.getOrderNo(), reason);

        // TODO: 推送到人工处理工作台
        // TODO: 发送紧急告警
        // TODO: 记录到异常处理表

        monitorService.sendUrgentAlert("订单需人工处理",
                String.format("orderNo=%s, reason=%s, amount=%d",
                        order.getOrderNo(), reason, order.getAmount()));
    }

    /**
     * 定时任务：检查异常订单
     *
     * 执行频率：每10分钟执行一次
     * 检查范围：
     * - 支付成功但状态为EXCEPTION的订单
     * - 长时间停留在PAYING状态的订单
     */
    @Scheduled(cron = "0 */10 * * * ?") // 每10分钟执行一次
    public void checkExceptionOrders() {
        log.info("开始检查异常订单");

        try {
            // 1. 查询异常订单
            List<Order> exceptionOrders = orderRepositoryV2.findExceptionOrders();

            if (exceptionOrders.isEmpty()) {
                log.info("无异常订单");
                return;
            }

            log.info("发现{}个异常订单", exceptionOrders.size());

            // 2. 处理异常订单
            for (Order order : exceptionOrders) {
                compensationExecutor.submit(() -> processExceptionOrder(order));
            }

        } catch (Exception e) {
            log.error("检查异常订单失败", e);
        }
    }

    /**
     * 处理异常订单
     */
    private void processExceptionOrder(Order order) {
        log.info("处理异常订单, orderNo={}, status={}", order.getOrderNo(), order.getStatus());

        // TODO: 实现异常订单处理逻辑
        // 1. 重试确认扣库存
        // 2. 重试创建HIS订单
        // 3. 如果失败，发起退款
        // 4. 推送人工处理
    }

    /**
     * 定时任务：清理历史数据
     *
     * 执行频率：每天凌晨2点执行
     * 清理范围：30天前的已完成/已释放的预扣记录
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupHistoryData() {
        log.info("开始清理历史预扣记录");

        try {
            LocalDateTime cleanupTime = LocalDateTime.now().minusDays(30);
            int deletedCount = preDeductionRepository.deleteCompletedRecordsBefore(cleanupTime);

            log.info("清理历史预扣记录完成, 删除{}条", deletedCount);

        } catch (Exception e) {
            log.error("清理历史数据失败", e);
        }
    }

    /**
     * 优雅停机
     */
    public void shutdown() {
        log.info("关闭补偿服务线程池");
        compensationExecutor.shutdown();
        try {
            if (!compensationExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                compensationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            compensationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

// ============== 相关接口和类 ==============

/**
 * 预扣库存Repository扩展方法
 */
interface InventoryPreDeductionRepository {
    InventoryPreDeduction findByOrderNo(String orderNo);
    void save(InventoryPreDeduction preDeduction);

    /**
     * 查询超时且状态为LOCKED的记录
     */
    List<InventoryPreDeduction> findExpiredLockedRecords(LocalDateTime now);

    /**
     * 删除指定时间之前的已完成记录
     */
    int deleteCompletedRecordsBefore(LocalDateTime time);
}

/**
 * 订单Repository扩展方法
 */
interface OrderRepository {
    Order findByOrderNo(String orderNo);
    void save(Order order);

    /**
     * 查询异常订单
     */
    List<Order> findExceptionOrders();
}

/**
 * 监控服务
 */
interface MonitorService {
    /**
     * 记录补偿任务指标
     */
    void recordCompensationMetrics(int totalCount, int successCount, int failCount);

    /**
     * 发送告警
     */
    void sendAlert(String title, String message);

    /**
     * 发送紧急告警
     */
    void sendUrgentAlert(String title, String message);
}

/**
 * 监控服务实现示例
 */
@Slf4j
@Service
class MonitorServiceImpl implements MonitorService {

    @Override
    public void recordCompensationMetrics(int totalCount, int successCount, int failCount) {
        // TODO: 上报到监控系统（Prometheus、云监控等）
        log.info("补偿任务指标: total={}, success={}, fail={}", totalCount, successCount, failCount);
    }

    @Override
    public void sendAlert(String title, String message) {
        // TODO: 发送告警（钉钉、邮件、短信等）
        log.warn("【告警】{}: {}", title, message);
    }

    @Override
    public void sendUrgentAlert(String title, String message) {
        // TODO: 发送紧急告警（电话、多渠道通知等）
        log.error("【紧急告警】{}: {}", title, message);
    }
}
