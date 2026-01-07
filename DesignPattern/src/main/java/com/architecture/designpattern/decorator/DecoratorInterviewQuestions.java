package com.architecture.designpattern.decorator;

import org.springframework.stereotype.Component;

@Component
public class DecoratorInterviewQuestions {
    public void whatIsDecorator() { System.out.println("装饰器模式：动态地给对象添加一些额外的职责"); }
    public void coreComponents() { System.out.println("核心组件：组件接口、具体组件、装饰器、具体装饰器"); }
    public void vsInheritance() { System.out.println("与继承区别：运行时组合，避免类爆炸问题"); }
    public void applicableScenarios() { System.out.println("适用场景：IO流、UI组件、缓存装饰、日志装饰"); }
    public void chainedDecorators() { System.out.println("装饰器链：多层装饰，功能叠加，顺序影响结果"); }
}