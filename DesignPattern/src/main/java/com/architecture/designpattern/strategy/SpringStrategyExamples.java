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
 * 核心解决的问题和背景：
 * 1. 业务场景多样化：不同业务需要不同的处理策略
 * 2. 代码扩展性：新增业务策略时不影响现有代码
 * 3. 算法替换性：运行时可以动态切换不同的处理算法
 * 4. 配置驱动：通过配置文件或注解驱动策略选择
 * 
 * 实际应用场景：
 * 1. 支付系统 - 不同支付渠道处理逻辑（支付宝、微信、银行卡、Apple Pay）
 * 2. 消息推送 - 多渠道消息推送（短信、邮件、APP推送、微信推送、钉钉推送）
 * 3. 数据导出 - 多格式数据导出（Excel、PDF、CSV、XML、JSON）
 * 4. 风控策略 - 多维度风控规则（黑名单、频率限制、金额限制、设备指纹、IP风控）
 * 5. 营销活动 - 多种优惠策略（满减、折扣、积分、买赠、新人礼包）
 * 6. 认证授权 - 多种认证方式（用户名密码、手机验证码、第三方登录、生物识别）
 * 7. 数据存储 - 多种存储策略（MySQL、Redis、MongoDB、Elasticsearch、文件存储）
 * 8. 缓存策略 - 多级缓存策略（本地缓存、分布式缓存、数据库缓存）
 * 9. 限流策略 - 多种限流算法（令牌桶、漏桶、滑动窗口、计数器）
 * 10. 负载均衡 - 多种负载均衡算法（轮询、随机、加权、最少连接）
 * 
 * 不用策略模式的问题：
 * - 大量if-else或switch-case判断，代码冗长且难维护
 * - 单一类承担多个职责，违反单一职责原则
 * - 新增策略需要修改现有代码，违反开闭原则
 * - 各种策略逻辑耦合在一起，测试困难
 * - 代码可读性差，业务逻辑混乱
 * - 难以进行并行开发，团队协作困难
 * - 策略变更影响范围大，容易引入Bug
 * 
 * 使用策略模式的优势：
 * - 策略之间相互独立，易于理解和维护
 * - 新增策略只需实现接口，不影响现有代码
 * - 每个策略类职责单一，便于单元测试
 * - 支持运行时动态切换策略
 * - 代码结构清晰，便于团队协作开发
 * - 符合开闭原则和依赖倒置原则
 */
@Component
public class SpringStrategyExamples {
    
    @Autowired
    private PaymentStrategyFactory paymentFactory;
    
    @Autowired
    private MessageStrategyManager messageManager;
    
    @Autowired
    private ExportStrategyRegistry exportRegistry;
    
    @Autowired
    private AuthenticationStrategyManager authManager;
    
    @Autowired
    private CacheStrategyManager cacheManager;
    
    @Autowired
    private RateLimitStrategyManager rateLimitManager;
    
    public void demonstrateSpringStrategy() {
        System.out.println("=== Spring策略模式全面演示 ===");
        
        // 1. 支付策略演示
        demonstratePaymentStrategy();
        
        // 2. 消息推送策略演示
        demonstrateMessageStrategy();
        
        // 3. 数据导出策略演示
        demonstrateExportStrategy();
        
        // 4. 认证策略演示
        demonstrateAuthenticationStrategy();
        
        // 5. 缓存策略演示
        demonstrateCacheStrategy();
        
        // 6. 限流策略演示
        demonstrateRateLimitStrategy();
        
        // 7. 策略组合演示
        demonstrateStrategyComposition();
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

// ============== 新增策略实现 ==============

private void demonstrateAuthenticationStrategy() {
    System.out.println("\n4. 认证策略演示：");
    
    // 用户名密码认证
    AuthenticationRequest request1 = new AuthenticationRequest();
    request1.setIdentifier("admin@example.com");
    request1.setCredential("password123");
    request1.setType("USERNAME_PASSWORD");
    
    AuthenticationResult result1 = authManager.authenticate(request1);
    System.out.println("用户名密码认证结果：" + result1);
    
    // 手机验证码认证
    AuthenticationRequest request2 = new AuthenticationRequest();
    request2.setIdentifier("13800138000");
    request2.setCredential("123456");
    request2.setType("SMS_CODE");
    
    AuthenticationResult result2 = authManager.authenticate(request2);
    System.out.println("短信验证码认证结果：" + result2);
    
    // 第三方OAuth认证
    AuthenticationRequest request3 = new AuthenticationRequest();
    request3.setIdentifier("oauth_token_12345");
    request3.setType("OAUTH");
    
    AuthenticationResult result3 = authManager.authenticate(request3);
    System.out.println("OAuth认证结果：" + result3);
}

private void demonstrateCacheStrategy() {
    System.out.println("\n5. 缓存策略演示：");
    
    CacheRequest request = new CacheRequest();
    request.setKey("user:1001");
    request.setValue("用户信息数据");
    request.setTtl(300);
    
    // 本地缓存
    cacheManager.put("LOCAL", request);
    String value1 = cacheManager.get("LOCAL", "user:1001");
    System.out.println("本地缓存获取：" + value1);
    
    // Redis缓存
    cacheManager.put("REDIS", request);
    String value2 = cacheManager.get("REDIS", "user:1001");
    System.out.println("Redis缓存获取：" + value2);
    
    // 多级缓存
    cacheManager.put("MULTI_LEVEL", request);
    String value3 = cacheManager.get("MULTI_LEVEL", "user:1001");
    System.out.println("多级缓存获取：" + value3);
}

private void demonstrateRateLimitStrategy() {
    System.out.println("\n6. 限流策略演示：");
    
    String clientId = "client_001";
    
    // 令牌桶限流
    boolean allowed1 = rateLimitManager.isAllowed("TOKEN_BUCKET", clientId);
    System.out.println("令牌桶限流结果：" + (allowed1 ? "允许访问" : "限流"));
    
    // 滑动窗口限流
    boolean allowed2 = rateLimitManager.isAllowed("SLIDING_WINDOW", clientId);
    System.out.println("滑动窗口限流结果：" + (allowed2 ? "允许访问" : "限流"));
    
    // 计数器限流
    boolean allowed3 = rateLimitManager.isAllowed("COUNTER", clientId);
    System.out.println("计数器限流结果：" + (allowed3 ? "允许访问" : "限流"));
}

private void demonstrateStrategyComposition() {
    System.out.println("\n7. 策略组合演示（订单处理流程）：");
    
    OrderProcessingContext context = new OrderProcessingContext();
    context.setOrderId("ORDER_2024001");
    context.setUserId("user_001");
    context.setAmount(1500.0);
    context.setPaymentType("ALIPAY");
    
    OrderProcessingPipeline pipeline = new OrderProcessingPipeline();
    
    // 组合多个策略：风控检查 -> 优惠计算 -> 支付处理 -> 库存扣减 -> 消息通知
    OrderProcessingResult result = pipeline.process(context);
    System.out.println("订单处理结果：" + result);
    
    System.out.println("\n策略组合的优势：");
    System.out.println("- 每个策略专注于单一职责");
    System.out.println("- 策略之间可以灵活组合");
    System.out.println("- 支持策略链的动态调整");
    System.out.println("- 便于扩展新的处理步骤");
}

// ============== 认证策略相关 ==============

/**
 * 认证策略接口
 */
interface AuthenticationStrategy {
    AuthenticationResult authenticate(AuthenticationRequest request);
    boolean supports(String type);
    String getAuthType();
}

/**
 * 用户名密码认证策略
 */
@Service
@Component
class UsernamePasswordAuthStrategy implements AuthenticationStrategy {
    
    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        System.out.println("执行用户名密码认证：" + request.getIdentifier());
        
        // 模拟用户名密码验证逻辑
        if ("admin@example.com".equals(request.getIdentifier()) && 
            "password123".equals(request.getCredential())) {
            return new AuthenticationResult(true, "认证成功", generateToken());
        }
        
        return new AuthenticationResult(false, "用户名或密码错误", null);
    }
    
    @Override
    public boolean supports(String type) {
        return "USERNAME_PASSWORD".equals(type);
    }
    
    @Override
    public String getAuthType() {
        return "USERNAME_PASSWORD";
    }
    
    private String generateToken() {
        return "token_" + System.currentTimeMillis();
    }
}

/**
 * 短信验证码认证策略
 */
@Service
@Component
class SmsCodeAuthStrategy implements AuthenticationStrategy {
    
    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        System.out.println("执行短信验证码认证：" + request.getIdentifier());
        
        // 模拟短信验证码验证逻辑
        if ("13800138000".equals(request.getIdentifier()) && 
            "123456".equals(request.getCredential())) {
            return new AuthenticationResult(true, "短信验证成功", generateToken());
        }
        
        return new AuthenticationResult(false, "验证码错误或已过期", null);
    }
    
    @Override
    public boolean supports(String type) {
        return "SMS_CODE".equals(type);
    }
    
    @Override
    public String getAuthType() {
        return "SMS_CODE";
    }
    
    private String generateToken() {
        return "sms_token_" + System.currentTimeMillis();
    }
}

/**
 * OAuth认证策略
 */
@Service
@Component
class OAuthAuthStrategy implements AuthenticationStrategy {
    
    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        System.out.println("执行OAuth认证：" + request.getIdentifier());
        
        // 模拟OAuth token验证逻辑
        if (request.getIdentifier() != null && 
            request.getIdentifier().startsWith("oauth_token_")) {
            return new AuthenticationResult(true, "OAuth认证成功", request.getIdentifier());
        }
        
        return new AuthenticationResult(false, "OAuth token无效", null);
    }
    
    @Override
    public boolean supports(String type) {
        return "OAUTH".equals(type);
    }
    
    @Override
    public String getAuthType() {
        return "OAUTH";
    }
}

/**
 * 认证策略管理器
 */
@Component
class AuthenticationStrategyManager {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, AuthenticationStrategy> strategyCache = new ConcurrentHashMap<>();
    
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        AuthenticationStrategy strategy = getStrategy(request.getType());
        if (strategy == null) {
            return new AuthenticationResult(false, "不支持的认证类型：" + request.getType(), null);
        }
        
        return strategy.authenticate(request);
    }
    
    private AuthenticationStrategy getStrategy(String type) {
        return strategyCache.computeIfAbsent(type, this::findStrategy);
    }
    
    private AuthenticationStrategy findStrategy(String type) {
        Map<String, AuthenticationStrategy> strategies = 
            applicationContext.getBeansOfType(AuthenticationStrategy.class);
        
        for (AuthenticationStrategy strategy : strategies.values()) {
            if (strategy.supports(type)) {
                return strategy;
            }
        }
        
        return null;
    }
}

// ============== 缓存策略相关 ==============

/**
 * 缓存策略接口
 */
interface CacheStrategy {
    void put(CacheRequest request);
    String get(String key);
    void remove(String key);
    String getCacheType();
}

/**
 * 本地缓存策略
 */
@Service
@Component
class LocalCacheStrategy implements CacheStrategy {
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    @Override
    public void put(CacheRequest request) {
        System.out.println("存储到本地缓存：" + request.getKey());
        cache.put(request.getKey(), request.getValue());
    }
    
    @Override
    public String get(String key) {
        String value = cache.get(key);
        System.out.println("从本地缓存获取：" + key + " -> " + value);
        return value;
    }
    
    @Override
    public void remove(String key) {
        cache.remove(key);
        System.out.println("从本地缓存删除：" + key);
    }
    
    @Override
    public String getCacheType() {
        return "LOCAL";
    }
}

/**
 * Redis缓存策略
 */
@Service
@Component
class RedisCacheStrategy implements CacheStrategy {
    
    private final Map<String, String> redisSimulator = new ConcurrentHashMap<>();
    
    @Override
    public void put(CacheRequest request) {
        System.out.println("存储到Redis缓存：" + request.getKey() + ", TTL=" + request.getTtl() + "秒");
        redisSimulator.put(request.getKey(), request.getValue());
    }
    
    @Override
    public String get(String key) {
        String value = redisSimulator.get(key);
        System.out.println("从Redis缓存获取：" + key + " -> " + value);
        return value;
    }
    
    @Override
    public void remove(String key) {
        redisSimulator.remove(key);
        System.out.println("从Redis缓存删除：" + key);
    }
    
    @Override
    public String getCacheType() {
        return "REDIS";
    }
}

/**
 * 多级缓存策略
 */
@Service
@Component
class MultiLevelCacheStrategy implements CacheStrategy {
    
    @Autowired
    private LocalCacheStrategy localCache;
    
    @Autowired
    private RedisCacheStrategy redisCache;
    
    @Override
    public void put(CacheRequest request) {
        System.out.println("存储到多级缓存：" + request.getKey());
        // 同时存储到本地和Redis
        localCache.put(request);
        redisCache.put(request);
    }
    
    @Override
    public String get(String key) {
        System.out.println("从多级缓存获取：" + key);
        
        // 先从本地缓存获取
        String value = localCache.get(key);
        if (value != null) {
            System.out.println("本地缓存命中");
            return value;
        }
        
        // 本地缓存未命中，从Redis获取
        value = redisCache.get(key);
        if (value != null) {
            System.out.println("Redis缓存命中，回填本地缓存");
            CacheRequest request = new CacheRequest();
            request.setKey(key);
            request.setValue(value);
            localCache.put(request);
        }
        
        return value;
    }
    
    @Override
    public void remove(String key) {
        System.out.println("从多级缓存删除：" + key);
        localCache.remove(key);
        redisCache.remove(key);
    }
    
    @Override
    public String getCacheType() {
        return "MULTI_LEVEL";
    }
}

/**
 * 缓存策略管理器
 */
@Component
class CacheStrategyManager {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, CacheStrategy> strategyMap = new ConcurrentHashMap<>();
    
    @Autowired
    public void initStrategies() {
        Map<String, CacheStrategy> strategies = applicationContext.getBeansOfType(CacheStrategy.class);
        for (CacheStrategy strategy : strategies.values()) {
            strategyMap.put(strategy.getCacheType(), strategy);
        }
    }
    
    public void put(String cacheType, CacheRequest request) {
        CacheStrategy strategy = strategyMap.get(cacheType);
        if (strategy != null) {
            strategy.put(request);
        } else {
            throw new IllegalArgumentException("不支持的缓存类型：" + cacheType);
        }
    }
    
    public String get(String cacheType, String key) {
        CacheStrategy strategy = strategyMap.get(cacheType);
        if (strategy != null) {
            return strategy.get(key);
        } else {
            throw new IllegalArgumentException("不支持的缓存类型：" + cacheType);
        }
    }
}

// ============== 限流策略相关 ==============

/**
 * 限流策略接口
 */
interface RateLimitStrategy {
    boolean isAllowed(String clientId);
    String getStrategyName();
    void reset(String clientId);
}

/**
 * 令牌桶限流策略
 */
@Service
@Component
class TokenBucketRateLimitStrategy implements RateLimitStrategy {
    
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    @Override
    public boolean isAllowed(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket(10, 1));
        boolean allowed = bucket.tryConsume();
        System.out.println("令牌桶限流检查 - 客户端：" + clientId + ", 结果：" + (allowed ? "通过" : "限流"));
        return allowed;
    }
    
    @Override
    public String getStrategyName() {
        return "TOKEN_BUCKET";
    }
    
    @Override
    public void reset(String clientId) {
        buckets.remove(clientId);
    }
    
    private static class TokenBucket {
        private final int capacity;
        private final int refillRate;
        private int tokens;
        private long lastRefillTime;
        
        public TokenBucket(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            int tokensToAdd = (int) (timePassed / 1000 * refillRate);
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }
}

/**
 * 滑动窗口限流策略
 */
@Service
@Component
class SlidingWindowRateLimitStrategy implements RateLimitStrategy {
    
    private final Map<String, SlidingWindow> windows = new ConcurrentHashMap<>();
    
    @Override
    public boolean isAllowed(String clientId) {
        SlidingWindow window = windows.computeIfAbsent(clientId, k -> new SlidingWindow(60000, 100));
        boolean allowed = window.isAllowed();
        System.out.println("滑动窗口限流检查 - 客户端：" + clientId + ", 结果：" + (allowed ? "通过" : "限流"));
        return allowed;
    }
    
    @Override
    public String getStrategyName() {
        return "SLIDING_WINDOW";
    }
    
    @Override
    public void reset(String clientId) {
        windows.remove(clientId);
    }
    
    private static class SlidingWindow {
        private final long windowSize;
        private final int limit;
        private final java.util.Deque<Long> requests = new java.util.ArrayDeque<>();
        
        public SlidingWindow(long windowSize, int limit) {
            this.windowSize = windowSize;
            this.limit = limit;
        }
        
        public synchronized boolean isAllowed() {
            long now = System.currentTimeMillis();
            
            // 清理过期请求
            while (!requests.isEmpty() && requests.peekFirst() <= now - windowSize) {
                requests.pollFirst();
            }
            
            // 检查是否超过限制
            if (requests.size() < limit) {
                requests.offerLast(now);
                return true;
            }
            
            return false;
        }
    }
}

/**
 * 计数器限流策略
 */
@Service
@Component
class CounterRateLimitStrategy implements RateLimitStrategy {
    
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    
    @Override
    public boolean isAllowed(String clientId) {
        Counter counter = counters.computeIfAbsent(clientId, k -> new Counter(60000, 50));
        boolean allowed = counter.isAllowed();
        System.out.println("计数器限流检查 - 客户端：" + clientId + ", 结果：" + (allowed ? "通过" : "限流"));
        return allowed;
    }
    
    @Override
    public String getStrategyName() {
        return "COUNTER";
    }
    
    @Override
    public void reset(String clientId) {
        counters.remove(clientId);
    }
    
    private static class Counter {
        private final long windowSize;
        private final int limit;
        private int count;
        private long windowStart;
        
        public Counter(long windowSize, int limit) {
            this.windowSize = windowSize;
            this.limit = limit;
            this.windowStart = System.currentTimeMillis();
        }
        
        public synchronized boolean isAllowed() {
            long now = System.currentTimeMillis();
            
            // 检查是否需要重置窗口
            if (now - windowStart >= windowSize) {
                count = 0;
                windowStart = now;
            }
            
            // 检查是否超过限制
            if (count < limit) {
                count++;
                return true;
            }
            
            return false;
        }
    }
}

/**
 * 限流策略管理器
 */
@Component
class RateLimitStrategyManager {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, RateLimitStrategy> strategies = new ConcurrentHashMap<>();
    
    @Autowired
    public void initStrategies() {
        Map<String, RateLimitStrategy> strategyBeans = applicationContext.getBeansOfType(RateLimitStrategy.class);
        for (RateLimitStrategy strategy : strategyBeans.values()) {
            strategies.put(strategy.getStrategyName(), strategy);
        }
    }
    
    public boolean isAllowed(String strategyName, String clientId) {
        RateLimitStrategy strategy = strategies.get(strategyName);
        if (strategy != null) {
            return strategy.isAllowed(clientId);
        } else {
            throw new IllegalArgumentException("不支持的限流策略：" + strategyName);
        }
    }
}

// ============== 策略组合相关 ==============

/**
 * 订单处理策略接口
 */
interface OrderProcessingStrategy {
    OrderProcessingResult process(OrderProcessingContext context);
    String getStepName();
    int getOrder();
}

/**
 * 订单处理流水线
 */
@Component
class OrderProcessingPipeline {
    
    private final java.util.List<OrderProcessingStrategy> strategies = java.util.Arrays.asList(
        new RiskCheckStrategy(),
        new PromotionCalculationStrategy(),
        new PaymentProcessingStrategy(),
        new InventoryDeductionStrategy(),
        new NotificationStrategy()
    );
    
    public OrderProcessingResult process(OrderProcessingContext context) {
        System.out.println("开始处理订单：" + context.getOrderId());
        
        for (OrderProcessingStrategy strategy : strategies) {
            System.out.println("执行步骤：" + strategy.getStepName());
            
            OrderProcessingResult result = strategy.process(context);
            if (!result.isSuccess()) {
                System.out.println("订单处理失败，步骤：" + strategy.getStepName() + ", 原因：" + result.getMessage());
                return result;
            }
        }
        
        System.out.println("订单处理完成：" + context.getOrderId());
        return new OrderProcessingResult(true, "订单处理成功");
    }
}

// 具体的订单处理策略实现
class RiskCheckStrategy implements OrderProcessingStrategy {
    @Override
    public OrderProcessingResult process(OrderProcessingContext context) {
        // 模拟风控检查
        if (context.getAmount() > 10000) {
            return new OrderProcessingResult(false, "订单金额超过风控限制");
        }
        return new OrderProcessingResult(true, "风控检查通过");
    }
    
    @Override
    public String getStepName() {
        return "风控检查";
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}

class PromotionCalculationStrategy implements OrderProcessingStrategy {
    @Override
    public OrderProcessingResult process(OrderProcessingContext context) {
        // 模拟优惠计算
        double discountAmount = context.getAmount() * 0.1;
        context.setDiscountAmount(discountAmount);
        return new OrderProcessingResult(true, "优惠计算完成，优惠金额：" + discountAmount);
    }
    
    @Override
    public String getStepName() {
        return "优惠计算";
    }
    
    @Override
    public int getOrder() {
        return 2;
    }
}

class PaymentProcessingStrategy implements OrderProcessingStrategy {
    @Override
    public OrderProcessingResult process(OrderProcessingContext context) {
        // 模拟支付处理
        double finalAmount = context.getAmount() - context.getDiscountAmount();
        System.out.println("处理" + context.getPaymentType() + "支付，金额：" + finalAmount);
        return new OrderProcessingResult(true, "支付处理完成");
    }
    
    @Override
    public String getStepName() {
        return "支付处理";
    }
    
    @Override
    public int getOrder() {
        return 3;
    }
}

class InventoryDeductionStrategy implements OrderProcessingStrategy {
    @Override
    public OrderProcessingResult process(OrderProcessingContext context) {
        // 模拟库存扣减
        System.out.println("扣减库存，订单：" + context.getOrderId());
        return new OrderProcessingResult(true, "库存扣减完成");
    }
    
    @Override
    public String getStepName() {
        return "库存扣减";
    }
    
    @Override
    public int getOrder() {
        return 4;
    }
}

class NotificationStrategy implements OrderProcessingStrategy {
    @Override
    public OrderProcessingResult process(OrderProcessingContext context) {
        // 模拟消息通知
        System.out.println("发送订单完成通知给用户：" + context.getUserId());
        return new OrderProcessingResult(true, "通知发送完成");
    }
    
    @Override
    public String getStepName() {
        return "消息通知";
    }
    
    @Override
    public int getOrder() {
        return 5;
    }
}

// ============== 数据模型类 ==============

class AuthenticationRequest {
    private String identifier;
    private String credential;
    private String type;
    private Map<String, Object> attributes = new java.util.HashMap<>();
    
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getAttributes() { return attributes; }
}

class AuthenticationResult {
    private boolean success;
    private String message;
    private String token;
    
    public AuthenticationResult(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    
    @Override
    public String toString() {
        return "AuthenticationResult{success=" + success + ", message='" + message + "', token='" + token + "'}";
    }
}

class CacheRequest {
    private String key;
    private String value;
    private int ttl;
    
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public int getTtl() { return ttl; }
    public void setTtl(int ttl) { this.ttl = ttl; }
}

class OrderProcessingContext {
    private String orderId;
    private String userId;
    private double amount;
    private String paymentType;
    private double discountAmount;
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
}

class OrderProcessingResult {
    private boolean success;
    private String message;
    
    public OrderProcessingResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    
    @Override
    public String toString() {
        return "OrderProcessingResult{success=" + success + ", message='" + message + "'}";
    }
}