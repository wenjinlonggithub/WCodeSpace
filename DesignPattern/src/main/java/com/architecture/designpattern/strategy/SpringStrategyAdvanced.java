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
 * 1. 消除复杂的if-else判断逻辑，提升代码可读性
 * 2. 提高代码的可扩展性和可维护性，符合开闭原则
 * 3. 实现策略的自动发现和注册，减少手动配置
 * 4. 支持策略链和组合策略，实现复杂业务流程
 * 5. 提供统一的策略管理和调用机制，简化客户端使用
 * 6. 支持策略的动态加载和热插拔
 * 7. 实现策略执行的监控、日志和性能优化
 * 8. 提供策略间的依赖注入和资源共享
 * 
 * 深层业务背景和解决方案：
 * 
 * 电商领域：
 * - 优惠券策略：满减、折扣、买赠、阶梯优惠、新人专享
 * - 定价策略：基础定价、会员定价、促销定价、动态定价、区域定价
 * - 库存策略：预占库存、实时扣减、分布式库存、虚拟库存
 * - 物流策略：就近发货、成本最优、时效最快、绿色物流
 * 
 * 金融风控：
 * - 规则引擎：黑名单校验、频率限制、金额限制、设备指纹、行为分析
 * - 模型策略：机器学习模型、专家系统、规则引擎、评分卡模型
 * - 决策引擎：实时决策、批量决策、人工审核、自动放行
 * 
 * 内容推荐：
 * - 推荐算法：协同过滤、基于内容、深度学习、混合推荐
 * - 召回策略：用户行为召回、内容标签召回、热度召回、实时召回
 * - 排序策略：相关度排序、时间排序、个性化排序、多目标优化
 * 
 * 数据处理：
 * - 清洗策略：去重、标准化、异常值处理、缺失值填充
 * - 存储策略：关系数据库、NoSQL、对象存储、搜索引擎
 * - 计算策略：实时计算、批处理、流式处理、混合计算
 * 
 * 不使用策略模式的痛点：
 * - 业务逻辑分散在各处，难以统一管理和优化
 * - 新增业务规则需要修改多处代码，容易引入Bug
 * - 复杂的条件判断导致代码难以理解和维护
 * - 不同业务策略之间耦合严重，难以独立测试
 * - 无法灵活组合和复用已有的业务逻辑
 * - 策略变更影响面大，部署风险高
 * 
 * 策略模式的企业级优势：
 * - 业务逻辑模块化，便于团队并行开发
 * - 策略可独立部署和版本管理
 * - 支持A/B测试和灰度发布
 * - 便于性能监控和业务分析
 * - 提高代码复用率，降低开发成本
 * - 增强系统的可测试性和可维护性
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
        
        // 5. 策略工厂与Spring集成演示
        demonstrateSpringStrategyFactory();
        
        // 6. 配置驱动策略选择演示
        demonstrateConfigDrivenStrategy();
        
        // 7. 策略监控与性能优化演示
        demonstrateStrategyMonitoring();
        
        // 8. 动态策略加载演示
        demonstrateDynamicStrategyLoading();
        
        // 9. 条件策略与Profile演示
        demonstrateConditionalStrategy();
        
        // 10. 事件驱动策略演示
        demonstrateEventDrivenStrategy();
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
    
    private void demonstrateSpringStrategyFactory() {
        System.out.println("\n5. Spring策略工厂集成演示：");
        
        SpringStrategyFactory factory = new SpringStrategyFactory(applicationContext);
        
        // 自动注册所有策略
        factory.registerStrategies();
        
        // 通过工厂获取策略
        PaymentProcessingStrategyW paymentStrategy = factory.getStrategy("PAYMENT", PaymentProcessingStrategyW.class);
        System.out.println("获取支付策略：" + paymentStrategy.getClass().getSimpleName());
        
        NotificationSendingStrategy notificationStrategy = factory.getStrategy("NOTIFICATION", NotificationSendingStrategy.class);
        System.out.println("获取通知策略：" + notificationStrategy.getClass().getSimpleName());
        
        System.out.println("策略工厂的优势：");
        System.out.println("- 统一的策略获取入口");
        System.out.println("- 自动发现和注册策略");
        System.out.println("- 支持泛型类型安全");
        System.out.println("- 便于扩展和维护");
    }
    
    private void demonstrateConfigDrivenStrategy() {
        System.out.println("\n6. 配置驱动策略选择演示：");
        
        ConfigDrivenStrategyManager configManager = new ConfigDrivenStrategyManager(applicationContext);
        
        // 模拟不同环境的配置
        ConfigurationContext config = new ConfigurationContext();
        config.setEnvironment("production");
        config.setRegion("china");
        config.setFeatureFlags(Map.of("enableNewPromotion", true, "enableML", false));
        
        String selectedStrategy = configManager.selectStrategy("PROMOTION", config);
        System.out.println("配置驱动选择的策略：" + selectedStrategy);
        
        // 切换到测试环境
        config.setEnvironment("test");
        selectedStrategy = configManager.selectStrategy("PROMOTION", config);
        System.out.println("测试环境选择的策略：" + selectedStrategy);
        
        System.out.println("配置驱动的好处：");
        System.out.println("- 无需代码修改即可切换策略");
        System.out.println("- 支持环境相关的策略选择");
        System.out.println("- 便于A/B测试和灰度发布");
        System.out.println("- 提高系统的灵活性");
    }
    
    private void demonstrateStrategyMonitoring() {
        System.out.println("\n7. 策略监控与性能优化演示：");
        
        MonitoredStrategyManager monitorManager = new MonitoredStrategyManager(applicationContext);
        
        // 执行策略并监控
        StrategyExecutionContext context = new StrategyExecutionContext();
        context.setStrategyType("PROMOTION");
        context.setRequestId("REQ_001");
        context.setParameters(Map.of("amount", 1000.0, "userId", "user123"));
        
        StrategyExecutionResult result = monitorManager.executeStrategy(context);
        System.out.println("策略执行结果：" + result);
        
        // 查看性能统计
        StrategyPerformanceMetrics metrics = monitorManager.getMetrics("PROMOTION");
        System.out.println("策略性能指标：" + metrics);
        
        System.out.println("策略监控的价值：");
        System.out.println("- 实时监控策略执行状况");
        System.out.println("- 性能瓶颈识别和优化");
        System.out.println("- 异常策略快速定位");
        System.out.println("- 业务指标跟踪分析");
    }
    
    private void demonstrateDynamicStrategyLoading() {
        System.out.println("\n8. 动态策略加载演示：");
        
        DynamicStrategyLoader loader = new DynamicStrategyLoader(applicationContext);
        
        // 动态加载新的策略
        String strategyCode = null;
            /**"""
            public class DynamicPromotionStrategy implements PromotionStrategy {
                public PromotionResult apply(Order order) {
                    double discount = order.getTotalAmount() * 0.05;
                    return new PromotionResult(true, discount, "动态策略5%折扣");
                }
                public boolean isApplicable(Order order) { return true; }
                public String getType() { return "DYNAMIC"; }
            }
            """;*/
        
        boolean loaded = loader.loadStrategy("DYNAMIC", strategyCode);
        System.out.println("动态策略加载结果：" + (loaded ? "成功" : "失败"));
        
        if (loaded) {
            // 使用动态加载的策略
            Order testOrder = new Order("TEST_ORDER", 2000.0);
            PromotionResult result = loader.executeStrategy("DYNAMIC", testOrder);
            System.out.println("动态策略执行结果：" + result);
        }
        
        System.out.println("动态加载的应用场景：");
        System.out.println("- 热修复业务逻辑Bug");
        System.out.println("- 快速响应营销活动需求");
        System.out.println("- 在线配置业务规则");
        System.out.println("- 支持插件化架构");
    }
    
    private void demonstrateConditionalStrategy() {
        System.out.println("\n9. 条件策略与Profile演示：");
        
        ConditionalStrategyManager conditionalManager = new ConditionalStrategyManager(applicationContext);
        
        // 根据条件自动选择策略
        ConditionContext condition = new ConditionContext();
        condition.setProfile("production");
        condition.setProperties(Map.of("spring.profiles.active", "production", "region", "china"));
        
        String selectedStrategy = conditionalManager.getConditionalStrategy("PAYMENT", condition);
        System.out.println("条件策略选择结果：" + selectedStrategy);
        
        // 切换到开发环境
        condition.setProfile("dev");
        condition.setProperties(Map.of("spring.profiles.active", "dev", "region", "china"));
        
        selectedStrategy = conditionalManager.getConditionalStrategy("PAYMENT", condition);
        System.out.println("开发环境策略：" + selectedStrategy);
        
        System.out.println("条件策略的优势：");
        System.out.println("- 基于运行时条件自动选择");
        System.out.println("- 与Spring Profile深度集成");
        System.out.println("- 支持复杂的条件逻辑");
        System.out.println("- 提高部署的灵活性");
    }
    
    private void demonstrateEventDrivenStrategy() {
        System.out.println("\n10. 事件驱动策略演示：");
        
        EventDrivenStrategyManager eventManager = new EventDrivenStrategyManager(applicationContext);
        
        // 发布业务事件，触发相应策略
        BusinessEvent orderEvent = new BusinessEvent();
        orderEvent.setEventType("ORDER_CREATED");
        orderEvent.setEventData(Map.of("orderId", "ORDER_001", "userId", "user123", "amount", 500.0));
        orderEvent.setTimestamp(System.currentTimeMillis());
        
        eventManager.handleEvent(orderEvent);
        
        // 发布用户事件
        BusinessEvent userEvent = new BusinessEvent();
        userEvent.setEventType("USER_LOGIN");
        userEvent.setEventData(Map.of("userId", "user123", "loginTime", System.currentTimeMillis()));
        userEvent.setTimestamp(System.currentTimeMillis());
        
        eventManager.handleEvent(userEvent);
        
        System.out.println("事件驱动策略的特点：");
        System.out.println("- 松耦合的事件处理机制");
        System.out.println("- 异步策略执行");
        System.out.println("- 事件溯源和重放");
        System.out.println("- 易于扩展新的事件处理器");
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

// ============== Spring策略工厂 ==============

/**
 * Spring集成的通用策略工厂
 */
@Component
class SpringStrategyFactory {
    
    private final ApplicationContext applicationContext;
    private final Map<String, Map<Class<?>, Object>> strategyRegistry = new ConcurrentHashMap<>();
    
    public SpringStrategyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getStrategy(String strategyName, Class<T> strategyType) {
        Map<Class<?>, Object> typeMap = strategyRegistry.get(strategyName);
        if (typeMap != null) {
            return (T) typeMap.get(strategyType);
        }
        return null;
    }
    
    public void registerStrategies() {
        // 注册支付策略
        registerStrategy("PAYMENT", PaymentProcessingStrategyW.class, new DefaultPaymentStrategyW());
        
        // 注册通知策略
        registerStrategy("NOTIFICATION", NotificationSendingStrategy.class, new DefaultNotificationStrategy());
        
        System.out.println("策略注册完成，共注册 " + strategyRegistry.size() + " 种策略类型");
    }
    
    private <T> void registerStrategy(String strategyName, Class<T> strategyType, T strategy) {
        strategyRegistry.computeIfAbsent(strategyName, k -> new ConcurrentHashMap<>())
                       .put(strategyType, strategy);
    }
}

interface PaymentProcessingStrategyW {
    void processPayment(String orderId, double amount);
}

interface NotificationSendingStrategy {
    void sendNotification(String userId, String message);
}

class DefaultPaymentStrategyW implements PaymentProcessingStrategyW {
    @Override
    public void processPayment(String orderId, double amount) {
        System.out.println("默认支付策略处理：订单" + orderId + "，金额" + amount);
    }
}

class DefaultNotificationStrategy implements NotificationSendingStrategy {
    @Override
    public void sendNotification(String userId, String message) {
        System.out.println("默认通知策略：向用户" + userId + "发送消息：" + message);
    }
}

// ============== 配置驱动策略 ==============

/**
 * 配置驱动的策略管理器
 */
@Component
class ConfigDrivenStrategyManager {
    
    private final ApplicationContext applicationContext;
    private final Map<String, StrategySelector> strategySelectors = new ConcurrentHashMap<>();
    
    public ConfigDrivenStrategyManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initializeStrategySelectors();
    }
    
    private void initializeStrategySelectors() {
        // 初始化不同业务域的策略选择器
        strategySelectors.put("PROMOTION", new PromotionStrategySelector());
        strategySelectors.put("PAYMENT", new PaymentStrategySelector());
        strategySelectors.put("NOTIFICATION", new NotificationStrategySelector());
    }
    
    public String selectStrategy(String domain, ConfigurationContext config) {
        StrategySelector selector = strategySelectors.get(domain);
        if (selector != null) {
            return selector.selectStrategy(config);
        }
        return "DEFAULT";
    }
}

interface StrategySelector {
    String selectStrategy(ConfigurationContext config);
}

class PromotionStrategySelector implements StrategySelector {
    @Override
    public String selectStrategy(ConfigurationContext config) {
        if ("test".equals(config.getEnvironment())) {
            return "TEST_PROMOTION";
        }
        
        if (config.getFeatureFlags().getOrDefault("enableNewPromotion", false)) {
            return "NEW_PROMOTION";
        }
        
        return "DEFAULT_PROMOTION";
    }
}

class PaymentStrategySelector implements StrategySelector {
    @Override
    public String selectStrategy(ConfigurationContext config) {
        if ("production".equals(config.getEnvironment())) {
            return "PRODUCTION_PAYMENT";
        } else if ("test".equals(config.getEnvironment())) {
            return "TEST_PAYMENT";
        }
        return "DEV_PAYMENT";
    }
}

class NotificationStrategySelector implements StrategySelector {
    @Override
    public String selectStrategy(ConfigurationContext config) {
        String region = config.getRegion();
        if ("china".equals(region)) {
            return "CHINA_NOTIFICATION";
        } else if ("us".equals(region)) {
            return "US_NOTIFICATION";
        }
        return "GLOBAL_NOTIFICATION";
    }
}

class ConfigurationContext {
    private String environment;
    private String region;
    private Map<String, Boolean> featureFlags = new java.util.HashMap<>();
    
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Map<String, Boolean> getFeatureFlags() { return featureFlags; }
    public void setFeatureFlags(Map<String, Boolean> featureFlags) { this.featureFlags = featureFlags; }
}

// ============== 策略监控 ==============

/**
 * 策略执行监控管理器
 */
@Component
class MonitoredStrategyManager {
    
    private final ApplicationContext applicationContext;
    private final Map<String, StrategyPerformanceMetrics> metricsMap = new ConcurrentHashMap<>();
    
    public MonitoredStrategyManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public StrategyExecutionResult executeStrategy(StrategyExecutionContext context) {
        String strategyType = context.getStrategyType();
        long startTime = System.currentTimeMillis();
        
        System.out.println("开始执行策略：" + strategyType + "，请求ID：" + context.getRequestId());
        
        try {
            // 模拟策略执行
            Thread.sleep(100); // 模拟执行时间
            
            StrategyExecutionResult result = new StrategyExecutionResult();
            result.setSuccess(true);
            result.setStrategyType(strategyType);
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            result.setResult(Map.of("discount", 100.0, "message", "策略执行成功"));
            
            // 更新性能指标
            updateMetrics(strategyType, result.getExecutionTime(), true);
            
            return result;
            
        } catch (Exception e) {
            updateMetrics(strategyType, System.currentTimeMillis() - startTime, false);
            
            StrategyExecutionResult result = new StrategyExecutionResult();
            result.setSuccess(false);
            result.setStrategyType(strategyType);
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            result.setErrorMessage(e.getMessage());
            
            return result;
        }
    }
    
    private void updateMetrics(String strategyType, long executionTime, boolean success) {
        StrategyPerformanceMetrics metrics = metricsMap.computeIfAbsent(strategyType, 
            k -> new StrategyPerformanceMetrics(strategyType));
        
        metrics.addExecution(executionTime, success);
    }
    
    public StrategyPerformanceMetrics getMetrics(String strategyType) {
        return metricsMap.get(strategyType);
    }
}

class StrategyExecutionContext {
    private String strategyType;
    private String requestId;
    private Map<String, Object> parameters;
    
    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}

class StrategyExecutionResult {
    private boolean success;
    private String strategyType;
    private long executionTime;
    private Map<String, Object> result;
    private String errorMessage;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    public Map<String, Object> getResult() { return result; }
    public void setResult(Map<String, Object> result) { this.result = result; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    @Override
    public String toString() {
        return "StrategyExecutionResult{" +
                "success=" + success +
                ", strategyType='" + strategyType + '\'' +
                ", executionTime=" + executionTime +
                ", result=" + result +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}

class StrategyPerformanceMetrics {
    private final String strategyType;
    private long totalExecutions = 0;
    private long successfulExecutions = 0;
    private long totalExecutionTime = 0;
    private long maxExecutionTime = 0;
    private long minExecutionTime = Long.MAX_VALUE;
    
    public StrategyPerformanceMetrics(String strategyType) {
        this.strategyType = strategyType;
    }
    
    public synchronized void addExecution(long executionTime, boolean success) {
        totalExecutions++;
        totalExecutionTime += executionTime;
        
        if (success) {
            successfulExecutions++;
        }
        
        maxExecutionTime = Math.max(maxExecutionTime, executionTime);
        minExecutionTime = Math.min(minExecutionTime, executionTime);
    }
    
    public double getSuccessRate() {
        return totalExecutions == 0 ? 0.0 : (double) successfulExecutions / totalExecutions;
    }
    
    public double getAverageExecutionTime() {
        return totalExecutions == 0 ? 0.0 : (double) totalExecutionTime / totalExecutions;
    }
    
    @Override
    public String toString() {
        return "StrategyPerformanceMetrics{" +
                "strategyType='" + strategyType + '\'' +
                ", totalExecutions=" + totalExecutions +
                ", successRate=" + String.format("%.2f%%", getSuccessRate() * 100) +
                ", avgExecutionTime=" + String.format("%.2fms", getAverageExecutionTime()) +
                ", maxExecutionTime=" + maxExecutionTime +
                ", minExecutionTime=" + (minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime) +
                '}';
    }
}

// ============== 动态策略加载 ==============

/**
 * 动态策略加载器
 */
@Component
class DynamicStrategyLoader {
    
    private final ApplicationContext applicationContext;
    private final Map<String, Object> dynamicStrategies = new ConcurrentHashMap<>();
    
    public DynamicStrategyLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public boolean loadStrategy(String strategyName, String strategyCode) {
        try {
            // 模拟动态编译和加载过程
            System.out.println("正在编译策略代码：" + strategyName);
            System.out.println("代码内容：\n" + strategyCode);
            
            // 实际项目中，这里会使用JavaCompiler API或脚本引擎
            // 这里我们模拟一个简单的动态策略
            DynamicPromotionStrategy strategy = new DynamicPromotionStrategy();
            dynamicStrategies.put(strategyName, strategy);
            
            System.out.println("策略加载成功：" + strategyName);
            return true;
            
        } catch (Exception e) {
            System.err.println("策略加载失败：" + e.getMessage());
            return false;
        }
    }
    
    public PromotionResult executeStrategy(String strategyName, Order order) {
        Object strategy = dynamicStrategies.get(strategyName);
        if (strategy instanceof PromotionStrategy) {
            return ((PromotionStrategy) strategy).apply(order);
        }
        return new PromotionResult(false, 0, "策略未找到：" + strategyName);
    }
    
    // 模拟的动态策略实现
    private static class DynamicPromotionStrategy implements PromotionStrategy {
        @Override
        public PromotionResult apply(Order order) {
            double discount = order.getTotalAmount() * 0.05; // 5%折扣
            return new PromotionResult(true, discount, "动态加载策略：5%折扣");
        }
        
        @Override
        public boolean isApplicable(Order order) {
            return order.getTotalAmount() > 0;
        }
        
        @Override
        public String getType() {
            return "DYNAMIC";
        }
    }
}

// ============== 条件策略 ==============

/**
 * 条件策略管理器
 */
@Component
class ConditionalStrategyManager {
    
    private final ApplicationContext applicationContext;
    private final Map<String, ConditionalStrategyRule> rules = new ConcurrentHashMap<>();
    
    public ConditionalStrategyManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initializeConditionalRules();
    }
    
    private void initializeConditionalRules() {
        // 支付策略条件规则
        rules.put("PAYMENT", new ConditionalStrategyRule() {
            @Override
            public String selectStrategy(ConditionContext context) {
                if ("production".equals(context.getProfile())) {
                    return "PRODUCTION_PAYMENT_STRATEGY";
                } else if ("dev".equals(context.getProfile())) {
                    return "DEV_PAYMENT_STRATEGY";
                }
                return "DEFAULT_PAYMENT_STRATEGY";
            }
        });
        
        // 通知策略条件规则
        rules.put("NOTIFICATION", new ConditionalStrategyRule() {
            @Override
            public String selectStrategy(ConditionContext context) {
                String region = (String) context.getProperties().get("region");
                if ("china".equals(region)) {
                    return "WECHAT_NOTIFICATION_STRATEGY";
                }
                return "EMAIL_NOTIFICATION_STRATEGY";
            }
        });
    }
    
    public String getConditionalStrategy(String strategyType, ConditionContext context) {
        ConditionalStrategyRule rule = rules.get(strategyType);
        if (rule != null) {
            return rule.selectStrategy(context);
        }
        return "DEFAULT_STRATEGY";
    }
}

interface ConditionalStrategyRule {
    String selectStrategy(ConditionContext context);
}

class ConditionContext {
    private String profile;
    private Map<String, Object> properties = new java.util.HashMap<>();
    
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }
}

// ============== 事件驱动策略 ==============

/**
 * 事件驱动策略管理器
 */
@Component
class EventDrivenStrategyManager {
    
    private final ApplicationContext applicationContext;
    private final Map<String, java.util.List<EventStrategyHandler>> eventHandlers = new ConcurrentHashMap<>();
    
    public EventDrivenStrategyManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        registerEventHandlers();
    }
    
    private void registerEventHandlers() {
        // 注册订单事件处理器
        java.util.List<EventStrategyHandler> orderHandlers = new java.util.ArrayList<>();
        orderHandlers.add(new OrderCreatedHandler());
        orderHandlers.add(new InventoryUpdateHandler());
        orderHandlers.add(new NotificationHandler());
        eventHandlers.put("ORDER_CREATED", orderHandlers);
        
        // 注册用户事件处理器
        java.util.List<EventStrategyHandler> userHandlers = new java.util.ArrayList<>();
        userHandlers.add(new UserLoginHandler());
        userHandlers.add(new SecurityCheckHandler());
        eventHandlers.put("USER_LOGIN", userHandlers);
    }
    
    public void handleEvent(BusinessEvent event) {
        System.out.println("处理业务事件：" + event.getEventType());
        
        java.util.List<EventStrategyHandler> handlers = eventHandlers.get(event.getEventType());
        if (handlers != null) {
            for (EventStrategyHandler handler : handlers) {
                try {
                    handler.handle(event);
                } catch (Exception e) {
                    System.err.println("事件处理失败：" + handler.getClass().getSimpleName() + 
                                     ", 错误：" + e.getMessage());
                }
            }
        } else {
            System.out.println("没有找到事件处理器：" + event.getEventType());
        }
    }
}

interface EventStrategyHandler {
    void handle(BusinessEvent event);
}

class OrderCreatedHandler implements EventStrategyHandler {
    @Override
    public void handle(BusinessEvent event) {
        Map<String, Object> data = event.getEventData();
        System.out.println("处理订单创建事件：订单ID=" + data.get("orderId") + 
                          ", 用户ID=" + data.get("userId") + 
                          ", 金额=" + data.get("amount"));
    }
}

class InventoryUpdateHandler implements EventStrategyHandler {
    @Override
    public void handle(BusinessEvent event) {
        System.out.println("更新库存：订单ID=" + event.getEventData().get("orderId"));
    }
}

class NotificationHandler implements EventStrategyHandler {
    @Override
    public void handle(BusinessEvent event) {
        System.out.println("发送订单通知：用户ID=" + event.getEventData().get("userId"));
    }
}

class UserLoginHandler implements EventStrategyHandler {
    @Override
    public void handle(BusinessEvent event) {
        System.out.println("用户登录处理：用户ID=" + event.getEventData().get("userId"));
    }
}

class SecurityCheckHandler implements EventStrategyHandler {
    @Override
    public void handle(BusinessEvent event) {
        System.out.println("安全检查：用户ID=" + event.getEventData().get("userId") + 
                          ", 登录时间=" + event.getEventData().get("loginTime"));
    }
}

class BusinessEvent {
    private String eventType;
    private Map<String, Object> eventData;
    private long timestamp;
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Map<String, Object> getEventData() { return eventData; }
    public void setEventData(Map<String, Object> eventData) { this.eventData = eventData; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}