package com.architecture.designpattern.strategy;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategySourceCodeAnalysis {

    /**
     * ====================
     * 策略模式源码分析
     * ====================
     */

    /**
     * 1. Java Comparator策略源码分析
     * 
     * Comparator是策略模式的经典实现：
     * - 策略接口：Comparator<T>
     * - 具体策略：各种比较实现
     * - 上下文：Collections.sort()、Arrays.sort()等
     * 
     * 源码路径：java.util.Comparator
     * 核心方法：int compare(T o1, T o2)
     */
    public void analyzeComparator() {
        System.out.println("=== Java Comparator策略模式分析 ===");
        
        List<String> names = Arrays.asList("张三", "李四", "王五", "赵六");
        
        // 策略1：按长度排序
        System.out.println("1. 按长度排序策略：");
        Comparator<String> lengthComparator = (s1, s2) -> Integer.compare(s1.length(), s2.length());
        names.sort(lengthComparator);
        System.out.println("排序结果：" + names);
        
        // 策略2：按字典序排序
        System.out.println("2. 按字典序排序策略：");
        names.sort(String::compareTo);
        System.out.println("排序结果：" + names);
        
        // 策略3：自定义排序策略
        System.out.println("3. 自定义排序策略：");
        names.sort(Comparator.comparing(s -> s.charAt(0)));
        System.out.println("排序结果：" + names);
        
        System.out.println("\n核心原理：");
        System.out.println("- Collections.sort()内部调用Arrays.sort()");
        System.out.println("- Arrays.sort()使用TimSort算法，支持Comparator策略");
        System.out.println("- 通过策略模式实现了算法与数据结构的解耦");
    }

    /**
     * 2. Spring Bean验证策略源码分析
     * 
     * Spring Validator是策略模式在框架中的应用：
     * - 策略接口：org.springframework.validation.Validator
     * - 具体策略：各种验证器实现
     * - 上下文：ValidationUtils、DataBinder等
     * 
     * 核心方法：
     * - boolean supports(Class<?> clazz)
     * - void validate(Object target, Errors errors)
     */
    public void analyzeSpringValidation() {
        System.out.println("\n=== Spring Validator策略模式分析 ===");
        
        // 模拟Spring验证策略
        ValidationContext context = new ValidationContext();
        
        // 策略1：邮箱验证
        EmailValidator emailValidator = new EmailValidator();
        context.addValidator(emailValidator);
        
        // 策略2：手机号验证
        PhoneValidator phoneValidator = new PhoneValidator();
        context.addValidator(phoneValidator);
        
        // 策略3：身份证验证
        IdCardValidator idCardValidator = new IdCardValidator();
        context.addValidator(idCardValidator);
        
        // 验证用户数据
        User user = new User();
        user.setEmail("invalid-email");
        user.setPhone("12345");
        user.setIdCard("123456");
        
        ValidationResult result = context.validate(user);
        System.out.println("验证结果：" + result);
        
        System.out.println("\nSpring源码核心逻辑：");
        System.out.println("1. @Valid注解触发验证");
        System.out.println("2. DataBinder调用Validator.validate()");
        System.out.println("3. 支持多个Validator组合验证");
        System.out.println("4. 错误信息统一收集到Errors对象");
    }

    /**
     * 3. ThreadPoolExecutor拒绝策略源码分析
     * 
     * 线程池的拒绝策略是策略模式的经典应用：
     * - 策略接口：RejectedExecutionHandler
     * - 具体策略：AbortPolicy、CallerRunsPolicy、DiscardPolicy、DiscardOldestPolicy
     * - 上下文：ThreadPoolExecutor
     * 
     * 源码路径：java.util.concurrent.ThreadPoolExecutor
     * 核心方法：void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
     */
    public void analyzeRejectedExecutionHandler() {
        System.out.println("\n=== ThreadPoolExecutor拒绝策略分析 ===");
        
        // 演示不同的拒绝策略
        demonstrateRejectionStrategies();
        
        System.out.println("\n源码分析：");
        System.out.println("1. 当线程池队列满时，触发拒绝策略");
        System.out.println("2. ThreadPoolExecutor.execute()调用reject()方法");
        System.out.println("3. reject()方法调用具体的RejectedExecutionHandler");
        System.out.println("4. 四种内置策略处理不同场景需求");
        
        // 自定义拒绝策略
        System.out.println("\n自定义拒绝策略示例：");
        CustomRejectedExecutionHandler customHandler = new CustomRejectedExecutionHandler();
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                1, 1, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                customHandler
        );
        
        // 提交任务触发拒绝策略
        for (int i = 0; i < 5; i++) {
            try {
                customExecutor.execute(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException e) {}
                });
            } catch (Exception e) {
                System.out.println("任务" + i + "被拒绝：" + e.getMessage());
            }
        }
        
        customExecutor.shutdown();
    }

    /**
     * 4. 策略工厂模式源码分析
     * 
     * 策略模式常与工厂模式结合使用：
     * - 消除客户端的if-else判断
     * - 实现策略的自动选择和创建
     * - 提供统一的策略获取接口
     */
    public void analyzeStrategyFactory() {
        System.out.println("\n=== 策略工厂模式分析 ===");
        
        // 传统方式：大量if-else
        System.out.println("1. 传统方式的问题：");
        demonstrateTraditionalWay();
        
        // 策略工厂方式
        System.out.println("\n2. 策略工厂的解决方案：");
        PaymentStrategyFactory factory = new PaymentStrategyFactory();
        
        String[] paymentTypes = {"ALIPAY", "WECHAT", "CREDIT_CARD"};
        for (String type : paymentTypes) {
            PaymentProcessor processor = factory.getProcessor(type);
            processor.process(1000.0);
        }
        
        System.out.println("\n策略工厂的优势：");
        System.out.println("- 消除if-else判断逻辑");
        System.out.println("- 支持策略的动态注册和发现");
        System.out.println("- 便于单元测试和扩展");
        System.out.println("- 符合开闭原则");
    }

    /**
     * 5. 函数式策略模式源码分析
     * 
     * Java 8引入Lambda表达式后，策略模式可以更简洁地实现：
     * - 策略接口可以是函数式接口
     * - 策略实现可以用Lambda表达式
     * - 方法引用可以作为策略
     */
    public void analyzeFunctionalStrategy() {
        System.out.println("\n=== 函数式策略模式分析 ===");
        
        // 传统策略模式
        System.out.println("1. 传统策略模式：");
        CalculatorContext calculator = new CalculatorContext();
        calculator.setStrategy(new AddStrategy());
        System.out.println("5 + 3 = " + calculator.execute(5, 3));
        
        calculator.setStrategy(new MultiplyStrategy());
        System.out.println("5 * 3 = " + calculator.execute(5, 3));
        
        // 函数式策略模式
        System.out.println("\n2. 函数式策略模式：");
        FunctionalCalculator funcCalculator = new FunctionalCalculator();
        
        // Lambda表达式作为策略
        funcCalculator.setOperation((a, b) -> a + b);
        System.out.println("5 + 3 = " + funcCalculator.calculate(5, 3));
        
        funcCalculator.setOperation((a, b) -> a * b);
        System.out.println("5 * 3 = " + funcCalculator.calculate(5, 3));
        
        // 方法引用作为策略
        funcCalculator.setOperation(Integer::sum);
        System.out.println("5 + 3 = " + funcCalculator.calculate(5, 3));
        
        funcCalculator.setOperation(Math::max);
        System.out.println("max(5, 3) = " + funcCalculator.calculate(5, 3));
        
        // 策略集合
        System.out.println("\n3. 策略集合应用：");
        Map<String, Function<Integer, Integer>> strategies = Map.of(
                "DOUBLE", x -> x * 2,
                "SQUARE", x -> x * x,
                "NEGATE", x -> -x
        );
        
        int value = 5;
        for (Map.Entry<String, Function<Integer, Integer>> entry : strategies.entrySet()) {
            int result = entry.getValue().apply(value);
            System.out.println(entry.getKey() + "(" + value + ") = " + result);
        }
        
        System.out.println("\n函数式策略的优势：");
        System.out.println("- 代码更简洁，减少样板代码");
        System.out.println("- 支持内联策略定义");
        System.out.println("- 便于策略组合和链式调用");
        System.out.println("- 更好的类型安全性");
    }
    
    /**
     * 6. Spring中策略模式的高级应用
     */
    public void analyzeSpringAdvancedUsage() {
        System.out.println("\n=== Spring策略模式高级应用 ===");
        
        System.out.println("1. @Conditional注解策略：");
        System.out.println("- 根据条件动态选择Bean");
        System.out.println("- 实现环境相关的策略切换");
        
        System.out.println("\n2. @Profile注解策略：");
        System.out.println("- 不同环境使用不同策略实现");
        System.out.println("- 开发、测试、生产环境策略隔离");
        
        System.out.println("\n3. ApplicationListener策略：");
        System.out.println("- 事件驱动的策略模式");
        System.out.println("- 松耦合的组件通信");
        
        System.out.println("\n4. HandlerMapping策略：");
        System.out.println("- SpringMVC中的请求处理策略");
        System.out.println("- 支持多种URL映射策略");
    }
    
    // ============== 辅助方法和类 ==============
    
    private void demonstrateRejectionStrategies() {
        System.out.println("1. AbortPolicy（默认策略）：抛出异常");
        System.out.println("2. CallerRunsPolicy：调用者线程执行");
        System.out.println("3. DiscardPolicy：静默丢弃");
        System.out.println("4. DiscardOldestPolicy：丢弃最老任务");
    }
    
    private void demonstrateTraditionalWay() {
        String paymentType = "ALIPAY";
        double amount = 1000.0;
        
        // 大量if-else判断
        if ("ALIPAY".equals(paymentType)) {
            System.out.println("处理支付宝支付：" + amount);
        } else if ("WECHAT".equals(paymentType)) {
            System.out.println("处理微信支付：" + amount);
        } else if ("CREDIT_CARD".equals(paymentType)) {
            System.out.println("处理信用卡支付：" + amount);
        } else {
            throw new IllegalArgumentException("不支持的支付类型");
        }
        
        System.out.println("问题：代码冗长、难扩展、违反开闭原则");
    }
}

// ============== 辅助类定义 ==============

// 验证策略相关
interface Validator {
    boolean supports(Class<?> clazz);
    ValidationResult validate(Object target);
}

class EmailValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }
    
    @Override
    public ValidationResult validate(Object target) {
        User user = (User) target;
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            return new ValidationResult(false, "邮箱格式错误");
        }
        return new ValidationResult(true, "邮箱验证通过");
    }
}

class PhoneValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }
    
    @Override
    public ValidationResult validate(Object target) {
        User user = (User) target;
        if (user.getPhone() == null || user.getPhone().length() != 11) {
            return new ValidationResult(false, "手机号格式错误");
        }
        return new ValidationResult(true, "手机号验证通过");
    }
}

class IdCardValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }
    
    @Override
    public ValidationResult validate(Object target) {
        User user = (User) target;
        if (user.getIdCard() == null || user.getIdCard().length() != 18) {
            return new ValidationResult(false, "身份证格式错误");
        }
        return new ValidationResult(true, "身份证验证通过");
    }
}

class ValidationContext {
    private List<Validator> validators = new ArrayList<>();
    
    public void addValidator(Validator validator) {
        validators.add(validator);
    }
    
    public ValidationResult validate(Object target) {
        StringBuilder errors = new StringBuilder();
        boolean hasError = false;
        
        for (Validator validator : validators) {
            if (validator.supports(target.getClass())) {
                ValidationResult result = validator.validate(target);
                if (!result.isValid()) {
                    hasError = true;
                    if (errors.length() > 0) errors.append("; ");
                    errors.append(result.getErrorMessage());
                }
            }
        }
        
        return new ValidationResult(!hasError, hasError ? errors.toString() : "验证通过");
    }
}

class ValidationResult {
    private boolean valid;
    private String errorMessage;
    
    public ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }
    
    public boolean isValid() { return valid; }
    public String getErrorMessage() { return errorMessage; }
    
    @Override
    public String toString() {
        return "ValidationResult{valid=" + valid + ", errorMessage='" + errorMessage + "'}";
    }
}

class User {
    private String email;
    private String phone;
    private String idCard;
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
}

// 自定义拒绝策略
class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        System.out.println("自定义拒绝策略：任务被拒绝，当前队列大小：" + executor.getQueue().size());
        // 可以实现记录日志、降级处理等逻辑
    }
}

// 支付处理器工厂
interface PaymentProcessor {
    void process(double amount);
}

class AlipayProcessor implements PaymentProcessor {
    @Override
    public void process(double amount) {
        System.out.println("支付宝处理：" + amount + " 元");
    }
}

class WechatProcessor implements PaymentProcessor {
    @Override
    public void process(double amount) {
        System.out.println("微信处理：" + amount + " 元");
    }
}

class CreditCardProcessor implements PaymentProcessor {
    @Override
    public void process(double amount) {
        System.out.println("信用卡处理：" + amount + " 元");
    }
}

class PaymentStrategyFactory {
    private final Map<String, PaymentProcessor> processors;
    
    public PaymentStrategyFactory() {
        processors = Map.of(
                "ALIPAY", new AlipayProcessor(),
                "WECHAT", new WechatProcessor(),
                "CREDIT_CARD", new CreditCardProcessor()
        );
    }
    
    public PaymentProcessor getProcessor(String type) {
        PaymentProcessor processor = processors.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("不支持的支付类型：" + type);
        }
        return processor;
    }
}

// 传统策略模式
interface CalculationStrategy {
    int execute(int a, int b);
}

class AddStrategy implements CalculationStrategy {
    @Override
    public int execute(int a, int b) {
        return a + b;
    }
}

class MultiplyStrategy implements CalculationStrategy {
    @Override
    public int execute(int a, int b) {
        return a * b;
    }
}

class CalculatorContext {
    private CalculationStrategy strategy;
    
    public void setStrategy(CalculationStrategy strategy) {
        this.strategy = strategy;
    }
    
    public int execute(int a, int b) {
        return strategy.execute(a, b);
    }
}

// 函数式策略模式
class FunctionalCalculator {
    private Function<Integer, Function<Integer, Integer>> operation;
    
    public void setOperation(Function<Integer, Function<Integer, Integer>> operation) {
        this.operation = operation;
    }
    
    public void setOperation(java.util.function.BinaryOperator<Integer> operation) {
        this.operation = a -> b -> operation.apply(a, b);
    }
    
    public int calculate(int a, int b) {
        return operation.apply(a).apply(b);
    }
}