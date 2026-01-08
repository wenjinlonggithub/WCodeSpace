package com.architecture.designpattern.strategy.demo;

/**
 * 计算器上下文类 - 策略模式演示
 * 持有策略对象的引用，负责调用具体的策略算法
 */
public class Calculator {
    
    private Strategy strategy;

    /**
     * 设置计算策略
     * @param strategy 具体的计算策略
     */
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    /**
     * 执行计算并返回结果
     * @param a 操作数a
     * @param b 操作数b
     * @return 计算结果
     */
    public int getResult(int a, int b) {
        if (strategy == null) {
            throw new IllegalStateException("计算策略未设置");
        }
        return this.strategy.calculate(a, b);
    }
}