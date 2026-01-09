package com.architecture.principles;

import java.util.List;
import java.util.ArrayList;

/**
 * 里氏替换原则 (LSP) 和 接口隔离原则 (ISP) 示例
 */

// ============= 里氏替换原则 (LSP) =============

// 违反LSP的例子
class Bird {
    public void fly() {
        System.out.println("Bird is flying");
    }
}

class Penguin extends Bird {
    @Override
    public void fly() {
        // 企鹅不能飞，违反了LSP
        throw new UnsupportedOperationException("Penguins cannot fly!");
    }
}

// 遵循LSP的例子
abstract class BirdLSP {
    public abstract void eat();
    public abstract void sleep();
}

interface Flyable {
    void fly();
}

interface Swimmable {
    void swim();
}

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

// 违反ISP的例子 - 胖接口
interface BadWorker {
    void work();
    void eat();
    void sleep();
    void code();      // 不是所有工人都会编程
    void design();    // 不是所有工人都会设计
    void manage();    // 不是所有工人都会管理
}

// 遵循ISP的例子 - 接口隔离
interface Worker {
    void work();
    void eat();
    void sleep();
}

interface Programmer {
    void code();
    void debug();
}

interface Designer {
    void design();
    void createMockup();
}

interface Manager {
    void manage();
    void planProject();
}

// 具体实现类只实现需要的接口
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

// 全栈开发者可以实现多个接口
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

public class LSPAndISPPrinciples {
    public static void main(String[] args) {
        System.out.println("=== Liskov Substitution Principle Demo ===");
        
        List<BirdLSP> birds = new ArrayList<>();
        birds.add(new Eagle());
        birds.add(new PenguinLSP());
        birds.add(new Duck());
        
        // 所有鸟类都可以正常执行基本行为
        for (BirdLSP bird : birds) {
            bird.eat();
            bird.sleep();
        }
        
        // 根据能力执行特定行为
        List<Flyable> flyingBirds = new ArrayList<>();
        flyingBirds.add(new Eagle());
        flyingBirds.add(new Duck());
        
        System.out.println("\nFlying birds:");
        for (Flyable bird : flyingBirds) {
            bird.fly();
        }
        
        List<Swimmable> swimmingBirds = new ArrayList<>();
        swimmingBirds.add(new PenguinLSP());
        swimmingBirds.add(new Duck());
        
        System.out.println("\nSwimming birds:");
        for (Swimmable bird : swimmingBirds) {
            bird.swim();
        }
        
        System.out.println("\n=== Interface Segregation Principle Demo ===");
        
        SoftwareDeveloper developer = new SoftwareDeveloper();
        developer.work();
        developer.code();
        developer.debug();
        
        UIDesigner designer = new UIDesigner();
        designer.work();
        designer.design();
        designer.createMockup();
        
        ProjectManager manager = new ProjectManager();
        manager.work();
        manager.manage();
        manager.planProject();
        
        FullStackDeveloper fullStack = new FullStackDeveloper();
        fullStack.work();
        fullStack.code();
        fullStack.design();
    }
}