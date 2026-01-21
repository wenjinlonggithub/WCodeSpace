package com.architecture.payment.service;

import com.architecture.payment.domain.Payment;
import com.architecture.payment.dto.CreatePaymentRequest;
import com.architecture.payment.dto.PaymentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付服务实现类
 *
 * 核心业务逻辑:
 * 1. 创建支付单
 * 2. 调用第三方支付接口
 * 3. 处理支付回调
 * 4. 处理退款
 *
 * 技术要点:
 * - 幂等性设计: 通过paymentNo保证同一订单不会重复创建支付单
 * - 异步通知: 支付成功后通过MQ异步通知订单服务
 * - 对账处理: 定时任务对账，处理异常订单
 *
 * 第三方支付对接流程:
 * 1. 商户调用支付接口，传入订单信息
 * 2. 支付平台返回支付链接/二维码
 * 3. 用户完成支付
 * 4. 支付平台异步回调通知商户
 * 5. 商户验证签名，更新订单状态
 * 6. 返回成功响应给支付平台
 *
 * @author Architecture Team
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // 模拟数据库存储
    private final Map<Long, Payment> paymentDatabase = new ConcurrentHashMap<>();
    private final Map<String, Payment> paymentNoIndex = new ConcurrentHashMap<>();
    private Long paymentIdSequence = 1L;

    /**
     * 创建支付单
     *
     * 业务流程:
     * 1. 生成支付单号
     * 2. 创建支付记录
     * 3. 调用第三方支付接口（模拟）
     * 4. 返回支付信息
     *
     * 幂等性保证:
     * - 通过orderNo查询，如果已存在支付单则直接返回
     * - 避免重复创建支付单
     *
     * @param request 创建支付请求
     * @return 支付信息
     */
    public PaymentDTO createPayment(CreatePaymentRequest request) {
        log.info("创建支付单, userId={}, orderNo={}, amount={}",
                request.getUserId(), request.getOrderNo(), request.getAmount());

        // 幂等性检查: 同一订单号只创建一次支付单
        Payment existingPayment = paymentNoIndex.get(request.getOrderNo());
        if (existingPayment != null) {
            log.info("支付单已存在, paymentNo={}", existingPayment.getPaymentNo());
            return convertToDTO(existingPayment);
        }

        // 生成支付单号
        String paymentNo = generatePaymentNo();

        // 创建支付记录
        Payment payment = new Payment();
        payment.setId(paymentIdSequence++);
        payment.setPaymentNo(paymentNo);
        payment.setOrderNo(request.getOrderNo());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPayMethod(request.getPayMethod() != null ? request.getPayMethod() : "ALIPAY");
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        paymentDatabase.put(payment.getId(), payment);
        paymentNoIndex.put(payment.getOrderNo(), payment);

        // 模拟调用第三方支付接口
        // 实际生产中这里会调用支付宝/微信支付接口
        simulateCallThirdPartyPayment(payment);

        log.info("支付单创建成功, paymentId={}, paymentNo={}", payment.getId(), paymentNo);
        return convertToDTO(payment);
    }

    /**
     * 模拟调用第三方支付接口
     *
     * 实际流程:
     * 1. 构建支付请求参数
     * 2. 生成签名（MD5/RSA）
     * 3. 调用支付接口
     * 4. 解析返回结果
     * 5. 返回支付链接/二维码
     *
     * @param payment 支付信息
     */
    private void simulateCallThirdPartyPayment(Payment payment) {
        log.info("调用第三方支付接口, paymentNo={}, method={}",
                payment.getPaymentNo(), payment.getPayMethod());

        // 模拟支付接口调用
        // Map<String, String> params = new HashMap<>();
        // params.put("out_trade_no", payment.getPaymentNo());
        // params.put("total_amount", payment.getAmount().toString());
        // params.put("subject", "订单支付");
        // params.put("notify_url", "http://localhost:8004/payment/notify");
        // String sign = generateSign(params, secretKey);
        // params.put("sign", sign);
        // String response = httpClient.post(paymentApiUrl, params);

        // 模拟生成支付链接
        String payUrl = "https://pay.example.com/qrcode?no=" + payment.getPaymentNo();
        log.info("支付链接生成成功: {}", payUrl);

        // 更新支付状态为支付中
        payment.setStatus(Payment.PaymentStatus.PAYING);
        payment.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 处理支付回调
     *
     * 支付平台异步通知流程:
     * 1. 接收支付平台回调请求
     * 2. 验证签名，防止伪造
     * 3. 验证金额，防止篡改
     * 4. 更新支付状态
     * 5. 通知订单服务（MQ异步）
     * 6. 返回成功响应
     *
     * 幂等性保证:
     * - 通过transactionId判断，相同交易号只处理一次
     * - 支付平台可能重复回调，必须保证幂等
     *
     * @param paymentNo 支付单号
     * @param transactionId 第三方交易号
     * @return true-处理成功, false-处理失败
     */
    public Boolean handlePaymentCallback(String paymentNo, String transactionId) {
        log.info("处理支付回调, paymentNo={}, transactionId={}", paymentNo, transactionId);

        // 1. 查询支付单
        Payment payment = findByPaymentNo(paymentNo);
        if (payment == null) {
            log.error("支付单不存在, paymentNo={}", paymentNo);
            return false;
        }

        // 2. 幂等性检查
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            log.info("支付单已处理，幂等返回成功, paymentNo={}", paymentNo);
            return true;
        }

        // 3. 验证签名（生产环境必须验证）
        // if (!verifySign(callbackParams, sign)) {
        //     log.error("签名验证失败");
        //     return false;
        // }

        // 4. 更新支付状态
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        payment.setPayTime(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        // 5. 发送MQ消息通知订单服务
        // mqTemplate.send("payment-success", payment);

        log.info("支付成功, paymentNo={}, transactionId={}", paymentNo, transactionId);
        return true;
    }

    /**
     * 查询支付单
     *
     * @param paymentId 支付ID
     * @return 支付信息
     */
    public PaymentDTO getPayment(Long paymentId) {
        log.info("查询支付单, paymentId={}", paymentId);

        Payment payment = paymentDatabase.get(paymentId);
        if (payment == null) {
            return null;
        }

        return convertToDTO(payment);
    }

    /**
     * 根据支付单号查询
     *
     * @param paymentNo 支付单号
     * @return 支付信息
     */
    private Payment findByPaymentNo(String paymentNo) {
        for (Payment payment : paymentDatabase.values()) {
            if (payment.getPaymentNo().equals(paymentNo)) {
                return payment;
            }
        }
        return null;
    }

    /**
     * 退款处理
     *
     * @param paymentId 支付ID
     * @return true-退款成功, false-退款失败
     */
    public Boolean refund(Long paymentId) {
        log.info("处理退款, paymentId={}", paymentId);

        Payment payment = paymentDatabase.get(paymentId);
        if (payment == null) {
            log.error("支付单不存在");
            return false;
        }

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            log.error("支付状态不允许退款, status={}", payment.getStatus());
            return false;
        }

        // 调用第三方退款接口（模拟）
        payment.setStatus(Payment.PaymentStatus.REFUNDING);
        payment.setUpdatedAt(LocalDateTime.now());

        // 模拟退款成功
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        log.info("退款成功, paymentId={}", paymentId);

        return true;
    }

    /**
     * Payment实体转DTO
     */
    private PaymentDTO convertToDTO(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentDTO dto = new PaymentDTO();
        BeanUtils.copyProperties(payment, dto);
        return dto;
    }

    /**
     * 生成支付单号
     *
     * 格式: PAY + 时间戳 + 随机数
     * 实际生产应使用雪花算法等分布式ID生成器
     *
     * @return 支付单号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
    }
}
