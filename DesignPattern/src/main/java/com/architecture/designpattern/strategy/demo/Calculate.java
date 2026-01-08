package com.architecture.designpattern.strategy.demo;

/**
 * 传统方式的计算类（用于对比策略模式的优势）
 */
public class Calculate {
    
    /**
     * 加法运算
     */
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * 减法运算
     */
    public int sub(int a, int b) {
        return a - b;
    }
}