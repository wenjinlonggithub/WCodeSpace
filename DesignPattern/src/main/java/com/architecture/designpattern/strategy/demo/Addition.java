package com.architecture.designpattern.strategy.demo;

/**
 * 加法策略实现
 */
public class Addition implements Strategy {
    
    @Override
    public int calculate(int a, int b) {
        System.out.println("执行加法运算：" + a + " + " + b + " = " + (a + b));
        return a + b;
    }
}