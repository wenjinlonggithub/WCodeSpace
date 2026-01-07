package com.architecture.designpattern.prototype;

import org.springframework.stereotype.Component;

@Component
public class PrototypeInterviewQuestions {
    public void whatIsPrototype() { System.out.println("原型模式：用原型实例指定创建对象的种类，通过拷贝这些原型创建新对象"); }
    public void shallowVsDeepCopy() { System.out.println("浅拷贝vs深拷贝：引用拷贝vs对象拷贝"); }
    public void applicableScenarios() { System.out.println("适用场景：对象创建成本高、需要大量相似对象、动态配置"); }
    public void cloneableInterface() { System.out.println("Cloneable接口：标记接口，Object.clone()方法"); }
    public void serializationClone() { System.out.println("序列化克隆：深拷贝实现，性能相对较低"); }
}