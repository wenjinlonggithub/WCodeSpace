package com.architecture.principles;

import java.util.List;
import java.util.ArrayList;

/**
 * 里氏替换原则 (LSP) 和 接口隔离原则 (ISP) 示例
 * 
 * 背景：
 * SOLID原则是面向对象设计的五大基本原则，由Robert C. Martin在2000年提出。
 * LSP和ISP是其中的重要组成部分，旨在提高代码的可维护性、可扩展性和可重用性。
 * 
 * 历史：
 * - 里氏替换原则(LSP)：1987年由Barbara Liskov提出，1994年与Jeannette Wing共同完善
 * - 接口隔离原则(ISP)：1996年由Robert C. Martin提出，作为SOLID原则的一部分
 * 
 * 里氏替换原则 (Liskov Substitution Principle - LSP)：
 * 原理：子类对象必须能够替换其基类对象，而不破坏程序的正确性
 * 核心思想：继承关系中，子类应该能够完全替代父类，且不改变程序的预期行为
 * 
 * 价值与意义：
 * 1. 确保继承关系的正确性和合理性
 * 2. 提高代码的可靠性和稳定性
 * 3. 支持多态性的正确实现
 * 4. 降低系统耦合度，提高可维护性
 * 
 * 接口隔离原则 (Interface Segregation Principle - ISP)：
 * 原理：客户端不应该依赖它不需要的接口，应该将大接口拆分为多个小接口
 * 核心思想：接口应该小而专一，避免"胖接口"
 * 
 * 价值与意义：
 * 1. 降低接口间的耦合度
 * 2. 提高系统的灵活性和可维护性
 * 3. 遵循单一职责原则
 * 4. 减少不必要的依赖
 * 
 * 常见问题：
 * LSP违反问题：
 * - 子类抛出父类方法未声明的异常
 * - 子类加强了前置条件或弱化了后置条件
 * - 子类修改了父类方法的预期行为
 * 
 * ISP违反问题：
 * - 创建过于庞大的接口（胖接口）
 * - 强迫客户端实现不需要的方法
 * - 接口职责不单一，包含多种不相关的功能
 */

// ============= 里氏替换原则 (LSP) =============

// 违反LSP的例子 - 经典的鸟类继承问题
// 问题：企鹅是鸟类，但不能飞行，违反了LSP原则
// 当我们用企鹅对象替换鸟类对象时，程序会抛出异常，破坏了程序的正确性
class Bird {
    public void fly() {
        System.out.println("Bird is flying");
    }
}

class Penguin extends Bird {
    @Override
    public void fly() {
        // 企鹅不能飞，违反了LSP
        // 这里抛出异常改变了父类方法的预期行为
        throw new UnsupportedOperationException("Penguins cannot fly!");
    }
}

// 遵循LSP的例子 - 正确的设计方案
// 解决方案：将飞行能力从基类中分离出来，使用接口来表示特定能力
// 这样每个子类只需要实现自己具备的能力，不会违反LSP原则
abstract class BirdLSP {
    public abstract void eat();
    public abstract void sleep();
}

// 将特定能力抽象为接口，遵循ISP原则
interface Flyable {
    void fly();
}

interface Swimmable {
    void swim();
}

// 老鹰：既是鸟类，又具备飞行能力
class Eagle extends BirdLSP implements Flyable {
    @Override
    public void eat() {
        System.out.println("Eagle is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Eagle is sleeping");
    }
    
    @Override
    public void fly() {
        System.out.println("Eagle is flying high");
    }
}

// 企鹅：是鸟类，但只具备游泳能力，不会飞行
class PenguinLSP extends BirdLSP implements Swimmable {
    @Override
    public void eat() {
        System.out.println("Penguin is eating fish");
    }
    
    @Override
    public void sleep() {
        System.out.println("Penguin is sleeping");
    }
    
    @Override
    public void swim() {
        System.out.println("Penguin is swimming");
    }
}

// 鸭子：既能飞又能游泳的鸟类
class Duck extends BirdLSP implements Flyable, Swimmable {
    @Override
    public void eat() {
        System.out.println("Duck is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Duck is sleeping");
    }
    
    @Override
    public void fly() {
        System.out.println("Duck is flying");
    }
    
    @Override
    public void swim() {
        System.out.println("Duck is swimming");
    }
}

// ============= 接口隔离原则 (ISP) =============

// 违反ISP的例子 - 胖接口问题
// 问题：这个接口包含了太多不相关的职责，强迫实现类实现不需要的方法
// 例如：普通工人不需要编程、设计和管理能力，但被迫实现这些方法
interface BadWorker {
    void work();
    void eat();
    void sleep();
    void code();      // 不是所有工人都会编程
    void design();    // 不是所有工人都会设计
    void manage();    // 不是所有工人都会管理
}

// 遵循ISP的例子 - 接口隔离
// 解决方案：将大接口拆分为多个小接口，每个接口职责单一
// 基础工人接口 - 所有工人都需要的基本能力
interface Worker {
    void work();
    void eat();
    void sleep();
}

// 编程能力接口 - 只有程序员需要
interface Programmer {
    void code();
    void debug();
}

// 设计能力接口 - 只有设计师需要
interface Designer {
    void design();
    void createMockup();
}

// 管理能力接口 - 只有管理者需要
interface Manager {
    void manage();
    void planProject();
}

// 具体实现类只实现需要的接口，体现了ISP的优势

// 软件开发者：只需要工作和编程能力
class SoftwareDeveloper implements Worker, Programmer {
    @Override
    public void work() {
        System.out.println("Software developer is working");
    }
    
    @Override
    public void eat() {
        System.out.println("Software developer is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Software developer is sleeping");
    }
    
    @Override
    public void code() {
        System.out.println("Software developer is coding");
    }
    
    @Override
    public void debug() {
        System.out.println("Software developer is debugging");
    }
}

// UI设计师：只需要工作和设计能力
class UIDesigner implements Worker, Designer {
    @Override
    public void work() {
        System.out.println("UI designer is working");
    }
    
    @Override
    public void eat() {
        System.out.println("UI designer is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("UI designer is sleeping");
    }
    
    @Override
    public void design() {
        System.out.println("UI designer is designing");
    }
    
    @Override
    public void createMockup() {
        System.out.println("UI designer is creating mockup");
    }
}

// 项目经理：只需要工作和管理能力
class ProjectManager implements Worker, Manager {
    @Override
    public void work() {
        System.out.println("Project manager is working");
    }
    
    @Override
    public void eat() {
        System.out.println("Project manager is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Project manager is sleeping");
    }
    
    @Override
    public void manage() {
        System.out.println("Project manager is managing team");
    }
    
    @Override
    public void planProject() {
        System.out.println("Project manager is planning project");
    }
}

// 全栈开发者：具备多种能力，可以实现多个接口
// 这展示了ISP的灵活性 - 需要什么能力就实现什么接口
class FullStackDeveloper implements Worker, Programmer, Designer {
    @Override
    public void work() {
        System.out.println("Full-stack developer is working");
    }
    
    @Override
    public void eat() {
        System.out.println("Full-stack developer is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Full-stack developer is sleeping");
    }
    
    @Override
    public void code() {
        System.out.println("Full-stack developer is coding");
    }
    
    @Override
    public void debug() {
        System.out.println("Full-stack developer is debugging");
    }
    
    @Override
    public void design() {
        System.out.println("Full-stack developer is designing");
    }
    
    @Override
    public void createMockup() {
        System.out.println("Full-stack developer is creating mockup");
    }
}

/**
 * 演示类：展示LSP和ISP原则的实际应用
 * 
 * 实际案例分析：
 * 1. 电商系统中的支付方式设计
 * 2. 图形界面组件的事件处理
 * 3. 数据库连接池的实现
 * 4. 微服务架构中的接口设计
 */
public class LSPAndISPPrinciples {
    public static void main(String[] args) {
        System.out.println("=== Liskov Substitution Principle Demo ===");
        System.out.println("LSP确保子类可以完全替换父类，不破坏程序逻辑");
        
        // LSP演示：所有鸟类子类都可以替换父类使用
        List<BirdLSP> birds = new ArrayList<>();
        birds.add(new Eagle());
        birds.add(new PenguinLSP());
        birds.add(new Duck());
        
        // 所有鸟类都可以正常执行基本行为，符合LSP
        System.out.println("\n所有鸟类的基本行为：");
        for (BirdLSP bird : birds) {
            bird.eat();
            bird.sleep();
        }
        
        // 根据能力执行特定行为，体现了正确的继承设计
        List<Flyable> flyingBirds = new ArrayList<>();
        flyingBirds.add(new Eagle());
        flyingBirds.add(new Duck());
        
        System.out.println("\n会飞的鸟类：");
        for (Flyable bird : flyingBirds) {
            bird.fly();
        }
        
        List<Swimmable> swimmingBirds = new ArrayList<>();
        swimmingBirds.add(new PenguinLSP());
        swimmingBirds.add(new Duck());
        
        System.out.println("\n会游泳的鸟类：");
        for (Swimmable bird : swimmingBirds) {
            bird.swim();
        }
        
        System.out.println("\n=== Interface Segregation Principle Demo ===");
        System.out.println("ISP确保接口职责单一，客户端只依赖需要的接口");
        
        // ISP演示：每个角色只实现需要的接口
        System.out.println("\n软件开发者的工作：");
        SoftwareDeveloper developer = new SoftwareDeveloper();
        developer.work();
        developer.code();
        developer.debug();
        
        System.out.println("\nUI设计师的工作：");
        UIDesigner designer = new UIDesigner();
        designer.work();
        designer.design();
        designer.createMockup();
        
        System.out.println("\n项目经理的工作：");
        ProjectManager manager = new ProjectManager();
        manager.work();
        manager.manage();
        manager.planProject();
        
        System.out.println("\n全栈开发者的工作（多接口实现）：");
        FullStackDeveloper fullStack = new FullStackDeveloper();
        fullStack.work();
        fullStack.code();
        fullStack.design();
        
        System.out.println("\n=== 原则总结 ===");
        System.out.println("LSP: 子类必须能够替换父类，保证程序正确性");
        System.out.println("ISP: 接口应该小而专一，避免强迫客户端依赖不需要的功能");
        System.out.println("两个原则共同作用，提高代码的可维护性和可扩展性");
    }
}