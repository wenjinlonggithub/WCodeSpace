package com.architecture.business.payment;

import com.architecture.business.order.PaymentResult;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 支付服务实现类
 *
 * 演示外部服务集成和事务处理
 */
public class PaymentServiceImpl implements PaymentService {

    // 模拟数据库
    private static final Map<String, PaymentRecord> paymentDatabase = new ConcurrentHashMap<>();
    private static final Map<Long, List<PaymentRecord>> userPaymentIndex = new ConcurrentHashMap<>();
    private static final Map<String, PaymentStatus> paymentStatusMap = new ConcurrentHashMap<>();

    @Override
    public PaymentResult processPayment(Long orderId, Long userId, BigDecimal amount, String paymentMethod) {
        System.out.println("[PaymentService] 处理支付: orderId=" + orderId +
                         ", amount=" + amount + ", method=" + paymentMethod);

        // 1. 验证支付参数
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentResult(false, "支付金额必须大于0");
        }

        if (!isValidPaymentMethod(paymentMethod)) {
            return new PaymentResult(false, "不支持的支付方式: " + paymentMethod);
        }

        // 2. 生成交易ID
        String transactionId = generateTransactionId();

        // 3. 调用第三方支付接口（模拟）
        boolean paymentSuccess = callThirdPartyPayment(paymentMethod, amount);

        if (!paymentSuccess) {
            System.out.println("[PaymentService] 支付失败");
            savePaymentStatus(transactionId, 2, amount);
            return new PaymentResult(false, "支付失败，请重试");
        }

        // 4. 保存支付记录
        PaymentRecord record = new PaymentRecord(transactionId, orderId, userId, amount, paymentMethod);
        paymentDatabase.put(transactionId, record);
        userPaymentIndex.computeIfAbsent(userId, k -> new ArrayList<>()).add(record);

        // 5. 保存支付状态
        savePaymentStatus(transactionId, 1, amount);

        System.out.println("[PaymentService] 支付成功: transactionId=" + transactionId);

        PaymentResult result = new PaymentResult(true, "支付成功");
        result.setTransactionId(transactionId);
        return result;
    }

    @Override
    public PaymentStatus queryPaymentStatus(String transactionId) {
        System.out.println("[PaymentService] 查询支付状态: transactionId=" + transactionId);

        PaymentStatus status = paymentStatusMap.get(transactionId);
        if (status == null) {
            throw new RuntimeException("交易不存在: " + transactionId);
        }

        System.out.println("[PaymentService] 支付状态: " + status.getStatusText());
        return status;
    }

    @Override
    public RefundResult refund(String transactionId, BigDecimal amount) {
        System.out.println("[PaymentService] 处理退款: transactionId=" + transactionId +
                         ", amount=" + amount);

        // 1. 查询原支付记录
        PaymentRecord record = paymentDatabase.get(transactionId);
        if (record == null) {
            return new RefundResult(false, "交易不存在");
        }

        PaymentStatus status = paymentStatusMap.get(transactionId);
        if (status.getStatus() != 1) {
            return new RefundResult(false, "交易状态不允许退款");
        }

        // 2. 验证退款金额
        if (amount.compareTo(record.getAmount()) > 0) {
            return new RefundResult(false, "退款金额不能大于支付金额");
        }

        // 3. 调用第三方退款接口（模拟）
        boolean refundSuccess = callThirdPartyRefund(transactionId, amount);

        if (!refundSuccess) {
            System.out.println("[PaymentService] 退款失败");
            return new RefundResult(false, "退款失败，请重试");
        }

        // 4. 更新支付状态
        status.setStatus(3); // 已退款
        record.setStatus(3);

        // 5. 生成退款ID
        String refundId = "REFUND_" + System.currentTimeMillis();

        System.out.println("[PaymentService] 退款成功: refundId=" + refundId);

        RefundResult result = new RefundResult(true, "退款成功");
        result.setRefundId(refundId);
        result.setRefundAmount(amount);
        return result;
    }

    @Override
    public List<PaymentRecord> getPaymentRecords(Long userId) {
        System.out.println("[PaymentService] 查询支付记录: userId=" + userId);

        List<PaymentRecord> records = userPaymentIndex.getOrDefault(userId, Collections.emptyList());
        System.out.println("[PaymentService] 查询到 " + records.size() + " 条记录");
        return new ArrayList<>(records);
    }

    private boolean isValidPaymentMethod(String paymentMethod) {
        return "alipay".equals(paymentMethod) ||
               "wechat".equals(paymentMethod) ||
               "credit_card".equals(paymentMethod);
    }

    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + new Random().nextInt(10000);
    }

    private boolean callThirdPartyPayment(String paymentMethod, BigDecimal amount) {
        // 模拟调用第三方支付接口
        System.out.println("[PaymentService] 调用第三方支付: " + paymentMethod);

        try {
            // 模拟网络延迟
            Thread.sleep(100);

            // 90% 成功率
            return Math.random() > 0.1;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private boolean callThirdPartyRefund(String transactionId, BigDecimal amount) {
        // 模拟调用第三方退款接口
        System.out.println("[PaymentService] 调用第三方退款");

        try {
            Thread.sleep(100);
            return Math.random() > 0.05; // 95% 成功率
        } catch (InterruptedException e) {
            return false;
        }
    }

    private void savePaymentStatus(String transactionId, Integer status, BigDecimal amount) {
        PaymentStatus paymentStatus = new PaymentStatus(transactionId, status, amount);
        paymentStatus.setPaymentTime(System.currentTimeMillis());
        paymentStatusMap.put(transactionId, paymentStatus);
    }
}

/**
 * 支付服务最佳实践：
 *
 * 1. 幂等性设计：
 *    - 使用唯一的交易ID防止重复支付
 *    - 支付前检查订单状态
 *    - 使用数据库唯一约束
 *
 * 2. 事务处理：
 *    - 本地事务：保证数据一致性
 *    - 分布式事务：使用 TCC 或 SAGA 模式
 *    - 补偿机制：失败时进行回滚
 *
 * 3. 异常处理：
 *    - 网络超时重试
 *    - 支付失败记录日志
 *    - 提供降级方案
 *
 * 4. 安全性：
 *    - 金额验证
 *    - 签名验证
 *    - 防重放攻击
 *    - 敏感数据加密
 *
 * 5. 异步处理：
 *    - 支付结果异步通知
 *    - 使用消息队列解耦
 *    - 定时对账
 *
 * 6. 监控告警：
 *    - 支付成功率监控
 *    - 响应时间监控
 *    - 异常告警
 *
 * 7. 配置管理：
 *    - 第三方接口地址配置化
 *    - 超时时间可配置
 *    - 开关控制（支持降级）
 */
