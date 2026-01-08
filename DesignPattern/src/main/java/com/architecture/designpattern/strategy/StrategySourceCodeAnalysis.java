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
    
    public void demonstrateAllSourceCodeAnalysis() {
        System.out.println("=== 策略模式源码深度分析 ===");
        
        // 1. Java Comparator策略源码分析
        analyzeComparator();
        
        // 2. Spring Bean验证策略源码分析
        analyzeSpringValidation();
        
        // 3. ThreadPoolExecutor拒绝策略源码分析
        analyzeRejectedExecutionHandler();
        
        // 4. 策略工厂模式源码分析
        analyzeStrategyFactory();
        
        // 5. 函数式策略模式源码分析
        analyzeFunctionalStrategy();
        
        // 6. Spring中策略模式的高级应用
        analyzeSpringAdvancedUsage();
        
        // 7. SpringMVC中的策略模式深度分析
        analyzeSpringMVCStrategy();
        
        // 8. Spring Boot自动配置中的策略模式
        analyzeSpringBootAutoConfiguration();
        
        // 9. Spring Security中的策略模式
        analyzeSpringSecurityStrategy();
        
        // 10. Spring Cloud中的策略模式
        analyzeSpringCloudStrategy();
        
        // 11. Spring Data中的策略模式
        analyzeSpringDataStrategy();
        
        // 12. Spring框架核心容器中的策略模式
        analyzeSpringCoreStrategy();
    }

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
    
    /**
     * 7. SpringMVC中的策略模式深度分析
     */
    public void analyzeSpringMVCStrategy() {
        System.out.println("\n=== SpringMVC策略模式深度分析 ===");
        
        System.out.println("1. HandlerMapping策略族：");
        System.out.println("源码位置：org.springframework.web.servlet.HandlerMapping");
        System.out.println("- RequestMappingHandlerMapping：注解驱动的URL映射");
        System.out.println("- SimpleUrlHandlerMapping：简单URL模式映射");
        System.out.println("- BeanNameUrlHandlerMapping：Bean名称URL映射");
        
        HandlerMappingDemo handlerDemo = new HandlerMappingDemo();
        handlerDemo.demonstrateHandlerMapping();
        
        System.out.println("\n2. HandlerAdapter策略族：");
        System.out.println("源码位置：org.springframework.web.servlet.HandlerAdapter");
        System.out.println("- RequestMappingHandlerAdapter：@RequestMapping处理");
        System.out.println("- HttpRequestHandlerAdapter：HttpRequestHandler处理");
        System.out.println("- SimpleControllerHandlerAdapter：Controller处理");
        
        HandlerAdapterDemo adapterDemo = new HandlerAdapterDemo();
        adapterDemo.demonstrateHandlerAdapter();
        
        System.out.println("\n3. ViewResolver策略族：");
        System.out.println("源码位置：org.springframework.web.servlet.ViewResolver");
        System.out.println("- InternalResourceViewResolver：JSP视图解析");
        System.out.println("- FreeMarkerViewResolver：FreeMarker模板解析");
        System.out.println("- ThymeleafViewResolver：Thymeleaf模板解析");
        
        System.out.println("\n核心源码分析：");
        System.out.println("DispatcherServlet.doDispatch()方法：");
        System.out.println("1. getHandler() - 策略选择请求处理器");
        System.out.println("2. getHandlerAdapter() - 策略选择适配器");
        System.out.println("3. handle() - 策略执行请求处理");
        System.out.println("4. resolveViewName() - 策略选择视图解析器");
    }
    
    /**
     * 8. Spring Boot自动配置中的策略模式
     */
    public void analyzeSpringBootAutoConfiguration() {
        System.out.println("\n=== Spring Boot自动配置策略模式 ===");
        
        System.out.println("1. ConditionalOnClass策略：");
        System.out.println("- 基于类路径存在性的条件策略");
        System.out.println("- 自动配置不同的技术栈");
        
        System.out.println("\n2. ConditionalOnProperty策略：");
        System.out.println("- 基于配置属性的条件策略");
        System.out.println("- 动态开启/关闭功能模块");
        
        ConditionalStrategyDemo conditionalDemo = new ConditionalStrategyDemo();
        conditionalDemo.demonstrateConditionalStrategy();
        
        System.out.println("\n3. ImportSelector策略：");
        System.out.println("源码位置：org.springframework.context.annotation.ImportSelector");
        System.out.println("- DeferredImportSelector：延迟导入策略");
        System.out.println("- AutoConfigurationImportSelector：自动配置导入");
        
        System.out.println("\n4. BeanPostProcessor策略：");
        System.out.println("- Bean初始化前后的处理策略");
        System.out.println("- AOP代理创建、依赖注入等");
        
        BeanPostProcessorDemo bppDemo = new BeanPostProcessorDemo();
        bppDemo.demonstrateBeanPostProcessor();
    }
    
    /**
     * 9. Spring Security中的策略模式
     */
    public void analyzeSpringSecurityStrategy() {
        System.out.println("\n=== Spring Security策略模式分析 ===");
        
        System.out.println("1. AuthenticationProvider策略：");
        System.out.println("源码位置：org.springframework.security.authentication.AuthenticationProvider");
        System.out.println("- DaoAuthenticationProvider：数据库认证");
        System.out.println("- JwtAuthenticationProvider：JWT Token认证");
        System.out.println("- LdapAuthenticationProvider：LDAP认证");
        
        AuthenticationDemo authDemo = new AuthenticationDemo();
        authDemo.demonstrateAuthentication();
        
        System.out.println("\n2. AccessDecisionManager策略：");
        System.out.println("源码位置：org.springframework.security.access.AccessDecisionManager");
        System.out.println("- AffirmativeBased：一票通过");
        System.out.println("- ConsensusBased：少数服从多数");
        System.out.println("- UnanimousBased：一票否决");
        
        System.out.println("\n3. PasswordEncoder策略：");
        System.out.println("源码位置：org.springframework.security.crypto.password.PasswordEncoder");
        System.out.println("- BCryptPasswordEncoder：BCrypt加密");
        System.out.println("- SCryptPasswordEncoder：SCrypt加密");
        System.out.println("- Pbkdf2PasswordEncoder：PBKDF2加密");
        
        PasswordEncodingDemo pwdDemo = new PasswordEncodingDemo();
        pwdDemo.demonstratePasswordEncoding();
    }
    
    /**
     * 10. Spring Cloud中的策略模式
     */
    public void analyzeSpringCloudStrategy() {
        System.out.println("\n=== Spring Cloud策略模式分析 ===");
        
        System.out.println("1. LoadBalancer策略：");
        System.out.println("源码位置：org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier");
        System.out.println("- RoundRobinLoadBalancer：轮询策略");
        System.out.println("- RandomLoadBalancer：随机策略");
        System.out.println("- WeightedResponseTimeRule：响应时间加权");
        
        LoadBalancerDemo lbDemo = new LoadBalancerDemo();
        lbDemo.demonstrateLoadBalancing();
        
        System.out.println("\n2. CircuitBreaker策略：");
        System.out.println("- Hystrix：Netflix的熔断器实现");
        System.out.println("- Resilience4j：轻量级熔断器");
        System.out.println("- Sentinel：阿里巴巴的流量控制");
        
        System.out.println("\n3. Gateway Filter策略：");
        System.out.println("源码位置：org.springframework.cloud.gateway.filter.GatewayFilter");
        System.out.println("- RateLimitGatewayFilterFactory：限流过滤");
        System.out.println("- RetryGatewayFilterFactory：重试过滤");
        System.out.println("- CircuitBreakerGatewayFilterFactory：熔断过滤");
        
        GatewayFilterDemo gwDemo = new GatewayFilterDemo();
        gwDemo.demonstrateGatewayFilters();
    }
    
    /**
     * 11. Spring Data中的策略模式
     */
    public void analyzeSpringDataStrategy() {
        System.out.println("\n=== Spring Data策略模式分析 ===");
        
        System.out.println("1. Repository策略：");
        System.out.println("源码位置：org.springframework.data.repository.Repository");
        System.out.println("- CrudRepository：基本CRUD操作");
        System.out.println("- PagingAndSortingRepository：分页排序");
        System.out.println("- JpaRepository：JPA特定操作");
        
        System.out.println("\n2. Query Strategy：");
        System.out.println("- MethodNameBasedQuery：方法名查询策略");
        System.out.println("- QueryByExampleExecutor：示例查询策略");
        System.out.println("- QuerydslPredicateExecutor：Querydsl查询策略");
        
        RepositoryDemo repoDemo = new RepositoryDemo();
        repoDemo.demonstrateRepositoryStrategy();
        
        System.out.println("\n3. AuditingHandler策略：");
        System.out.println("源码位置：org.springframework.data.auditing.AuditingHandler");
        System.out.println("- 创建时间、修改时间自动填充");
        System.out.println("- 创建人、修改人自动记录");
        System.out.println("- 支持自定义审计策略");
    }
    
    /**
     * 12. Spring框架核心容器中的策略模式
     */
    public void analyzeSpringCoreStrategy() {
        System.out.println("\n=== Spring核心容器策略模式分析 ===");
        
        System.out.println("1. BeanFactory策略：");
        System.out.println("源码位置：org.springframework.beans.factory.BeanFactory");
        System.out.println("- DefaultListableBeanFactory：默认实现");
        System.out.println("- XmlBeanFactory：XML配置工厂");
        System.out.println("- AnnotationConfigApplicationContext：注解配置工厂");
        
        System.out.println("\n2. ResourceLoader策略：");
        System.out.println("源码位置：org.springframework.core.io.ResourceLoader");
        System.out.println("- ClassPathResource：类路径资源加载");
        System.out.println("- FileSystemResource：文件系统资源加载");
        System.out.println("- UrlResource：URL资源加载");
        
        ResourceLoadingDemo resourceDemo = new ResourceLoadingDemo();
        resourceDemo.demonstrateResourceLoading();
        
        System.out.println("\n3. PropertyResolver策略：");
        System.out.println("源码位置：org.springframework.core.env.PropertyResolver");
        System.out.println("- SystemEnvironmentPropertySource：系统环境变量");
        System.out.println("- SystemPropertiesPropertySource：系统属性");
        System.out.println("- PropertiesPropertySource：Properties文件");
        
        System.out.println("\n4. ConversionService策略：");
        System.out.println("源码位置：org.springframework.core.convert.ConversionService");
        System.out.println("- DefaultConversionService：默认转换服务");
        System.out.println("- FormattingConversionService：格式化转换");
        System.out.println("- 支持自定义Converter注册");
        
        ConversionServiceDemo convDemo = new ConversionServiceDemo();
        convDemo.demonstrateConversion();
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

// ============== 新增Spring源码分析演示类 ==============

/**
 * HandlerMapping策略演示
 */
class HandlerMappingDemo {
    public void demonstrateHandlerMapping() {
        System.out.println("\nHandlerMapping策略选择演示：");
        
        MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/users");
        System.out.println("请求：" + request1.getRequestURI());
        System.out.println("选择策略：RequestMappingHandlerMapping");
        System.out.println("处理器：UserController.getUsers()");
        
        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/hello");
        System.out.println("\n请求：" + request2.getRequestURI());
        System.out.println("选择策略：SimpleUrlHandlerMapping");
        System.out.println("处理器：HelloController");
        
        System.out.println("\n核心源码分析：");
        System.out.println("DispatcherServlet.getHandler()遍历所有HandlerMapping");
        System.out.println("每个HandlerMapping.getHandler()返回匹配的处理器");
        System.out.println("策略模式实现了不同URL映射规则的解耦");
    }
}

/**
 * HandlerAdapter策略演示
 */
class HandlerAdapterDemo {
    public void demonstrateHandlerAdapter() {
        System.out.println("\nHandlerAdapter策略适配演示：");
        
        System.out.println("处理器类型：@RequestMapping注解方法");
        System.out.println("选择适配器：RequestMappingHandlerAdapter");
        System.out.println("适配功能：参数绑定、返回值处理、异常处理");
        
        System.out.println("\n处理器类型：HttpRequestHandler接口");
        System.out.println("选择适配器：HttpRequestHandlerAdapter");
        System.out.println("适配功能：直接调用handleRequest方法");
        
        System.out.println("\n处理器类型：Controller接口");
        System.out.println("选择适配器：SimpleControllerHandlerAdapter");
        System.out.println("适配功能：调用handleRequest返回ModelAndView");
        
        System.out.println("\n策略模式优势：");
        System.out.println("- 支持多种类型的处理器");
        System.out.println("- 新增处理器类型无需修改核心代码");
        System.out.println("- 每种适配器专注于特定的处理逻辑");
    }
}

/**
 * 条件策略演示
 */
class ConditionalStrategyDemo {
    public void demonstrateConditionalStrategy() {
        System.out.println("\nSpring Boot条件策略演示：");
        
        System.out.println("场景1：数据源自动配置");
        System.out.println("@ConditionalOnClass(DataSource.class)");
        System.out.println("- 类路径存在DataSource：配置数据源相关Bean");
        System.out.println("- 类路径不存在：跳过数据源配置");
        
        System.out.println("\n场景2：缓存策略选择");
        System.out.println("@ConditionalOnProperty(name=\"cache.type\", havingValue=\"redis\")");
        System.out.println("- 配置cache.type=redis：使用Redis缓存策略");
        System.out.println("- 其他配置：使用默认内存缓存策略");
        
        System.out.println("\n场景3：Web容器选择");
        System.out.println("@ConditionalOnClass(name=\"org.apache.catalina.startup.Tomcat\")");
        System.out.println("- Tomcat在类路径：配置Tomcat容器");
        System.out.println("- Jetty在类路径：配置Jetty容器");
        
        System.out.println("\n核心实现机制：");
        System.out.println("- Condition接口定义条件判断策略");
        System.out.println("- ConditionEvaluator负责评估条件");
        System.out.println("- ConfigurationClassPostProcessor处理条件注解");
    }
}

/**
 * BeanPostProcessor策略演示
 */
class BeanPostProcessorDemo {
    public void demonstrateBeanPostProcessor() {
        System.out.println("\nBeanPostProcessor策略演示：");
        
        System.out.println("1. AutowiredAnnotationBeanPostProcessor：");
        System.out.println("   - 处理@Autowired注解");
        System.out.println("   - 实现依赖注入策略");
        
        System.out.println("\n2. CommonAnnotationBeanPostProcessor：");
        System.out.println("   - 处理@Resource注解");
        System.out.println("   - 处理@PostConstruct和@PreDestroy");
        
        System.out.println("\n3. AsyncAnnotationBeanPostProcessor：");
        System.out.println("   - 处理@Async注解");
        System.out.println("   - 创建异步执行代理");
        
        System.out.println("\n4. ScheduledAnnotationBeanPostProcessor：");
        System.out.println("   - 处理@Scheduled注解");
        System.out.println("   - 注册定时任务");
        
        BeanPostProcessorExample example = new BeanPostProcessorExample();
        example.demonstrateProcessing();
    }
}

/**
 * 认证策略演示
 */
class AuthenticationDemo {
    public void demonstrateAuthentication() {
        System.out.println("\nSpring Security认证策略演示：");
        
        System.out.println("1. 数据库认证策略：");
        MockAuthenticationProvider daoProvider = new MockAuthenticationProvider("DAO");
        boolean result1 = daoProvider.authenticate("user", "password");
        System.out.println("   认证结果：" + (result1 ? "成功" : "失败"));
        
        System.out.println("\n2. LDAP认证策略：");
        MockAuthenticationProvider ldapProvider = new MockAuthenticationProvider("LDAP");
        boolean result2 = ldapProvider.authenticate("admin", "admin123");
        System.out.println("   认证结果：" + (result2 ? "成功" : "失败"));
        
        System.out.println("\n3. JWT Token认证策略：");
        MockAuthenticationProvider jwtProvider = new MockAuthenticationProvider("JWT");
        boolean result3 = jwtProvider.authenticate("jwt_token_12345", null);
        System.out.println("   认证结果：" + (result3 ? "成功" : "失败"));
        
        System.out.println("\n策略选择流程：");
        System.out.println("AuthenticationManager.authenticate()");
        System.out.println("-> 遍历AuthenticationProvider列表");
        System.out.println("-> 找到支持的Provider执行认证");
        System.out.println("-> 返回Authentication结果");
    }
}

/**
 * 密码编码策略演示
 */
class PasswordEncodingDemo {
    public void demonstratePasswordEncoding() {
        System.out.println("\n密码编码策略演示：");
        
        String rawPassword = "myPassword123";
        
        System.out.println("原始密码：" + rawPassword);
        
        // BCrypt策略
        System.out.println("\n1. BCrypt编码策略：");
        MockPasswordEncoder bcryptEncoder = new MockPasswordEncoder("BCrypt");
        String bcryptEncoded = bcryptEncoder.encode(rawPassword);
        System.out.println("   编码结果：" + bcryptEncoded);
        System.out.println("   验证结果：" + bcryptEncoder.matches(rawPassword, bcryptEncoded));
        
        // SCrypt策略
        System.out.println("\n2. SCrypt编码策略：");
        MockPasswordEncoder scryptEncoder = new MockPasswordEncoder("SCrypt");
        String scryptEncoded = scryptEncoder.encode(rawPassword);
        System.out.println("   编码结果：" + scryptEncoded);
        System.out.println("   验证结果：" + scryptEncoder.matches(rawPassword, scryptEncoded));
        
        System.out.println("\n策略选择建议：");
        System.out.println("- BCrypt：广泛使用，安全性高");
        System.out.println("- SCrypt：内存消耗大，抗ASIC攻击");
        System.out.println("- PBKDF2：标准算法，兼容性好");
    }
}

/**
 * 负载均衡策略演示
 */
class LoadBalancerDemo {
    public void demonstrateLoadBalancing() {
        System.out.println("\nSpring Cloud负载均衡策略演示：");
        
        List<String> servers = List.of("server1:8080", "server2:8080", "server3:8080");
        
        System.out.println("可用服务器：" + servers);
        
        // 轮询策略
        RoundRobinLoadBalancer roundRobin = new RoundRobinLoadBalancer(servers);
        System.out.println("\n1. 轮询策略：");
        for (int i = 0; i < 5; i++) {
            System.out.println("   请求" + (i+1) + " -> " + roundRobin.choose());
        }
        
        // 随机策略
        RandomLoadBalancer random = new RandomLoadBalancer(servers);
        System.out.println("\n2. 随机策略：");
        for (int i = 0; i < 5; i++) {
            System.out.println("   请求" + (i+1) + " -> " + random.choose());
        }
        
        // 加权策略
        WeightedLoadBalancer weighted = new WeightedLoadBalancer(servers);
        System.out.println("\n3. 加权策略：");
        for (int i = 0; i < 5; i++) {
            System.out.println("   请求" + (i+1) + " -> " + weighted.choose());
        }
        
        System.out.println("\n负载均衡策略选择原则：");
        System.out.println("- 轮询：简单均匀，适合同构服务器");
        System.out.println("- 随机：无状态，适合大规模集群");
        System.out.println("- 加权：考虑服务器性能差异");
    }
}

/**
 * Gateway过滤器策略演示
 */
class GatewayFilterDemo {
    public void demonstrateGatewayFilters() {
        System.out.println("\nSpring Cloud Gateway过滤器策略演示：");
        
        MockGatewayFilterChain filterChain = new MockGatewayFilterChain();
        
        // 添加不同的过滤器策略
        filterChain.addFilter(new RateLimitGatewayFilter());
        filterChain.addFilter(new AuthenticationGatewayFilter());
        filterChain.addFilter(new LoggingGatewayFilter());
        
        // 模拟请求处理
        MockServerHttpRequest request = new MockServerHttpRequest("/api/users");
        filterChain.filter(request);
        
        System.out.println("\nGateway过滤器策略分类：");
        System.out.println("1. Pre过滤器：请求处理前执行");
        System.out.println("   - 认证授权、参数校验、限流控制");
        System.out.println("2. Post过滤器：请求处理后执行");
        System.out.println("   - 响应修改、日志记录、监控统计");
        System.out.println("3. Route过滤器：路由级别过滤");
        System.out.println("   - 特定路由的定制化处理");
    }
}

/**
 * Repository策略演示
 */
class RepositoryDemo {
    public void demonstrateRepositoryStrategy() {
        System.out.println("\nSpring Data Repository策略演示：");
        
        System.out.println("1. 方法名查询策略：");
        System.out.println("   findByUsernameAndAge(String username, int age)");
        System.out.println("   -> SELECT * FROM user WHERE username = ? AND age = ?");
        
        System.out.println("\n2. @Query注解策略：");
        System.out.println("   @Query(\"SELECT u FROM User u WHERE u.email = ?1\")");
        System.out.println("   -> 使用JPQL或原生SQL查询");
        
        System.out.println("\n3. Example查询策略：");
        System.out.println("   Example<User> example = Example.of(user);");
        System.out.println("   -> 基于实体对象模板查询");
        
        System.out.println("\n4. Specification查询策略：");
        System.out.println("   Specification<User> spec = (root, query, cb) -> {...}");
        System.out.println("   -> 动态条件查询，类型安全");
        
        MockRepositoryExample repoExample = new MockRepositoryExample();
        repoExample.demonstrateQueryStrategies();
    }
}

/**
 * 资源加载策略演示
 */
class ResourceLoadingDemo {
    public void demonstrateResourceLoading() {
        System.out.println("\nSpring资源加载策略演示：");
        
        MockResourceLoader resourceLoader = new MockResourceLoader();
        
        System.out.println("1. 类路径资源加载：");
        String resource1 = resourceLoader.getResource("classpath:application.properties");
        System.out.println("   资源路径：" + resource1);
        
        System.out.println("\n2. 文件系统资源加载：");
        String resource2 = resourceLoader.getResource("file:/path/to/config.xml");
        System.out.println("   资源路径：" + resource2);
        
        System.out.println("\n3. URL资源加载：");
        String resource3 = resourceLoader.getResource("http://example.com/config.json");
        System.out.println("   资源路径：" + resource3);
        
        System.out.println("\n4. 相对路径资源加载：");
        String resource4 = resourceLoader.getResource("config/database.properties");
        System.out.println("   资源路径：" + resource4);
        
        System.out.println("\n资源加载策略特点：");
        System.out.println("- 统一的Resource接口抽象");
        System.out.println("- 支持多种资源位置协议");
        System.out.println("- 自动识别资源类型");
        System.out.println("- 延迟加载，节省内存");
    }
}

/**
 * 类型转换策略演示
 */
class ConversionServiceDemo {
    public void demonstrateConversion() {
        System.out.println("\nSpring类型转换策略演示：");
        
        MockConversionService conversionService = new MockConversionService();
        
        System.out.println("1. 基本类型转换：");
        String strToInt = conversionService.convert("123", String.class, Integer.class);
        System.out.println("   String -> Integer: " + strToInt);
        
        System.out.println("\n2. 集合类型转换：");
        String listToSet = conversionService.convert("[1,2,3,2,1]", List.class, Set.class);
        System.out.println("   List -> Set: " + listToSet);
        
        System.out.println("\n3. 自定义对象转换：");
        String userToDto = conversionService.convert("User{id=1, name='张三'}", User.class, UserDTO.class);
        System.out.println("   User -> UserDTO: " + userToDto);
        
        System.out.println("\n4. 日期时间转换：");
        String dateToString = conversionService.convert("2024-01-01T10:00:00", java.time.LocalDateTime.class, String.class);
        System.out.println("   LocalDateTime -> String: " + dateToString);
        
        System.out.println("\n转换策略架构：");
        System.out.println("- Converter<S,T>：一对一转换");
        System.out.println("- ConverterFactory：一对多转换");
        System.out.println("- GenericConverter：通用转换");
        System.out.println("- FormattingConversionService：格式化转换");
    }
}

// ============== 辅助模拟类 ==============

class MockHttpServletRequest {
    private String method;
    private String requestURI;
    
    public MockHttpServletRequest(String method, String requestURI) {
        this.method = method;
        this.requestURI = requestURI;
    }
    
    public String getMethod() { return method; }
    public String getRequestURI() { return requestURI; }
}

class BeanPostProcessorExample {
    public void demonstrateProcessing() {
        System.out.println("\nBean处理过程演示：");
        System.out.println("1. 实例化Bean");
        System.out.println("2. 填充属性值");
        System.out.println("3. BeanPostProcessor.postProcessBeforeInitialization()");
        System.out.println("4. 初始化方法调用");
        System.out.println("5. BeanPostProcessor.postProcessAfterInitialization()");
        System.out.println("6. Bean使用");
        System.out.println("7. 销毁Bean");
    }
}

class MockAuthenticationProvider {
    private String type;
    
    public MockAuthenticationProvider(String type) {
        this.type = type;
    }
    
    public boolean authenticate(String username, String password) {
        System.out.println("   使用" + type + "认证策略验证：" + username);
        // 模拟认证逻辑
        return username != null && !username.isEmpty();
    }
}

class MockPasswordEncoder {
    private String algorithm;
    
    public MockPasswordEncoder(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public String encode(String rawPassword) {
        return algorithm + "_" + rawPassword.hashCode();
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}

class RoundRobinLoadBalancer {
    private List<String> servers;
    private int index = 0;
    
    public RoundRobinLoadBalancer(List<String> servers) {
        this.servers = servers;
    }
    
    public String choose() {
        String server = servers.get(index % servers.size());
        index++;
        return server;
    }
}

class RandomLoadBalancer {
    private List<String> servers;
    private java.util.Random random = new java.util.Random();
    
    public RandomLoadBalancer(List<String> servers) {
        this.servers = servers;
    }
    
    public String choose() {
        return servers.get(random.nextInt(servers.size()));
    }
}

class WeightedLoadBalancer {
    private List<String> servers;
    private Map<String, Integer> weights;
    
    public WeightedLoadBalancer(List<String> servers) {
        this.servers = servers;
        this.weights = Map.of(
            servers.get(0), 3,
            servers.get(1), 2,
            servers.get(2), 1
        );
    }
    
    public String choose() {
        // 简化的加权轮询算法
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        int randomWeight = new java.util.Random().nextInt(totalWeight) + 1;
        
        int currentWeight = 0;
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomWeight <= currentWeight) {
                return entry.getKey();
            }
        }
        return servers.get(0);
    }
}

// Gateway过滤器相关模拟类
interface GatewayFilter {
    void filter(MockServerHttpRequest request);
    String getFilterName();
}

class RateLimitGatewayFilter implements GatewayFilter {
    @Override
    public void filter(MockServerHttpRequest request) {
        System.out.println("   执行限流过滤器：检查请求频率");
    }
    
    @Override
    public String getFilterName() { return "RateLimit"; }
}

class AuthenticationGatewayFilter implements GatewayFilter {
    @Override
    public void filter(MockServerHttpRequest request) {
        System.out.println("   执行认证过滤器：验证JWT Token");
    }
    
    @Override
    public String getFilterName() { return "Authentication"; }
}

class LoggingGatewayFilter implements GatewayFilter {
    @Override
    public void filter(MockServerHttpRequest request) {
        System.out.println("   执行日志过滤器：记录请求信息");
    }
    
    @Override
    public String getFilterName() { return "Logging"; }
}

class MockGatewayFilterChain {
    private List<GatewayFilter> filters = new ArrayList<>();
    
    public void addFilter(GatewayFilter filter) {
        filters.add(filter);
    }
    
    public void filter(MockServerHttpRequest request) {
        System.out.println("处理请求：" + request.getPath());
        for (GatewayFilter filter : filters) {
            filter.filter(request);
        }
        System.out.println("请求处理完成");
    }
}

class MockServerHttpRequest {
    private String path;
    
    public MockServerHttpRequest(String path) {
        this.path = path;
    }
    
    public String getPath() { return path; }
}

class MockRepositoryExample {
    public void demonstrateQueryStrategies() {
        System.out.println("\n查询策略执行示例：");
        System.out.println("1. findByUsername('admin') -> 方法名解析策略");
        System.out.println("2. findUsersByCustomQuery() -> @Query注解策略");
        System.out.println("3. findAll(example) -> Example查询策略");
        System.out.println("4. findAll(specification) -> Specification查询策略");
    }
}

class MockResourceLoader {
    public String getResource(String location) {
        if (location.startsWith("classpath:")) {
            return "ClassPathResource: " + location.substring(10);
        } else if (location.startsWith("file:")) {
            return "FileSystemResource: " + location.substring(5);
        } else if (location.startsWith("http://") || location.startsWith("https://")) {
            return "UrlResource: " + location;
        } else {
            return "DefaultResource: " + location;
        }
    }
}

class MockConversionService {
    public String convert(String source, Class<?> sourceType, Class<?> targetType) {
        return "转换: " + sourceType.getSimpleName() + "(" + source + ") -> " + targetType.getSimpleName();
    }
}

class User {
    private Long id;
    private String name;
    
    public Long getId() { return id; }
    public String getName() { return name; }
}

class UserDTO {
    private Long id;
    private String name;
    
    public Long getId() { return id; }
    public String getName() { return name; }
}