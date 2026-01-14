package com.architecture.medicalpayment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 支付回调处理器
 *
 * 核心职责：
 * 1. 接收微信支付回调通知
 * 2. 验证回调签名和幂等性
 * 3. 确认扣库存
 * 4. 创建HIS订单
 * 5. 异常处理和补偿
 *
 * @author architecture
 */
@Slf4j
@Service
public class PaymentCallbackHandler {

    @Autowired
    private InventoryPreDeductionService inventoryService;

    @Autowired
    private HisOrderService hisOrderService;

    @Autowired
    private OrderRepositoryV2 orderRepositoryV2;

    @Autowired
    private RefundService refundService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CALLBACK_IDEMPOTENT_KEY = "payment:callback:";
    private static final int CALLBACK_LOCK_EXPIRE_SECONDS = 60;

    /**
     * 处理微信支付回调
     *
     * 核心流程：
     * 1. 幂等性校验
     * 2. 验证签名
     * 3. 确认扣库存
     * 4. 创建HIS订单
     * 5. 更新本地订单状态
     * 6. 异常处理
     *
     * @param callbackRequest 微信回调请求
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentCallbackResponse handlePaymentCallback(WeChatPaymentCallback callbackRequest) {
        String orderNo = callbackRequest.getOutTradeNo();
        String transactionId = callbackRequest.getTransactionId();

        log.info("收到微信支付回调, orderNo={}, transactionId={}, payResult={}",
                orderNo, transactionId, callbackRequest.getTradeState());

        try {
            // 1. 幂等性校验：防止微信重复回调
            if (!acquireCallbackLock(orderNo)) {
                log.warn("回调正在处理中，忽略重复回调, orderNo={}", orderNo);
                return PaymentCallbackResponse.success("处理中");
            }

            // 2. 验证签名
            if (!verifySignature(callbackRequest)) {
                log.error("回调签名验证失败, orderNo={}", orderNo);
                return PaymentCallbackResponse.fail("签名验证失败");
            }

            // 3. 查询订单
            Order order = orderRepositoryV2.findByOrderNo(orderNo);
            if (order == null) {
                log.error("订单不存在, orderNo={}", orderNo);
                return PaymentCallbackResponse.fail("订单不存在");
            }

            // 4. 幂等性校验：订单状态
            if (order.getStatus() == OrderStatus.PAID) {
                log.warn("订单已支付，无需重复处理, orderNo={}", orderNo);
                return PaymentCallbackResponse.success("订单已支付");
            }

            // 5. 判断支付结果
            if (!"SUCCESS".equals(callbackRequest.getTradeState())) {
                log.warn("支付失败或未完成, orderNo={}, tradeState={}",
                        orderNo, callbackRequest.getTradeState());
                handlePaymentFail(order);
                return PaymentCallbackResponse.success("支付失败");
            }

            // 6. 支付成功，处理订单
            return handlePaymentSuccess(order, callbackRequest);

        } catch (Exception e) {
            log.error("处理支付回调异常, orderNo={}", orderNo, e);
            return PaymentCallbackResponse.fail("系统异常");
        } finally {
            // 释放幂等锁
            releaseCallbackLock(orderNo);
        }
    }

    /**
     * 处理支付成功的情况
     *
     * 关键步骤：
     * 1. 确认扣库存
     * 2. 创建HIS订单
     * 3. 更新订单状态
     * 4. 异常补偿
     */
    private PaymentCallbackResponse handlePaymentSuccess(Order order, WeChatPaymentCallback callback) {
        String orderNo = order.getOrderNo();

        log.info("处理支付成功回调, orderNo={}", orderNo);

        // 1. 确认扣库存
        ConfirmDeductionResult confirmResult = inventoryService.confirmDeduction(orderNo);
        if (!confirmResult.isSuccess()) {
            log.error("确认扣库存失败, orderNo={}, message={}",
                    orderNo, confirmResult.getMessage());

            // 库存确认失败，需要退款
            if (confirmResult.isNeedRefund()) {
                initiateRefund(order, "库存不足");
            }

            // 更新订单状态为异常
            updateOrderStatus(order, OrderStatus.EXCEPTION, confirmResult.getMessage());

            return PaymentCallbackResponse.fail("库存确认失败");
        }

        // 2. 创建HIS订单
        HisOrderCreateResult hisResult = createHisOrder(order);
        if (!hisResult.isSuccess()) {
            log.error("创建HIS订单失败, orderNo={}, message={}",
                    orderNo, hisResult.getMessage());

            // HIS订单创建失败，发起退款
            initiateRefund(order, "HIS订单创建失败");

            // 更新订单状态为异常
            updateOrderStatus(order, OrderStatus.EXCEPTION, hisResult.getMessage());

            return PaymentCallbackResponse.fail("HIS订单创建失败");
        }

        // 3. 更新本地订单状态
        order.setStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
        order.setTransactionId(callback.getTransactionId());
        order.setHisOrderNo(hisResult.getHisOrderNo());
        order.setUpdatedTime(LocalDateTime.now());
        orderRepositoryV2.save(order);

        log.info("支付回调处理成功, orderNo={}, hisOrderNo={}",
                orderNo, hisResult.getHisOrderNo());

        // 4. 发送成功通知（短信、APP推送等）
        sendSuccessNotification(order);

        return PaymentCallbackResponse.success("处理成功");
    }

    /**
     * 处理支付失败的情况
     *
     * 1. 释放预扣库存
     * 2. 更新订单状态
     */
    private void handlePaymentFail(Order order) {
        String orderNo = order.getOrderNo();
        log.info("处理支付失败, orderNo={}", orderNo);

        // 1. 释放预扣库存
        ReleaseInventoryResult releaseResult = inventoryService.releaseInventory(orderNo);
        if (!releaseResult.isSuccess()) {
            log.warn("释放库存失败, orderNo={}, message={}",
                    orderNo, releaseResult.getMessage());
        }

        // 2. 更新订单状态
        updateOrderStatus(order, OrderStatus.PAYMENT_FAILED, "支付失败");
    }

    /**
     * 创建HIS订单
     */
    private HisOrderCreateResult createHisOrder(Order order) {
        try {
            HisOrderCreateResponse response = hisOrderService.createOrder(
                    HisOrderCreateRequest.builder()
                            .orderNo(order.getOrderNo())
                            .patientId(order.getPatientId())
                            .drugId(order.getDrugId())
                            .quantity(order.getQuantity())
                            .amount(order.getAmount())
                            .build()
            );

            return HisOrderCreateResult.builder()
                    .success(response.isSuccess())
                    .message(response.getMessage())
                    .hisOrderNo(response.getHisOrderNo())
                    .build();

        } catch (Exception e) {
            log.error("调用HIS创建订单异常, orderNo={}", order.getOrderNo(), e);
            return HisOrderCreateResult.builder()
                    .success(false)
                    .message("HIS系统异常")
                    .build();
        }
    }

    /**
     * 发起退款
     *
     * 业务场景：
     * 1. 库存确认失败
     * 2. HIS订单创建失败
     *
     * 退款方式：
     * - 优先自动退款
     * - 失败后记录异常，人工处理
     */
    private void initiateRefund(Order order, String reason) {
        log.info("发起退款, orderNo={}, reason={}", order.getOrderNo(), reason);

        try {
            RefundRequest refundRequest = RefundRequest.builder()
                    .orderNo(order.getOrderNo())
                    .transactionId(order.getTransactionId())
                    .refundAmount(order.getAmount())
                    .reason(reason)
                    .build();

            RefundResult refundResult = refundService.refund(refundRequest);

            if (refundResult.isSuccess()) {
                log.info("自动退款成功, orderNo={}, refundNo={}",
                        order.getOrderNo(), refundResult.getRefundNo());

                // 更新订单状态
                order.setStatus(OrderStatus.REFUNDED);
                order.setRefundNo(refundResult.getRefundNo());
                order.setRefundTime(LocalDateTime.now());
                orderRepositoryV2.save(order);

            } else {
                log.error("自动退款失败, orderNo={}, message={}",
                        order.getOrderNo(), refundResult.getMessage());

                // 记录退款异常，触发人工处理
                recordRefundException(order, reason, refundResult.getMessage());
            }

        } catch (Exception e) {
            log.error("退款异常, orderNo={}", order.getOrderNo(), e);
            recordRefundException(order, reason, e.getMessage());
        }
    }

    /**
     * 记录退款异常
     *
     * 触发告警，人工介入处理
     */
    private void recordRefundException(Order order, String reason, String errorMessage) {
        // TODO: 记录到异常表
        // TODO: 发送告警（钉钉、短信等）
        // TODO: 推送到人工处理工作台

        log.error("【严重告警】退款失败需人工处理, orderNo={}, reason={}, error={}",
                order.getOrderNo(), reason, errorMessage);
    }

    /**
     * 更新订单状态
     */
    private void updateOrderStatus(Order order, OrderStatus status, String remark) {
        order.setStatus(status);
        order.setRemark(remark);
        order.setUpdatedTime(LocalDateTime.now());
        orderRepositoryV2.save(order);
    }

    /**
     * 发送成功通知
     */
    private void sendSuccessNotification(Order order) {
        // TODO: 发送短信
        // TODO: APP推送
        log.info("发送支付成功通知, orderNo={}", order.getOrderNo());
    }

    /**
     * 获取回调幂等锁
     */
    private boolean acquireCallbackLock(String orderNo) {
        String key = CALLBACK_IDEMPOTENT_KEY + orderNo;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", CALLBACK_LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放回调幂等锁
     */
    private void releaseCallbackLock(String orderNo) {
        String key = CALLBACK_IDEMPOTENT_KEY + orderNo;
        redisTemplate.delete(key);
    }

    /**
     * 验证微信回调签名
     */
    private boolean verifySignature(WeChatPaymentCallback callback) {
        // TODO: 实现微信签名验证逻辑
        // 参考：https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_1.shtml
        return true;
    }
}

// ============== 相关DTO和实体类 ==============

/**
 * 微信支付回调请求
 */
@lombok.Data
class WeChatPaymentCallback {
    private String outTradeNo;      // 商户订单号
    private String transactionId;   // 微信支付订单号
    private String tradeState;      // 交易状态：SUCCESS/REFUND/NOTPAY/CLOSED/REVOKED/USERPAYING/PAYERROR
    private String tradeStateDesc;  // 交易状态描述
    private Integer totalFee;       // 总金额（分）
    private String timeEnd;         // 支付完成时间
    private String signature;       // 签名
}

/**
 * 支付回调响应
 */
@lombok.Data
@lombok.Builder
class PaymentCallbackResponse {
    private String code;
    private String message;

    public static PaymentCallbackResponse success(String message) {
        return PaymentCallbackResponse.builder()
                .code("SUCCESS")
                .message(message)
                .build();
    }

    public static PaymentCallbackResponse fail(String message) {
        return PaymentCallbackResponse.builder()
                .code("FAIL")
                .message(message)
                .build();
    }
}

/**
 * HIS订单创建结果
 */
@lombok.Data
@lombok.Builder
class HisOrderCreateResult {
    private boolean success;
    private String message;
    private String hisOrderNo;
}

/**
 * 订单状态
 */
enum OrderStatus {
    CREATED,          // 已创建
    PAYING,           // 支付中
    PAID,             // 已支付
    PAYMENT_FAILED,   // 支付失败
    EXCEPTION,        // 异常
    REFUNDED,         // 已退款
    COMPLETED         // 已完成
}

/**
 * 订单实体
 */
@lombok.Data
class Order {
    private Long id;
    private String orderNo;
    private Long patientId;
    private Long drugId;
    private Integer quantity;
    private Integer amount;
    private OrderStatus status;
    private String transactionId;
    private String hisOrderNo;
    private String refundNo;
    private LocalDateTime payTime;
    private LocalDateTime refundTime;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

// ============== HIS接口相关类（模拟） ==============

interface HisOrderService {
    HisOrderCreateResponse createOrder(HisOrderCreateRequest request);
}

@lombok.Data
@lombok.Builder
class HisOrderCreateRequest {
    private String orderNo;
    private Long patientId;
    private Long drugId;
    private Integer quantity;
    private Integer amount;
}

@lombok.Data
class HisOrderCreateResponse {
    private boolean success;
    private String message;
    private String hisOrderNo;
}

// ============== 退款相关类（模拟） ==============

interface RefundService {
    RefundResult refund(RefundRequest request);
}

@lombok.Data
@lombok.Builder
class RefundRequest {
    private String orderNo;
    private String transactionId;
    private Integer refundAmount;
    private String reason;
}

@lombok.Data
class RefundResult {
    private boolean success;
    private String message;
    private String refundNo;
}

interface OrderRepositoryV2 {
    Order findByOrderNo(String orderNo);
    void save(Order order);
}
