package com.architecture.designpattern.proxy;

import org.springframework.stereotype.Component;

@Component
public class ProxySourceCodeAnalysis {
    public void analyzeStaticProxy() { System.out.println("静态代理：编译时确定，代理类与目标类一对一"); }
    public void analyzeJDKProxy() { System.out.println("JDK动态代理：基于接口，Proxy.newProxyInstance"); }
    public void analyzeCGLIBProxy() { System.out.println("CGLIB代理：基于继承，字节码生成，无需接口"); }
    public void analyzeSpringProxy() { System.out.println("Spring代理：ProxyFactory、AOP联盟、切面织入"); }
    public void analyzeMyBatisMapper() { System.out.println("MyBatis Mapper：动态代理生成DAO实现"); }
}