package com.architecture.designpattern.builder;

import org.springframework.stereotype.Component;

@Component
public class BuilderInterviewQuestions {

    /**
     * ====================
     * 建造者模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是建造者模式？
     */
    public void whatIsBuilder() {
        System.out.println("建造者模式：将复杂对象的构建过程与表示分离，使同样构建过程可创建不同表示");
    }

    /**
     * Q2: 建造者模式的核心组件？
     */
    public void coreComponents() {
        System.out.println("核心组件：产品、抽象建造者、具体建造者、指挥者");
    }

    /**
     * Q3: 建造者模式与工厂模式的区别？
     */
    public void vsFactoryPattern() {
        System.out.println("区别：建造者关注构建过程，工厂关注产品创建");
    }

    /**
     * Q4: 建造者模式适用场景？
     */
    public void applicableScenarios() {
        System.out.println("适用场景：复杂对象构建、多步骤创建、可选参数众多");
    }

    /**
     * Q5: 链式调用建造者的优势？
     */
    public void fluentBuilderAdvantages() {
        System.out.println("链式建造者：API友好、可读性强、类型安全、编译时检查");
    }
}