package com.example.demo;

/**
 * 循环依赖演示类
 * 
 * 🎯 演示目标:
 * 1. 展示Spring如何处理循环依赖
 * 2. 理解三级缓存机制
 * 3. 对比构造器注入与属性注入的差异
 * 4. 演示循环依赖的解决方案
 * 
 * 🔍 关键概念:
 * - 循环依赖: A依赖B，B依赖A
 * - 三级缓存: singletonObjects, earlySingletonObjects, singletonFactories
 * - 构造器循环依赖无法解决，属性循环依赖可以解决
 * 
 * 📚 面试重点:
 * - Spring如何解决循环依赖问题？
 * - 为什么构造器注入的循环依赖无法解决？
 * - 三级缓存的作用和工作原理？
 */
public class CircularDependencyDemo {
    
    /**
     * ServiceA - 依赖ServiceB
     */
    public static class ServiceA {
        private ServiceB serviceB;
        private String name;
        
        public ServiceA() {
            System.out.println("🏗️ ServiceA 构造器调用");
        }
        
        public ServiceA(ServiceB serviceB) {
            this.serviceB = serviceB;
            System.out.println("🏗️ ServiceA 构造器调用(带ServiceB参数)");
        }
        
        public void setServiceB(ServiceB serviceB) {
            this.serviceB = serviceB;
            System.out.println("🔗 ServiceA 注入依赖: ServiceB");
        }
        
        public void setName(String name) {
            this.name = name;
            System.out.println("🔧 ServiceA 注入属性: name=" + name);
        }
        
        public ServiceB getServiceB() {
            return serviceB;
        }
        
        public String getName() {
            return name;
        }
        
        public void doWork() {
            System.out.println("💼 ServiceA 执行业务逻辑");
            System.out.println("    名称: " + name);
            if (serviceB != null) {
                System.out.println("    调用 ServiceB...");
                serviceB.process();
            } else {
                System.out.println("    ⚠️ ServiceB 未注入");
            }
        }
        
        @Override
        public String toString() {
            return "ServiceA{name='" + name + "', serviceB=" + 
                   (serviceB != null ? "ServiceB@" + Integer.toHexString(serviceB.hashCode()) : "null") + "}";
        }
    }
    
    /**
     * ServiceB - 依赖ServiceA
     */
    public static class ServiceB {
        private ServiceA serviceA;
        private String description;
        
        public ServiceB() {
            System.out.println("🏗️ ServiceB 构造器调用");
        }
        
        public ServiceB(ServiceA serviceA) {
            this.serviceA = serviceA;
            System.out.println("🏗️ ServiceB 构造器调用(带ServiceA参数)");
        }
        
        public void setServiceA(ServiceA serviceA) {
            this.serviceA = serviceA;
            System.out.println("🔗 ServiceB 注入依赖: ServiceA");
        }
        
        public void setDescription(String description) {
            this.description = description;
            System.out.println("🔧 ServiceB 注入属性: description=" + description);
        }
        
        public ServiceA getServiceA() {
            return serviceA;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void process() {
            System.out.println("⚙️ ServiceB 执行处理逻辑");
            System.out.println("    描述: " + description);
            if (serviceA != null) {
                System.out.println("    关联的 ServiceA: " + serviceA.getName());
            } else {
                System.out.println("    ⚠️ ServiceA 未注入");
            }
        }
        
        @Override
        public String toString() {
            return "ServiceB{description='" + description + "', serviceA=" + 
                   (serviceA != null ? "ServiceA@" + Integer.toHexString(serviceA.hashCode()) : "null") + "}";
        }
    }
    
    /**
     * 演示循环依赖的检测和处理
     */
    public static void demonstrateCircularDependency() {
        System.out.println("🔄 === 循环依赖演示 === 🔄");
        System.out.println();
        
        System.out.println("📚 理论说明:");
        System.out.println("  循环依赖: A -> B, B -> A");
        System.out.println("  Spring解决方案: 三级缓存 + 提前暴露");
        System.out.println("  适用范围: 仅限属性注入的单例Bean");
        System.out.println("  不适用: 构造器注入、Prototype作用域");
        System.out.println();
        
        System.out.println("🔍 三级缓存机制:");
        System.out.println("  1级: singletonObjects - 完成的单例对象");
        System.out.println("  2级: earlySingletonObjects - 提前暴露的对象");  
        System.out.println("  3级: singletonFactories - 对象工厂");
        System.out.println();
        
        System.out.println("⚡ 解决步骤:");
        System.out.println("  1. 创建ServiceA实例，加入3级缓存");
        System.out.println("  2. 注入ServiceA属性时需要ServiceB");
        System.out.println("  3. 创建ServiceB实例，加入3级缓存");
        System.out.println("  4. 注入ServiceB属性时需要ServiceA");
        System.out.println("  5. 从3级缓存获取ServiceA，移至2级缓存");
        System.out.println("  6. ServiceB完成创建，移至1级缓存");
        System.out.println("  7. ServiceA完成创建，移至1级缓存");
        System.out.println();
        
        System.out.println("💡 关键点:");
        System.out.println("  ✅ 允许: setter注入的单例循环依赖");
        System.out.println("  ❌ 禁止: 构造器注入的循环依赖");
        System.out.println("  ❌ 禁止: prototype作用域的循环依赖");
        System.out.println();
    }
    
    /**
     * 验证循环依赖解决方案
     */
    public static void verifyCircularDependencyResolution(ServiceA serviceA, ServiceB serviceB) {
        System.out.println("✅ === 循环依赖验证 === ✅");
        System.out.println();
        
        if (serviceA != null && serviceB != null) {
            System.out.println("🔍 依赖关系验证:");
            System.out.println("  ServiceA实例: " + serviceA);
            System.out.println("  ServiceB实例: " + serviceB);
            System.out.println();
            
            // 验证双向引用
            ServiceB serviceAToB = serviceA.getServiceB();
            ServiceA serviceBToA = serviceB.getServiceA();
            
            System.out.println("🔗 引用关系验证:");
            System.out.println("  ServiceA -> ServiceB: " + 
                (serviceAToB != null ? "✅ 已注入" : "❌ 未注入"));
            System.out.println("  ServiceB -> ServiceA: " + 
                (serviceBToA != null ? "✅ 已注入" : "❌ 未注入"));
            
            if (serviceAToB != null && serviceBToA != null) {
                boolean correctReference = (serviceAToB == serviceB) && (serviceBToA == serviceA);
                System.out.println("  引用正确性: " + 
                    (correctReference ? "✅ 正确" : "❌ 错误"));
                    
                // 演示业务调用
                System.out.println();
                System.out.println("🎭 业务逻辑演示:");
                serviceA.doWork();
                System.out.println();
                serviceB.process();
            }
            
        } else {
            System.out.println("❌ 循环依赖未正确解决");
            System.out.println("  ServiceA: " + (serviceA != null ? "✅" : "❌"));
            System.out.println("  ServiceB: " + (serviceB != null ? "✅" : "❌"));
        }
        
        System.out.println();
    }
    
    /**
     * 演示构造器循环依赖问题
     */
    public static void demonstrateConstructorCircularDependencyIssue() {
        System.out.println("⚠️ === 构造器循环依赖问题演示 === ⚠️");
        System.out.println();
        
        System.out.println("📝 问题描述:");
        System.out.println("  构造器注入要求在创建对象时就提供所有依赖");
        System.out.println("  但循环依赖中，A需要B，B需要A，无法确定创建顺序");
        System.out.println();
        
        System.out.println("🔍 失败原因:");
        System.out.println("  1. 创建ServiceA需要ServiceB实例");
        System.out.println("  2. 创建ServiceB需要ServiceA实例");
        System.out.println("  3. 形成死锁，无法完成对象创建");
        System.out.println();
        
        System.out.println("💡 解决方案:");
        System.out.println("  1. 改用setter注入 (推荐)");
        System.out.println("  2. 使用@Lazy注解延迟注入");
        System.out.println("  3. 重新设计类结构，消除循环依赖");
        System.out.println("  4. 使用事件驱动或观察者模式解耦");
        System.out.println();
        
        try {
            System.out.println("🧪 模拟构造器循环依赖错误:");
            System.out.println("  new ServiceA(new ServiceB(serviceA)) // 无法实现");
            System.out.println("  ↳ 编译错误: serviceA尚未定义");
        } catch (Exception e) {
            System.out.println("  ❌ 异常: " + e.getMessage());
        }
        
        System.out.println();
    }
}