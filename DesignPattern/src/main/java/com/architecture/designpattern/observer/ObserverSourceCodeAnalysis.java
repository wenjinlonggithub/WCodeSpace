package com.architecture.designpattern.observer;

import org.springframework.stereotype.Component;

@Component
public class ObserverSourceCodeAnalysis {

    /**
     * ====================
     * 观察者模式源码分析
     * ====================
     */

    /**
     * 1. Java Observable/Observer
     */
    public void analyzeJavaObservable() {
        System.out.println("Java Observable：内置观察者实现，已废弃，推荐事件机制");
    }

    /**
     * 2. Spring事件机制
     */
    public void analyzeSpringEvent() {
        System.out.println("Spring事件：ApplicationEvent、EventListener、事件总线");
    }

    /**
     * 3. Swing事件模型
     */
    public void analyzeSwingEvent() {
        System.out.println("Swing事件：ActionListener、事件分发线程、事件适配器");
    }

    /**
     * 4. RxJava响应式编程
     */
    public void analyzeRxJava() {
        System.out.println("RxJava：Observable、Observer、操作符、调度器");
    }

    /**
     * 5. 消息队列观察者
     */
    public void analyzeMessageQueue() {
        System.out.println("消息队列：发布订阅模式、主题订阅、消息路由");
    }
}