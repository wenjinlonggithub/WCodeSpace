package com.architecture.designpattern.iterator;

import org.springframework.stereotype.Component;

@Component
public class IteratorInterviewQuestions {

    /**
     * ====================
     * 迭代器模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是迭代器模式？
     */
    public void whatIsIterator() {
        System.out.println("迭代器模式：提供一种方法顺序访问聚合对象中各个元素");
    }

    /**
     * Q2: 迭代器模式的核心组件有哪些？
     */
    public void coreComponents() {
        System.out.println("核心组件：迭代器接口、具体迭代器、聚合接口、具体聚合");
    }

    /**
     * Q3: 迭代器模式适用于哪些场景？
     */
    public void applicableScenarios() {
        System.out.println("适用场景：集合遍历、数据结构访问、流式处理");
    }

    /**
     * Q4: Java中的Iterator接口原理？
     */
    public void javaIteratorPrinciple() {
        System.out.println("Java Iterator：hasNext()、next()、remove()方法，支持fail-fast");
    }

    /**
     * Q5: 外部迭代器和内部迭代器的区别？
     */
    public void externalVsInternalIterator() {
        System.out.println("区别：外部迭代器客户端控制，内部迭代器聚合控制");
    }
}