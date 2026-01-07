package com.architecture.designpattern.composite;

import java.util.ArrayList;
import java.util.List;

public class CompositeExample {
    
    public void demonstratePattern() {
        System.out.println("=== 组合模式演示 ===");
        
        // 文件系统演示
        System.out.println("1. 文件系统演示:");
        
        // 创建文件
        FileComponent file1 = new File("document.txt", 100);
        FileComponent file2 = new File("image.jpg", 500);
        FileComponent file3 = new File("video.mp4", 2000);
        
        // 创建目录
        Directory root = new Directory("根目录");
        Directory documents = new Directory("Documents");
        Directory pictures = new Directory("Pictures");
        
        // 构建文件树
        documents.add(file1);
        pictures.add(file2);
        pictures.add(file3);
        
        root.add(documents);
        root.add(pictures);
        
        // 显示文件树结构
        root.display(0);
        System.out.println("总大小: " + root.getSize() + " KB");
        
        // 组织结构演示
        System.out.println("\n2. 组织结构演示:");
        
        // 创建员工
        Employee ceo = new Employee("张总", "CEO", 100000);
        Employee cto = new Employee("李总", "CTO", 80000);
        Employee hr = new Employee("王经理", "HR经理", 50000);
        
        Employee dev1 = new Employee("小明", "开发工程师", 30000);
        Employee dev2 = new Employee("小红", "开发工程师", 32000);
        Employee tester = new Employee("小李", "测试工程师", 28000);
        
        // 构建组织结构
        ceo.add(cto);
        ceo.add(hr);
        
        cto.add(dev1);
        cto.add(dev2);
        cto.add(tester);
        
        // 显示组织结构
        ceo.printStructure(0);
        System.out.println("总薪资成本: " + ceo.getSalary());
        
        // 图形组合演示
        System.out.println("\n3. 图形组合演示:");
        
        // 创建基础图形
        Graphic line1 = new Line("直线1");
        Graphic line2 = new Line("直线2");
        Graphic circle1 = new Circle("圆形1");
        Graphic rectangle1 = new Rectangle("矩形1");
        
        // 创建复合图形
        CompositeGraphic house = new CompositeGraphic("房子");
        house.add(rectangle1); // 房屋主体
        house.add(new Circle("门把手"));
        
        CompositeGraphic picture = new CompositeGraphic("完整图片");
        picture.add(line1);
        picture.add(line2);
        picture.add(circle1);
        picture.add(house);
        
        // 绘制整个图形
        picture.draw();
    }
}

// 1. 文件系统示例
// 抽象文件组件
abstract class FileComponent {
    protected String name;
    
    public FileComponent(String name) {
        this.name = name;
    }
    
    public abstract void display(int depth);
    public abstract int getSize();
    
    // 默认实现，叶子节点会抛出异常
    public void add(FileComponent component) {
        throw new UnsupportedOperationException("叶子节点不支持添加操作");
    }
    
    public void remove(FileComponent component) {
        throw new UnsupportedOperationException("叶子节点不支持删除操作");
    }
}

// 文件类（叶子）
class File extends FileComponent {
    private int size;
    
    public File(String name, int size) {
        super(name);
        this.size = size;
    }
    
    @Override
    public void display(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        System.out.println(indent + "📄 " + name + " (" + size + " KB)");
    }
    
    @Override
    public int getSize() {
        return size;
    }
}

// 目录类（组合）
class Directory extends FileComponent {
    private List<FileComponent> children = new ArrayList<>();
    
    public Directory(String name) {
        super(name);
    }
    
    @Override
    public void add(FileComponent component) {
        children.add(component);
    }
    
    @Override
    public void remove(FileComponent component) {
        children.remove(component);
    }
    
    @Override
    public void display(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        System.out.println(indent + "📁 " + name + "/");
        
        for (FileComponent child : children) {
            child.display(depth + 1);
        }
    }
    
    @Override
    public int getSize() {
        int totalSize = 0;
        for (FileComponent child : children) {
            totalSize += child.getSize();
        }
        return totalSize;
    }
}

// 2. 组织结构示例
class Employee {
    private String name;
    private String position;
    private int salary;
    private List<Employee> subordinates = new ArrayList<>();
    
    public Employee(String name, String position, int salary) {
        this.name = name;
        this.position = position;
        this.salary = salary;
    }
    
    public void add(Employee employee) {
        subordinates.add(employee);
    }
    
    public void remove(Employee employee) {
        subordinates.remove(employee);
    }
    
    public void printStructure(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        System.out.println(indent + "👤 " + name + " (" + position + ") - 薪资: " + salary);
        
        for (Employee subordinate : subordinates) {
            subordinate.printStructure(depth + 1);
        }
    }
    
    public int getSalary() {
        int totalSalary = salary;
        for (Employee subordinate : subordinates) {
            totalSalary += subordinate.getSalary();
        }
        return totalSalary;
    }
    
    public String getName() { return name; }
    public String getPosition() { return position; }
}

// 3. 图形组合示例
// 抽象图形组件
interface Graphic {
    void draw();
}

// 基础图形（叶子）
class Line implements Graphic {
    private String name;
    
    public Line(String name) {
        this.name = name;
    }
    
    @Override
    public void draw() {
        System.out.println("绘制线条: " + name);
    }
}

class Circle implements Graphic {
    private String name;
    
    public Circle(String name) {
        this.name = name;
    }
    
    @Override
    public void draw() {
        System.out.println("绘制圆形: " + name);
    }
}

class Rectangle implements Graphic {
    private String name;
    
    public Rectangle(String name) {
        this.name = name;
    }
    
    @Override
    public void draw() {
        System.out.println("绘制矩形: " + name);
    }
}

// 复合图形（组合）
class CompositeGraphic implements Graphic {
    private String name;
    private List<Graphic> graphics = new ArrayList<>();
    
    public CompositeGraphic(String name) {
        this.name = name;
    }
    
    public void add(Graphic graphic) {
        graphics.add(graphic);
    }
    
    public void remove(Graphic graphic) {
        graphics.remove(graphic);
    }
    
    @Override
    public void draw() {
        System.out.println("开始绘制复合图形: " + name);
        for (Graphic graphic : graphics) {
            graphic.draw();
        }
        System.out.println("完成绘制复合图形: " + name);
    }
}