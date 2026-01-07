package com.architecture.designpattern.proxy;

import org.springframework.stereotype.Component;

@Component
public class ProxyInterviewQuestions {
    public void whatIsProxy() { System.out.println("代理模式：为其他对象提供代理以控制对这个对象的访问"); }
    public void proxyTypes() { System.out.println("代理类型：静态代理、JDK动态代理、CGLIB代理"); }
    public void applicableScenarios() { System.out.println("适用场景：权限控制、缓存、延迟加载、日志记录"); }
    public void vsDecoratorPattern() { System.out.println("区别：代理控制访问，装饰器增强功能"); }
    public void springAOP() { System.out.println("Spring AOP：基于代理实现横切关注点"); }
}