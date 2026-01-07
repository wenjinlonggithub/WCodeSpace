package com.architecture.designpattern.factory;

public class FactoryExample {
    
    public void demonstratePattern() {
        System.out.println("=== 工厂模式演示 ===");
        
        // 简单工厂模式
        System.out.println("1. 简单工厂模式:");
        Product product1 = SimpleFactory.createProduct("A");
        product1.use();
        
        Product product2 = SimpleFactory.createProduct("B");
        product2.use();
        
        // 工厂方法模式
        System.out.println("\n2. 工厂方法模式:");
        Creator creator1 = new ConcreteCreatorA();
        Product product3 = creator1.factoryMethod();
        product3.use();
        
        Creator creator2 = new ConcreteCreatorB();
        Product product4 = creator2.factoryMethod();
        product4.use();
        
        // 抽象工厂模式
        System.out.println("\n3. 抽象工厂模式:");
        AbstractFactory windowsFactory = new WindowsFactory();
        Button windowsButton = windowsFactory.createButton();
        TextField windowsTextField = windowsFactory.createTextField();
        windowsButton.click();
        windowsTextField.input("Windows文本");
        
        AbstractFactory macFactory = new MacFactory();
        Button macButton = macFactory.createButton();
        TextField macTextField = macFactory.createTextField();
        macButton.click();
        macTextField.input("Mac文本");
    }
}

// 产品接口
interface Product {
    void use();
}

// 具体产品A
class ConcreteProductA implements Product {
    @Override
    public void use() {
        System.out.println("使用产品A");
    }
}

// 具体产品B
class ConcreteProductB implements Product {
    @Override
    public void use() {
        System.out.println("使用产品B");
    }
}

// 简单工厂
class SimpleFactory {
    public static Product createProduct(String type) {
        switch (type.toUpperCase()) {
            case "A":
                return new ConcreteProductA();
            case "B":
                return new ConcreteProductB();
            default:
                throw new IllegalArgumentException("未知产品类型: " + type);
        }
    }
}

// 工厂方法模式 - 抽象创建者
abstract class Creator {
    public abstract Product factoryMethod();
    
    public void someOperation() {
        Product product = factoryMethod();
        product.use();
    }
}

// 具体创建者A
class ConcreteCreatorA extends Creator {
    @Override
    public Product factoryMethod() {
        return new ConcreteProductA();
    }
}

// 具体创建者B
class ConcreteCreatorB extends Creator {
    @Override
    public Product factoryMethod() {
        return new ConcreteProductB();
    }
}

// 抽象工厂模式 - 产品族
interface Button {
    void click();
}

interface TextField {
    void input(String text);
}

// Windows产品族
class WindowsButton implements Button {
    @Override
    public void click() {
        System.out.println("Windows按钮被点击");
    }
}

class WindowsTextField implements TextField {
    @Override
    public void input(String text) {
        System.out.println("Windows文本框输入: " + text);
    }
}

// Mac产品族
class MacButton implements Button {
    @Override
    public void click() {
        System.out.println("Mac按钮被点击");
    }
}

class MacTextField implements TextField {
    @Override
    public void input(String text) {
        System.out.println("Mac文本框输入: " + text);
    }
}

// 抽象工厂
interface AbstractFactory {
    Button createButton();
    TextField createTextField();
}

// Windows工厂
class WindowsFactory implements AbstractFactory {
    @Override
    public Button createButton() {
        return new WindowsButton();
    }
    
    @Override
    public TextField createTextField() {
        return new WindowsTextField();
    }
}

// Mac工厂
class MacFactory implements AbstractFactory {
    @Override
    public Button createButton() {
        return new MacButton();
    }
    
    @Override
    public TextField createTextField() {
        return new MacTextField();
    }
}