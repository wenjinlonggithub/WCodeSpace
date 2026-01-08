package com.architecture.designpattern.strategy.demo;

/**
 * 策略接口 - 简单演示版本
 */
public interface Strategy {
    /**
     * 执行计算
     * @param a 操作数a
     * @param b 操作数b
     * @return 计算结果
     */
    int calculate(int a, int b);
}