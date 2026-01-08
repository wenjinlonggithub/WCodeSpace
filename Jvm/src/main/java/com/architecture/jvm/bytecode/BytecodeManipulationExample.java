package com.architecture.jvm.bytecode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 字节码操作和动态生成示例
 * 
 * 展示动态代理、字节码生成等技术
 */
public class BytecodeManipulationExample {
    
    public static void main(String[] args) {
        BytecodeManipulationExample example = new BytecodeManipulationExample();
        
        System.out.println("=== 字节码操作和动态生成示例 ===\n");
        
        // 1. JDK动态代理
        example.demonstrateJDKDynamicProxy();
        
        // 2. 字节码生成原理
        example.explainBytecodeGeneration();
        
        // 3. 常见应用场景
        example.commonApplicationScenarios();
        
        // 4. 性能和注意事项
        example.performanceConsiderations();
    }
    
    /**
     * 演示JDK动态代理
     */
    private void demonstrateJDKDynamicProxy() {
        System.out.println("=== JDK动态代理演示 ===");
        
        System.out.println("1. 动态代理基本使用:");
        
        // 创建目标对象
        UserService userService = new UserServiceImpl();
        
        // 创建代理对象
        UserService proxyService = (UserService) Proxy.newProxyInstance(
                userService.getClass().getClassLoader(),
                userService.getClass().getInterfaces(),
                new LoggingInvocationHandler(userService)
        );
        
        // 使用代理对象
        System.out.println("调用代理对象方法:");
        User user = proxyService.getUserById(123L);
        proxyService.updateUser(user);
        
        System.out.println("\n2. 动态代理原理:");
        System.out.println("   • JVM运行时动态生成代理类");
        System.out.println("   • 代理类继承Proxy类，实现目标接口");
        System.out.println("   • 所有方法调用都委托给InvocationHandler");
        System.out.println("   • 通过反射调用目标对象方法");
        
        // 显示代理类信息
        Class<?> proxyClass = proxyService.getClass();
        System.out.println("\n3. 代理类信息:");
        System.out.println("   代理类名: " + proxyClass.getName());
        System.out.println("   父类: " + proxyClass.getSuperclass().getName());
        System.out.println("   实现接口: ");
        for (Class<?> intf : proxyClass.getInterfaces()) {
            System.out.println("     " + intf.getName());
        }
        
        // 分析生成的代理类结构
        analyzeProxyClass();
        
        System.out.println();
    }
    
    /**
     * 解释字节码生成原理
     */
    private void explainBytecodeGeneration() {
        System.out.println("=== 字节码生成原理 ===");
        
        System.out.println("1. 动态代理字节码生成过程:");
        System.out.println("   1. 分析目标接口，获取所有方法信息");
        System.out.println("   2. 生成代理类的字节码:");
        System.out.println("      • 类头信息（magic number、版本等）");
        System.out.println("      • 常量池（类名、方法名、描述符等）");
        System.out.println("      • 字段信息（Method对象缓存）");
        System.out.println("      • 方法信息（构造器、接口方法实现）");
        System.out.println("   3. 使用defineClass加载字节码到JVM");
        System.out.println("   4. 通过反射创建代理类实例");
        
        System.out.println("\n2. 生成的代理类结构（伪代码）:");
        System.out.println("```java");
        System.out.println("public final class $Proxy0 extends Proxy implements UserService {");
        System.out.println("    private static Method m1;");
        System.out.println("    private static Method m2;");
        System.out.println("    private static Method m3; // getUserById");
        System.out.println("    private static Method m4; // updateUser");
        System.out.println("    ");
        System.out.println("    static {");
        System.out.println("        // 初始化Method对象");
        System.out.println("        m3 = Class.forName(\"UserService\").getMethod(\"getUserById\", Long.class);");
        System.out.println("        m4 = Class.forName(\"UserService\").getMethod(\"updateUser\", User.class);");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    public $Proxy0(InvocationHandler h) { super(h); }");
        System.out.println("    ");
        System.out.println("    public User getUserById(Long id) {");
        System.out.println("        return (User) this.h.invoke(this, m3, new Object[]{id});");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    public void updateUser(User user) {");
        System.out.println("        this.h.invoke(this, m4, new Object[]{user});");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");
        
        System.out.println("\n3. 字节码指令分析:");
        System.out.println("   代理方法的字节码模式:");
        System.out.println("   • aload_0           // 加载this");
        System.out.println("   • getfield h        // 获取InvocationHandler");
        System.out.println("   • aload_0           // 加载this（作为proxy参数）");
        System.out.println("   • getstatic m3      // 获取Method对象");
        System.out.println("   • 构造参数数组...");
        System.out.println("   • invokeinterface invoke  // 调用invoke方法");
        System.out.println("   • checkcast/return  // 类型转换并返回");
        
        System.out.println();
    }
    
    /**
     * 常见应用场景
     */
    private void commonApplicationScenarios() {
        System.out.println("=== 字节码操作应用场景 ===");
        
        System.out.println("1. AOP（面向切面编程）:");
        System.out.println("   • 日志记录: 方法执行前后记录日志");
        System.out.println("   • 性能监控: 统计方法执行时间");
        System.out.println("   • 安全检查: 权限验证和访问控制");
        System.out.println("   • 事务管理: 自动开启、提交、回滚事务");
        System.out.println("   • 缓存处理: 自动缓存方法返回值");
        
        // AOP示例
        performanceMonitoringExample();
        
        System.out.println("\n2. 框架开发:");
        System.out.println("   • Spring: Bean代理、AOP实现");
        System.out.println("   • MyBatis: Mapper接口动态实现");
        System.out.println("   • Hibernate: 懒加载代理");
        System.out.println("   • RPC框架: 远程服务调用代理");
        System.out.println("   • 模拟框架: Mock对象生成");
        
        System.out.println("\n3. 代码增强:");
        System.out.println("   • 字段访问控制");
        System.out.println("   • 方法参数校验");
        System.out.println("   • 异常处理增强");
        System.out.println("   • 调试信息注入");
        System.out.println("   • 性能统计埋点");
        
        System.out.println("\n4. 工具和插件:");
        System.out.println("   • Java Agent: 运行时修改字节码");
        System.out.println("   • IDE插件: 代码分析和增强");
        System.out.println("   • 测试工具: Mock和Stub生成");
        System.out.println("   • 监控工具: 应用性能监控");
        System.out.println("   • 代码混淆: 保护知识产权");
        
        System.out.println();
    }
    
    /**
     * 性能监控示例
     */
    private void performanceMonitoringExample() {
        System.out.println("\n   性能监控代理示例:");
        
        // 创建带性能监控的代理
        Calculator calculator = new CalculatorImpl();
        Calculator monitoredCalculator = (Calculator) Proxy.newProxyInstance(
                calculator.getClass().getClassLoader(),
                calculator.getClass().getInterfaces(),
                new PerformanceMonitoringHandler(calculator)
        );
        
        // 执行被监控的方法
        long result1 = monitoredCalculator.fibonacci(30);
        double result2 = monitoredCalculator.sqrt(16.0);
        
        System.out.println("   fibonacci(30) = " + result1);
        System.out.println("   sqrt(16.0) = " + result2);
    }
    
    /**
     * 性能和注意事项
     */
    private void performanceConsiderations() {
        System.out.println("=== 性能和注意事项 ===");
        
        System.out.println("1. 性能影响:");
        System.out.println("   • 反射调用开销: 比直接调用慢5-10倍");
        System.out.println("   • 字节码生成时间: 首次创建代理较慢");
        System.out.println("   • 内存占用: 额外的Class和Method对象");
        System.out.println("   • GC压力: 更多的临时对象创建");
        
        // 性能对比测试
        performanceComparisonTest();
        
        System.out.println("\n2. 优化策略:");
        System.out.println("   • 代理缓存: 缓存已创建的代理类");
        System.out.println("   • 方法缓存: 缓存Method对象避免重复查找");
        System.out.println("   • 字节码优化: 生成更高效的字节码");
        System.out.println("   • 编译时代理: 使用编译时字节码生成");
        
        System.out.println("\n3. 使用注意事项:");
        System.out.println("   • 接口限制: JDK动态代理只支持接口");
        System.out.println("   • 类型安全: 注意类型转换和泛型擦除");
        System.out.println("   • 异常处理: 正确处理代理过程中的异常");
        System.out.println("   • 调试困难: 代理类的调试和排错较复杂");
        System.out.println("   • 序列化问题: 代理对象的序列化需特殊处理");
        
        System.out.println("\n4. 替代方案:");
        System.out.println("   • CGLib: 支持类代理，不限制接口");
        System.out.println("   • ByteBuddy: 现代字节码操作库");
        System.out.println("   • ASM: 底层字节码操作，性能最高");
        System.out.println("   • Javassist: 源码级别的字节码操作");
        System.out.println("   • 编译时注解: 使用APT在编译时生成代码");
        
        System.out.println();
    }
    
    /**
     * 性能对比测试
     */
    private void performanceComparisonTest() {
        Calculator calculator = new CalculatorImpl();
        Calculator proxy = (Calculator) Proxy.newProxyInstance(
                calculator.getClass().getClassLoader(),
                calculator.getClass().getInterfaces(),
                new SimpleInvocationHandler(calculator)
        );
        
        int iterations = 1000000;
        
        // 直接调用测试
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            calculator.add(1, 2);
        }
        long directTime = System.currentTimeMillis() - startTime;
        
        // 代理调用测试
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            proxy.add(1, 2);
        }
        long proxyTime = System.currentTimeMillis() - startTime;
        
        System.out.println("\n   性能对比测试结果 (" + iterations + "次调用):");
        System.out.println("   直接调用: " + directTime + " ms");
        System.out.println("   代理调用: " + proxyTime + " ms");
        
        if (directTime > 0) {
            double overhead = ((double)(proxyTime - directTime) / directTime) * 100;
            System.out.printf("   代理开销: %.2f%%\n", overhead);
        }
    }
    
    /**
     * 分析代理类结构
     */
    private void analyzeProxyClass() {
        System.out.println("\n4. 动态代理类生成分析:");
        
        // 获取代理类生成器信息
        System.out.println("   • 代理类命名规则: $ProxyN (N为递增数字)");
        System.out.println("   • 生成位置: sun.misc.ProxyGenerator");
        System.out.println("   • 父类: java.lang.reflect.Proxy");
        System.out.println("   • 实现: 目标接口 + 基础方法(equals、hashCode、toString)");
        
        System.out.println("\n   代理类字节码特征:");
        System.out.println("   • 每个接口方法对应一个Method静态字段");
        System.out.println("   • 静态初始化块初始化所有Method对象");
        System.out.println("   • 所有方法实现都调用InvocationHandler.invoke");
        System.out.println("   • 异常处理包装为UndeclaredThrowableException");
    }
    
    // 辅助接口和类定义
    
    interface UserService {
        User getUserById(Long id);
        void updateUser(User user);
    }
    
    static class UserServiceImpl implements UserService {
        @Override
        public User getUserById(Long id) {
            return new User(id, "User" + id);
        }
        
        @Override
        public void updateUser(User user) {
            System.out.println("   更新用户: " + user.getName());
        }
    }
    
    static class User {
        private Long id;
        private String name;
        
        public User(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Long getId() { return id; }
        public String getName() { return name; }
    }
    
    static class LoggingInvocationHandler implements InvocationHandler {
        private final Object target;
        
        public LoggingInvocationHandler(Object target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("   [LOG] 调用方法: " + method.getName());
            long startTime = System.currentTimeMillis();
            
            try {
                Object result = method.invoke(target, args);
                long endTime = System.currentTimeMillis();
                System.out.println("   [LOG] 方法执行完成，耗时: " + (endTime - startTime) + "ms");
                return result;
            } catch (Exception e) {
                System.out.println("   [LOG] 方法执行异常: " + e.getMessage());
                throw e;
            }
        }
    }
    
    interface Calculator {
        int add(int a, int b);
        long fibonacci(int n);
        double sqrt(double x);
    }
    
    static class CalculatorImpl implements Calculator {
        @Override
        public int add(int a, int b) {
            return a + b;
        }
        
        @Override
        public long fibonacci(int n) {
            if (n <= 1) return n;
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
        
        @Override
        public double sqrt(double x) {
            return Math.sqrt(x);
        }
    }
    
    static class PerformanceMonitoringHandler implements InvocationHandler {
        private final Object target;
        
        public PerformanceMonitoringHandler(Object target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long startTime = System.nanoTime();
            
            try {
                Object result = method.invoke(target, args);
                long endTime = System.nanoTime();
                double duration = (endTime - startTime) / 1000000.0; // 转换为毫秒
                
                System.out.printf("   [PERF] %s 执行时间: %.3f ms\n", 
                                method.getName(), duration);
                return result;
            } catch (Exception e) {
                System.out.println("   [PERF] 方法执行失败: " + e.getMessage());
                throw e;
            }
        }
    }
    
    static class SimpleInvocationHandler implements InvocationHandler {
        private final Object target;
        
        public SimpleInvocationHandler(Object target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(target, args);
        }
    }
}