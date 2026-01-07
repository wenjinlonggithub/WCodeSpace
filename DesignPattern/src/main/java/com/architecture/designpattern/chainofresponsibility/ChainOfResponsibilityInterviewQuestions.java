package com.architecture.designpattern.chainofresponsibility;

import org.springframework.stereotype.Component;

@Component
public class ChainOfResponsibilityInterviewQuestions {

    /**
     * ====================
     * 责任链模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是责任链模式？它解决了什么问题？
     * 
     * A: 责任链模式定义：
     * 避免请求发送者与接收者耦合，让多个对象都有可能接收请求，
     * 将这些对象连接成一条链，沿着链传递请求，直到有对象处理它为止。
     * 
     * 解决的问题：
     * 1. 发送者与接收者解耦
     *    - 发送者无需知道具体由谁处理
     *    - 接收者无需知道链的结构
     * 
     * 2. 动态组合处理器
     *    - 运行时可以改变链的组成
     *    - 可以添加或删除处理器
     * 
     * 3. 增强系统灵活性
     *    - 新增处理器无需修改现有代码
     *    - 处理器可以重复使用
     * 
     * 应用场景：
     * - HTTP请求过滤器链
     * - 异常处理机制
     * - 审批流程系统
     * - 日志处理系统
     * - 权限验证链
     */
    public void whatIsChainOfResponsibility() {
        System.out.println("责任链模式：请求沿链传递，直到有对象处理 - 解耦发送者与接收者");
    }

    /**
     * Q2: 责任链模式的结构组成有哪些？
     * 
     * A: 核心组成：
     * 
     * 1. Handler（处理器接口）
     *    - 定义处理请求的接口
     *    - 维护下一个处理器的引用
     *    
     * 2. ConcreteHandler（具体处理器）
     *    - 实现处理请求的具体逻辑
     *    - 决定是否处理请求或传递给下一个处理器
     * 
     * 3. Request（请求对象）
     *    - 封装请求数据
     *    - 包含处理所需的信息
     * 
     * 4. Client（客户端）
     *    - 创建责任链
     *    - 发送请求到链的第一个处理器
     * 
     * UML类图关系：
     * ```
     * Client --> Handler
     * Handler o--> Handler (聚合关系，下一个处理器)
     * ConcreteHandlerA --|> Handler
     * ConcreteHandlerB --|> Handler
     * ConcreteHandlerC --|> Handler
     * ```
     * 
     * 代码结构：
     * ```java
     * abstract class Handler {
     *     protected Handler nextHandler;
     *     public void setNextHandler(Handler next);
     *     public abstract void handleRequest(Request request);
     * }
     * ```
     */
    public void chainOfResponsibilityStructure() {
        System.out.println("结构组成：Handler + ConcreteHandler + Request + Client");
    }

    /**
     * Q3: 责任链模式有哪几种实现方式？
     * 
     * A: 实现方式分类：
     * 
     * 1. 纯粹责任链（Pure Chain）
     *    - 一个请求只能被一个处理器处理
     *    - 处理后不再传递
     * ```java
     * public void handleRequest(Request request) {
     *     if (canHandle(request)) {
     *         doHandle(request);
     *         return; // 处理后直接返回
     *     }
     *     if (nextHandler != null) {
     *         nextHandler.handleRequest(request);
     *     }
     * }
     * ```
     * 
     * 2. 非纯粹责任链（Impure Chain）
     *    - 处理器可以部分处理请求
     *    - 处理后仍然传递给下一个处理器
     * ```java
     * public void handleRequest(Request request) {
     *     if (canHandle(request)) {
     *         doHandle(request);
     *     }
     *     if (nextHandler != null) {
     *         nextHandler.handleRequest(request); // 继续传递
     *     }
     * }
     * ```
     * 
     * 3. 基于数组/List的实现
     * ```java
     * public class ChainManager {
     *     private List<Handler> handlers = new ArrayList<>();
     *     
     *     public void handle(Request request) {
     *         for (Handler handler : handlers) {
     *             if (handler.canHandle(request)) {
     *                 handler.handle(request);
     *                 break; // 或继续，取决于需求
     *             }
     *         }
     *     }
     * }
     * ```
     * 
     * 4. 基于Stream的函数式实现
     * ```java
     * handlers.stream()
     *         .filter(handler -> handler.canHandle(request))
     *         .findFirst()
     *         .ifPresent(handler -> handler.handle(request));
     * ```
     */
    public void implementationTypes() {
        System.out.println("实现方式：纯粹/非纯粹责任链、基于集合、函数式实现");
    }

    /**
     * Q4: Servlet Filter是如何实现责任链模式的？
     * 
     * A: Servlet Filter实现分析：
     * 
     * 1. 核心组件
     *    - Filter：处理器接口
     *    - FilterChain：责任链管理器
     *    - FilterConfig：过滤器配置
     * 
     * 2. 关键实现
     * ```java
     * public interface Filter {
     *     void doFilter(ServletRequest request, ServletResponse response,
     *                  FilterChain chain) throws IOException, ServletException;
     * }
     * 
     * public interface FilterChain {
     *     void doFilter(ServletRequest request, ServletResponse response)
     *         throws IOException, ServletException;
     * }
     * ```
     * 
     * 3. FilterChain实现原理
     * ```java
     * public class ApplicationFilterChain implements FilterChain {
     *     private ApplicationFilterConfig[] filters;
     *     private int pos = 0; // 当前位置
     *     private int n = 0;   // 总数量
     *     
     *     public void doFilter(ServletRequest request, ServletResponse response) {
     *         if (pos < n) {
     *             ApplicationFilterConfig filterConfig = filters[pos++];
     *             Filter filter = filterConfig.getFilter();
     *             filter.doFilter(request, response, this);
     *         } else {
     *             // 执行实际的Servlet
     *             servlet.service(request, response);
     *         }
     *     }
     * }
     * ```
     * 
     * 4. 特点
     *    - 支持前置和后置处理
     *    - 通过数组管理过滤器链
     *    - 使用索引控制执行顺序
     *    - 可以中断或继续传递
     * 
     * 5. 使用示例
     * ```java
     * @WebFilter("/*")
     * public class LoggingFilter implements Filter {
     *     public void doFilter(ServletRequest request, ServletResponse response,
     *                         FilterChain chain) {
     *         System.out.println("请求前置处理");
     *         chain.doFilter(request, response);
     *         System.out.println("请求后置处理");
     *     }
     * }
     * ```
     */
    public void servletFilterImplementation() {
        System.out.println("Servlet Filter：数组管理 + 索引控制 + 前后置处理");
    }

    /**
     * Q5: Spring Security的Filter Chain是如何工作的？
     * 
     * A: Spring Security Filter Chain分析：
     * 
     * 1. 核心组件
     *    - SecurityFilterChain：安全过滤器链配置
     *    - FilterChainProxy：过滤器链代理
     *    - VirtualFilterChain：虚拟过滤器链
     * 
     * 2. FilterChainProxy实现
     * ```java
     * public class FilterChainProxy extends GenericFilterBean {
     *     private List<SecurityFilterChain> filterChains;
     *     
     *     public void doFilter(ServletRequest request, ServletResponse response,
     *                         FilterChain chain) {
     *         List<Filter> filters = getFilters((HttpServletRequest) request);
     *         VirtualFilterChain virtualFilterChain = 
     *             new VirtualFilterChain(chain, filters);
     *         virtualFilterChain.doFilter(request, response);
     *     }
     * }
     * ```
     * 
     * 3. VirtualFilterChain实现
     * ```java
     * private static class VirtualFilterChain implements FilterChain {
     *     private FilterChain originalChain;
     *     private List<Filter> additionalFilters;
     *     private int currentPosition = 0;
     *     
     *     public void doFilter(ServletRequest request, ServletResponse response) {
     *         if (currentPosition == additionalFilters.size()) {
     *             originalChain.doFilter(request, response);
     *         } else {
     *             Filter nextFilter = additionalFilters.get(currentPosition++);
     *             nextFilter.doFilter(request, response, this);
     *         }
     *     }
     * }
     * ```
     * 
     * 4. 常见Security Filter顺序
     *    - SecurityContextPersistenceFilter
     *    - LogoutFilter
     *    - UsernamePasswordAuthenticationFilter
     *    - BasicAuthenticationFilter
     *    - ExceptionTranslationFilter
     *    - FilterSecurityInterceptor
     * 
     * 5. 配置示例
     * ```java
     * @EnableWebSecurity
     * public class SecurityConfig {
     *     @Bean
     *     public SecurityFilterChain filterChain(HttpSecurity http) {
     *         return http
     *             .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
     *             .formLogin(Customizer.withDefaults())
     *             .build();
     *     }
     * }
     * ```
     */
    public void springSecurityFilterChain() {
        System.out.println("Spring Security：FilterChainProxy + VirtualFilterChain + 多层过滤");
    }

    /**
     * Q6: 责任链模式的优缺点是什么？
     * 
     * A: 优点：
     * 
     * 1. 降低耦合度
     *    - 发送者不需要知道接收者
     *    - 接收者不需要知道其他接收者
     * 
     * 2. 增强灵活性
     *    - 可以动态添加/删除处理器
     *    - 可以改变处理器顺序
     * 
     * 3. 增强扩展性
     *    - 新增处理器无需修改现有代码
     *    - 符合开闭原则
     * 
     * 4. 简化对象
     *    - 对象只需专注于自己的处理逻辑
     *    - 无需关心链的结构
     * 
     * 5. 分散职责
     *    - 每个处理器职责单一
     *    - 符合单一职责原则
     * 
     * 缺点：
     * 
     * 1. 性能问题
     *    - 从链头遍历到链尾，影响性能
     *    - 长链可能导致性能下降
     * 
     * 2. 调试困难
     *    - 请求处理流程不明确
     *    - 难以观察运行时链的结构
     * 
     * 3. 不保证被处理
     *    - 可能出现请求无法被处理的情况
     *    - 需要提供默认处理机制
     * 
     * 4. 递归调用风险
     *    - 链过长可能导致栈溢出
     *    - 需要控制链的长度
     * 
     * 5. 建立链的负担
     *    - 需要仔细配置链的结构
     *    - 链的建立可能比较复杂
     */
    public void advantagesAndDisadvantages() {
        System.out.println("优点：降低耦合、增强灵活性；缺点：性能问题、调试困难");
    }

    /**
     * Q7: 如何优化责任链模式的性能？
     * 
     * A: 性能优化策略：
     * 
     * 1. 处理器排序优化
     * ```java
     * // 将最常用的处理器放在链的前面
     * public class OptimizedChainBuilder {
     *     public Chain buildChain() {
     *         return Chain.builder()
     *             .addHandler(new FastHandler())     // 快速处理器在前
     *             .addHandler(new MediumHandler())   // 中等速度处理器
     *             .addHandler(new SlowHandler())     // 慢速处理器在后
     *             .build();
     *     }
     * }
     * ```
     * 
     * 2. 提前终止策略
     * ```java
     * public class EarlyTerminationHandler extends BaseHandler {
     *     @Override
     *     public void handle(Request request) {
     *         if (canHandleQuickly(request)) {
     *             handleQuickly(request);
     *             return; // 提前终止，不继续传递
     *         }
     *         super.handle(request);
     *     }
     * }
     * ```
     * 
     * 3. 处理器缓存
     * ```java
     * public class CachedChain {
     *     private final Map<String, Handler> handlerCache = 
     *         new ConcurrentHashMap<>();
     *     
     *     public void handle(Request request) {
     *         String requestType = request.getType();
     *         Handler handler = handlerCache.computeIfAbsent(requestType, 
     *             type -> findHandlerByType(type));
     *         handler.handle(request);
     *     }
     * }
     * ```
     * 
     * 4. 异步处理
     * ```java
     * public class AsyncChain {
     *     private final ExecutorService executor = 
     *         Executors.newFixedThreadPool(10);
     *     
     *     public CompletableFuture<Response> handleAsync(Request request) {
     *         return CompletableFuture.supplyAsync(() -> {
     *             return processChain(request);
     *         }, executor);
     *     }
     * }
     * ```
     * 
     * 5. 分支策略
     * ```java
     * public class BranchingChain {
     *     private final Map<Predicate<Request>, Handler> branches = 
     *         new HashMap<>();
     *     
     *     public void handle(Request request) {
     *         Handler handler = branches.entrySet().stream()
     *             .filter(entry -> entry.getKey().test(request))
     *             .map(Map.Entry::getValue)
     *             .findFirst()
     *             .orElse(defaultHandler);
     *         handler.handle(request);
     *     }
     * }
     * ```
     * 
     * 6. 批处理优化
     * ```java
     * public class BatchChain {
     *     public void handleBatch(List<Request> requests) {
     *         Map<Class<? extends Request>, List<Request>> grouped = 
     *             requests.stream()
     *                 .collect(Collectors.groupingBy(Request::getClass));
     *         
     *         grouped.forEach((type, batch) -> {
     *             Handler handler = getHandlerForType(type);
     *             handler.handleBatch(batch);
     *         });
     *     }
     * }
     * ```
     */
    public void performanceOptimization() {
        System.out.println("性能优化：排序优化、提前终止、缓存、异步、分支、批处理");
    }

    /**
     * Q8: 责任链模式与其他模式的区别？
     * 
     * A: 与其他模式对比：
     * 
     * 1. 责任链 vs 装饰者模式
     *    责任链：
     *    - 请求可能被任意一个处理器处理
     *    - 处理器之间是平等的
     *    - 重点在于传递和处理
     *    
     *    装饰者：
     *    - 所有装饰者都会处理请求
     *    - 装饰者有包含关系
     *    - 重点在于功能增强
     * 
     * 2. 责任链 vs 策略模式
     *    责任链：
     *    - 运行时决定由哪个处理器处理
     *    - 处理器形成链式结构
     *    - 可能有多个处理器参与
     *    
     *    策略：
     *    - 编译时或运行时选择一种策略
     *    - 策略之间相互独立
     *    - 只有一个策略执行
     * 
     * 3. 责任链 vs 命令模式
     *    责任链：
     *    - 重点在于处理请求的传递
     *    - 处理器可以拒绝处理
     *    - 链的结构可能改变
     *    
     *    命令：
     *    - 重点在于封装请求
     *    - 支持撤销、重做、排队
     *    - 命令与接收者分离
     * 
     * 4. 责任链 vs 观察者模式
     *    责任链：
     *    - 一个请求可能被一个或多个处理器处理
     *    - 处理器之间有顺序关系
     *    - 可以中断传递
     *    
     *    观察者：
     *    - 所有观察者都会收到通知
     *    - 观察者之间没有顺序关系
     *    - 无法中断通知
     * 
     * 选择建议：
     * - 需要多个对象处理请求且顺序重要：责任链
     * - 需要增强对象功能：装饰者
     * - 需要动态选择算法：策略
     * - 需要封装请求并支持撤销：命令
     * - 需要一对多通知：观察者
     */
    public void patternComparison() {
        System.out.println("模式对比：装饰者、策略、命令、观察者 - 关注点和使用场景不同");
    }

    /**
     * Q9: 如何处理责任链中的异常？
     * 
     * A: 异常处理策略：
     * 
     * 1. 异常终止策略
     * ```java
     * public abstract class SafeHandler {
     *     public final void handle(Request request) {
     *         try {
     *             doHandle(request);
     *         } catch (Exception e) {
     *             handleException(e, request);
     *             return; // 异常时终止链
     *         }
     *         
     *         if (nextHandler != null) {
     *             nextHandler.handle(request);
     *         }
     *     }
     *     
     *     protected abstract void doHandle(Request request);
     *     protected abstract void handleException(Exception e, Request request);
     * }
     * ```
     * 
     * 2. 异常继续策略
     * ```java
     * public class ContinueOnExceptionHandler extends BaseHandler {
     *     @Override
     *     public void handle(Request request) {
     *         try {
     *             if (canHandle(request)) {
     *                 doHandle(request);
     *             }
     *         } catch (Exception e) {
     *             logError(e, request);
     *             // 继续执行，不终止链
     *         }
     *         
     *         if (nextHandler != null) {
     *             nextHandler.handle(request);
     *         }
     *     }
     * }
     * ```
     * 
     * 3. 异常重试策略
     * ```java
     * public class RetryableHandler extends BaseHandler {
     *     private final int maxRetries = 3;
     *     
     *     @Override
     *     public void handle(Request request) {
     *         for (int i = 0; i < maxRetries; i++) {
     *             try {
     *                 if (canHandle(request)) {
     *                     doHandle(request);
     *                     return;
     *                 }
     *                 break;
     *             } catch (Exception e) {
     *                 if (i == maxRetries - 1) {
     *                     throw new ChainException("重试失败", e);
     *                 }
     *                 try {
     *                     Thread.sleep(1000 * (i + 1));
     *                 } catch (InterruptedException ie) {
     *                     Thread.currentThread().interrupt();
     *                     throw new ChainException("重试被中断", ie);
     *                 }
     *             }
     *         }
     *         
     *         if (nextHandler != null) {
     *             nextHandler.handle(request);
     *         }
     *     }
     * }
     * ```
     * 
     * 4. 异常收集策略
     * ```java
     * public class ExceptionCollectingChain {
     *     private final List<Exception> exceptions = new ArrayList<>();
     *     
     *     public void handle(Request request) {
     *         for (Handler handler : handlers) {
     *             try {
     *                 handler.handle(request);
     *             } catch (Exception e) {
     *                 exceptions.add(e);
     *             }
     *         }
     *         
     *         if (!exceptions.isEmpty()) {
     *             throw new AggregateException(exceptions);
     *         }
     *     }
     * }
     * ```
     * 
     * 5. 降级处理策略
     * ```java
     * public class FallbackChain {
     *     public void handle(Request request) {
     *         try {
     *             primaryHandler.handle(request);
     *         } catch (Exception e) {
     *             try {
     *                 fallbackHandler.handle(request);
     *             } catch (Exception fallbackException) {
     *                 defaultHandler.handle(request);
     *             }
     *         }
     *     }
     * }
     * ```
     */
    public void exceptionHandling() {
        System.out.println("异常处理：终止、继续、重试、收集、降级策略");
    }

    /**
     * Q10: 责任链模式在实际项目中的最佳实践？
     * 
     * A: 最佳实践：
     * 
     * 1. 设计原则
     * ```java
     * // 单一职责：每个处理器只负责一种类型的请求
     * public class AuthenticationHandler extends BaseHandler {
     *     // 只处理身份认证
     * }
     * 
     * public class AuthorizationHandler extends BaseHandler {
     *     // 只处理权限验证
     * }
     * 
     * // 开闭原则：通过配置或注入添加新处理器
     * @Configuration
     * public class ChainConfiguration {
     *     @Bean
     *     public HandlerChain securityChain() {
     *         return HandlerChain.builder()
     *             .addHandler(authenticationHandler())
     *             .addHandler(authorizationHandler())
     *             .addHandler(validationHandler())
     *             .build();
     *     }
     * }
     * ```
     * 
     * 2. 性能考虑
     * ```java
     * // 使用Builder模式构建链
     * public class HandlerChain {
     *     public static Builder builder() {
     *         return new Builder();
     *     }
     *     
     *     public static class Builder {
     *         private List<Handler> handlers = new ArrayList<>();
     *         
     *         public Builder addHandler(Handler handler) {
     *             handlers.add(handler);
     *             return this;
     *         }
     *         
     *         public HandlerChain build() {
     *             // 优化：按处理频率排序
     *             handlers.sort(Comparator.comparing(Handler::getPriority));
     *             return new HandlerChain(handlers);
     *         }
     *     }
     * }
     * ```
     * 
     * 3. 监控和日志
     * ```java
     * public class MonitoredHandler extends BaseHandler {
     *     private static final Logger logger = LoggerFactory.getLogger(MonitoredHandler.class);
     *     private final MeterRegistry meterRegistry;
     *     
     *     @Override
     *     public void handle(Request request) {
     *         Timer.Sample sample = Timer.start(meterRegistry);
     *         try {
     *             logger.info("开始处理请求: {}", request.getId());
     *             doHandle(request);
     *             logger.info("完成处理请求: {}", request.getId());
     *         } catch (Exception e) {
     *             logger.error("处理请求失败: " + request.getId(), e);
     *             throw e;
     *         } finally {
     *             sample.stop(Timer.builder("handler.duration")
     *                 .tag("handler", this.getClass().getSimpleName())
     *                 .register(meterRegistry));
     *         }
     *     }
     * }
     * ```
     * 
     * 4. 测试策略
     * ```java
     * @Test
     * public void testChainExecution() {
     *     // 测试正常流程
     *     Handler mockHandler1 = mock(Handler.class);
     *     Handler mockHandler2 = mock(Handler.class);
     *     
     *     when(mockHandler1.canHandle(any())).thenReturn(false);
     *     when(mockHandler2.canHandle(any())).thenReturn(true);
     *     
     *     HandlerChain chain = HandlerChain.builder()
     *         .addHandler(mockHandler1)
     *         .addHandler(mockHandler2)
     *         .build();
     *     
     *     Request request = new TestRequest();
     *     chain.handle(request);
     *     
     *     verify(mockHandler1).handle(request);
     *     verify(mockHandler2).handle(request);
     * }
     * ```
     * 
     * 5. 配置管理
     * ```yaml
     * # application.yml
     * chain:
     *   handlers:
     *     - type: authentication
     *       enabled: true
     *       order: 1
     *     - type: authorization
     *       enabled: true
     *       order: 2
     *     - type: validation
     *       enabled: false
     *       order: 3
     * ```
     * 
     * 6. 线程安全
     * ```java
     * // 确保处理器是线程安全的
     * public class ThreadSafeHandler extends BaseHandler {
     *     private final ThreadLocal<Context> context = new ThreadLocal<>();
     *     
     *     @Override
     *     public void handle(Request request) {
     *         try {
     *             context.set(new Context());
     *             doHandle(request);
     *         } finally {
     *             context.remove();
     *         }
     *     }
     * }
     * ```
     */
    public void bestPractices() {
        System.out.println("最佳实践：单一职责、Builder模式、监控日志、测试策略、配置管理");
    }
}