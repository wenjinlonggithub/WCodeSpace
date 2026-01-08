package com.architecture.designpattern.strategy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Spring中策略模式的高级用法和最佳实践
 * 
 * 核心解决的问题：
 * 1. 消除复杂的if-else判断逻辑
 * 2. 提高代码的可扩展性和可维护性
 * 3. 实现策略的自动发现和注册
 * 4. 支持策略链和组合策略
 * 5. 提供统一的策略管理和调用机制
 * 
 * 业务背景：
 * - 电商系统中的优惠券策略（满减、折扣、买赠等）
 * - 风控系统中的规则引擎（黑名单、频率限制、金额限制等）
 * - 营销活动中的推荐策略（协同过滤、基于内容、热度推荐等）
 * - 数据处理中的清洗策略（去重、格式化、验证等）
 */
@Component
public class SpringStrategyAdvanced implements ApplicationContextAware, InitializingBean {
    
    private ApplicationContext applicationContext;
    private PromotionStrategyManager promotionManager;
    private RiskControlManager riskControlManager;
    private RecommendationEngine recommendationEngine;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.promotionManager = new PromotionStrategyManager(applicationContext);
        this.riskControlManager = new RiskControlManager(applicationContext);
        this.recommendationEngine = new RecommendationEngine(applicationContext);
    }
    
    public void demonstrateAdvancedStrategy() {
        System.out.println("=== Spring策略模式高级用法演示 ===");
        
        // 1. 优惠券策略演示
        demonstratePromotionStrategy();
        
        // 2. 风控策略演示
        demonstrateRiskControlStrategy();
        
        // 3. 推荐策略演示
        demonstrateRecommendationStrategy();
        
        // 4. 策略链演示
        demonstrateStrategyChain();
    }
    
    private void demonstratePromotionStrategy() {
        System.out.println("\n1. 优惠券策略演示：");
        
        Order order = new Order("ORDER001", 1000.0);
        order.addItem(new OrderItem("商品A", 300.0, 1));
        order.addItem(new OrderItem("商品B", 700.0, 1));
        
        // 满减策略
        PromotionResult result1 = promotionManager.applyPromotion("FULL_REDUCTION", order);
        System.out.println("满减策略结果：" + result1);
        
        // 折扣策略
        PromotionResult result2 = promotionManager.applyPromotion("DISCOUNT", order);
        System.out.println("折扣策略结果：" + result2);
        
        // 买赠策略
        PromotionResult result3 = promotionManager.applyPromotion("BUY_GET", order);
        System.out.println("买赠策略结果：" + result3);
    }
    
    private void demonstrateRiskControlStrategy() {
        System.out.println("\n2. 风控策略演示：");
        
        RiskContext context = new RiskContext();
        context.setUserId("user123");
        context.setAmount(5000.0);
        context.setDeviceId("device456");
        context.setIpAddress("192.168.1.1");
        
        RiskResult riskResult = riskControlManager.evaluateRisk(context);
        System.out.println("风控评估结果：" + riskResult);
    }
    
    private void demonstrateRecommendationStrategy() {
        System.out.println("\n3. 推荐策略演示：");
        
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId("user123");
        request.setCategory("电子产品");
        request.setLimit(5);
        
        List<RecommendationResult> results = recommendationEngine.recommend("COLLABORATIVE_FILTERING", request);
        System.out.println("协同过滤推荐结果：" + results);
        
        results = recommendationEngine.recommend("CONTENT_BASED", request);
        System.out.println("基于内容推荐结果：" + results);
    }
    
    private void demonstrateStrategyChain() {
        System.out.println("\n4. 策略链演示：");
        
        DataProcessingContext context = new DataProcessingContext();
        context.setRawData("  重复数据,重复数据,无效格式,正常数据  ");
        
        DataProcessingChain chain = new DataProcessingChain(applicationContext);
        String result = chain.process(context);
        System.out.println("数据处理链结果：" + result);
    }
}

// ============== 优惠券策略相关 ==============

/**
 * 促销策略标识注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface PromotionType {
    String value();
    int priority() default 0;
}

/**
 * 促销策略接口
 */
interface PromotionStrategy {
    PromotionResult apply(Order order);
    boolean isApplicable(Order order);
    String getType();
}

/**
 * 满减策略
 */
@Service
@PromotionType(value = "FULL_REDUCTION", priority = 1)
class FullReductionStrategy implements PromotionStrategy {
    
    private static final double THRESHOLD = 500.0;
    private static final double REDUCTION = 50.0;
    
    @Override
    public PromotionResult apply(Order order) {
        if (!isApplicable(order)) {
            return new PromotionResult(false, 0, "不满足满减条件");
        }
        
        return new PromotionResult(true, REDUCTION, "满" + THRESHOLD + "减" + REDUCTION);
    }
    
    @Override
    public boolean isApplicable(Order order) {
        return order.getTotalAmount() >= THRESHOLD;
    }
    
    @Override
    public String getType() {
        return "FULL_REDUCTION";
    }
}

/**
 * 折扣策略
 */
@Service
@PromotionType(value = "DISCOUNT", priority = 2)
class DiscountStrategy implements PromotionStrategy {
    
    private static final double DISCOUNT_RATE = 0.9;
    
    @Override
    public PromotionResult apply(Order order) {
        if (!isApplicable(order)) {
            return new PromotionResult(false, 0, "不满足折扣条件");
        }
        
        double discountAmount = order.getTotalAmount() * (1 - DISCOUNT_RATE);
        return new PromotionResult(true, discountAmount, "9折优惠");
    }
    
    @Override
    public boolean isApplicable(Order order) {
        return order.getItems().size() >= 2;
    }
    
    @Override
    public String getType() {
        return "DISCOUNT";
    }
}

/**
 * 买赠策略
 */
@Service
@PromotionType(value = "BUY_GET", priority = 3)
class BuyGetStrategy implements PromotionStrategy {
    
    @Override
    public PromotionResult apply(Order order) {
        if (!isApplicable(order)) {
            return new PromotionResult(false, 0, "不满足买赠条件");
        }
        
        return new PromotionResult(true, 0, "买一赠一优惠券");
    }
    
    @Override
    public boolean isApplicable(Order order) {
        return order.getItems().stream().anyMatch(item -> item.getQuantity() >= 2);
    }
    
    @Override
    public String getType() {
        return "BUY_GET";
    }
}

/**
 * 促销策略管理器
 */
class PromotionStrategyManager {
    
    private final Map<String, PromotionStrategy> strategies = new ConcurrentHashMap<>();
    
    public PromotionStrategyManager(ApplicationContext applicationContext) {
        initStrategies(applicationContext);
    }
    
    private void initStrategies(ApplicationContext applicationContext) {
        Map<String, PromotionStrategy> strategyBeans = applicationContext.getBeansOfType(PromotionStrategy.class);
        for (PromotionStrategy strategy : strategyBeans.values()) {
            strategies.put(strategy.getType(), strategy);
        }
    }
    
    public PromotionResult applyPromotion(String type, Order order) {
        PromotionStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的促销类型：" + type);
        }
        
        return strategy.apply(order);
    }
    
    public PromotionResult applyBestPromotion(Order order) {
        return strategies.values().stream()
                .filter(strategy -> strategy.isApplicable(order))
                .map(strategy -> strategy.apply(order))
                .filter(PromotionResult::isSuccess)
                .max((r1, r2) -> Double.compare(r1.getDiscountAmount(), r2.getDiscountAmount()))
                .orElse(new PromotionResult(false, 0, "无可用促销"));
    }
}

// ============== 风控策略相关 ==============

/**
 * 风控规则接口
 */
interface RiskRule {
    RiskResult evaluate(RiskContext context);
    String getRuleName();
    int getPriority();
}

/**
 * 黑名单规则
 */
@Service
class BlacklistRule implements RiskRule {
    
    private static final List<String> BLACKLIST = List.of("user999", "user888");
    
    @Override
    public RiskResult evaluate(RiskContext context) {
        if (BLACKLIST.contains(context.getUserId())) {
            return new RiskResult(true, 100, "用户在黑名单中");
        }
        return new RiskResult(false, 0, "黑名单检查通过");
    }
    
    @Override
    public String getRuleName() {
        return "BLACKLIST";
    }
    
    @Override
    public int getPriority() {
        return 1;
    }
}

/**
 * 金额限制规则
 */
@Service
class AmountLimitRule implements RiskRule {
    
    private static final double MAX_AMOUNT = 10000.0;
    
    @Override
    public RiskResult evaluate(RiskContext context) {
        if (context.getAmount() > MAX_AMOUNT) {
            return new RiskResult(true, 80, "交易金额超过限制");
        }
        return new RiskResult(false, 0, "金额检查通过");
    }
    
    @Override
    public String getRuleName() {
        return "AMOUNT_LIMIT";
    }
    
    @Override
    public int getPriority() {
        return 2;
    }
}

/**
 * 频率限制规则
 */
@Service
class FrequencyLimitRule implements RiskRule {
    
    @Override
    public RiskResult evaluate(RiskContext context) {
        // 模拟频率检查
        if (context.getUserId().endsWith("123")) {
            return new RiskResult(true, 60, "交易频率过高");
        }
        return new RiskResult(false, 0, "频率检查通过");
    }
    
    @Override
    public String getRuleName() {
        return "FREQUENCY_LIMIT";
    }
    
    @Override
    public int getPriority() {
        return 3;
    }
}

/**
 * 风控管理器
 */
class RiskControlManager {
    
    private final List<RiskRule> rules;
    
    public RiskControlManager(ApplicationContext applicationContext) {
        this.rules = applicationContext.getBeansOfType(RiskRule.class).values()
                .stream()
                .sorted((r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority()))
                .collect(Collectors.toList());
    }
    
    public RiskResult evaluateRisk(RiskContext context) {
        for (RiskRule rule : rules) {
            RiskResult result = rule.evaluate(context);
            if (result.isRisky()) {
                return result;
            }
        }
        return new RiskResult(false, 0, "风控检查通过");
    }
}

// ============== 推荐策略相关 ==============

/**
 * 推荐策略接口
 */
interface RecommendationStrategy {
    List<RecommendationResult> recommend(RecommendationRequest request);
    String getStrategyName();
}

/**
 * 协同过滤推荐
 */
@Service
class CollaborativeFilteringStrategy implements RecommendationStrategy {
    
    @Override
    public List<RecommendationResult> recommend(RecommendationRequest request) {
        // 模拟协同过滤算法
        return List.of(
                new RecommendationResult("商品A", 0.95, "协同过滤"),
                new RecommendationResult("商品B", 0.87, "协同过滤"),
                new RecommendationResult("商品C", 0.82, "协同过滤")
        );
    }
    
    @Override
    public String getStrategyName() {
        return "COLLABORATIVE_FILTERING";
    }
}

/**
 * 基于内容的推荐
 */
@Service
class ContentBasedStrategy implements RecommendationStrategy {
    
    @Override
    public List<RecommendationResult> recommend(RecommendationRequest request) {
        // 模拟基于内容的推荐算法
        return List.of(
                new RecommendationResult("商品D", 0.92, "内容相似"),
                new RecommendationResult("商品E", 0.88, "内容相似"),
                new RecommendationResult("商品F", 0.85, "内容相似")
        );
    }
    
    @Override
    public String getStrategyName() {
        return "CONTENT_BASED";
    }
}

/**
 * 推荐引擎
 */
class RecommendationEngine {
    
    private final Map<String, RecommendationStrategy> strategies = new ConcurrentHashMap<>();
    
    public RecommendationEngine(ApplicationContext applicationContext) {
        initStrategies(applicationContext);
    }
    
    private void initStrategies(ApplicationContext applicationContext) {
        Map<String, RecommendationStrategy> strategyBeans = applicationContext.getBeansOfType(RecommendationStrategy.class);
        for (RecommendationStrategy strategy : strategyBeans.values()) {
            strategies.put(strategy.getStrategyName(), strategy);
        }
    }
    
    public List<RecommendationResult> recommend(String strategyName, RecommendationRequest request) {
        RecommendationStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的推荐策略：" + strategyName);
        }
        
        return strategy.recommend(request);
    }
}

// ============== 策略链相关 ==============

/**
 * 数据处理策略接口
 */
interface DataProcessingStrategy {
    String process(String data);
    String getProcessorName();
    int getOrder();
}

/**
 * 去重处理策略
 */
@Service
class DeduplicationProcessor implements DataProcessingStrategy {
    
    @Override
    public String process(String data) {
        // 简单去重逻辑
        String[] parts = data.split(",");
        return String.join(",", java.util.Arrays.stream(parts).distinct().toArray(String[]::new));
    }
    
    @Override
    public String getProcessorName() {
        return "DEDUPLICATION";
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}

/**
 * 格式化处理策略
 */
@Service
class FormatProcessor implements DataProcessingStrategy {
    
    @Override
    public String process(String data) {
        // 去除空格和无效字符
        return data.trim().replaceAll("\\s+", " ");
    }
    
    @Override
    public String getProcessorName() {
        return "FORMAT";
    }
    
    @Override
    public int getOrder() {
        return 2;
    }
}

/**
 * 验证处理策略
 */
@Service
class ValidationProcessor implements DataProcessingStrategy {
    
    @Override
    public String process(String data) {
        // 过滤无效数据
        String[] parts = data.split(",");
        return String.join(",", java.util.Arrays.stream(parts)
                .filter(part -> !part.contains("无效"))
                .toArray(String[]::new));
    }
    
    @Override
    public String getProcessorName() {
        return "VALIDATION";
    }
    
    @Override
    public int getOrder() {
        return 3;
    }
}

/**
 * 数据处理策略链
 */
class DataProcessingChain {
    
    private final List<DataProcessingStrategy> processors;
    
    public DataProcessingChain(ApplicationContext applicationContext) {
        this.processors = applicationContext.getBeansOfType(DataProcessingStrategy.class).values()
                .stream()
                .sorted((p1, p2) -> Integer.compare(p1.getOrder(), p2.getOrder()))
                .collect(Collectors.toList());
    }
    
    public String process(DataProcessingContext context) {
        String data = context.getRawData();
        
        for (DataProcessingStrategy processor : processors) {
            data = processor.process(data);
            System.out.println(processor.getProcessorName() + " 处理后：" + data);
        }
        
        return data;
    }
}

// ============== 数据模型类 ==============

class Order {
    private String orderId;
    private double totalAmount;
    private List<OrderItem> items = new java.util.ArrayList<>();
    
    public Order(String orderId, double totalAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
    }
    
    public String getOrderId() { return orderId; }
    public double getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return items; }
}

class OrderItem {
    private String name;
    private double price;
    private int quantity;
    
    public OrderItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}

class PromotionResult {
    private boolean success;
    private double discountAmount;
    private String description;
    
    public PromotionResult(boolean success, double discountAmount, String description) {
        this.success = success;
        this.discountAmount = discountAmount;
        this.description = description;
    }
    
    public boolean isSuccess() { return success; }
    public double getDiscountAmount() { return discountAmount; }
    public String getDescription() { return description; }
    
    @Override
    public String toString() {
        return "PromotionResult{success=" + success + ", discountAmount=" + discountAmount + ", description='" + description + "'}";
    }
}

class RiskContext {
    private String userId;
    private Double amount;
    private String deviceId;
    private String ipAddress;
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}

class RiskResult {
    private boolean risky;
    private int riskScore;
    private String reason;
    
    public RiskResult(boolean risky, int riskScore, String reason) {
        this.risky = risky;
        this.riskScore = riskScore;
        this.reason = reason;
    }
    
    public boolean isRisky() { return risky; }
    public int getRiskScore() { return riskScore; }
    public String getReason() { return reason; }
    
    @Override
    public String toString() {
        return "RiskResult{risky=" + risky + ", riskScore=" + riskScore + ", reason='" + reason + "'}";
    }
}

class RecommendationRequest {
    private String userId;
    private String category;
    private int limit;
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}

class RecommendationResult {
    private String itemName;
    private double score;
    private String reason;
    
    public RecommendationResult(String itemName, double score, String reason) {
        this.itemName = itemName;
        this.score = score;
        this.reason = reason;
    }
    
    public String getItemName() { return itemName; }
    public double getScore() { return score; }
    public String getReason() { return reason; }
    
    @Override
    public String toString() {
        return "RecommendationResult{itemName='" + itemName + "', score=" + score + ", reason='" + reason + "'}";
    }
}

class DataProcessingContext {
    private String rawData;
    private Map<String, Object> metadata = new java.util.HashMap<>();
    
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
    public Map<String, Object> getMetadata() { return metadata; }
}