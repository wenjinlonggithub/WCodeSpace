package com.architecture.designpattern.observer;

import org.springframework.stereotype.Component;

@Component
public class ObserverInterviewQuestions {

    /**
     * ====================
     * 观察者模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是观察者模式？
     */
    public void whatIsObserver() {
        System.out.println("观察者模式：定义对象间一对多依赖关系，当主题状态改变时通知所有观察者");
    }

    /**
     * Q2: 观察者模式的核心组件？
     */
    public void coreComponents() {
        System.out.println("核心组件：主题（被观察者）、观察者、具体主题、具体观察者");
    }

    /**
     * Q3: 观察者模式的优缺点？
     */
    public void advantagesAndDisadvantages() {
        System.out.println("优点：松耦合、动态关系；缺点：可能循环依赖、性能问题");
    }

    /**
     * Q4: 如何避免观察者模式的内存泄漏？
     */
    public void avoidMemoryLeaks() {
        System.out.println("避免内存泄漏：弱引用、及时取消订阅、生命周期管理");
    }

    /**
     * Q5: 观察者模式在Java中的应用？
     */
    public void javaApplications() {
        System.out.println("Java应用：Event机制、Swing事件、PropertyChangeListener、RxJava");
    }
}