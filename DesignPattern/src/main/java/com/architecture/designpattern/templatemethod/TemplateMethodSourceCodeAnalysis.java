package com.architecture.designpattern.templatemethod;

import org.springframework.stereotype.Component;

@Component
public class TemplateMethodSourceCodeAnalysis {
    public void analyzeAbstractList() { System.out.println("AbstractList：定义List操作模板，子类实现具体方法"); }
    public void analyzeHttpServlet() { System.out.println("HttpServlet：service方法模板，doGet/doPost具体实现"); }
    public void analyzeSpringJdbcTemplate() { System.out.println("JdbcTemplate：数据库操作模板，execute方法框架"); }
    public void analyzeInputStream() { System.out.println("InputStream：read()抽象方法，read(byte[])模板方法"); }
    public void analyzeJUnitTestCase() { System.out.println("JUnit TestCase：setUp/tearDown模板，测试生命周期"); }
}