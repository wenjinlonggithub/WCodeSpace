package com.architecture.designpattern.bridge;

import org.springframework.stereotype.Component;

@Component
public class BridgeInterviewQuestions {

    /**
     * ====================
     * 桥接模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是桥接模式？
     */
    public void whatIsBridge() {
        System.out.println("桥接模式：将抽象部分与实现部分分离，使它们能够独立地变化");
    }

    /**
     * Q2: 桥接模式的核心组件？
     */
    public void coreComponents() {
        System.out.println("核心组件：抽象、扩展抽象、实现者、具体实现者");
    }

    /**
     * Q3: 桥接模式与适配器模式的区别？
     */
    public void vsAdapterPattern() {
        System.out.println("区别：桥接设计时分离，适配器设计后集成");
    }

    /**
     * Q4: 桥接模式适用场景？
     */
    public void applicableScenarios() {
        System.out.println("适用场景：跨平台开发、驱动程序、图形渲染、数据库抽象");
    }

    /**
     * Q5: 如何设计一个灵活的桥接？
     */
    public void flexibleBridgeDesign() {
        System.out.println("灵活设计：工厂模式创建实现者，配置驱动，插件化架构");
    }
}