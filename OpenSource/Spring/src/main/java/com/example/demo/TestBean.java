package com.example.demo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 完整的TestBean类 - Spring演示用
 * 
 * 🎯 主要功能:
 * 1. 属性注入和构造器注入演示
 * 2. Bean生命周期回调演示
 * 3. 不同作用域的行为对比
 * 4. 业务逻辑执行演示
 * 5. 状态管理和数据持久化
 * 
 * 🔍 学习要点:
 * - 理解Bean的实例化过程
 * - 掌握依赖注入的两种方式
 * - 观察生命周期方法的调用时机
 * - 体会单例与原型模式的差异
 */
public class TestBean {
    
    // 静态计数器，用于跟踪实例创建
    private static final AtomicLong instanceCounter = new AtomicLong(0);
    
    // 基本属性
    private String message;
    private int number;
    
    // 扩展属性
    private final long instanceId;
    private final String creationTime;
    private boolean initialized = false;
    private boolean destroyed = false;
    private final List<String> operationHistory;
    
    // 状态管理
    private String status;
    private int operationCount;
    
    /**
     * 默认构造器 - 用于属性注入演示
     * 
     * 🔍 关键点:
     * - Spring会先调用无参构造器
     * - 然后通过setter方法注入属性
     * - 最后调用init-method
     */
    public TestBean() {
        this.instanceId = instanceCounter.incrementAndGet();
        this.creationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        this.message = "Default TestBean Message";
        this.number = 42;
        this.status = "CREATED";
        this.operationCount = 0;
        this.operationHistory = new ArrayList<>();
        
        System.out.println("🏗️ 构造器: TestBean#" + instanceId + " 已创建 (" + creationTime + ")");
        addOperation("构造器调用");
    }
    
    /**
     * 带参数构造器 - 用于构造器注入演示
     * 
     * 🔍 关键点:
     * - 构造器注入是推荐的方式
     * - 可以保证必要属性不为null
     * - 支持final属性的初始化
     */
    public TestBean(String message, int number) {
        this.instanceId = instanceCounter.incrementAndGet();
        this.creationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        this.message = message;
        this.number = number;
        this.status = "CREATED_WITH_PARAMS";
        this.operationCount = 0;
        this.operationHistory = new ArrayList<>();
        
        System.out.println("🏗️ 构造器(带参数): TestBean#" + instanceId + " 已创建 (" + creationTime + ")");
        System.out.println("    ↳ 注入参数: message='" + message + "', number=" + number);
        addOperation("构造器注入: " + message + ", " + number);
    }
    
    // ===== Getter和Setter方法 =====
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置消息 - 属性注入演示
     */
    public void setMessage(String message) {
        String oldMessage = this.message;
        this.message = message;
        System.out.println("🔧 属性注入: TestBean#" + instanceId + " message: '" + oldMessage + "' -> '" + message + "'");
        addOperation("属性注入: message=" + message);
    }
    
    public int getNumber() {
        return number;
    }
    
    /**
     * 设置数字 - 属性注入演示
     */
    public void setNumber(int number) {
        int oldNumber = this.number;
        this.number = number;
        System.out.println("🔧 属性注入: TestBean#" + instanceId + " number: " + oldNumber + " -> " + number);
        addOperation("属性注入: number=" + number);
    }
    
    // ===== 生命周期方法 =====
    
    /**
     * Bean初始化方法 - 由XML配置中init-method指定
     * 
     * 🔍 关键点:
     * - 在所有属性注入完成后调用
     * - 可以进行复杂的初始化逻辑
     * - 等同于@PostConstruct注解
     */
    public void init() {
        this.initialized = true;
        this.status = "INITIALIZED";
        
        System.out.println("🌱 初始化方法: TestBean#" + instanceId + " 初始化完成");
        System.out.println("    ↳ 当前状态: " + status);
        System.out.println("    ↳ 属性值: message='" + message + "', number=" + number);
        
        // 模拟复杂初始化逻辑
        performComplexInitialization();
        addOperation("初始化方法调用");
    }
    
    /**
     * Bean销毁方法 - 由XML配置中destroy-method指定
     * 
     * 🔍 关键点:
     * - 容器关闭时调用(仅限单例)
     * - 用于清理资源和状态
     * - 等同于@PreDestroy注解
     * 
     * ⚠️ 注意: 只有现代Spring容器(如DefaultListableBeanFactory)
     * 支持destroy-method自动调用
     */
    public void destroy() {
        if (destroyed) {
            return; // 防止重复销毁
        }
        
        this.destroyed = true;
        this.status = "DESTROYED";
        
        System.out.println("🧹 销毁方法: TestBean#" + instanceId + " 开始销毁");
        System.out.println("    ↳ 操作历史记录: " + operationHistory.size() + " 条");
        
        // 清理资源
        cleanupResources();
        addOperation("销毁方法调用");
        
        System.out.println("🧹 TestBean#" + instanceId + " 销毁完成");
    }
    
    // ===== 业务方法 =====
    
    /**
     * 模拟业务操作
     */
    public void doSomething() {
        if (!initialized) {
            System.out.println("⚠️ 警告: TestBean#" + instanceId + " 尚未初始化");
            return;
        }
        
        operationCount++;
        System.out.println("💼 业务操作: TestBean#" + instanceId + " 正在执行业务逻辑 (第" + operationCount + "次)");
        System.out.println("    ↳ 处理消息: " + message);
        System.out.println("    ↳ 计算结果: " + number + " * 2 = " + (number * 2));
        
        addOperation("业务操作#" + operationCount);
        
        // 模拟处理时间
        try {
            Thread.sleep(10); // 10ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✅ 业务操作完成");
    }
    
    /**
     * 获取Bean详细信息
     */
    public void showDetailInfo() {
        System.out.println("📊 TestBean#" + instanceId + " 详细信息:");
        System.out.println("    ↳ 创建时间: " + creationTime);
        System.out.println("    ↳ 当前状态: " + status);
        System.out.println("    ↳ 初始化状态: " + (initialized ? "✅ 已初始化" : "❌ 未初始化"));
        System.out.println("    ↳ 操作次数: " + operationCount);
        System.out.println("    ↳ 历史记录: " + operationHistory.size() + " 条");
    }
    
    /**
     * 获取操作历史
     */
    public List<String> getOperationHistory() {
        return new ArrayList<>(operationHistory); // 返回副本以保证封装性
    }
    
    // ===== 内部辅助方法 =====
    
    /**
     * 模拟复杂初始化逻辑
     */
    private void performComplexInitialization() {
        System.out.println("    ↳ 🔍 执行复杂初始化逻辑...");
        
        // 模拟数据库连接初始化
        System.out.println("      ▫ 初始化数据库连接...");
        
        // 模拟缓存预热
        System.out.println("      ▫ 预热缓存系统...");
        
        // 模拟配置加载
        System.out.println("      ▫ 加载配置参数...");
        
        System.out.println("    ↳ ✅ 复杂初始化完成");
    }
    
    /**
     * 清理资源
     */
    private void cleanupResources() {
        System.out.println("    ↳ 🧹 清理资源...");
        
        // 模拟关闭数据库连接
        System.out.println("      ▫ 关闭数据库连接...");
        
        // 模拟清理缓存
        System.out.println("      ▫ 清理缓存数据...");
        
        // 模拟释放线程池
        System.out.println("      ▫ 释放线程池资源...");
        
        System.out.println("    ↳ ✅ 资源清理完成");
    }
    
    /**
     * 添加操作记录
     */
    private void addOperation(String operation) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        operationHistory.add(timestamp + " - " + operation);
    }
    
    // ===== 状态查询方法 =====
    
    public long getInstanceId() {
        return instanceId;
    }
    
    public String getCreationTime() {
        return creationTime;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
    
    public String getStatus() {
        return status;
    }
    
    public int getOperationCount() {
        return operationCount;
    }
    
    public static long getTotalInstanceCount() {
        return instanceCounter.get();
    }
    
    @Override
    public String toString() {
        return String.format("TestBean#%d{message='%s', number=%d, status='%s', created='%s', operations=%d}",
                instanceId, message, number, status, creationTime, operationCount);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TestBean testBean = (TestBean) obj;
        return instanceId == testBean.instanceId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(instanceId);
    }
    
    /**
     * 静态工厂方法 - 用于XML配置中factory-method演示
     * 
     * 🔍 关键点:
     * - 静态工厂方法不需要实例化工厂类
     * - Spring会直接调用类的静态方法
     * - 适用于复杂的实例创建逻辑
     */
    public static TestBean createInstance(String message, int number) {
        System.out.println("🏠 静态工厂方法: 创建 TestBean 实例");
        System.out.println("    ↳ 参数: message='" + message + "', number=" + number);
        
        TestBean instance = new TestBean(message, number);
        instance.addOperation("静态工厂方法创建");
        
        System.out.println("    ↳ ✅ 实例创建成功: #" + instance.getInstanceId());
        return instance;
    }
    
    /**
     * 清理所有实例的静态计数器
     */
    public static void resetInstanceCounter() {
        instanceCounter.set(0);
        System.out.println("🔄 TestBean 实例计数器已重置");
    }
}