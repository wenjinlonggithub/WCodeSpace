package com.architecture.designpattern.strategy;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

interface DiscountStrategy {
    double applyDiscount(OrderDiscountExample order);
    String getStrategyName();
}

class FullReductionDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(OrderDiscountExample order) {
        return order.getOriginalPrice() >= 500 ? order.getOriginalPrice() - 50 : order.getOriginalPrice();
    }
    
    @Override
    public String getStrategyName() {
        return "FULL_REDUCTION";
    }
}

class MemberDiscountStrategy implements DiscountStrategy {
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

class QuantityDiscountStrategy implements DiscountStrategy {
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
    private final Map<String, DiscountStrategy> strategies;
    
    public DiscountStrategyManager() {
        strategies = Map.of(
            "FULL_REDUCTION", new FullReductionDiscountStrategy(),
            "MEMBER_DISCOUNT", new MemberDiscountStrategy(),
            "QUANTITY_DISCOUNT", new QuantityDiscountStrategy()
        );
    }
    
    public double applyDiscount(String strategyName, OrderDiscountExample order) {
        DiscountStrategy strategy = strategies.get(strategyName);
        return strategy != null ? strategy.applyDiscount(order) : order.getOriginalPrice();
    }
    
    public double getBestDiscount(OrderDiscountExample order) {
        return strategies.values().stream()
                .mapToDouble(strategy -> strategy.applyDiscount(order))
                .min()
                .orElse(order.getOriginalPrice());
    }
}