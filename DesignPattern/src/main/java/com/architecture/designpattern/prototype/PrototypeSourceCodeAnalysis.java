package com.architecture.designpattern.prototype;

import org.springframework.stereotype.Component;

@Component
public class PrototypeSourceCodeAnalysis {
    public void analyzeObjectClone() { System.out.println("Object.clone()：native方法，浅拷贝实现"); }
    public void analyzeArrayListClone() { System.out.println("ArrayList.clone()：浅拷贝，元素引用共享"); }
    public void analyzeSpringBeanPrototype() { System.out.println("Spring原型Bean：每次获取创建新实例"); }
    public void analyzeCopyConstructor() { System.out.println("拷贝构造器：自定义拷贝逻辑，类型安全"); }
    public void analyzeBuilderPattern() { System.out.println("建造者模式：结合原型模式，复制和定制对象"); }
}