package com.architecture.designpattern.strategy;

import lombok.val;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class StrategyInterviewQuestions {

    /**
     * ====================
     * 策略模式面试问题汇总与精讲
     * ====================
     */

    public void demonstrateAllQuestions() {
        System.out.println("=== 策略模式面试题精讲 ===");
        
        whatIsStrategy();
        coreComponents();
        advantagesAndDisadvantages();
        vsStatePattern();
        vsTemplateMethod();
        eliminateIfElse();
        springIntegration();
        realWorldApplications();
        performanceConsiderations();
        designPatternCombinations();
        practicalImplementation();
        commonMistakes();
        //springSpecificQuestions();
        //enterpriseScenarios();
        //performanceOptimization();
        //advancedTopics();
    }

    /**
     * Q1: 什么是策略模式？请详细解释其定义和核心思想
     * 
     * 标准答案：
     * 策略模式定义了一系列算法，把它们一个个封装起来，并且使它们可相互替换。
     * 策略模式让算法的变化独立于使用算法的客户端。
     */
    public void whatIsStrategy() {
        System.out.println("\n=== Q1: 什么是策略模式？ ===");
        System.out.println("定义：策略模式定义了一系列算法，把它们封装起来，使它们可以相互替换");
        System.out.println("核心思想：");
        System.out.println("1. 将算法的定义与使用分离");
        System.out.println("2. 算法可以在运行时动态切换");
        System.out.println("3. 新增算法不影响现有代码");
        System.out.println("4. 遵循开闭原则和单一职责原则");
        
        // 示例代码
        System.out.println("\n示例：计算器策略");
        CalculatorExample calculator = new CalculatorExample();
        
        // 加法策略
        calculator.setStrategy((a, b) -> a + b);
        System.out.println("5 + 3 = " + calculator.calculate(5, 3));
        
        // 乘法策略
        calculator.setStrategy((a, b) -> a * b);
        System.out.println("5 * 3 = " + calculator.calculate(5, 3));
    }

    /**
     * Q2: 策略模式的核心组件有哪些？各自的作用是什么？
     */
    public void coreComponents() {
        System.out.println("\n=== Q2: 策略模式的核心组件 ===");
        System.out.println("1. Strategy（策略接口）：");
        System.out.println("   - 定义所有具体策略的通用接口");
        System.out.println("   - 声明策略算法的抽象方法");
        System.out.println("   - 例：public interface PaymentStrategy { void pay(double amount); }");
        
        System.out.println("\n2. ConcreteStrategy（具体策略）：");
        System.out.println("   - 实现策略接口的具体算法");
        System.out.println("   - 封装特定的算法逻辑");
        System.out.println("   - 例：AlipayPayment、WechatPayment");
        
        System.out.println("\n3. Context（上下文）：");
        System.out.println("   - 持有策略对象的引用");
        System.out.println("   - 提供客户端调用策略的接口");
        System.out.println("   - 可以向策略传递数据");
        System.out.println("   - 例：PaymentContext、ShoppingCart");
        
        // 组件关系图（文字描述）
        System.out.println("\n组件关系：");
        System.out.println("Client -> Context -> Strategy <- ConcreteStrategy");
        System.out.println("客户端通过上下文使用策略，具体策略实现算法逻辑");
    }

    /**
     * Q3: 策略模式的优缺点是什么？
     */
    public void advantagesAndDisadvantages() {
        System.out.println("\n=== Q3: 策略模式的优缺点 ===");
        System.out.println("优点：");
        System.out.println("1. 算法可以自由切换");
        System.out.println("2. 避免使用多重条件判断");
        System.out.println("3. 扩展性良好，符合开闭原则");
        System.out.println("4. 算法的秘密性和安全性");
        System.out.println("5. 提高算法的保密性和安全性");
        System.out.println("6. 便于单元测试");
        
        System.out.println("\n缺点：");
        System.out.println("1. 策略类数量增多");
        System.out.println("2. 所有策略类都需要对外暴露");
        System.out.println("3. 客户端必须知道所有策略类");
        System.out.println("4. 增加了对象的数目");
        System.out.println("5. 只适合扁平的算法结构");
        
        System.out.println("\n适用场景：");
        System.out.println("- 多种算法完成同一工作");
        System.out.println("- 需要在运行时切换算法");
        System.out.println("- 算法使用的数据不应该暴露给客户端");
        System.out.println("- 一个类定义了多种行为，表现为多个条件语句");
    }

    /**
     * Q4: 策略模式与状态模式的区别？
     */
    public void vsStatePattern() {
        System.out.println("\n=== Q4: 策略模式 vs 状态模式 ===");
        System.out.println("相同点：");
        System.out.println("- 都消除了大量的条件语句");
        System.out.println("- 都是对象行为型模式");
        System.out.println("- 都使用组合关系");
        
        System.out.println("\n不同点：");
        System.out.println("策略模式:");
        System.out.println("- 策略之间相互独立");
        System.out.println("- 客户端选择使用哪个策略");
        System.out.println("- 策略可以随意切换");
        System.out.println("- 关注算法的替换");
        System.out.println("- 例：支付方式选择");
        
        System.out.println("\n状态模式:");
        System.out.println("- 状态之间可能有依赖关系");
        System.out.println("- 状态自身决定下一个状态");
        System.out.println("- 状态转换有规则");
        System.out.println("- 关注对象状态的变化");
        System.out.println("- 例：订单状态流转");
        
        // 示例对比
        demonstrateStrategyVsState();
    }

    /**
     * Q5: 策略模式与模板方法模式的区别？
     */
    public void vsTemplateMethod() {
        System.out.println("\n=== Q5: 策略模式 vs 模板方法模式 ===");
        System.out.println("策略模式（组合）:");
        System.out.println("- 使用组合关系");
        System.out.println("- 运行时选择算法");
        System.out.println("- 整个算法可替换");
        System.out.println("- 更灵活，但类更多");
        
        System.out.println("\n模板方法模式（继承）:");
        System.out.println("- 使用继承关系");
        System.out.println("- 编译时确定算法框架");
        System.out.println("- 只替换算法中的某些步骤");
        System.out.println("- 代码复用性更好");
        
        System.out.println("\n选择建议：");
        System.out.println("- 需要完全替换算法：使用策略模式");
        System.out.println("- 算法框架固定，只变化部分步骤：使用模板方法");
        System.out.println("- 运行时切换：策略模式");
        System.out.println("- 代码复用：模板方法");
    }

    /**
     * Q6: 如何消除策略模式中的if-else判断？
     */
    public void eliminateIfElse() {
        System.out.println("\n=== Q6: 消除策略模式的if-else ===");
        System.out.println("问题：传统方式仍然需要if-else来选择策略");
        
        System.out.println("\n解决方案1：策略工厂 + Map映射");
        StrategyFactoryExample factory = new StrategyFactoryExample();
        factory.processPayment("ALIPAY", 100.0);
        factory.processPayment("WECHAT", 200.0);
        
        System.out.println("\n解决方案2：注解驱动");
        System.out.println("- 使用@PaymentType注解标识策略");
        System.out.println("- Spring启动时扫描并注册策略");
        System.out.println("- 运行时根据类型获取策略");
        
        System.out.println("\n解决方案3：枚举策略");
        EnumStrategyExample.processDiscount(EnumStrategyExample.DiscountType.FULL_REDUCTION, 1000.0);
        EnumStrategyExample.processDiscount(EnumStrategyExample.DiscountType.PERCENTAGE, 1000.0);
        
        System.out.println("\n解决方案4：函数式接口");
        System.out.println("- 使用Lambda表达式定义策略");
        System.out.println("- Map存储策略名称和函数的映射");
        System.out.println("- 更简洁的代码实现");
    }

    /**
     * Q7: 在Spring中如何优雅地使用策略模式？
     */
    public void springIntegration() {
        System.out.println("\n=== Q7: Spring中的策略模式最佳实践 ===");
        System.out.println("1. 使用@Component注解注册策略：");
        System.out.println("   @Service");
        System.out.println("   @PaymentType(\"ALIPAY\")");
        System.out.println("   public class AlipayStrategy implements PaymentStrategy");
        
        System.out.println("\n2. 策略管理器自动注入：");
        System.out.println("   @Autowired");
        System.out.println("   private ApplicationContext context;");
        System.out.println("   PaymentStrategy strategy = context.getBean(type, PaymentStrategy.class);");
        
        System.out.println("\n3. 使用InitializingBean初始化策略映射：");
        System.out.println("   public void afterPropertiesSet() {");
        System.out.println("       // 扫描所有策略并建立映射关系");
        System.out.println("   }");
        
        System.out.println("\n4. 配置文件驱动策略选择：");
        System.out.println("   payment.default.strategy=ALIPAY");
        System.out.println("   payment.fallback.strategy=WECHAT");
        
        System.out.println("\n5. 策略链模式：");
        System.out.println("   - 多个策略组成处理链");
        System.out.println("   - 数据经过多个策略处理");
        System.out.println("   - 支持策略组合和复用");
    }

    /**
     * Q8: 策略模式的实际应用场景有哪些？
     */
    public void realWorldApplications() {
        System.out.println("\n=== Q8: 策略模式的实际应用场景 ===");
        System.out.println("1. 电商系统：");
        System.out.println("   - 支付策略：支付宝、微信、银行卡");
        System.out.println("   - 促销策略：满减、折扣、买赠、积分");
        System.out.println("   - 物流策略：顺丰、申通、圆通");
        System.out.println("   - 推荐策略：协同过滤、内容推荐、热度推荐");
        
        System.out.println("\n2. 数据处理：");
        System.out.println("   - 排序策略：快排、归并、堆排");
        System.out.println("   - 压缩策略：ZIP、RAR、GZIP");
        System.out.println("   - 序列化策略：JSON、XML、Protobuf");
        System.out.println("   - 缓存策略：LRU、LFU、FIFO");
        
        System.out.println("\n3. 业务规则：");
        System.out.println("   - 风控策略：黑名单、频率限制、金额限制");
        System.out.println("   - 定价策略：基础定价、会员定价、促销定价");
        System.out.println("   - 路由策略：最短路径、负载均衡、地理位置");
        
        System.out.println("\n4. 框架中的应用：");
        System.out.println("   - Spring的Validator验证策略");
        System.out.println("   - Java的Comparator排序策略");
        System.out.println("   - ThreadPoolExecutor的拒绝策略");
        System.out.println("   - SpringMVC的HandlerMapping策略");
    }

    /**
     * Q9: 策略模式的性能考虑因素有哪些？
     */
    public void performanceConsiderations() {
        System.out.println("\n=== Q9: 策略模式的性能考虑 ===");
        System.out.println("1. 对象创建开销：");
        System.out.println("   - 策略对象的创建和销毁成本");
        System.out.println("   - 建议：使用单例模式或对象池");
        System.out.println("   - Spring中使用@Service默认单例");
        
        System.out.println("\n2. 策略选择开销：");
        System.out.println("   - 避免复杂的if-else判断");
        System.out.println("   - 使用Map缓存策略映射");
        System.out.println("   - 预编译策略选择逻辑");
        
        System.out.println("\n3. 内存占用：");
        System.out.println("   - 策略类的数量影响内存占用");
        System.out.println("   - 考虑延迟加载不常用策略");
        System.out.println("   - 使用弱引用管理策略实例");
        
        System.out.println("\n4. 线程安全：");
        System.out.println("   - 无状态策略天然线程安全");
        System.out.println("   - 有状态策略需要考虑并发问题");
        System.out.println("   - 使用ThreadLocal存储状态");
        
        System.out.println("\n5. 优化建议：");
        System.out.println("   - 策略预热：提前初始化常用策略");
        System.out.println("   - 策略缓存：缓存计算结果");
        System.out.println("   - 异步执行：非关键策略异步处理");
    }

    /**
     * Q10: 策略模式可以与哪些设计模式结合使用？
     */
    public void designPatternCombinations() {
        System.out.println("\n=== Q10: 策略模式与其他模式的结合 ===");
        System.out.println("1. 策略模式 + 工厂模式：");
        System.out.println("   - 工厂负责创建和选择策略");
        System.out.println("   - 客户端不需要知道具体策略类");
        System.out.println("   - 实现策略的自动选择");
        
        System.out.println("\n2. 策略模式 + 单例模式：");
        System.out.println("   - 策略对象通常无状态，可以设计为单例");
        System.out.println("   - 减少对象创建开销");
        System.out.println("   - 提高性能和内存利用率");
        
        System.out.println("\n3. 策略模式 + 装饰者模式：");
        System.out.println("   - 对策略功能进行增强");
        System.out.println("   - 例：添加日志、缓存、监控");
        System.out.println("   - 保持策略接口不变");
        
        System.out.println("\n4. 策略模式 + 观察者模式：");
        System.out.println("   - 策略执行后通知观察者");
        System.out.println("   - 实现策略执行的监控和日志");
        System.out.println("   - 解耦策略执行和后续处理");
        
        System.out.println("\n5. 策略模式 + 责任链模式：");
        System.out.println("   - 多个策略组成处理链");
        System.out.println("   - 数据依次经过多个策略处理");
        System.out.println("   - 例：数据清洗流水线");
    }

    /**
     * Q11: 请实现一个完整的策略模式示例
     */
    public void practicalImplementation() {
        System.out.println("\n=== Q11: 完整的策略模式实现示例 ===");
        System.out.println("场景：电商平台的订单折扣计算");
        
        // 创建订单
        OrderDiscountExample order = new OrderDiscountExample(1000.0, "VIP", 3);
        
        // 应用不同的折扣策略
        DiscountStrategyManager manager = new DiscountStrategyManager();
        
        double finalPrice1 = manager.applyDiscount("FULL_REDUCTION", order);
        System.out.println("满减策略后价格：" + finalPrice1);
        
        double finalPrice2 = manager.applyDiscount("MEMBER_DISCOUNT", order);
        System.out.println("会员折扣后价格：" + finalPrice2);
        
        double finalPrice3 = manager.applyDiscount("QUANTITY_DISCOUNT", order);
        System.out.println("数量折扣后价格：" + finalPrice3);
        
        // 自动选择最优策略
        double bestPrice = manager.getBestDiscount(order);
        System.out.println("最优折扣后价格：" + bestPrice);
    }

    /**
     * Q12: 策略模式实现中的常见错误有哪些？
     */
    public void commonMistakes() {
        System.out.println("\n=== Q12: 策略模式的常见错误 ===");
        System.out.println("1. 策略接口设计过于复杂：");
        System.out.println("   - 错误：接口包含太多方法");
        System.out.println("   - 正确：单一职责，接口简洁");
        System.out.println("   - 建议：遵循接口隔离原则");
        
        System.out.println("\n2. 上下文与策略耦合过紧：");
        System.out.println("   - 错误：上下文直接操作策略内部状态");
        System.out.println("   - 正确：通过接口方法交互");
        System.out.println("   - 建议：保持接口的抽象性");
        
        System.out.println("\n3. 策略选择逻辑复杂化：");
        System.out.println("   - 错误：在客户端写复杂的选择逻辑");
        System.out.println("   - 正确：使用工厂模式封装选择逻辑");
        System.out.println("   - 建议：策略选择应该简单明了");
        
        System.out.println("\n4. 过度使用策略模式：");
        System.out.println("   - 错误：为简单逻辑创建策略");
        System.out.println("   - 正确：只有真正需要替换的算法才使用");
        System.out.println("   - 建议：权衡复杂度和灵活性");
        
        System.out.println("\n5. 忽略策略的线程安全：");
        System.out.println("   - 错误：有状态策略在多线程环境下共享");
        System.out.println("   - 正确：保持策略无状态或使用ThreadLocal");
        System.out.println("   - 建议：优先设计无状态策略");
        
        System.out.println("\n6. 策略命名不规范：");
        System.out.println("   - 错误：策略名称不清晰，难以理解");
        System.out.println("   - 正确：使用业务语言命名");
        System.out.println("   - 建议：命名应体现策略的业务含义");
    }

    // ============== 辅助示例类 ==============
    
    private void demonstrateStrategyVsState() {
        System.out.println("\n示例对比：");
        
        // 策略模式：支付方式选择
        System.out.println("策略模式 - 支付选择：");
        PaymentContextExample payment = new PaymentContextExample();
        payment.setStrategy("ALIPAY");
        payment.pay(100.0);
        payment.setStrategy("WECHAT");  // 可以随意切换
        payment.pay(200.0);
        
        // 状态模式：订单状态流转
        System.out.println("\n状态模式 - 订单状态：");
        OrderStateExample orderState = new OrderStateExample();
        orderState.nextState();  // 待支付 -> 已支付
        orderState.nextState();  // 已支付 -> 已发货
        // orderState.setState("DELIVERED"); // 不能随意设置状态
    }
}

// ============== 示例实现类 ==============

// 计算器示例
class CalculatorExample {
    private java.util.function.BinaryOperator<Integer> strategy;
    
    public void setStrategy(java.util.function.BinaryOperator<Integer> strategy) {
        this.strategy = strategy;
    }
    
    public int calculate(int a, int b) {
        return strategy.apply(a, b);
    }
}

// 策略工厂示例
class StrategyFactoryExample {
    private final Map<String, java.util.function.Consumer<Double>> strategies;
    
    public StrategyFactoryExample() {
        strategies = Map.of(
            "ALIPAY", amount -> System.out.println("支付宝支付：" + amount + "元"),
            "WECHAT", amount -> System.out.println("微信支付：" + amount + "元"),
            "BANK_CARD", amount -> System.out.println("银行卡支付：" + amount + "元")
        );
    }
    
    public void processPayment(String type, Double amount) {
        java.util.function.Consumer<Double> strategy = strategies.get(type);
        if (strategy != null) {
            strategy.accept(amount);
        } else {
            System.out.println("不支持的支付类型：" + type);
        }
    }
}

// 枚举策略示例
class EnumStrategyExample {
    enum DiscountType {
        FULL_REDUCTION(amount -> amount >= 500 ? amount - 50 : amount),
        PERCENTAGE(amount -> amount * 0.9),
        NO_DISCOUNT(amount -> amount);
        
        private final Function<Double, Double> calculator;
        
        DiscountType(Function<Double, Double> calculator) {
            this.calculator = calculator;
        }
        
        public double calculate(double amount) {
            return calculator.apply(amount);
        }
    }
    
    public static void processDiscount(DiscountType type, double amount) {
        double finalAmount = type.calculate(amount);
        System.out.println(type + "策略：" + amount + " -> " + finalAmount);
    }
}

// 支付上下文示例
class PaymentContextExample {
    private String currentStrategy;
    
    public void setStrategy(String strategy) {
        this.currentStrategy = strategy;
    }
    
    public void pay(double amount) {
        System.out.println("使用" + currentStrategy + "支付：" + amount + "元");
    }
}

// 订单状态示例
class OrderStateExample {
    private String currentState = "PENDING_PAYMENT";
    
    public void nextState() {
        switch (currentState) {
            case "PENDING_PAYMENT":
                currentState = "PAID";
                System.out.println("订单状态：待支付 -> 已支付");
                break;
            case "PAID":
                currentState = "SHIPPED";
                System.out.println("订单状态：已支付 -> 已发货");
                break;
            case "SHIPPED":
                currentState = "DELIVERED";
                System.out.println("订单状态：已发货 -> 已送达");
                break;
            default:
                System.out.println("订单已完成，无法继续流转");
        }
    }
}

// 订单折扣示例
class OrderDiscountExample {
    private double originalPrice;
    private String memberLevel;
    private int quantity;
    
    public OrderDiscountExample(double originalPrice, String memberLevel, int quantity) {
        this.originalPrice = originalPrice;
        this.memberLevel = memberLevel;
        this.quantity = quantity;
    }
    
    public double getOriginalPrice() { return originalPrice; }
    public String getMemberLevel() { return memberLevel; }
    public int getQuantity() { return quantity; }
}

interface DiscountStrategySec {
    double applyDiscount(OrderDiscountExample order);
    String getStrategyName();
}

class FullReductionDiscountStrategySec implements DiscountStrategySec {
    @Override
    public double applyDiscount(OrderDiscountExample order) {
        return order.getOriginalPrice() >= 500 ? order.getOriginalPrice() - 50 : order.getOriginalPrice();
    }
    
    @Override
    public String getStrategyName() {
        return "FULL_REDUCTION";
    }
}

class MemberDiscountStrategySec implements DiscountStrategySec {
    @Override
    public double applyDiscount(OrderDiscountExample order) {
        double discount = "VIP".equals(order.getMemberLevel()) ? 0.8 : 0.9;
        return order.getOriginalPrice() * discount;
    }
    
    @Override
    public String getStrategyName() {
        return "MEMBER_DISCOUNT";
    }
}

class QuantityDiscountStrategySec implements DiscountStrategySec {
    @Override
    public double applyDiscount(OrderDiscountExample order) {
        double discount = order.getQuantity() >= 3 ? 0.85 : 1.0;
        return order.getOriginalPrice() * discount;
    }
    
    @Override
    public String getStrategyName() {
        return "QUANTITY_DISCOUNT";
    }
}

class DiscountStrategyManager {
    private final Map<String, DiscountStrategySec> strategies;
    
    public DiscountStrategyManager() {
        strategies = Map.of(
            "FULL_REDUCTION", new FullReductionDiscountStrategySec(),
            "MEMBER_DISCOUNT", new MemberDiscountStrategySec(),
            "QUANTITY_DISCOUNT", new QuantityDiscountStrategySec()
        );
    }
    
    public double applyDiscount(String strategyName, OrderDiscountExample order) {
        DiscountStrategySec strategy = strategies.get(strategyName);
        return strategy != null ? strategy.applyDiscount(order) : order.getOriginalPrice();
    }
    
    public double getBestDiscount(OrderDiscountExample order) {
        return strategies.values().stream()
                .mapToDouble(strategy -> strategy.applyDiscount(order))
                .min()
                .orElse(order.getOriginalPrice());
    }
    
    /**
     * Q13: 在Spring框架中使用策略模式的最佳实践有哪些？
     */
    public void springSpecificQuestions() {
        System.out.println("\n=== Q13: Spring框架中策略模式的最佳实践 ===");
        
        System.out.println("1. 使用@Component注解自动注册策略：");
        System.out.println("   @Service");
        System.out.println("   @PaymentType(\"ALIPAY\")");
        System.out.println("   public class AlipayStrategy implements PaymentStrategy {}");
        
        System.out.println("\n2. 利用ApplicationContext自动装配策略：");
        System.out.println("   @Autowired");
        System.out.println("   private ApplicationContext applicationContext;");
        System.out.println("   Map<String, PaymentStrategy> strategies = ");
        System.out.println("       applicationContext.getBeansOfType(PaymentStrategy.class);");
        
        System.out.println("\n3. 使用@PostConstruct初始化策略映射：");
        System.out.println("   @PostConstruct");
        System.out.println("   public void initStrategies() {");
        System.out.println("       // 扫描并建立策略映射");
        System.out.println("   }");
        
        System.out.println("\n4. 结合@Conditional实现条件策略：");
        System.out.println("   @Component");
        System.out.println("   @ConditionalOnProperty(name=\"payment.strategy\", havingValue=\"alipay\")");
        System.out.println("   public class AlipayStrategy implements PaymentStrategy {}");
        
        System.out.println("\n5. 使用@Profile支持环境相关策略：");
        System.out.println("   @Component");
        System.out.println("   @Profile(\"production\")");
        System.out.println("   public class ProductionPaymentStrategy implements PaymentStrategy {}");
        
        System.out.println("\n面试官常问的深度问题：");
        System.out.println("Q: 如何处理策略的循环依赖？");
        System.out.println("A: 使用@Lazy注解延迟加载，或重新设计策略依赖关系");
        
        System.out.println("\nQ: 策略模式与Spring AOP如何结合？");
        System.out.println("A: 可以用AOP实现策略执行的切面，如日志、监控、异常处理等");
        
        System.out.println("\nQ: 如何实现策略的热插拔？");
        System.out.println("A: 结合Spring Boot Actuator和配置中心，动态刷新Bean配置");
    }
    
    /**
     * Q14: 在大型企业系统中，策略模式如何解决复杂业务场景？
     */
    public void enterpriseScenarios() {
        System.out.println("\n=== Q14: 企业级复杂业务场景应用 ===");
        
        System.out.println("场景1: 电商平台多维度定价策略");
        System.out.println("挑战：");
        System.out.println("- 基础价格、会员价格、促销价格、动态定价");
        System.out.println("- 地区差异化定价、时间段定价");
        System.out.println("- VIP等级定价、批量采购定价");
        
        System.out.println("\n解决方案：");
        System.out.println("- 使用策略链模式，按优先级执行定价策略");
        System.out.println("- 配置驱动的策略选择器");
        System.out.println("- 策略组合器支持多个定价因子");
        
        PricingStrategyDemo pricingDemo = new PricingStrategyDemo();
        pricingDemo.demonstratePricingStrategy();
        
        System.out.println("\n场景2: 金融风控多层级决策");
        System.out.println("挑战：");
        System.out.println("- 规则引擎、机器学习模型、专家系统");
        System.out.println("- 实时决策、批量处理、人工审核");
        System.out.println("- A/B测试、模型切换、降级策略");
        
        RiskControlDemo riskDemo = new RiskControlDemo();
        riskDemo.demonstrateRiskControl();
        
        System.out.println("\n场景3: 内容推荐算法引擎");
        System.out.println("特点：");
        System.out.println("- 多种推荐算法：协同过滤、内容相似、深度学习");
        System.out.println("- 实时个性化、召回排序、多目标优化");
        System.out.println("- 冷启动处理、降级兜底、效果评估");
        
        RecommendationEngineDemo recDemo = new RecommendationEngineDemo();
        recDemo.demonstrateRecommendation();
        
        System.out.println("\n企业级实施要点：");
        System.out.println("1. 策略版本管理：支持策略的版本控制和回滚");
        System.out.println("2. 配置中心集成：策略参数的动态配置");
        System.out.println("3. 监控告警：策略执行状况的实时监控");
        System.out.println("4. 灰度发布：新策略的渐进式上线");
        System.out.println("5. 性能优化：策略执行的缓存和并行化");
    }
    
    /**
     * Q15: 策略模式的性能优化有哪些关键技术？
     */
    public void performanceOptimization() {
        System.out.println("\n=== Q15: 策略模式性能优化技术 ===");
        
        System.out.println("1. 策略缓存优化：");
        System.out.println("问题：频繁创建策略对象导致GC压力");
        System.out.println("解决：");
        System.out.println("- 策略对象池化，重用策略实例");
        System.out.println("- 使用ConcurrentHashMap缓存策略");
        System.out.println("- Spring单例模式自动管理策略生命周期");
        
        StrategyCache cacheDemo = new StrategyCache();
        cacheDemo.demonstrateCaching();
        
        System.out.println("\n2. 预编译策略选择：");
        System.out.println("问题：运行时策略选择开销大");
        System.out.println("解决：");
        System.out.println("- 编译时生成策略路由表");
        System.out.println("- 使用注解处理器预生成策略映射");
        System.out.println("- 避免反射，使用直接方法调用");
        
        System.out.println("\n3. 并行策略执行：");
        System.out.println("场景：需要执行多个独立策略");
        System.out.println("解决：");
        System.out.println("- 使用CompletableFuture并行执行");
        System.out.println("- ForkJoinPool分治策略处理");
        System.out.println("- 异步策略执行，避免阻塞主线程");
        
        ParallelStrategyDemo parallelDemo = new ParallelStrategyDemo();
        parallelDemo.demonstrateParallelExecution();
        
        System.out.println("\n4. 内存优化技术：");
        System.out.println("- 策略无状态设计，避免内存泄漏");
        System.out.println("- 使用弱引用管理策略缓存");
        System.out.println("- 延迟加载非核心策略");
        System.out.println("- 策略数据的压缩存储");
        
        System.out.println("\n性能测试要点：");
        System.out.println("- 策略执行时间分布统计");
        System.out.println("- 内存使用量监控");
        System.out.println("- 并发场景下的性能表现");
        System.out.println("- 不同JVM参数的影响分析");
    }
    
    /**
     * Q16: 策略模式的高级主题和前沿技术
     */
    public void advancedTopics() {
        System.out.println("\n=== Q16: 策略模式的高级主题 ===");
        
        System.out.println("1. 函数式策略模式：");
        System.out.println("传统策略模式 vs 函数式策略模式");
        System.out.println("优势：");
        System.out.println("- 代码更简洁，减少类的数量");
        System.out.println("- 支持Lambda表达式和方法引用");
        System.out.println("- 更好的组合性和可读性");
        
        FunctionalStrategyDemo funcDemo = new FunctionalStrategyDemo();
        funcDemo.demonstrateFunctionalStrategy();
        
        System.out.println("\n2. 响应式策略模式：");
        System.out.println("结合Reactive Streams和RxJava");
        System.out.println("特点：");
        System.out.println("- 异步非阻塞策略执行");
        System.out.println("- 背压处理和流控制");
        System.out.println("- 事件驱动的策略触发");
        
        ReactiveStrategyDemo reactiveDemo = new ReactiveStrategyDemo();
        reactiveDemo.demonstrateReactiveStrategy();
        
        System.out.println("\n3. 微服务中的策略模式：");
        System.out.println("分布式策略执行");
        System.out.println("- 策略服务化，独立部署和扩缩容");
        System.out.println("- 服务发现和负载均衡");
        System.out.println("- 断路器和降级策略");
        
        System.out.println("\n4. AI/ML集成的智能策略：");
        System.out.println("- 机器学习模型作为策略实现");
        System.out.println("- 在线学习和模型更新");
        System.out.println("- A/B测试和多臂老虎机算法");
        System.out.println("- 策略效果的实时反馈和优化");
        
        System.out.println("\n5. 云原生策略模式：");
        System.out.println("- 容器化策略部署");
        System.out.println("- Serverless策略函数");
        System.out.println("- 策略编排和工作流");
        System.out.println("- 多云和混合云策略路由");
        
        System.out.println("\n面试加分点：");
        System.out.println("- 了解业界最新的策略模式应用");
        System.out.println("- 能结合具体技术栈讨论实现方案");
        System.out.println("- 有实际的性能优化和架构设计经验");
        System.out.println("- 能从业务价值角度分析技术选择");
        
        System.out.println("\n常见陷阱：");
        System.out.println("- 过度设计，为简单逻辑使用策略模式");
        System.out.println("- 忽略策略的生命周期管理");
        System.out.println("- 策略接口设计不合理，频繁变更");
        System.out.println("- 没有考虑策略执行的异常处理");
        System.out.println("- 忽略了策略模式对系统复杂度的影响");
    }
}

// ============== 面试题演示类实现 ==============

/**
 * 定价策略演示
 */
class PricingStrategyDemo {
    public void demonstratePricingStrategy() {
        System.out.println("\n定价策略链演示：");
        
        Product product = new Product("iPhone 15", 8999.0, "PREMIUM");
        Customer customer = new Customer("VIP001", "VIP", "BEIJING");
        
        PricingContext context = new PricingContext(product, customer, 2);
        PricingEngine engine = new PricingEngine();
        
        double finalPrice = engine.calculatePrice(context);
        System.out.println("最终价格：" + finalPrice + " 元");
        
        // 展示不同策略的影响
        System.out.println("定价过程分解：");
        System.out.println("- 基础价格：" + product.getBasePrice());
        System.out.println("- VIP折扣：-" + (product.getBasePrice() * 0.1));
        System.out.println("- 地区调整：+" + (product.getBasePrice() * 0.05));
        System.out.println("- 批量优惠：-" + (product.getBasePrice() * 0.03));
    }
}

/**
 * 风控决策演示
 */
class RiskControlDemo {
    public void demonstrateRiskControl() {
        System.out.println("\n多层级风控决策演示：");
        
        TransactionContext transaction = new TransactionContext();
        transaction.setUserId("USER_123");
        transaction.setAmount(50000.0);
        transaction.setDeviceId("DEVICE_456");
        transaction.setIpAddress("192.168.1.100");
        
        RiskDecisionEngine engine = new RiskDecisionEngine();
        RiskDecision decision = engine.evaluateRisk(transaction);
        
        System.out.println("风控决策结果：" + decision);
        
        if (decision.getAction().equals("MANUAL_REVIEW")) {
            System.out.println("触发人工审核原因：" + decision.getReason());
        }
    }
}

/**
 * 推荐引擎演示
 */
class RecommendationEngineDemo {
    public void demonstrateRecommendation() {
        System.out.println("\n智能推荐引擎演示：");
        
        RecommendationContext context = new RecommendationContext();
        context.setUserId("USER_789");
        context.setScenario("HOME_PAGE");
        context.setLimit(10);
        
        SmartRecommendationEngine engine = new SmartRecommendationEngine();
        List<RecommendationItem> recommendations = engine.recommend(context);
        
        System.out.println("推荐结果：");
        for (RecommendationItem item : recommendations) {
            System.out.println("- " + item.getTitle() + " (分数:" + item.getScore() + 
                             ", 策略:" + item.getStrategy() + ")");
        }
    }
}

/**
 * 策略缓存演示
 */
class StrategyCache {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    public void demonstrateCaching() {
        System.out.println("\n策略缓存优化演示：");
        
        long start = System.nanoTime();
        
        // 模拟无缓存的策略获取
        for (int i = 0; i < 1000; i++) {
            createNewStrategy("PAYMENT");
        }
        
        long withoutCache = System.nanoTime() - start;
        System.out.println("无缓存耗时：" + withoutCache / 1000000 + "ms");
        
        start = System.nanoTime();
        
        // 模拟有缓存的策略获取
        for (int i = 0; i < 1000; i++) {
            getCachedStrategy("PAYMENT");
        }
        
        long withCache = System.nanoTime() - start;
        System.out.println("有缓存耗时：" + withCache / 1000000 + "ms");
        System.out.println("性能提升：" + (withoutCache / withCache) + "倍");
    }
    
    private Object createNewStrategy(String type) {
        // 模拟策略对象创建开销
        return new Object();
    }
    
    private Object getCachedStrategy(String type) {
        return cache.computeIfAbsent(type, k -> new Object());
    }
}

/**
 * 并行策略执行演示
 */
class ParallelStrategyDemo {
    public void demonstrateParallelExecution() {
        System.out.println("\n并行策略执行演示：");
        
        List<String> strategyTypes = List.of("RISK", "PRICING", "PROMOTION", "INVENTORY");
        
        long start = System.currentTimeMillis();
        
        // 串行执行
        for (String type : strategyTypes) {
            executeStrategy(type);
        }
        
        long serialTime = System.currentTimeMillis() - start;
        System.out.println("串行执行耗时：" + serialTime + "ms");
        
        start = System.currentTimeMillis();
        
        // 并行执行
        CompletableFuture<?>[] futures = strategyTypes.stream()
                .map(type -> CompletableFuture.runAsync(() -> executeStrategy(type)))
                .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        
        long parallelTime = System.currentTimeMillis() - start;
        System.out.println("并行执行耗时：" + parallelTime + "ms");
        System.out.println("性能提升：" + (double)serialTime / parallelTime + "倍");
    }
    
    private void executeStrategy(String type) {
        try {
            // 模拟策略执行时间
            Thread.sleep(100);
            System.out.println("执行策略：" + type + " [线程:" + Thread.currentThread().getName() + "]");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * 函数式策略演示
 */
class FunctionalStrategyDemo {
    public void demonstrateFunctionalStrategy() {
        System.out.println("\n函数式策略模式演示：");
        
        // 传统方式
        DiscountCalculator traditionalCalculator = new DiscountCalculator();
        traditionalCalculator.setStrategy(new VipDiscountStrategy());
        double result1 = traditionalCalculator.calculate(1000.0);
        System.out.println("传统方式结果：" + result1);
        
        // 函数式方式
        FunctionalDiscountCalculator funcCalculator = new FunctionalDiscountCalculator();
        
        // 使用Lambda表达式
        funcCalculator.setDiscountFunction(amount -> amount * 0.8); // 8折
        double result2 = funcCalculator.calculate(1000.0);
        System.out.println("Lambda表达式结果：" + result2);
        
        // 使用方法引用
        funcCalculator.setDiscountFunction(this::calculateStudentDiscount);
        double result3 = funcCalculator.calculate(1000.0);
        System.out.println("方法引用结果：" + result3);
        
        // 策略组合
        Function<Double, Double> combinedStrategy = 
            ((Function<Double, Double>) amount -> amount * 0.9) // 9折
            .andThen(amount -> Math.max(amount - 50, amount * 0.8)); // 再减50或8折取较小值
        
        funcCalculator.setDiscountFunction(combinedStrategy);
        double result4 = funcCalculator.calculate(1000.0);
        System.out.println("组合策略结果：" + result4);
    }
    
    private double calculateStudentDiscount(double amount) {
        return amount * 0.7; // 学生7折
    }
}

/**
 * 响应式策略演示
 */
class ReactiveStrategyDemo {
    public void demonstrateReactiveStrategy() {
        System.out.println("\n响应式策略模式演示：");
        
        // 模拟响应式数据流
        System.out.println("处理数据流中的策略：");
        
        List<OrderEvent> events = List.of(
            new OrderEvent("ORDER_1", 100.0, "VIP"),
            new OrderEvent("ORDER_2", 500.0, "NORMAL"),
            new OrderEvent("ORDER_3", 1500.0, "VIP"),
            new OrderEvent("ORDER_4", 200.0, "STUDENT")
        );
        
        // 模拟流式处理（简化版，实际会使用RxJava或Reactor）
        events.stream()
              .map(this::applyDiscountStrategy)
              .forEach(result -> System.out.println("处理结果：" + result));
    }
    
    private OrderProcessResult applyDiscountStrategy(OrderEvent event) {
        /*val discountedAmount = switch (event.getCustomerType()) {
            case "VIP" -> event.getAmount() * 0.8;
            case "STUDENT" -> event.getAmount() * 0.7;
            default -> event.getAmount() * 0.95;
        };
        
        return new OrderProcessResult(event.getOrderId(), discountedAmount, 
                                    event.getCustomerType() + "_DISCOUNT");*/
    }
}

// ============== 辅助数据类 ==============

class Product {
    private String name;
    private double basePrice;
    private String category;
    
    public Product(String name, double basePrice, String category) {
        this.name = name;
        this.basePrice = basePrice;
        this.category = category;
    }
    
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public String getCategory() { return category; }
}

class Customer {
    private String id;
    private String level;
    private String region;
    
    public Customer(String id, String level, String region) {
        this.id = id;
        this.level = level;
        this.region = region;
    }
    
    public String getId() { return id; }
    public String getLevel() { return level; }
    public String getRegion() { return region; }
}

class PricingContext {
    private Product product;
    private Customer customer;
    private int quantity;
    
    public PricingContext(Product product, Customer customer, int quantity) {
        this.product = product;
        this.customer = customer;
        this.quantity = quantity;
    }
    
    public Product getProduct() { return product; }
    public Customer getCustomer() { return customer; }
    public int getQuantity() { return quantity; }
}

class PricingEngine {
    public double calculatePrice(PricingContext context) {
        double price = context.getProduct().getBasePrice();
        
        // VIP折扣
        if ("VIP".equals(context.getCustomer().getLevel())) {
            price *= 0.9;
        }
        
        // 地区价格调整
        if ("BEIJING".equals(context.getCustomer().getRegion())) {
            price *= 1.05;
        }
        
        // 批量折扣
        if (context.getQuantity() >= 2) {
            price *= 0.97;
        }
        
        return price * context.getQuantity();
    }
}

class TransactionContext {
    private String userId;
    private double amount;
    private String deviceId;
    private String ipAddress;
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}

class RiskDecision {
    private String action;
    private String reason;
    private int riskScore;
    
    public RiskDecision(String action, String reason, int riskScore) {
        this.action = action;
        this.reason = reason;
        this.riskScore = riskScore;
    }
    
    public String getAction() { return action; }
    public String getReason() { return reason; }
    public int getRiskScore() { return riskScore; }
    
    @Override
    public String toString() {
        return "RiskDecision{action='" + action + "', reason='" + reason + 
               "', riskScore=" + riskScore + "}";
    }
}

class RiskDecisionEngine {
    public RiskDecision evaluateRisk(TransactionContext context) {
        int riskScore = 0;
        StringBuilder reasons = new StringBuilder();
        
        // 金额风险
        if (context.getAmount() > 10000) {
            riskScore += 30;
            reasons.append("大额交易;");
        }
        
        // 设备风险（模拟）
        if (context.getDeviceId().contains("456")) {
            riskScore += 20;
            reasons.append("可疑设备;");
        }
        
        // IP风险（模拟）
        if (!context.getIpAddress().startsWith("192.168")) {
            riskScore += 25;
            reasons.append("异常IP;");
        }
        
        String action = riskScore > 50 ? "REJECT" : 
                       riskScore > 30 ? "MANUAL_REVIEW" : "APPROVE";
        
        return new RiskDecision(action, reasons.toString(), riskScore);
    }
}

class RecommendationContext {
    private String userId;
    private String scenario;
    private int limit;
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}

class RecommendationItem {
    private String title;
    private double score;
    private String strategy;
    
    public RecommendationItem(String title, double score, String strategy) {
        this.title = title;
        this.score = score;
        this.strategy = strategy;
    }
    
    public String getTitle() { return title; }
    public double getScore() { return score; }
    public String getStrategy() { return strategy; }
}

class SmartRecommendationEngine {
    public List<RecommendationItem> recommend(RecommendationContext context) {
        return List.of(
            new RecommendationItem("iPhone 15", 0.95, "协同过滤"),
            new RecommendationItem("MacBook Pro", 0.88, "内容相似"),
            new RecommendationItem("AirPods", 0.82, "购买关联"),
            new RecommendationItem("iPad", 0.75, "用户画像"),
            new RecommendationItem("Apple Watch", 0.70, "热度推荐")
        );
    }
}

// 传统策略模式相关类
interface TraditionalDiscountStrategy {
    double calculateDiscount(double amount);
}

class VipDiscountStrategy implements TraditionalDiscountStrategy {
    @Override
    public double calculateDiscount(double amount) {
        return amount * 0.8;
    }
}

class DiscountCalculator {
    private TraditionalDiscountStrategy strategy;
    
    public void setStrategy(TraditionalDiscountStrategy strategy) {
        this.strategy = strategy;
    }
    
    public double calculate(double amount) {
        return strategy.calculateDiscount(amount);
    }
}

// 函数式策略模式相关类
class FunctionalDiscountCalculator {
    private Function<Double, Double> discountFunction;
    
    public void setDiscountFunction(Function<Double, Double> discountFunction) {
        this.discountFunction = discountFunction;
    }
    
    public double calculate(double amount) {
        return discountFunction.apply(amount);
    }
}

// 响应式相关类
class OrderEvent {
    private String orderId;
    private double amount;
    private String customerType;
    
    public OrderEvent(String orderId, double amount, String customerType) {
        this.orderId = orderId;
        this.amount = amount;
        this.customerType = customerType;
    }
    
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getCustomerType() { return customerType; }
}

class OrderProcessResult {
    private String orderId;
    private double finalAmount;
    private String appliedStrategy;
    
    public OrderProcessResult(String orderId, double finalAmount, String appliedStrategy) {
        this.orderId = orderId;
        this.finalAmount = finalAmount;
        this.appliedStrategy = appliedStrategy;
    }
    
    @Override
    public String toString() {
        return "OrderProcessResult{orderId='" + orderId + "', finalAmount=" + finalAmount + 
               ", appliedStrategy='" + appliedStrategy + "'}";
    }
}