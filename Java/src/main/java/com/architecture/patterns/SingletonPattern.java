package com.architecture.patterns;

/**
 * 单例模式实现 - 线程安全的懒汉式
 * 
 * 核心原理：
 * 1. 私有构造函数防止外部实例化
 * 2. 静态变量保存唯一实例
 * 3. 双重检查锁定确保线程安全
 * 4. volatile关键字防止指令重排序
 */
public class SingletonPattern {
    
    // volatile确保多线程环境下的可见性和有序性
    private static volatile SingletonPattern instance;
    
    // 私有构造函数
    private SingletonPattern() {
        // 防止反射攻击
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to create instance");
        }
    }
    
    // 双重检查锁定（Double-Checked Locking）
    public static SingletonPattern getInstance() {
        if (instance == null) {
            synchronized (SingletonPattern.class) {
                if (instance == null) {
                    instance = new SingletonPattern();
                }
            }
        }
        return instance;
    }
    
    public void doSomething() {
        System.out.println("Singleton instance is working...");
    }
}

/**
 * 枚举实现单例 - 最佳实践
 * 优点：
 * 1. 线程安全
 * 2. 防止反射攻击
 * 3. 防止序列化破坏
 * 4. 代码简洁
 */
enum SingletonEnum {
    INSTANCE;
    
    public void doSomething() {
        System.out.println("Enum singleton is working...");
    }
}

/**
 * 静态内部类实现单例 - 推荐方式
 * 优点：
 * 1. 懒加载
 * 2. 线程安全
 * 3. 性能高（无同步开销）
 */
class SingletonInnerClass {
    
    private SingletonInnerClass() {}
    
    private static class SingletonHolder {
        private static final SingletonInnerClass INSTANCE = new SingletonInnerClass();
    }
    
    public static SingletonInnerClass getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    public void doSomething() {
        System.out.println("Inner class singleton is working...");
    }
}