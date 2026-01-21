package com.architecture.payment.controller;

import com.architecture.payment.dto.CreatePaymentRequest;
import com.architecture.payment.dto.PaymentDTO;
import com.architecture.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制器
 *
 * 提供支付相关的REST接口:
 * - POST /payment/create: 创建支付单（Order Service调用）
 * - GET /payment/{id}: 查询支付单
 * - POST /payment/notify: 支付回调接口（第三方支付平台调用）
 * - POST /payment/refund/{id}: 退款
 *
 * 安全要点:
 * 1. 回调接口必须验证签名
 * 2. 所有金额相关操作需要验证
 * 3. 防止重放攻击（timestamp + nonce）
 *
 * @author Architecture Team
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付单
     *
     * 此接口会被Order Service调用
     * 调用时机: 创建订单后
     *
     * @param request 创建支付请求
     * @return 支付信息
     */
    @PostMapping("/create")
    public PaymentDTO createPayment(@RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    /**
     * 查询支付单
     *
     * @param id 支付ID
     * @return 支付信息
     */
    @GetMapping("/{id}")
    public PaymentDTO getPayment(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }

    /**
     * 支付回调接口
     *
     * 此接口由第三方支付平台调用
     * 调用时机: 用户完成支付后
     *
     * 实际接口参数示例:
     * {
     *   "out_trade_no": "PAY123456",
     *   "transaction_id": "2024012012345678",
     *   "total_amount": "99.99",
     *   "trade_status": "TRADE_SUCCESS",
     *   "sign": "ABC123..."
     * }
     *
     * @param paymentNo 支付单号
     * @param transactionId 第三方交易号
     * @return "success" 或 "fail"
     */
    @PostMapping("/notify")
    public String paymentNotify(@RequestParam String paymentNo,
                                @RequestParam String transactionId) {
        Boolean success = paymentService.handlePaymentCallback(paymentNo, transactionId);
        return success ? "success" : "fail";
    }

    /**
     * 退款
     *
     * @param id 支付ID
     * @return true-退款成功, false-退款失败
     */
    @PostMapping("/refund/{id}")
    public Boolean refund(@PathVariable Long id) {
        return paymentService.refund(id);
    }
}
