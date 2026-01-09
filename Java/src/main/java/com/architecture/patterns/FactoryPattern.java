package com.architecture.patterns;

/**
 * 工厂模式实现
 * 
 * 核心原理：
 * 1. 定义创建对象的接口，让子类决定实例化哪个类
 * 2. 将对象的创建延迟到子类
 * 3. 符合开闭原则，易于扩展
 */

// 产品接口
interface Product {
    void use();
    String getType();
}

// 具体产品A
class ConcreteProductA implements Product {
    @Override
    public void use() {
        System.out.println("Using Product A");
    }
    
    @Override
    public String getType() {
        return "Product A";
    }
}

// 具体产品B
class ConcreteProductB implements Product {
    @Override
    public void use() {
        System.out.println("Using Product B");
    }
    
    @Override
    public String getType() {
        return "Product B";
    }
}

// 抽象工厂
abstract class Factory {
    public abstract Product createProduct();
    
    // 模板方法
    public final Product getProduct() {
        Product product = createProduct();
        System.out.println("Created: " + product.getType());
        return product;
    }
}

// 具体工厂A
class ConcreteFactoryA extends Factory {
    @Override
    public Product createProduct() {
        return new ConcreteProductA();
    }
}

// 具体工厂B
class ConcreteFactoryB extends Factory {
    @Override
    public Product createProduct() {
        return new ConcreteProductB();
    }
}

// 简单工厂（静态工厂）
class SimpleFactory {
    public static Product createProduct(String type) {
        switch (type.toLowerCase()) {
            case "a":
                return new ConcreteProductA();
            case "b":
                return new ConcreteProductB();
            default:
                throw new IllegalArgumentException("Unknown product type: " + type);
        }
    }
}

public class FactoryPattern {
    public static void main(String[] args) {
        // 工厂方法模式
        Factory factoryA = new ConcreteFactoryA();
        Product productA = factoryA.getProduct();
        productA.use();
        
        Factory factoryB = new ConcreteFactoryB();
        Product productB = factoryB.getProduct();
        productB.use();
        
        // 简单工厂模式
        Product simpleProductA = SimpleFactory.createProduct("a");
        simpleProductA.use();
        
        Product simpleProductB = SimpleFactory.createProduct("b");
        simpleProductB.use();
    }
}