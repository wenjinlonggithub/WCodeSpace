package com.architecture.designpattern.factory;

import org.springframework.stereotype.Component;

@Component
public class FactoryInterviewQuestions {
    public void whatIsFactory() { System.out.println("工厂模式：定义创建对象的接口，让子类决定实例化哪个类"); }
    public void factoryTypes() { System.out.println("工厂类型：简单工厂、工厂方法、抽象工厂"); }
    public void applicableScenarios() { System.out.println("适用场景：对象创建复杂、需要解耦创建和使用"); }
    public void vsBuilderPattern() { System.out.println("区别：工厂创建对象，建造者构建复杂对象"); }
    public void springBeanFactory() { System.out.println("Spring应用：BeanFactory、FactoryBean、IOC容器"); }
}