package com.architecture.designpattern.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingletonExample {
    private static final Logger logger = LoggerFactory.getLogger(SingletonExample.class);

    public static void main(String[] args) {
        // 设置控制台输出编码为UTF-8，支持中文显示
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");

        new SingletonExample().demonstratePatterns();
    }

    public void demonstratePatterns() {
        logger.info("=== 单例模式演示 ===");
        
        logger.info("1. 饿汉式单例");
        EagerSingleton eager1 = EagerSingleton.getInstance();
        EagerSingleton eager2 = EagerSingleton.getInstance();
        logger.info("两个实例是否相同: {}", eager1 == eager2);
        
        logger.info("2. 懒汉式单例（双重检查锁定）");
        LazySingleton lazy1 = LazySingleton.getInstance();
        LazySingleton lazy2 = LazySingleton.getInstance();
        logger.info("两个实例是否相同: {}", lazy1 == lazy2);
        
        logger.info("3. 枚举单例");
        EnumSingleton enum1 = EnumSingleton.INSTANCE;
        EnumSingleton enum2 = EnumSingleton.INSTANCE;
        logger.info("两个实例是否相同: {}", enum1 == enum2);
        enum1.doSomething();
        
        logger.info("4. 静态内部类单例");
        InnerClassSingleton inner1 = InnerClassSingleton.getInstance();
        InnerClassSingleton inner2 = InnerClassSingleton.getInstance();
        logger.info("两个实例是否相同: {}", inner1 == inner2);
    }
}

class EagerSingleton {
    private static final EagerSingleton INSTANCE = new EagerSingleton();
    
    private EagerSingleton() {}
    
    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
}

class LazySingleton {
    private static volatile LazySingleton instance;
    
    private LazySingleton() {}
    
    public static LazySingleton getInstance() {
        if (instance == null) {
            synchronized (LazySingleton.class) {
                if (instance == null) {
                    instance = new LazySingleton();
                }
            }
        }
        return instance;
    }
}

enum EnumSingleton {
    INSTANCE;
    
    private static final Logger logger = LoggerFactory.getLogger(EnumSingleton.class);
    
    public void doSomething() {
        logger.info("枚举单例执行操作");
    }
}

class InnerClassSingleton {
    private InnerClassSingleton() {}
    
    private static class SingletonHolder {
        private static final InnerClassSingleton INSTANCE = new InnerClassSingleton();
    }
    
    public static InnerClassSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}