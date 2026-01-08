package com.architecture.designpattern.strategy;

/**
 * 策略模式完整演示
 * 展示所有策略模式的实现和应用场景
 */
public class StrategyDemo {
    
    /**
     * 演示基础策略模式概念
     */
    public static void demonstrateBasicConcepts() {
        System.out.println("========== 策略模式基础概念演示 ==========");
        
        // 1. 基本策略使用
        System.out.println("=== 基本策略使用 ===");
        Strategy<String, String> upperCaseStrategy = input -> input.toUpperCase();
        Strategy<String, String> lowerCaseStrategy = input -> input.toLowerCase();
        Strategy<String, String> reverseStrategy = input -> new StringBuilder(input).reverse().toString();
        
        String text = "Hello World";
        
        StrategyContext<String, String> context = new StrategyContext<>(upperCaseStrategy);
        System.out.println("大写转换: " + context.execute(text));
        
        context.setStrategy(lowerCaseStrategy);
        System.out.println("小写转换: " + context.execute(text));
        
        context.setStrategy(reverseStrategy);
        System.out.println("反转字符串: " + context.execute(text));
        
        System.out.println();
        
        // 2. 策略注册表使用
        System.out.println("=== 策略注册表使用 ===");
        StrategyRegistry<String, String> registry = new StrategyRegistry<>();
        registry.register("UPPER", upperCaseStrategy);
        registry.register("LOWER", lowerCaseStrategy);
        registry.register("REVERSE", reverseStrategy);
        registry.setDefaultStrategy(upperCaseStrategy);
        
        System.out.println("注册的策略: " + registry.getAllNames());
        
        for (String strategyName : registry.getAllNames()) {
            Strategy<String, String> strategy = registry.get(strategyName);
            System.out.println(strategyName + ": " + strategy.execute(text));
        }
        
        System.out.println();
        
        // 3. 策略链使用
        System.out.println("=== 策略链使用 ===");
        StrategyChain<String> chain = new StrategyChain<>();
        chain.addStrategy(input -> input.trim())
             .addStrategy(input -> input.toLowerCase())
             .addStrategy(input -> input.replace(" ", "_"));
        
        String messyText = "  Hello World  ";
        System.out.println("原始文本: '" + messyText + "'");
        System.out.println("链式处理后: '" + chain.execute(messyText) + "'");
        
        System.out.println();
        
        // 4. 条件策略选择
        System.out.println("=== 条件策略选择 ===");
        ConditionalStrategySelector<String, String> selector = new ConditionalStrategySelector<>();
        selector.when(s -> s.length() > 10, upperCaseStrategy)
                .when(s -> s.length() > 5, lowerCaseStrategy)
                .otherwise(reverseStrategy);
        
        String[] testStrings = {"Hi", "Hello", "Hello World Strategy"};
        for (String testStr : testStrings) {
            System.out.println(String.format("'%s' (长度%d) -> '%s'", 
                testStr, testStr.length(), selector.execute(testStr)));
        }
        
        System.out.println();
    }
    
    /**
     * 演示所有实现的策略模式示例
     */
    public static void demonstrateAllExamples() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("策略模式完整演示 - 开始");
        System.out.println("=".repeat(60));
        
        // 基础概念演示
        demonstrateBasicConcepts();
        
        // 经典示例演示
        System.out.println("\n" + "=".repeat(60));
        System.out.println("经典策略模式示例");
        System.out.println("=".repeat(60));
        ClassicExamples.demonstrateCalculator();
        ClassicExamples.demonstratePayment();
        
        // 企业级示例演示
        System.out.println("\n" + "=".repeat(60));
        System.out.println("企业级策略模式示例");
        System.out.println("=".repeat(60));
        EnterpriseExamples.demonstrateMessagePush();
        EnterpriseExamples.demonstrateAuthentication();
        EnterpriseExamples.demonstrateCache();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("策略模式完整演示 - 结束");
        System.out.println("=".repeat(60));
    }
    
    /**
     * 打印策略模式总结
     */
    public static void printStrategySummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("策略模式总结");
        System.out.println("=".repeat(80));
        
        System.out.println("✅ 已实现的功能模块:");
        System.out.println("   1. 核心策略框架 (StrategyPattern.java)");
        System.out.println("      - 基础策略接口 Strategy<T,R>");
        System.out.println("      - 抽象策略基类 AbstractStrategy<T,R>");
        System.out.println("      - 策略上下文 StrategyContext<T,R>");
        System.out.println("      - 策略注册表 StrategyRegistry<T,R>");
        System.out.println("      - 策略链执行器 StrategyChain<T>");
        System.out.println("      - 条件策略选择器 ConditionalStrategySelector<T,R>");
        System.out.println();
        
        System.out.println("   2. 经典应用示例 (ClassicExamples.java)");
        System.out.println("      - 计算器策略: 加、减、乘、除运算");
        System.out.println("      - 支付策略: 支付宝、微信、银行卡支付");
        System.out.println("      - 自动策略选择: 根据金额自动选择支付方式");
        System.out.println();
        
        System.out.println("   3. 企业级应用示例 (EnterpriseExamples.java)");
        System.out.println("      - 消息推送策略: 邮件、短信、APP推送");
        System.out.println("      - 用户认证策略: 用户名密码、OAuth认证");
        System.out.println("      - 缓存策略: 内存缓存、Redis缓存");
        System.out.println("      - 优先级驱动的自动策略选择");
        System.out.println();
        
        System.out.println("🎯 策略模式的优势:");
        System.out.println("   - ✨ 算法族封装: 将算法封装成独立的策略类");
        System.out.println("   - 🔄 运行时切换: 可在运行时动态选择和切换策略");
        System.out.println("   - 📈 易于扩展: 新增策略不需要修改现有代码");
        System.out.println("   - 🧪 易于测试: 每个策略可以独立测试");
        System.out.println("   - 🎛️ 灵活配置: 支持条件选择和策略组合");
        System.out.println();
        
        System.out.println("💡 适用场景:");
        System.out.println("   - 需要在运行时选择算法的系统");
        System.out.println("   - 有多种方式实现同一功能的场景");
        System.out.println("   - 需要避免复杂条件语句的情况");
        System.out.println("   - 算法经常变化的业务逻辑");
        System.out.println();
        
        System.out.println("🚀 高级特性:");
        System.out.println("   - 函数式接口支持 (FunctionalInterface)");
        System.out.println("   - 泛型类型安全");
        System.out.println("   - 链式策略执行");
        System.out.println("   - 条件驱动的策略选择");
        System.out.println("   - 策略注册与工厂模式结合");
        System.out.println();
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * 主方法 - 完整演示入口
     */
    public static void main(String[] args) {
        // 演示所有策略模式示例
        demonstrateAllExamples();
        
        // 打印总结
        printStrategySummary();
        
        System.out.println("🎉 策略模式演示完成！");
    }
}