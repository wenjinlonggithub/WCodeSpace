package com.architecture.designpattern.strategy.demo;

/**
 * 客户端演示类 - 简单的策略模式使用示例
 */
public class Client {
    
    public static void main(String[] args) {
        System.out.println("========== 简单策略模式演示 ==========");
        
        Calculator calculator = new Calculator();
        
        // 演示加法策略
        System.out.println("=== 使用加法策略 ===");
        calculator.setStrategy(new Addition());
        int result1 = calculator.getResult(10, 5);
        System.out.println("结果: " + result1);
        
        // 演示减法策略
        System.out.println("\n=== 使用减法策略 ===");
        calculator.setStrategy(new Subtraction());
        int result2 = calculator.getResult(10, 5);
        System.out.println("结果: " + result2);
        
        System.out.println("\n=== 对比传统方式 ===");
        Calculate traditionalCalc = new Calculate();
        System.out.println("传统加法: " + traditionalCalc.add(10, 5));
        System.out.println("传统减法: " + traditionalCalc.sub(10, 5));
        
        System.out.println("\n策略模式的优势:");
        System.out.println("- 算法可以动态切换");
        System.out.println("- 易于扩展新的算法");
        System.out.println("- 消除条件判断语句");
        System.out.println("- 提高代码复用性");
    }
}