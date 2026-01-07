package com.architecture.designpattern.prototype;

import java.util.ArrayList;
import java.util.List;

public class PrototypeExample {
    
    public void demonstratePattern() {
        System.out.println("=== 原型模式演示 ===");
        
        // 浅克隆演示
        System.out.println("1. 浅克隆演示:");
        ShallowPrototype original = new ShallowPrototype("原始对象", new ArrayList<>());
        original.getList().add("数据1");
        original.getList().add("数据2");
        
        try {
            ShallowPrototype cloned = original.clone();
            System.out.println("原始对象: " + original);
            System.out.println("克隆对象: " + cloned);
            System.out.println("是否是同一个对象: " + (original == cloned));
            System.out.println("list是否是同一个对象: " + (original.getList() == cloned.getList()));
            
            // 修改克隆对象的list
            cloned.getList().add("数据3");
            System.out.println("修改克隆对象后:");
            System.out.println("原始对象list: " + original.getList());
            System.out.println("克隆对象list: " + cloned.getList());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        
        // 深克隆演示
        System.out.println("\n2. 深克隆演示:");
        DeepPrototype deepOriginal = new DeepPrototype("深克隆原始", new ArrayList<>());
        deepOriginal.getList().add("深克隆数据1");
        deepOriginal.getList().add("深克隆数据2");
        
        try {
            DeepPrototype deepCloned = deepOriginal.clone();
            System.out.println("原始对象: " + deepOriginal);
            System.out.println("克隆对象: " + deepCloned);
            System.out.println("是否是同一个对象: " + (deepOriginal == deepCloned));
            System.out.println("list是否是同一个对象: " + (deepOriginal.getList() == deepCloned.getList()));
            
            // 修改克隆对象的list
            deepCloned.getList().add("深克隆数据3");
            System.out.println("修改克隆对象后:");
            System.out.println("原始对象list: " + deepOriginal.getList());
            System.out.println("克隆对象list: " + deepCloned.getList());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        
        // 原型管理器演示
        System.out.println("\n3. 原型管理器演示:");
        PrototypeManager manager = new PrototypeManager();
        
        // 注册原型
        manager.addPrototype("circle", new Circle(5, "红色"));
        manager.addPrototype("rectangle", new Rectangle(10, 20, "蓝色"));
        
        try {
            // 克隆并使用
            Shape circle1 = manager.getPrototype("circle");
            Shape circle2 = manager.getPrototype("circle");
            Shape rectangle1 = manager.getPrototype("rectangle");
            
            circle1.draw();
            circle2.draw();
            rectangle1.draw();
            
            System.out.println("circle1 == circle2: " + (circle1 == circle2));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}

// 浅克隆示例
class ShallowPrototype implements Cloneable {
    private String name;
    private List<String> list;
    
    public ShallowPrototype(String name, List<String> list) {
        this.name = name;
        this.list = list;
    }
    
    @Override
    protected ShallowPrototype clone() throws CloneNotSupportedException {
        return (ShallowPrototype) super.clone(); // 浅克隆
    }
    
    public List<String> getList() { return list; }
    public String getName() { return name; }
    
    @Override
    public String toString() {
        return "ShallowPrototype{name='" + name + "', list=" + list + "}";
    }
}

// 深克隆示例
class DeepPrototype implements Cloneable {
    private String name;
    private List<String> list;
    
    public DeepPrototype(String name, List<String> list) {
        this.name = name;
        this.list = list;
    }
    
    @Override
    protected DeepPrototype clone() throws CloneNotSupportedException {
        DeepPrototype cloned = (DeepPrototype) super.clone();
        // 深克隆：手动克隆引用对象
        cloned.list = new ArrayList<>(this.list);
        return cloned;
    }
    
    public List<String> getList() { return list; }
    public String getName() { return name; }
    
    @Override
    public String toString() {
        return "DeepPrototype{name='" + name + "', list=" + list + "}";
    }
}

// 形状抽象类
abstract class Shape implements Cloneable {
    protected String color;
    
    public Shape(String color) {
        this.color = color;
    }
    
    public abstract void draw();
    
    @Override
    protected Shape clone() throws CloneNotSupportedException {
        return (Shape) super.clone();
    }
}

// 圆形
class Circle extends Shape {
    private int radius;
    
    public Circle(int radius, String color) {
        super(color);
        this.radius = radius;
    }
    
    @Override
    public void draw() {
        System.out.println("绘制半径为 " + radius + " 的 " + color + " 圆形");
    }
    
    @Override
    protected Circle clone() throws CloneNotSupportedException {
        return (Circle) super.clone();
    }
}

// 矩形
class Rectangle extends Shape {
    private int width;
    private int height;
    
    public Rectangle(int width, int height, String color) {
        super(color);
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void draw() {
        System.out.println("绘制 " + width + "x" + height + " 的 " + color + " 矩形");
    }
    
    @Override
    protected Rectangle clone() throws CloneNotSupportedException {
        return (Rectangle) super.clone();
    }
}

// 原型管理器
class PrototypeManager {
    private java.util.Map<String, Shape> prototypes = new java.util.HashMap<>();
    
    public void addPrototype(String key, Shape prototype) {
        prototypes.put(key, prototype);
    }
    
    public Shape getPrototype(String key) throws CloneNotSupportedException {
        Shape prototype = prototypes.get(key);
        if (prototype != null) {
            return prototype.clone();
        }
        return null;
    }
}