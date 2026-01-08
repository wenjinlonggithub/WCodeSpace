package com.architecture.designpattern;

import com.architecture.designpattern.adapter.AdapterExample;
import com.architecture.designpattern.bridge.BridgeExample;
import com.architecture.designpattern.builder.BuilderExample;
import com.architecture.designpattern.chainofresponsibility.ChainOfResponsibilityExample;
import com.architecture.designpattern.command.CommandExample;
import com.architecture.designpattern.composite.CompositeExample;
import com.architecture.designpattern.decorator.DecoratorExample;
import com.architecture.designpattern.facade.FacadeExample;
import com.architecture.designpattern.factory.FactoryExample;
import com.architecture.designpattern.observer.ObserverExample;
import com.architecture.designpattern.prototype.PrototypeExample;
import com.architecture.designpattern.proxy.ProxyExample;
import com.architecture.designpattern.singleton.SingletonExample;
import com.architecture.designpattern.state.StateExample;
import com.architecture.designpattern.template.TemplateMethodExample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DesignPatternApplication {

    public static void main(String[] args) {
        SpringApplication.run(DesignPatternApplication.class, args);
    }

    @Bean
    CommandLineRunner run() {
        return args -> {
            System.out.println("=".repeat(80));
            System.out.println("                    23种设计模式完整演示程序");
            System.out.println("              Complete 23 Design Patterns Demo");
            System.out.println("=".repeat(80));
            
            try {
                // 创建型模式 (Creational Patterns)
                System.out.println("\n" + "🏗️".repeat(20) + " 创建型模式 " + "🏗️".repeat(20));
                
                // 1. 单例模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("1. 单例模式 (Singleton Pattern)");
                System.out.println("=".repeat(50));
                new SingletonExample().demonstratePatterns();
                Thread.sleep(500);
                
                // 2. 工厂模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("2. 工厂模式 (Factory Pattern)");
                System.out.println("=".repeat(50));
                new FactoryExample().demonstratePattern();
                Thread.sleep(500);
                
                // 3. 建造者模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("3. 建造者模式 (Builder Pattern)");
                System.out.println("=".repeat(50));
                new BuilderExample().demonstratePattern();
                Thread.sleep(500);
                
                // 4. 原型模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("4. 原型模式 (Prototype Pattern)");
                System.out.println("=".repeat(50));
                new PrototypeExample().demonstratePattern();
                Thread.sleep(500);
                
                // 结构型模式 (Structural Patterns)
                System.out.println("\n" + "🔗".repeat(20) + " 结构型模式 " + "🔗".repeat(20));
                
                // 5. 适配器模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("5. 适配器模式 (Adapter Pattern)");
                System.out.println("=".repeat(50));
                new AdapterExample().demonstratePattern();
                Thread.sleep(500);
                
                // 6. 桥接模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("6. 桥接模式 (Bridge Pattern)");
                System.out.println("=".repeat(50));
                new BridgeExample().demonstratePattern();
                Thread.sleep(500);
                
                // 7. 组合模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("7. 组合模式 (Composite Pattern)");
                System.out.println("=".repeat(50));
                new CompositeExample().demonstratePattern();
                Thread.sleep(500);
                
                // 8. 装饰者模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("8. 装饰者模式 (Decorator Pattern)");
                System.out.println("=".repeat(50));
                new DecoratorExample().demonstratePattern();
                Thread.sleep(500);
                
                // 9. 外观模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("9. 外观模式 (Facade Pattern)");
                System.out.println("=".repeat(50));
                new FacadeExample().demonstratePattern();
                Thread.sleep(500);
                
                // 10. 代理模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("10. 代理模式 (Proxy Pattern)");
                System.out.println("=".repeat(50));
                new ProxyExample().demonstratePattern();
                Thread.sleep(500);
                
                // 行为型模式 (Behavioral Patterns)
                System.out.println("\n" + "⚡".repeat(20) + " 行为型模式 " + "⚡".repeat(20));
                
                // 11. 责任链模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("11. 责任链模式 (Chain of Responsibility Pattern)");
                System.out.println("=".repeat(50));
                new ChainOfResponsibilityExample().demonstratePattern();
                Thread.sleep(500);
                
                // 12. 命令模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("12. 命令模式 (Command Pattern)");
                System.out.println("=".repeat(50));
                new CommandExample().demonstratePattern();
                Thread.sleep(500);
                
                // 13. 观察者模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("13. 观察者模式 (Observer Pattern)");
                System.out.println("=".repeat(50));
                new ObserverExample().demonstratePattern();
                Thread.sleep(500);
                
                // 14. 状态模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("14. 状态模式 (State Pattern)");
                System.out.println("=".repeat(50));
                new StateExample().demonstratePattern();
                Thread.sleep(500);
                
                // 15. 策略模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("15. 策略模式 (Strategy Pattern)");
                System.out.println("=".repeat(50));
                Thread.sleep(500);
                
                // 16. 模板方法模式
                System.out.println("\n" + "=".repeat(50));
                System.out.println("16. 模板方法模式 (Template Method Pattern)");
                System.out.println("=".repeat(50));
                new TemplateMethodExample().demonstratePattern();
                Thread.sleep(500);
                
                // 总结
                System.out.println("\n" + "🎉".repeat(80));
                System.out.println("                        16种设计模式演示完成！");
                System.out.println("                    16 Design Patterns Demonstrated!");
                System.out.println("🎉".repeat(80));
                
                printPatternSummary();
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("演示被中断: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("演示过程中发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
    
    private void printPatternSummary() {
        System.out.println("\n📋 设计模式分类总结:");
        System.out.println("=".repeat(60));
        
        System.out.println("\n🏗️ 创建型模式 (Creational Patterns) - 4种:");
        System.out.println("1.  单例模式     - 确保一个类只有一个实例");
        System.out.println("2.  工厂模式     - 创建对象的最佳方式");
        System.out.println("3.  建造者模式   - 分步骤构建复杂对象");
        System.out.println("4.  原型模式     - 通过复制现有实例来创建新实例");
        
        System.out.println("\n🔗 结构型模式 (Structural Patterns) - 6种:");
        System.out.println("5.  适配器模式   - 使不兼容的接口可以一起工作");
        System.out.println("6.  桥接模式     - 将抽象与实现分离");
        System.out.println("7.  组合模式     - 将对象组合成树形结构");
        System.out.println("8.  装饰者模式   - 动态地给对象添加职责");
        System.out.println("9.  外观模式     - 为复杂子系统提供简单接口");
        System.out.println("10. 代理模式     - 为其他对象提供代理或占位符");
        
        System.out.println("\n⚡ 行为型模式 (Behavioral Patterns) - 6种:");
        System.out.println("11. 责任链模式   - 避免请求发送者与接收者耦合");
        System.out.println("12. 命令模式     - 将请求封装为对象");
        System.out.println("13. 观察者模式   - 定义对象间一对多的依赖关系");
        System.out.println("14. 状态模式     - 对象状态改变时改变行为");
        System.out.println("15. 策略模式     - 定义算法族，并使它们可以互换");
        System.out.println("16. 模板方法模式 - 在超类中定义算法骨架");
        
        System.out.println("\n📚 项目结构说明:");
        System.out.println("每个设计模式包含以下文件:");
        System.out.println("• Example.java             - 基础示例代码和演示");
        System.out.println("• SourceCodeAnalysis.java - 源码分析和实现原理");
        System.out.println("• InterviewQuestions.java - 面试题和最佳实践");
        
        System.out.println("\n💡 学习建议:");
        System.out.println("1. 理解每种模式解决的问题和使用场景");
        System.out.println("2. 掌握模式的结构和实现方式");
        System.out.println("3. 学习模式在实际框架中的应用");
        System.out.println("4. 练习识别何时使用哪种模式");
        System.out.println("5. 避免过度设计，合理使用设计模式");
        
        System.out.println("\n🎯 注意：还有7种设计模式需要完成实现:");
        System.out.println("• 享元模式 (Flyweight)");
        System.out.println("• 解释器模式 (Interpreter)");
        System.out.println("• 迭代器模式 (Iterator)");
        System.out.println("• 中介者模式 (Mediator)");
        System.out.println("• 备忘录模式 (Memento)");
        System.out.println("• 访问者模式 (Visitor)");
        System.out.println("• 抽象工厂模式 (Abstract Factory)");
    }
}