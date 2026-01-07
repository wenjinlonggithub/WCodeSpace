package com.architecture.designpattern.templatemethod;

import org.springframework.stereotype.Component;

@Component
public class TemplateMethodInterviewQuestions {
    public void whatIsTemplateMethod() { System.out.println("模板方法模式：定义算法骨架，让子类重定义算法的某些特定步骤"); }
    public void coreComponents() { System.out.println("核心组件：抽象模板、具体实现类、钩子方法"); }
    public void applicableScenarios() { System.out.println("适用场景：算法框架、数据处理流程、测试框架、Spring模板"); }
    public void hookMethods() { System.out.println("钩子方法：可选重写、控制算法流程、提供扩展点"); }
    public void vsStrategyPattern() { System.out.println("与策略模式区别：模板方法控制算法骨架，策略封装算法"); }
}