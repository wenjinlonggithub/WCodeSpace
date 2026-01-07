package com.architecture.designpattern.visitor;

import org.springframework.stereotype.Component;

@Component
public class VisitorInterviewQuestions {

    /**
     * ====================
     * 访问者模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是访问者模式？
     */
    public void whatIsVisitor() {
        System.out.println("访问者模式：表示一个作用于对象结构中元素的操作");
    }

    /**
     * Q2: 访问者模式的核心组件有哪些？
     */
    public void coreComponents() {
        System.out.println("核心组件：抽象访问者、具体访问者、元素、对象结构");
    }

    /**
     * Q3: 访问者模式适用于哪些场景？
     */
    public void applicableScenarios() {
        System.out.println("适用场景：语法树遍历、编译器、文件系统操作");
    }

    /**
     * Q4: 访问者模式的优缺点是什么？
     */
    public void advantagesAndDisadvantages() {
        System.out.println("优点：扩展操作容易；缺点：添加新元素困难");
    }

    /**
     * Q5: 双分派模式是什么？
     */
    public void doubleDispatch() {
        System.out.println("双分派：根据访问者和元素的类型选择方法");
    }
}