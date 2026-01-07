package com.architecture.designpattern.facade;

import org.springframework.stereotype.Component;

@Component
public class FacadeSourceCodeAnalysis {
    public void analyzeSpringContext() { System.out.println("Spring ApplicationContext：统一的容器访问接口"); }
    public void analyzeJDBCUtils() { System.out.println("JDBC工具类：封装复杂的数据库操作"); }
    public void analyzeSLF4JLogger() { System.out.println("SLF4J Logger：统一日志框架接口"); }
    public void analyzeRestTemplate() { System.out.println("RestTemplate：HTTP客户端操作外观"); }
    public void analyzeServiceLayer() { System.out.println("Service层：业务逻辑外观，封装复杂操作"); }
}