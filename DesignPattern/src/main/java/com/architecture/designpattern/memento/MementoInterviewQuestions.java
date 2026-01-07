package com.architecture.designpattern.memento;

import org.springframework.stereotype.Component;

@Component
public class MementoInterviewQuestions {

    /**
     * ====================
     * 备忘录模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是备忘录模式？
     */
    public void whatIsMemento() {
        System.out.println("备忘录模式：在不破坏封装性的前提下捕获对象内部状态并在对象之外保存");
    }

    /**
     * Q2: 备忘录模式的核心组件有哪些？
     */
    public void coreComponents() {
        System.out.println("核心组件：备忘录、发起人、管理者");
    }

    /**
     * Q3: 备忘录模式适用于哪些场景？
     */
    public void applicableScenarios() {
        System.out.println("适用场景：撤销操作、快照功能、事务回滚、编辑器");
    }

    /**
     * Q4: 如何解决备忘录模式的内存消耗问题？
     */
    public void memoryOptimization() {
        System.out.println("内存优化：增量备忘录、历史限制、压缩存储");
    }

    /**
     * Q5: 备忘录模式与原型模式的区别？
     */
    public void vsPrototypePattern() {
        System.out.println("区别：备忘录为状态恢复，原型为对象克隆");
    }
}