package com.architecture.designpattern.composite;

import org.springframework.stereotype.Component;

@Component
public class CompositeSourceCodeAnalysis {
    public void analyzeSwingComponent() { System.out.println("Swing组件：Component基类，Container组合容器"); }
    public void analyzeFileSystem() { System.out.println("文件系统：File类，目录和文件统一接口"); }
    public void analyzeSpringBeanDefinition() { System.out.println("Spring Bean定义：组合Bean配置，嵌套结构"); }
    public void analyzeJUnitTestSuite() { System.out.println("JUnit测试套件：Test接口，TestCase和TestSuite组合"); }
    public void analyzeXMLDOM() { System.out.println("XML DOM：Node接口，Element和Text组合树结构"); }
}