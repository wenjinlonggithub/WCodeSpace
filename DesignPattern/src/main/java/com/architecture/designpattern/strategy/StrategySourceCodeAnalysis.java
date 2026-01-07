package com.architecture.designpattern.strategy;

import org.springframework.stereotype.Component;

@Component
public class StrategySourceCodeAnalysis {

    /**
     * ====================
     * 策略模式源码分析
     * ====================
     */

    /**
     * 1. Java Comparator策略
     */
    public void analyzeComparator() {
        System.out.println("Comparator：比较策略接口，Collections.sort使用");
    }

    /**
     * 2. Spring Bean验证策略
     */
    public void analyzeSpringValidation() {
        System.out.println("Spring验证：Validator策略，不同验证算法实现");
    }

    /**
     * 3. ThreadPoolExecutor拒绝策略
     */
    public void analyzeRejectedExecutionHandler() {
        System.out.println("线程池策略：RejectedExecutionHandler，不同拒绝策略");
    }

    /**
     * 4. 策略工厂模式
     */
    public void analyzeStrategyFactory() {
        System.out.println("策略工厂：结合工厂模式，自动选择策略实现");
    }

    /**
     * 5. 函数式策略模式
     */
    public void analyzeFunctionalStrategy() {
        System.out.println("函数式策略：Lambda表达式、方法引用、策略即函数");
    }
}