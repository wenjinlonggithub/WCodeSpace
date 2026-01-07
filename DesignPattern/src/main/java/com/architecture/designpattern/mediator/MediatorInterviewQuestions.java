package com.architecture.designpattern.mediator;

import org.springframework.stereotype.Component;

@Component
public class MediatorInterviewQuestions {

    /**
     * ====================
     * 中介者模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是中介者模式？
     */
    public void whatIsMediator() {
        System.out.println("中介者模式：定义对象间的一对多依赖关系，解耦对象间的直接通信");
    }

    /**
     * Q2: 中介者模式的核心组件有哪些？
     */
    public void coreComponents() {
        System.out.println("核心组件：抽象中介者、具体中介者、同事类");
    }

    /**
     * Q3: 中介者模式适用于哪些场景？
     */
    public void applicableScenarios() {
        System.out.println("适用场景：组件通信、工作流、用户界面、聊天系统");
    }

    /**
     * Q4: 中介者模式与观察者模式的区别？
     */
    public void vsObserverPattern() {
        System.out.println("区别：中介者是一对多双向通信，观察者是一对多单向通知");
    }

    /**
     * Q5: 如何设计一个灵活的中介者？
     */
    public void flexibleMediatorDesign() {
        System.out.println("灵活设计：事件驱动、消息路由、插件化架构");
    }
}