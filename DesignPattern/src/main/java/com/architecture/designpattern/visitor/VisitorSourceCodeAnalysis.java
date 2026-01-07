package com.architecture.designpattern.visitor;

import org.springframework.stereotype.Component;

@Component
public class VisitorSourceCodeAnalysis {

    /**
     * ====================
     * 访问者模式源码分析
     * ====================
     */

    /**
     * 1. 基础访问者实现
     */
    public void analyzeBasicImplementation() {
        System.out.println("基础实现：双分派机制，操作与数据结构分离");
    }

    /**
     * 2. Java NIO Files.walkFileTree
     */
    public void analyzeNIOFileVisitor() {
        System.out.println("NIO FileVisitor：文件树遍历，访问者模式应用");
    }

    /**
     * 3. 编译器语法树遍历
     */
    public void analyzeCompilerASTVisitor() {
        System.out.println("编译器AST：语法分析、语义分析、代码生成");
    }

    /**
     * 4. Spring BeanDefinition访问者
     */
    public void analyzeSpringBeanDefinitionVisitor() {
        System.out.println("Spring BeanVisitor：Bean定义遍历，属性处理");
    }

    /**
     * 5. 性能优化的访问者
     */
    public void analyzeOptimizedVisitor() {
        System.out.println("性能优化：访问者缓存，并行访问，懒加载");
    }
}