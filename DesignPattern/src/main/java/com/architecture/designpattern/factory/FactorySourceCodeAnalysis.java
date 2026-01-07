package com.architecture.designpattern.factory;

import org.springframework.stereotype.Component;

@Component
public class FactorySourceCodeAnalysis {
    public void analyzeSimpleFactory() { System.out.println("简单工厂：一个工厂类负责创建所有产品"); }
    public void analyzeFactoryMethod() { System.out.println("工厂方法：每个产品对应一个工厂类"); }
    public void analyzeCalendarGetInstance() { System.out.println("Calendar.getInstance：简单工厂模式应用"); }
    public void analyzeSpringBeanFactory() { System.out.println("Spring BeanFactory：IOC容器工厂实现"); }
    public void analyzeLoggerFactory() { System.out.println("LoggerFactory：SLF4J日志工厂"); }
}