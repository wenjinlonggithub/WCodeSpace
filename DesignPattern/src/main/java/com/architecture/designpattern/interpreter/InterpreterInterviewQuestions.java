package com.architecture.designpattern.interpreter;

import org.springframework.stereotype.Component;

@Component
public class InterpreterInterviewQuestions {

    /**
     * ====================
     * 解释器模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是解释器模式？它解决了什么问题？
     */
    public void whatIsInterpreter() {
        System.out.println("解释器模式：为语言定义文法表示，并定义解释器解释语言中的句子");
    }

    /**
     * Q2: 解释器模式的核心组件有哪些？
     */
    public void coreComponents() {
        System.out.println("核心组件：抽象表达式、终结符表达式、非终结符表达式、上下文");
    }

    /**
     * Q3: 解释器模式适用于哪些场景？
     */
    public void applicableScenarios() {
        System.out.println("适用场景：DSL解析、SQL解析、表达式计算、规则引擎");
    }

    /**
     * Q4: 解释器模式的优缺点是什么？
     */
    public void advantagesAndDisadvantages() {
        System.out.println("优点：易扩展语法、易实现；缺点：复杂语法效率低、难调试");
    }

    /**
     * Q5: 如何实现一个简单的数学表达式解释器？
     */
    public void mathExpressionInterpreter() {
        System.out.println("数学解释器：终结符表达式(数字)、非终结符表达式(运算符)");
    }
}