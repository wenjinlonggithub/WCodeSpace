package com.architecture.designpattern.adapter;

import org.springframework.stereotype.Component;

@Component
public class AdapterInterviewQuestions {

    /**
     * ====================
     * 适配器模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是适配器模式？它解决了什么问题？
     * 
     * A: 适配器模式定义：
     * 将一个类的接口转换成客户端所期待的另一个接口，使原本不兼容的类能够一起工作。
     * 
     * 解决的问题：
     * 1. 接口不兼容
     *    - 新旧系统接口不匹配
     *    - 第三方库接口与系统不符
     * 
     * 2. 代码复用
     *    - 无需修改现有代码
     *    - 提高代码重用性
     * 
     * 3. 系统集成
     *    - 不同系统间的数据交换
     *    - 遗留系统的现代化改造
     * 
     * 适用场景：
     * - 使用第三方库但接口不匹配
     * - 旧系统与新系统的集成
     * - 不同数据格式的转换
     */
    public void whatIsAdapter() {
        System.out.println("适配器模式：接口转换，使不兼容的类能够协同工作");
    }

    /**
     * Q2: 适配器模式有哪几种实现方式？
     * 
     * A: 两种实现方式：
     * 
     * 1. 对象适配器（组合方式）
     *    - 使用组合关系
     *    - 适配器包含被适配者的实例
     *    - 更灵活，支持多重继承效果
     * 
     * 2. 类适配器（继承方式）
     *    - 使用继承关系
     *    - 适配器继承被适配者
     *    - Java单继承限制，使用较少
     * 
     * 对比：
     * - 对象适配器更常用
     * - 类适配器耦合度更高
     * - 对象适配器支持适配接口和类
     * 
     * 示例：
     * ```java
     * // 对象适配器
     * class ObjectAdapter implements Target {
     *     private Adaptee adaptee;
     *     
     *     public ObjectAdapter(Adaptee adaptee) {
     *         this.adaptee = adaptee;
     *     }
     *     
     *     public void request() {
     *         adaptee.specificRequest();
     *     }
     * }
     * ```
     */
    public void implementationTypes() {
        System.out.println("实现方式：对象适配器（组合）、类适配器（继承）");
    }

    /**
     * Q3: 适配器模式的核心组件有哪些？
     * 
     * A: 核心组件：
     * 
     * 1. 目标接口（Target）
     *    - 客户端期望的接口
     *    - 定义客户端使用的特定领域相关的接口
     * 
     * 2. 被适配者（Adaptee）
     *    - 需要适配的现有接口
     *    - 通常是已存在的、不兼容的接口
     * 
     * 3. 适配器（Adapter）
     *    - 实现目标接口
     *    - 包装被适配者，将其接口转换为目标接口
     * 
     * 4. 客户端（Client）
     *    - 使用目标接口的代码
     *    - 不直接与被适配者交互
     * 
     * 协作关系：
     * - Client调用Target接口
     * - Adapter实现Target接口
     * - Adapter内部调用Adaptee的方法
     */
    public void coreComponents() {
        System.out.println("核心组件：目标接口、被适配者、适配器、客户端");
    }

    /**
     * Q4: 适配器模式在实际开发中的应用场景？
     * 
     * A: 实际应用场景：
     * 
     * 1. 数据库适配
     * ```java
     * // 统一数据库访问接口
     * interface DatabaseAdapter {
     *     void connect();
     *     ResultSet query(String sql);
     * }
     * 
     * // MySQL适配器
     * class MySQLAdapter implements DatabaseAdapter {
     *     private MySQLDriver driver; // 被适配者
     *     
     *     public void connect() {
     *         driver.establishConnection();
     *     }
     * }
     * ```
     * 
     * 2. 日志框架适配
     * ```java
     * // SLF4J适配不同的日志实现
     * class Log4jAdapter implements Logger {
     *     private org.apache.log4j.Logger logger;
     *     
     *     public void info(String message) {
     *         logger.info(message);
     *     }
     * }
     * ```
     * 
     * 3. 支付接口适配
     * ```java
     * // 统一支付接口
     * interface PaymentAdapter {
     *     PayResult pay(PayRequest request);
     * }
     * 
     * // 支付宝适配器
     * class AlipayAdapter implements PaymentAdapter {
     *     private AlipayClient client;
     *     
     *     public PayResult pay(PayRequest request) {
     *         // 转换参数格式
     *         AlipayRequest alipayRequest = convert(request);
     *         AlipayResponse response = client.execute(alipayRequest);
     *         return convertResult(response);
     *     }
     * }
     * ```
     * 
     * 4. 缓存系统适配
     * ```java
     * // 统一缓存接口
     * interface CacheAdapter {
     *     void put(String key, Object value);
     *     Object get(String key);
     * }
     * 
     * // Redis适配器
     * class RedisAdapter implements CacheAdapter {
     *     private JedisCluster jedis;
     *     
     *     public void put(String key, Object value) {
     *         jedis.set(key, serialize(value));
     *     }
     * }
     * ```
     */
    public void realWorldApplications() {
        System.out.println("应用场景：数据库适配、日志框架、支付接口、缓存系统");
    }

    /**
     * Q5: 适配器模式的优缺点是什么？
     * 
     * A: 优点：
     * 
     * 1. 代码重用性
     *    - 可以重用现有的类
     *    - 无需修改原有代码
     * 
     * 2. 解耦性
     *    - 将接口转换代码与业务逻辑分离
     *    - 客户端与被适配者解耦
     * 
     * 3. 开闭原则
     *    - 对扩展开放，对修改关闭
     *    - 可以添加新的适配器而不影响现有代码
     * 
     * 4. 透明性
     *    - 客户端不知道适配器的存在
     *    - 使用统一的接口访问
     * 
     * 缺点：
     * 
     * 1. 增加系统复杂性
     *    - 增加了类的数量
     *    - 理解和维护成本上升
     * 
     * 2. 性能损耗
     *    - 多了一层调用
     *    - 可能存在数据转换开销
     * 
     * 3. 功能限制
     *    - 只能适配被适配者已有的功能
     *    - 无法添加新功能
     */
    public void advantagesAndDisadvantages() {
        System.out.println("优点：代码重用、解耦、开闭原则；缺点：复杂性增加、性能损耗");
    }

    /**
     * Q6: 适配器模式与装饰器模式的区别？
     * 
     * A: 主要区别：
     * 
     * 1. 设计意图：
     *    - 适配器：接口转换，解决兼容性问题
     *    - 装饰器：功能增强，保持接口不变
     * 
     * 2. 接口关系：
     *    - 适配器：目标接口与被适配者接口不同
     *    - 装饰器：装饰器与被装饰者实现相同接口
     * 
     * 3. 功能变化：
     *    - 适配器：不改变功能，只改变接口
     *    - 装饰器：增强或修改原有功能
     * 
     * 4. 使用时机：
     *    - 适配器：设计后期，解决集成问题
     *    - 装饰器：设计时就考虑，提供扩展点
     * 
     * 5. 结构特点：
     *    - 适配器：一对一的接口映射
     *    - 装饰器：可以层层嵌套装饰
     * 
     * 代码对比：
     * ```java
     * // 适配器：接口不同
     * class Adapter implements TargetInterface {
     *     private LegacyClass legacy;
     *     public void newMethod() {
     *         legacy.oldMethod(); // 接口转换
     *     }
     * }
     * 
     * // 装饰器：接口相同
     * class Decorator implements Component {
     *     private Component component;
     *     public void operation() {
     *         component.operation(); // 功能增强
     *         additionalBehavior();
     *     }
     * }
     * ```
     */
    public void vsDecoratorPattern() {
        System.out.println("区别：适配器转换接口，装饰器增强功能");
    }

    /**
     * Q7: 如何设计一个灵活的适配器？
     * 
     * A: 设计策略：
     * 
     * 1. 使用泛型适配器
     * ```java
     * public abstract class GenericAdapter<T, R> {
     *     protected T adaptee;
     *     
     *     public GenericAdapter(T adaptee) {
     *         this.adaptee = adaptee;
     *     }
     *     
     *     public abstract R adapt();
     * }
     * ```
     * 
     * 2. 配置驱动适配
     * ```java
     * @Configuration
     * public class AdapterConfiguration {
     *     @Bean
     *     @ConditionalOnProperty(name = "payment.provider", havingValue = "alipay")
     *     public PaymentAdapter alipayAdapter() {
     *         return new AlipayAdapter();
     *     }
     *     
     *     @Bean
     *     @ConditionalOnProperty(name = "payment.provider", havingValue = "wechat")
     *     public PaymentAdapter wechatAdapter() {
     *         return new WechatAdapter();
     *     }
     * }
     * ```
     * 
     * 3. 策略模式结合
     * ```java
     * public class AdapterFactory {
     *     private static final Map<String, Supplier<PaymentAdapter>> adapters = new HashMap<>();
     *     
     *     static {
     *         adapters.put("alipay", AlipayAdapter::new);
     *         adapters.put("wechat", WechatAdapter::new);
     *     }
     *     
     *     public static PaymentAdapter getAdapter(String type) {
     *         return adapters.get(type).get();
     *     }
     * }
     * ```
     * 
     * 4. 注解驱动
     * ```java
     * @AdapterFor("mysql")
     * public class MySQLAdapter implements DatabaseAdapter {
     *     // 实现
     * }
     * 
     * @Component
     * public class AdapterRegistry {
     *     @EventListener(ContextRefreshedEvent.class)
     *     public void registerAdapters(ContextRefreshedEvent event) {
     *         // 扫描@AdapterFor注解并注册
     *     }
     * }
     * ```
     */
    public void flexibleAdapterDesign() {
        System.out.println("灵活设计：泛型适配器、配置驱动、策略模式、注解驱动");
    }

    /**
     * Q8: Spring框架中适配器模式的应用？
     * 
     * A: Spring中的应用：
     * 
     * 1. HandlerAdapter
     * ```java
     * // Spring MVC中的处理器适配器
     * public interface HandlerAdapter {
     *     boolean supports(Object handler);
     *     ModelAndView handle(HttpServletRequest request, 
     *                        HttpServletResponse response, 
     *                        Object handler) throws Exception;
     * }
     * 
     * // 适配不同类型的Controller
     * public class SimpleControllerHandlerAdapter implements HandlerAdapter {
     *     public boolean supports(Object handler) {
     *         return (handler instanceof Controller);
     *     }
     *     
     *     public ModelAndView handle(..., Object handler) {
     *         return ((Controller) handler).handleRequest(request, response);
     *     }
     * }
     * ```
     * 
     * 2. JpaRepositoryFactoryBean
     * ```java
     * // 适配JPA Repository接口
     * public class JpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> 
     *         extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {
     *     
     *     protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
     *         return new JpaRepositoryFactory(em);
     *     }
     * }
     * ```
     * 
     * 3. MessageConverter
     * ```java
     * // 适配不同的消息转换器
     * public interface HttpMessageConverter<T> {
     *     boolean canRead(Class<?> clazz, MediaType mediaType);
     *     boolean canWrite(Class<?> clazz, MediaType mediaType);
     *     T read(Class<? extends T> clazz, HttpInputMessage inputMessage);
     *     void write(T t, MediaType contentType, HttpOutputMessage outputMessage);
     * }
     * ```
     * 
     * 4. TaskExecutor
     * ```java
     * // 适配不同的任务执行器
     * public interface TaskExecutor extends Executor {
     *     void execute(Runnable task);
     * }
     * 
     * public class SimpleAsyncTaskExecutor implements AsyncTaskExecutor {
     *     public void execute(Runnable task) {
     *         new Thread(task).start(); // 适配Thread执行
     *     }
     * }
     * ```
     */
    public void springFrameworkApplications() {
        System.out.println("Spring应用：HandlerAdapter、RepositoryFactory、MessageConverter、TaskExecutor");
    }

    /**
     * Q9: 如何测试适配器模式？
     * 
     * A: 测试策略：
     * 
     * 1. 接口兼容性测试
     * ```java
     * @Test
     * public void testAdapterCompatibility() {
     *     // 准备
     *     LegacyPaymentService legacyService = new LegacyPaymentService();
     *     PaymentAdapter adapter = new PaymentAdapterImpl(legacyService);
     *     
     *     // 测试
     *     PaymentRequest request = new PaymentRequest("100", "USD");
     *     PaymentResult result = adapter.pay(request);
     *     
     *     // 验证
     *     assertNotNull(result);
     *     assertEquals("SUCCESS", result.getStatus());
     * }
     * ```
     * 
     * 2. 数据转换测试
     * ```java
     * @Test
     * public void testDataConversion() {
     *     // 测试参数转换
     *     PaymentRequest modernRequest = new PaymentRequest("100", "USD");
     *     LegacyPaymentRequest legacyRequest = adapter.convertRequest(modernRequest);
     *     
     *     assertEquals(10000, legacyRequest.getAmountInCents()); // $100 = 10000 cents
     *     assertEquals("USD", legacyRequest.getCurrencyCode());
     * }
     * ```
     * 
     * 3. Mock测试
     * ```java
     * @Test
     * public void testWithMock() {
     *     // 准备Mock
     *     LegacyPaymentService mockService = Mockito.mock(LegacyPaymentService.class);
     *     when(mockService.processPayment(any())).thenReturn(createSuccessResponse());
     *     
     *     PaymentAdapter adapter = new PaymentAdapterImpl(mockService);
     *     
     *     // 执行测试
     *     PaymentResult result = adapter.pay(new PaymentRequest("100", "USD"));
     *     
     *     // 验证Mock调用
     *     verify(mockService).processPayment(argThat(req -> 
     *         req.getAmountInCents() == 10000 && "USD".equals(req.getCurrencyCode())));
     * }
     * ```
     * 
     * 4. 异常处理测试
     * ```java
     * @Test
     * public void testExceptionHandling() {
     *     LegacyPaymentService faultyService = new LegacyPaymentService() {
     *         @Override
     *         public LegacyResponse processPayment(LegacyRequest request) {
     *             throw new LegacyException("Service unavailable");
     *         }
     *     };
     *     
     *     PaymentAdapter adapter = new PaymentAdapterImpl(faultyService);
     *     
     *     assertThrows(PaymentException.class, () -> {
     *         adapter.pay(new PaymentRequest("100", "USD"));
     *     });
     * }
     * ```
     */
    public void testingStrategies() {
        System.out.println("测试策略：接口兼容性、数据转换、Mock测试、异常处理");
    }

    /**
     * Q10: 适配器模式的性能优化？
     * 
     * A: 优化策略：
     * 
     * 1. 缓存转换结果
     * ```java
     * public class CachedAdapter implements PaymentAdapter {
     *     private final PaymentService service;
     *     private final Cache<String, PaymentResult> cache;
     *     
     *     public PaymentResult pay(PaymentRequest request) {
     *         String cacheKey = generateKey(request);
     *         return cache.computeIfAbsent(cacheKey, k -> {
     *             return service.processPayment(convert(request));
     *         });
     *     }
     * }
     * ```
     * 
     * 2. 延迟初始化
     * ```java
     * public class LazyAdapter implements DatabaseAdapter {
     *     private volatile Connection connection;
     *     
     *     public ResultSet query(String sql) {
     *         if (connection == null) {
     *             synchronized (this) {
     *                 if (connection == null) {
     *                     connection = createConnection(); // 延迟创建
     *                 }
     *             }
     *         }
     *         return connection.prepareStatement(sql).executeQuery();
     *     }
     * }
     * ```
     * 
     * 3. 批量转换
     * ```java
     * public class BatchAdapter implements DataAdapter {
     *     private static final int BATCH_SIZE = 100;
     *     
     *     public void processData(List<DataItem> items) {
     *         for (int i = 0; i < items.size(); i += BATCH_SIZE) {
     *             List<DataItem> batch = items.subList(i, 
     *                 Math.min(i + BATCH_SIZE, items.size()));
     *             processBatch(batch); // 批量处理
     *         }
     *     }
     * }
     * ```
     * 
     * 4. 对象池化
     * ```java
     * public class PooledAdapter implements ServiceAdapter {
     *     private final ObjectPool<ExpensiveResource> pool;
     *     
     *     public Result process(Request request) {
     *         ExpensiveResource resource = pool.borrowObject();
     *         try {
     *             return resource.process(adapt(request));
     *         } finally {
     *             pool.returnObject(resource);
     *         }
     *     }
     * }
     * ```
     * 
     * 5. 异步适配
     * ```java
     * public class AsyncAdapter implements PaymentAdapter {
     *     private final Executor executor;
     *     
     *     public CompletableFuture<PaymentResult> payAsync(PaymentRequest request) {
     *         return CompletableFuture.supplyAsync(() -> {
     *             return legacyService.processPayment(convert(request));
     *         }, executor).thenApply(this::convertResult);
     *     }
     * }
     * ```
     */
    public void performanceOptimization() {
        System.out.println("性能优化：缓存转换、延迟初始化、批量处理、对象池化、异步适配");
    }
}