package com.architecture.principles;

/**
 * SOLID原则实现示例
 * 
 * S - Single Responsibility Principle (单一职责原则)
 * O - Open/Closed Principle (开闭原则)
 * L - Liskov Substitution Principle (里氏替换原则)
 * I - Interface Segregation Principle (接口隔离原则)
 * D - Dependency Inversion Principle (依赖倒置原则)
 */

// ============= 单一职责原则 (SRP) =============

// 违反SRP的例子
class BadEmployee {
    private String name;
    private double salary;
    
    // 职责1: 员工数据管理
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setSalary(double salary) { this.salary = salary; }
    public double getSalary() { return salary; }
    
    // 职责2: 薪资计算 - 违反SRP
    public double calculatePay() {
        return salary * 12;
    }
    
    // 职责3: 数据持久化 - 违反SRP
    public void saveToDatabase() {
        System.out.println("Saving employee to database...");
    }
    
    // 职责4: 报表生成 - 违反SRP
    public void generateReport() {
        System.out.println("Generating employee report...");
    }
}

// 遵循SRP的例子
class Employee {
    private String name;
    private double salary;
    
    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }
    
    // 只负责员工数据管理
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setSalary(double salary) { this.salary = salary; }
    public double getSalary() { return salary; }
}

class PayrollCalculator {
    // 只负责薪资计算
    public double calculatePay(Employee employee) {
        return employee.getSalary() * 12;
    }
    
    public double calculateBonus(Employee employee) {
        return employee.getSalary() * 0.1;
    }
}

class EmployeeRepository {
    // 只负责数据持久化
    public void save(Employee employee) {
        System.out.println("Saving employee " + employee.getName() + " to database...");
    }
    
    public Employee findById(int id) {
        System.out.println("Finding employee by id: " + id);
        return new Employee("John Doe", 50000);
    }
}

class EmployeeReportGenerator {
    // 只负责报表生成
    public void generateReport(Employee employee) {
        System.out.println("=== Employee Report ===");
        System.out.println("Name: " + employee.getName());
        System.out.println("Salary: $" + employee.getSalary());
    }
    
    public void generatePayrollReport(Employee employee, PayrollCalculator calculator) {
        System.out.println("=== Payroll Report ===");
        System.out.println("Name: " + employee.getName());
        System.out.println("Annual Pay: $" + calculator.calculatePay(employee));
        System.out.println("Bonus: $" + calculator.calculateBonus(employee));
    }
}

// ============= 开闭原则 (OCP) =============

// 抽象基类
abstract class Shape {
    public abstract double calculateArea();
    public abstract void draw();
}

// 具体实现类
class Rectangle extends Shape {
    private double width;
    private double height;
    
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return width * height;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing Rectangle: " + width + "x" + height);
    }
}

class Circle extends Shape {
    private double radius;
    
    public Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing Circle with radius: " + radius);
    }
}

// 新增形状时不需要修改现有代码
class Triangle extends Shape {
    private double base;
    private double height;
    
    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return 0.5 * base * height;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing Triangle: base=" + base + ", height=" + height);
    }
}

// 形状处理器 - 对扩展开放，对修改关闭
class ShapeProcessor {
    public void processShapes(Shape[] shapes) {
        double totalArea = 0;
        for (Shape shape : shapes) {
            shape.draw();
            totalArea += shape.calculateArea();
        }
        System.out.println("Total area: " + totalArea);
    }
}

public class SOLIDPrinciples {
    public static void main(String[] args) {
        System.out.println("=== Single Responsibility Principle Demo ===");
        
        Employee employee = new Employee("Alice Johnson", 60000);
        PayrollCalculator calculator = new PayrollCalculator();
        EmployeeRepository repository = new EmployeeRepository();
        EmployeeReportGenerator reportGenerator = new EmployeeReportGenerator();
        
        repository.save(employee);
        reportGenerator.generatePayrollReport(employee, calculator);
        
        System.out.println("\n=== Open/Closed Principle Demo ===");
        
        Shape[] shapes = {
            new Rectangle(5, 10),
            new Circle(7),
            new Triangle(6, 8)
        };
        
        ShapeProcessor processor = new ShapeProcessor();
        processor.processShapes(shapes);
    }
}