package com.architecture.designpattern.strategy;

import org.springframework.stereotype.Component;

@Component
public class StrategyInterviewQuestions {

    /**
     * ====================
     * 策略模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是策略模式？
     */
    public void whatIsStrategy() {
        System.out.println("策略模式：定义算法族，分别封装，使它们可以互相替换");
    }

    /**
     * Q2: 策略模式的核心组件？
     */
    public void coreComponents() {
        System.out.println("核心组件：策略接口、具体策略、上下文");
    }

    /**
     * Q3: 策略模式与状态模式的区别？
     */
    public void vsStatePattern() {
        System.out.println("区别：策略由客户端选择，状态自动转换");
    }

    /**
     * Q4: 如何消除策略模式的if-else？
     */
    public void eliminateIfElse() {
        System.out.println("消除if-else：工厂+注册、注解驱动、配置映射");
    }

    /**
     * Q5: 策略模式的实际应用？
     */
    public void realWorldApplications() {
        System.out.println("实际应用：支付策略、排序算法、压缩算法、折扣计算");
    }
}