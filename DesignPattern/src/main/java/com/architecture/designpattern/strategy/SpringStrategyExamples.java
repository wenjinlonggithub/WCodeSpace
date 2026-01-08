package com.architecture.designpattern.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring中策略模式的各种案例实现
 * 
 * 使用场景：
 * 1. 支付系统 - 不同支付渠道处理逻辑
 * 2. 消息推送 - 短信、邮件、APP推送等
 * 3. 数据导出 - Excel、PDF、CSV等格式
 * 4. 风控策略 - 不同业务线的风控规则
 * 5. 营销活动 - 满减、折扣、积分等优惠策略
 * 
 * 不用策略模式的问题：
 * - 大量if-else判断，代码冗长且难维护
 * - 新增策略需要修改现有代码，违反开闭原则
 * - 各种策略耦合在一起，测试困难
 * - 代码可读性差，业务逻辑混乱
 */
@Component
public class SpringStrategyExamples {
    
    @Autowired
    private PaymentStrategyFactory paymentFactory;
    
    @Autowired
    private MessageStrategyManager messageManager;
    
    @Autowired
    private ExportStrategyRegistry exportRegistry;
    
    public void demonstrateSpringStrategy() {
        System.out.println("=== Spring策略模式演示 ===");
        
        // 1. 支付策略演示
        demonstratePaymentStrategy();
        
        // 2. 消息推送策略演示
        demonstrateMessageStrategy();
        
        // 3. 数据导出策略演示
        demonstrateExportStrategy();
    }
    
    private void demonstratePaymentStrategy() {
        System.out.println("\n1. 支付策略演示：");
        
        // 不同支付方式处理
        PaymentRequest request = new PaymentRequest("ORDER001", 1000.0, "ALIPAY");
        PaymentStrategy strategy = paymentFactory.getStrategy(request.getPaymentType());
        PaymentResult result = strategy.processPayment(request);
        System.out.println("支付结果：" + result);
        
        // 微信支付
        request = new PaymentRequest("ORDER002", 500.0, "WECHAT");
        strategy = paymentFactory.getStrategy(request.getPaymentType());
        result = strategy.processPayment(request);
        System.out.println("支付结果：" + result);
    }
    
    private void demonstrateMessageStrategy() {
        System.out.println("\n2. 消息推送策略演示：");
        
        MessageContext context = new MessageContext();
        context.setUserId("user123");
        context.setContent("您的订单已发货");
        
        // 短信推送
        messageManager.sendMessage("SMS", context);
        
        // 邮件推送
        messageManager.sendMessage("EMAIL", context);
        
        // APP推送
        messageManager.sendMessage("APP", context);
    }
    
    private void demonstrateExportStrategy() {
        System.out.println("\n3. 数据导出策略演示：");
        
        ExportData data = new ExportData();
        data.addRecord("张三", "28", "工程师");
        data.addRecord("李四", "32", "产品经理");
        
        // Excel导出
        ExportStrategy excelStrategy = exportRegistry.getStrategy("EXCEL");
        excelStrategy.export(data);
        
        // PDF导出
        ExportStrategy pdfStrategy = exportRegistry.getStrategy("PDF");
        pdfStrategy.export(data);
    }
}

// ============== 支付策略相关 ==============

/**
 * 支付策略接口
 */
interface PaymentStrategy {
    PaymentResult processPayment(PaymentRequest request);
    boolean supports(String paymentType);
}

/**
 * 支付策略标识注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface PaymentType {
    String value();
}

/**
 * 支付宝支付策略
 */
@Service
@PaymentType("ALIPAY")
class AlipayPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        System.out.println("处理支付宝支付：订单" + request.getOrderId() + "，金额" + request.getAmount());
        
        // 模拟支付宝支付流程
        if (validateAlipayAccount(request)) {
            return new PaymentResult(true, "支付宝支付成功", generateTransactionId());
        }
        return new PaymentResult(false, "支付宝支付失败", null);
    }
    
    @Override
    public boolean supports(String paymentType) {
        return "ALIPAY".equals(paymentType);
    }
    
    private boolean validateAlipayAccount(PaymentRequest request) {
        // 支付宝账户验证逻辑
        return request.getAmount() > 0;
    }
    
    private String generateTransactionId() {
        return "ALIPAY_" + System.currentTimeMillis();
    }
}

/**
 * 微信支付策略
 */
@Service
@PaymentType("WECHAT")
class WechatPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        System.out.println("处理微信支付：订单" + request.getOrderId() + "，金额" + request.getAmount());
        
        // 模拟微信支付流程
        if (validateWechatAccount(request)) {
            return new PaymentResult(true, "微信支付成功", generateTransactionId());
        }
        return new PaymentResult(false, "微信支付失败", null);
    }
    
    @Override
    public boolean supports(String paymentType) {
        return "WECHAT".equals(paymentType);
    }
    
    private boolean validateWechatAccount(PaymentRequest request) {
        // 微信账户验证逻辑
        return request.getAmount() > 0;
    }
    
    private String generateTransactionId() {
        return "WECHAT_" + System.currentTimeMillis();
    }
}

/**
 * 银行卡支付策略
 */
@Service
@PaymentType("BANK_CARD")
class BankCardPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        System.out.println("处理银行卡支付：订单" + request.getOrderId() + "，金额" + request.getAmount());
        
        // 模拟银行卡支付流程
        if (validateBankCard(request)) {
            return new PaymentResult(true, "银行卡支付成功", generateTransactionId());
        }
        return new PaymentResult(false, "银行卡支付失败", null);
    }
    
    @Override
    public boolean supports(String paymentType) {
        return "BANK_CARD".equals(paymentType);
    }
    
    private boolean validateBankCard(PaymentRequest request) {
        // 银行卡验证逻辑
        return request.getAmount() > 0;
    }
    
    private String generateTransactionId() {
        return "BANK_" + System.currentTimeMillis();
    }
}

/**
 * 支付策略工厂
 */
@Component
class PaymentStrategyFactory {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, PaymentStrategy> strategyCache = new ConcurrentHashMap<>();
    
    public PaymentStrategy getStrategy(String paymentType) {
        return strategyCache.computeIfAbsent(paymentType, this::findStrategy);
    }
    
    private PaymentStrategy findStrategy(String paymentType) {
        Map<String, PaymentStrategy> strategies = applicationContext.getBeansOfType(PaymentStrategy.class);
        
        for (PaymentStrategy strategy : strategies.values()) {
            if (strategy.supports(paymentType)) {
                return strategy;
            }
        }
        
        throw new IllegalArgumentException("不支持的支付类型：" + paymentType);
    }
}

// ============== 消息推送策略相关 ==============

/**
 * 消息推送策略接口
 */
interface MessageStrategy {
    void sendMessage(MessageContext context);
    String getType();
}

/**
 * 短信推送策略
 */
@Service("SMS")
class SmsMessageStrategy implements MessageStrategy {
    
    @Override
    public void sendMessage(MessageContext context) {
        System.out.println("发送短信给用户 " + context.getUserId() + "：" + context.getContent());
        // 调用短信服务商API
    }
    
    @Override
    public String getType() {
        return "SMS";
    }
}

/**
 * 邮件推送策略
 */
@Service("EMAIL")
class EmailMessageStrategy implements MessageStrategy {
    
    @Override
    public void sendMessage(MessageContext context) {
        System.out.println("发送邮件给用户 " + context.getUserId() + "：" + context.getContent());
        // 调用邮件服务
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
}

/**
 * APP推送策略
 */
@Service("APP")
class AppMessageStrategy implements MessageStrategy {
    
    @Override
    public void sendMessage(MessageContext context) {
        System.out.println("发送APP推送给用户 " + context.getUserId() + "：" + context.getContent());
        // 调用APP推送服务
    }
    
    @Override
    public String getType() {
        return "APP";
    }
}

/**
 * 消息策略管理器
 */
@Component
class MessageStrategyManager {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    public void sendMessage(String type, MessageContext context) {
        MessageStrategy strategy = applicationContext.getBean(type, MessageStrategy.class);
        strategy.sendMessage(context);
    }
}

// ============== 数据导出策略相关 ==============

/**
 * 数据导出策略接口
 */
interface ExportStrategy {
    void export(ExportData data);
    String getFormat();
}

/**
 * Excel导出策略
 */
@Service
class ExcelExportStrategy implements ExportStrategy {
    
    @Override
    public void export(ExportData data) {
        System.out.println("导出Excel格式数据，共 " + data.getRecords().size() + " 条记录");
        // 实际Excel导出逻辑
    }
    
    @Override
    public String getFormat() {
        return "EXCEL";
    }
}

/**
 * PDF导出策略
 */
@Service
class PdfExportStrategy implements ExportStrategy {
    
    @Override
    public void export(ExportData data) {
        System.out.println("导出PDF格式数据，共 " + data.getRecords().size() + " 条记录");
        // 实际PDF导出逻辑
    }
    
    @Override
    public String getFormat() {
        return "PDF";
    }
}

/**
 * CSV导出策略
 */
@Service
class CsvExportStrategy implements ExportStrategy {
    
    @Override
    public void export(ExportData data) {
        System.out.println("导出CSV格式数据，共 " + data.getRecords().size() + " 条记录");
        // 实际CSV导出逻辑
    }
    
    @Override
    public String getFormat() {
        return "CSV";
    }
}

/**
 * 导出策略注册中心
 */
@Component
class ExportStrategyRegistry {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, ExportStrategy> strategyMap = new ConcurrentHashMap<>();
    
    @Autowired
    public void initStrategies() {
        Map<String, ExportStrategy> strategies = applicationContext.getBeansOfType(ExportStrategy.class);
        for (ExportStrategy strategy : strategies.values()) {
            strategyMap.put(strategy.getFormat(), strategy);
        }
    }
    
    public ExportStrategy getStrategy(String format) {
        ExportStrategy strategy = strategyMap.get(format);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的导出格式：" + format);
        }
        return strategy;
    }
}

// ============== 数据模型类 ==============

class PaymentRequest {
    private String orderId;
    private Double amount;
    private String paymentType;
    
    public PaymentRequest(String orderId, Double amount, String paymentType) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentType = paymentType;
    }
    
    public String getOrderId() { return orderId; }
    public Double getAmount() { return amount; }
    public String getPaymentType() { return paymentType; }
}

class PaymentResult {
    private boolean success;
    private String message;
    private String transactionId;
    
    public PaymentResult(boolean success, String message, String transactionId) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
    }
    
    @Override
    public String toString() {
        return "PaymentResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}

class MessageContext {
    private String userId;
    private String content;
    private String title;
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

class ExportData {
    private java.util.List<String[]> records = new java.util.ArrayList<>();
    
    public void addRecord(String... fields) {
        records.add(fields);
    }
    
    public java.util.List<String[]> getRecords() {
        return records;
    }
}