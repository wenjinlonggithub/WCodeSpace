package com.architecture.designpattern.decorator;

import org.springframework.stereotype.Component;

@Component
public class DecoratorSourceCodeAnalysis {
    public void analyzeJavaIO() { System.out.println("Java IO流：InputStream基类，BufferedInputStream等装饰器"); }
    public void analyzeSpringCache() { System.out.println("Spring缓存：CacheManager装饰器，添加缓存功能"); }
    public void analyzeServletWrapper() { System.out.println("Servlet包装器：HttpServletRequestWrapper装饰请求"); }
    public void analyzeSpringTransaction() { System.out.println("Spring事务：TransactionTemplate装饰器模式"); }
    public void analyzeCollections() { System.out.println("Collections工具：synchronizedList等装饰器方法"); }
}