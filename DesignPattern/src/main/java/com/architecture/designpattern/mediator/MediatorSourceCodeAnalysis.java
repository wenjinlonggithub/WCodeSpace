package com.architecture.designpattern.mediator;

import org.springframework.stereotype.Component;

@Component
public class MediatorSourceCodeAnalysis {

    /**
     * ====================
     * 中介者模式源码分析
     * ====================
     */

    /**
     * 1. 基础中介者实现
     */
    public void analyzeBasicImplementation() {
        System.out.println("基础实现：中介者统一管理同事间的通信");
    }

    /**
     * 2. 事件驱动的中介者
     */
    public void analyzeEventDrivenMediator() {
        System.out.println("事件驱动：消息类型、事件分发、异步处理");
    }

    /**
     * 3. Java Swing中的中介者
     */
    public void analyzeSwingMediator() {
        System.out.println("Swing中介者：ActionListener、事件分发机制");
    }

    /**
     * 4. Spring事件机制
     */
    public void analyzeSpringEventMechanism() {
        System.out.println("Spring事件：ApplicationEventPublisher，解耦组件通信");
    }

    /**
     * 5. 消息队列中介者
     */
    public void analyzeMessageQueueMediator() {
        System.out.println("消息队列：RabbitMQ、Kafka作为分布式中介者");
    }
}