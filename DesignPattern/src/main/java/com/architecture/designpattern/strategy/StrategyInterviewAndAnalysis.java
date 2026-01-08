package com.architecture.designpattern.strategy;

import java.util.List;
import java.util.ArrayList;

/**
 * 策略模式面试题和源码分析
 * 包含常见面试问题、代码实现分析、最佳实践等
 */
public class StrategyInterviewAndAnalysis {

    /**
     * 面试题演示和分析
     */
    public static class InterviewQuestions {
        
        /**
         * 面试题1：策略模式基本概念
         */
        public static void question1_BasicConcepts() {
            System.out.println("========== 面试题1：策略模式基本概念 ==========");
            System.out.println("❓ 问题：请解释策略模式的定义、组成部分和适用场景？");
            System.out.println();
            
            System.out.println("✅ 答案：");
            System.out.println("定义：定义一系列算法，把它们一个个封装起来，并且使它们可相互替换。");
            System.out.println("      策略模式使得算法可独立于使用它的客户而变化。");
            System.out.println();
            
            System.out.println("组成部分：");
            System.out.println("1. Strategy（抽象策略）：定义所有具体策略的公共接口");
            System.out.println("2. ConcreteStrategy（具体策略）：实现抽象策略定义的接口");
            System.out.println("3. Context（上下文）：持有一个策略对象的引用");
            System.out.println();
            
            System.out.println("适用场景：");
            System.out.println("- 有多种方式实现同一功能时");
            System.out.println("- 需要在运行时选择算法时");
            System.out.println("- 避免复杂的条件语句时");
            System.out.println("- 算法经常变化的业务逻辑");
            System.out.println();
        }
        
        /**
         * 面试题2：策略模式vs状态模式
         */
        public static void question2_StrategyVsState() {
            System.out.println("========== 面试题2：策略模式vs状态模式 ==========");
            System.out.println("❓ 问题：策略模式和状态模式有什么区别？");
            System.out.println();
            
            System.out.println("✅ 答案：");
            System.out.println("相同点：");
            System.out.println("- 都有Context类持有状态/策略对象");
            System.out.println("- 都可以动态改变对象行为");
            System.out.println("- 都遵循开闭原则");
            System.out.println();
            
            System.out.println("不同点：");
            System.out.println("策略模式：");
            System.out.println("- 客户端知道所有策略，主动选择策略");
            System.out.println("- 策略之间相互独立，没有依赖关系");
            System.out.println("- 关注算法的替换");
            System.out.println();
            
            System.out.println("状态模式：");
            System.out.println("- 客户端不知道状态，由Context或状态自己决定转换");
            System.out.println("- 状态之间可能有转换关系");
            System.out.println("- 关注对象状态的变化");
            System.out.println();
            
            // 代码示例对比
            demonstrateStrategyVsState();
            System.out.println();
        }
        
        private static void demonstrateStrategyVsState() {
            System.out.println("💡 代码示例对比：");
            System.out.println();
            
            System.out.println("策略模式示例（客户端主动选择）：");
            System.out.println("Context context = new Context();");
            System.out.println("context.setStrategy(new ConcreteStrategyA()); // 客户端选择");
            System.out.println("context.execute();");
            System.out.println();
            
            System.out.println("状态模式示例（状态自动转换）：");
            System.out.println("Context context = new Context();");
            System.out.println("context.request(); // 状态可能自动转换到下一个状态");
            System.out.println("context.request(); // 再次调用可能触发不同状态的行为");
            System.out.println();
        }
        
        /**
         * 面试题3：策略模式的优缺点
         */
        public static void question3_ProsAndCons() {
            System.out.println("========== 面试题3：策略模式的优缺点 ==========");
            System.out.println("❓ 问题：策略模式有哪些优点和缺点？");
            System.out.println();
            
            System.out.println("✅ 答案：");
            System.out.println("优点：");
            System.out.println("1. 算法可以自由切换");
            System.out.println("2. 避免使用多重条件判断");
            System.out.println("3. 扩展性良好，符合开闭原则");
            System.out.println("4. 算法可以复用");
            System.out.println("5. 提高了算法的保密性和安全性");
            System.out.println();
            
            System.out.println("缺点：");
            System.out.println("1. 策略类数量增多");
            System.out.println("2. 客户端必须知道所有策略类，并自行决定使用哪一个");
            System.out.println("3. 策略类之间没有继承关系，无法复用公共代码");
            System.out.println();
            
            demonstrateProsAndCons();
            System.out.println();
        }
        
        private static void demonstrateProsAndCons() {
            System.out.println("💡 优缺点代码示例：");
            System.out.println();
            
            System.out.println("❌ 不使用策略模式（多重条件判断）：");
            System.out.println("public void processPayment(String type, double amount) {");
            System.out.println("    if (\"ALIPAY\".equals(type)) {");
            System.out.println("        // 支付宝支付逻辑");
            System.out.println("    } else if (\"WECHAT\".equals(type)) {");
            System.out.println("        // 微信支付逻辑");
            System.out.println("    } else if (\"BANK\".equals(type)) {");
            System.out.println("        // 银行卡支付逻辑");
            System.out.println("    }");
            System.out.println("    // 新增支付方式需要修改这个方法");
            System.out.println("}");
            System.out.println();
            
            System.out.println("✅ 使用策略模式：");
            System.out.println("public void processPayment(PaymentStrategy strategy, double amount) {");
            System.out.println("    strategy.pay(amount); // 算法可以自由切换");
            System.out.println("    // 新增支付方式只需要实现PaymentStrategy接口");
            System.out.println("}");
            System.out.println();
        }
        
        /**
         * 面试题4：策略模式的实际应用
         */
        public static void question4_RealWorldApplications() {
            System.out.println("========== 面试题4：策略模式的实际应用 ==========");
            System.out.println("❓ 问题：请举例说明策略模式在实际项目中的应用场景？");
            System.out.println();
            
            System.out.println("✅ 答案：");
            System.out.println("1. 支付系统：支付宝、微信、银行卡等不同支付方式");
            System.out.println("2. 物流系统：顺丰、申通、圆通等不同物流策略");
            System.out.println("3. 优惠系统：满减、折扣、积分等不同优惠策略");
            System.out.println("4. 缓存策略：内存缓存、Redis缓存、文件缓存等");
            System.out.println("5. 排序算法：快排、归并、堆排序等");
            System.out.println("6. 压缩算法：ZIP、RAR、7Z等不同压缩策略");
            System.out.println("7. 消息推送：短信、邮件、APP推送等");
            System.out.println("8. 认证方式：用户名密码、手机验证码、OAuth等");
            System.out.println();
            
            demonstrateRealWorldExample();
            System.out.println();
        }
        
        private static void demonstrateRealWorldExample() {
            System.out.println("💡 实际应用示例 - 电商优惠券系统：");
            System.out.println();
            
            // 优惠券策略接口
            interface CouponStrategy {
                double calculateDiscount(double originalPrice);
                String getDescription();
            }
            
            // 满减优惠券
            class FullReductionCoupon implements CouponStrategy {
                private double threshold;
                private double reduction;
                
                public FullReductionCoupon(double threshold, double reduction) {
                    this.threshold = threshold;
                    this.reduction = reduction;
                }
                
                @Override
                public double calculateDiscount(double originalPrice) {
                    return originalPrice >= threshold ? reduction : 0;
                }
                
                @Override
                public String getDescription() {
                    return String.format("满%.0f减%.0f", threshold, reduction);
                }
            }
            
            // 折扣优惠券
            class DiscountCoupon implements CouponStrategy {
                private double discount; // 0.8 表示8折
                
                public DiscountCoupon(double discount) {
                    this.discount = discount;
                }
                
                @Override
                public double calculateDiscount(double originalPrice) {
                    return originalPrice * (1 - discount);
                }
                
                @Override
                public String getDescription() {
                    return String.format("%.0f折", discount * 10);
                }
            }
            
            // 优惠券使用示例
            System.out.println("// 创建不同类型的优惠券");
            CouponStrategy fullReduction = new FullReductionCoupon(100, 20);
            CouponStrategy discount = new DiscountCoupon(0.8);
            
            double originalPrice = 150.0;
            System.out.println(String.format("原价: %.2f", originalPrice));
            System.out.println(String.format("%s: 优惠%.2f元", 
                fullReduction.getDescription(), 
                fullReduction.calculateDiscount(originalPrice)));
            System.out.println(String.format("%s: 优惠%.2f元", 
                discount.getDescription(), 
                discount.calculateDiscount(originalPrice)));
            System.out.println();
        }
        
        /**
         * 面试题5：策略模式的改进和优化
         */
        public static void question5_ImprovementsAndOptimizations() {
            System.out.println("========== 面试题5：策略模式的改进和优化 ==========");
            System.out.println("❓ 问题：如何优化策略模式，解决策略类数量过多的问题？");
            System.out.println();
            
            System.out.println("✅ 答案：");
            System.out.println("1. 使用工厂模式 + 策略模式");
            System.out.println("2. 使用枚举策略");
            System.out.println("3. 使用函数式接口（Java 8+）");
            System.out.println("4. 使用策略注册表");
            System.out.println("5. 结合反射和注解");
            System.out.println();
            
            demonstrateOptimizations();
            System.out.println();
        }
        
        private static void demonstrateOptimizations() {
            System.out.println("💡 优化示例：");
            System.out.println();
            
            System.out.println("1. 函数式接口优化：");
            System.out.println("// 传统方式需要创建多个策略类");
            System.out.println("interface Calculator { int calculate(int a, int b); }");
            System.out.println("class Add implements Calculator { ... }");
            System.out.println("class Subtract implements Calculator { ... }");
            System.out.println();
            
            System.out.println("// 函数式接口方式");
            System.out.println("Calculator add = (a, b) -> a + b;");
            System.out.println("Calculator subtract = (a, b) -> a - b;");
            System.out.println("Calculator multiply = (a, b) -> a * b;");
            System.out.println();
            
            System.out.println("2. 枚举策略优化：");
            System.out.println("enum Operation {");
            System.out.println("    PLUS((x, y) -> x + y),");
            System.out.println("    MINUS((x, y) -> x - y),");
            System.out.println("    TIMES((x, y) -> x * y);");
            System.out.println("    ");
            System.out.println("    private final BinaryOperator<Double> operation;");
            System.out.println("    public double apply(double x, double y) {");
            System.out.println("        return operation.apply(x, y);");
            System.out.println("    }");
            System.out.println("}");
            System.out.println();
        }
    }
    
    /**
     * 源码分析部分
     */
    public static class SourceCodeAnalysis {
        
        /**
         * 分析JDK中的策略模式应用
         */
        public static void analyzeJdkStrategyPattern() {
            System.out.println("========== JDK中的策略模式分析 ==========");
            System.out.println();
            
            System.out.println("1. Comparator接口：");
            System.out.println("   - java.util.Comparator是典型的策略接口");
            System.out.println("   - Collections.sort()方法接受Comparator策略");
            System.out.println("   - 不同的Comparator实现不同的比较策略");
            System.out.println();
            
            demonstrateComparatorStrategy();
            
            System.out.println("2. ThreadPoolExecutor拒绝策略：");
            System.out.println("   - RejectedExecutionHandler接口定义拒绝策略");
            System.out.println("   - AbortPolicy、DiscardPolicy等具体策略");
            System.out.println("   - 线程池满时采用不同的拒绝策略");
            System.out.println();
            
            System.out.println("3. javax.servlet.http.HttpServlet：");
            System.out.println("   - service()方法根据HTTP方法选择策略");
            System.out.println("   - doGet()、doPost()等方法是具体策略");
            System.out.println();
            
            System.out.println("4. Spring框架中的策略模式：");
            System.out.println("   - ApplicationContextInitializer策略");
            System.out.println("   - HandlerMapping策略");
            System.out.println("   - ViewResolver策略");
            System.out.println("   - TransactionManager策略");
            System.out.println();
        }
        
        private static void demonstrateComparatorStrategy() {
            System.out.println("💡 Comparator策略模式示例：");
            
            List<String> words = new ArrayList<>();
            words.add("apple");
            words.add("banana");
            words.add("cat");
            
            System.out.println("原始列表: " + words);
            
            // 长度排序策略
            words.sort((s1, s2) -> Integer.compare(s1.length(), s2.length()));
            System.out.println("按长度排序: " + words);
            
            // 字母排序策略
            words.sort(String::compareTo);
            System.out.println("按字母排序: " + words);
            
            // 反向排序策略
            words.sort((s1, s2) -> s2.compareTo(s1));
            System.out.println("反向排序: " + words);
            System.out.println();
        }
        
        /**
         * 分析开源框架中的策略模式
         */
        public static void analyzeFrameworkStrategyPattern() {
            System.out.println("========== 开源框架中的策略模式分析 ==========");
            System.out.println();
            
            System.out.println("1. Apache Commons中的策略模式：");
            System.out.println("   - FileUtils.copyFile()支持不同的复制策略");
            System.out.println("   - StringUtils.split()支持不同的分隔策略");
            System.out.println();
            
            System.out.println("2. MyBatis中的策略模式：");
            System.out.println("   - Executor接口：SimpleExecutor、ReuseExecutor、BatchExecutor");
            System.out.println("   - StatementHandler接口：不同SQL处理策略");
            System.out.println("   - ParameterHandler接口：参数处理策略");
            System.out.println();
            
            System.out.println("3. Netty中的策略模式：");
            System.out.println("   - ChannelHandler：不同的事件处理策略");
            System.out.println("   - EventLoop：不同的事件循环策略");
            System.out.println("   - ByteBuf分配策略：Pooled vs Unpooled");
            System.out.println();
            
            System.out.println("4. Redis中的策略模式：");
            System.out.println("   - 内存淘汰策略：LRU、LFU、Random等");
            System.out.println("   - 持久化策略：RDB、AOF");
            System.out.println("   - 集群策略：主从复制、哨兵、Cluster");
            System.out.println();
        }
        
        /**
         * 策略模式最佳实践
         */
        public static void bestPractices() {
            System.out.println("========== 策略模式最佳实践 ==========");
            System.out.println();
            
            System.out.println("1. 设计原则：");
            System.out.println("   ✅ 策略接口应该简单明确");
            System.out.println("   ✅ 策略实现应该无状态或线程安全");
            System.out.println("   ✅ 使用泛型提高类型安全");
            System.out.println("   ✅ 考虑使用函数式接口");
            System.out.println();
            
            System.out.println("2. 性能优化：");
            System.out.println("   ✅ 策略对象可以重用，避免重复创建");
            System.out.println("   ✅ 使用策略注册表缓存策略实例");
            System.out.println("   ✅ 考虑延迟加载策略");
            System.out.println("   ✅ 避免在策略中进行重量级操作");
            System.out.println();
            
            System.out.println("3. 扩展性考虑：");
            System.out.println("   ✅ 预留策略扩展点");
            System.out.println("   ✅ 支持策略组合和链式调用");
            System.out.println("   ✅ 提供默认策略");
            System.out.println("   ✅ 考虑策略的配置化");
            System.out.println();
            
            System.out.println("4. 测试建议：");
            System.out.println("   ✅ 每个策略独立测试");
            System.out.println("   ✅ 测试策略切换逻辑");
            System.out.println("   ✅ 测试异常情况处理");
            System.out.println("   ✅ 性能测试不同策略的执行效率");
            System.out.println();
        }
    }
    
    /**
     * 演示所有面试题
     */
    public static void demonstrateInterviewQuestions() {
        System.out.println("🎯 策略模式面试题精讲");
        System.out.println("=".repeat(80));
        
        InterviewQuestions.question1_BasicConcepts();
        InterviewQuestions.question2_StrategyVsState();
        InterviewQuestions.question3_ProsAndCons();
        InterviewQuestions.question4_RealWorldApplications();
        InterviewQuestions.question5_ImprovementsAndOptimizations();
        
        System.out.println("=".repeat(80));
        System.out.println("面试题演示完成！");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 演示源码分析
     */
    public static void demonstrateSourceCodeAnalysis() {
        System.out.println("\n🔍 策略模式源码分析");
        System.out.println("=".repeat(80));
        
        SourceCodeAnalysis.analyzeJdkStrategyPattern();
        SourceCodeAnalysis.analyzeFrameworkStrategyPattern();
        SourceCodeAnalysis.bestPractices();
        
        System.out.println("=".repeat(80));
        System.out.println("源码分析完成！");
        System.out.println("=".repeat(80));
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        demonstrateInterviewQuestions();
        demonstrateSourceCodeAnalysis();
    }
}