package com.architecture.designpattern.strategy.demo;

/**
 * 减法策略实现
 */
public class Subtraction implements Strategy {
    
    @Override
    public int calculate(int a, int b) {
        System.out.println("执行减法运算：" + a + " - " + b + " = " + (a - b));
        return a - b;
    }
}