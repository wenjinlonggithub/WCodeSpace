package com.language.features;

import java.util.*;
import java.util.function.*;
import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * Java语言核心特性详解
 * 
 * 涵盖内容：
 * 1. 基本数据类型和包装类
 * 2. 字符串处理和内存优化
 * 3. 面向对象特性（封装、继承、多态）
 * 4. 内部类和匿名类
 * 5. 枚举类型详解
 * 6. 异常处理机制
 * 7. 泛型系统深入
 * 8. 注解系统
 */

public class JavaLanguageFeatures {
    
    /**
     * 基本数据类型和包装类详解
     * 
     * 重点：
     * 1. 8种基本数据类型的特点
     * 2. 自动装箱和拆箱机制
     * 3. 包装类的缓存机制
     * 4. 数值转换和精度问题
     */
    public static void demonstratePrimitiveTypes() {
        System.out.println("=== Primitive Types and Wrapper Classes ===");
        
        // 1. 基本数据类型范围
        System.out.println("\n--- Primitive Type Ranges ---");
        System.out.println("byte: " + Byte.MIN_VALUE + " to " + Byte.MAX_VALUE);
        System.out.println("short: " + Short.MIN_VALUE + " to " + Short.MAX_VALUE);
        System.out.println("int: " + Integer.MIN_VALUE + " to " + Integer.MAX_VALUE);
        System.out.println("long: " + Long.MIN_VALUE + " to " + Long.MAX_VALUE);
        System.out.println("float: " + Float.MIN_VALUE + " to " + Float.MAX_VALUE);
        System.out.println("double: " + Double.MIN_VALUE + " to " + Double.MAX_VALUE);
        System.out.println("char: " + (int)Character.MIN_VALUE + " to " + (int)Character.MAX_VALUE);
        
        // 2. 自动装箱和拆箱
        System.out.println("\n--- Autoboxing and Unboxing ---");
        Integer boxed = 100; // 自动装箱
        int unboxed = boxed; // 自动拆箱
        System.out.println("Boxed: " + boxed + ", Unboxed: " + unboxed);
        
        // 3. 包装类缓存机制（-128到127）
        System.out.println("\n--- Wrapper Class Caching ---");
        Integer a1 = 127;
        Integer a2 = 127;
        Integer b1 = 128;
        Integer b2 = 128;
        
        System.out.println("127 == 127: " + (a1 == a2)); // true - 缓存范围内
        System.out.println("128 == 128: " + (b1 == b2)); // false - 超出缓存范围
        System.out.println("127.equals(127): " + a1.equals(a2)); // true
        System.out.println("128.equals(128): " + b1.equals(b2)); // true
        
        // 4. 数值转换和精度问题
        System.out.println("\n--- Numeric Conversion and Precision ---");
        float f = 0.1f + 0.2f;
        double d = 0.1 + 0.2;
        System.out.println("0.1f + 0.2f = " + f);
        System.out.println("0.1 + 0.2 = " + d);
        System.out.println("Is equal to 0.3? " + (d == 0.3)); // false!
        
        // 使用BigDecimal进行精确计算
        java.math.BigDecimal bd1 = new java.math.BigDecimal("0.1");
        java.math.BigDecimal bd2 = new java.math.BigDecimal("0.2");
        java.math.BigDecimal bd3 = bd1.add(bd2);
        System.out.println("BigDecimal: 0.1 + 0.2 = " + bd3);
        
        // 5. 位运算操作
        System.out.println("\n--- Bitwise Operations ---");
        int x = 12; // 1100 in binary
        int y = 10; // 1010 in binary
        
        System.out.println("x = " + x + " (binary: " + Integer.toBinaryString(x) + ")");
        System.out.println("y = " + y + " (binary: " + Integer.toBinaryString(y) + ")");
        System.out.println("x & y = " + (x & y) + " (AND)");
        System.out.println("x | y = " + (x | y) + " (OR)");
        System.out.println("x ^ y = " + (x ^ y) + " (XOR)");
        System.out.println("~x = " + (~x) + " (NOT)");
        System.out.println("x << 1 = " + (x << 1) + " (Left shift)");
        System.out.println("x >> 1 = " + (x >> 1) + " (Right shift)");
    }
    
    /**
     * 字符串处理和内存优化
     * 
     * 重点：
     * 1. String不可变性原理
     * 2. 字符串常量池机制
     * 3. StringBuilder vs StringBuffer
     * 4. 字符串拼接优化
     * 5. 正则表达式应用
     */
    public static void demonstrateStringHandling() {
        System.out.println("\n=== String Handling and Memory Optimization ===");
        
        // 1. String不可变性演示
        System.out.println("\n--- String Immutability ---");
        String original = "Hello";
        String modified = original.concat(" World");
        System.out.println("Original: " + original); // 仍然是 "Hello"
        System.out.println("Modified: " + modified); // "Hello World"
        System.out.println("Same object? " + (original == modified)); // false
        
        // 2. 字符串常量池
        System.out.println("\n--- String Constant Pool ---");
        String s1 = "Java";
        String s2 = "Java";
        String s3 = new String("Java");
        String s4 = s3.intern();
        
        System.out.println("s1 == s2: " + (s1 == s2)); // true - 同一个常量池对象
        System.out.println("s1 == s3: " + (s1 == s3)); // false - s3在堆中
        System.out.println("s1 == s4: " + (s1 == s4)); // true - intern()返回常量池对象
        
        // 3. 字符串拼接性能对比
        System.out.println("\n--- String Concatenation Performance ---");
        int iterations = 10000;
        
        // String + 操作（编译器优化）
        long startTime = System.nanoTime();
        String result1 = "";
        for (int i = 0; i < iterations; i++) {
            result1 += "a"; // 每次创建新对象
        }
        long stringTime = System.nanoTime() - startTime;
        
        // StringBuilder
        startTime = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sb.append("a");
        }
        String result2 = sb.toString();
        long sbTime = System.nanoTime() - startTime;
        
        // StringBuffer（线程安全）
        startTime = System.nanoTime();
        StringBuffer sbf = new StringBuffer();
        for (int i = 0; i < iterations; i++) {
            sbf.append("a");
        }
        String result3 = sbf.toString();
        long sbfTime = System.nanoTime() - startTime;
        
        System.out.println("String concatenation: " + stringTime / 1_000_000.0 + " ms");
        System.out.println("StringBuilder: " + sbTime / 1_000_000.0 + " ms");
        System.out.println("StringBuffer: " + sbfTime / 1_000_000.0 + " ms");
        
        // 4. 字符串方法详解
        System.out.println("\n--- String Methods ---");
        String text = "  Hello, Java World!  ";
        System.out.println("Original: '" + text + "'");
        System.out.println("Length: " + text.length());
        System.out.println("Trimmed: '" + text.trim() + "'");
        System.out.println("Uppercase: " + text.toUpperCase());
        System.out.println("Lowercase: " + text.toLowerCase());
        System.out.println("Substring(2, 7): '" + text.substring(2, 7) + "'");
        System.out.println("Replace 'Java' with 'Python': " + text.replace("Java", "Python"));
        System.out.println("Contains 'Java': " + text.contains("Java"));
        System.out.println("Starts with '  Hello': " + text.startsWith("  Hello"));
        System.out.println("Ends with '!  ': " + text.endsWith("!  "));
        
        // 5. 字符串分割和连接
        System.out.println("\n--- String Split and Join ---");
        String csv = "apple,banana,cherry,date";
        String[] fruits = csv.split(",");
        System.out.println("Split result: " + Arrays.toString(fruits));
        
        String joined = String.join(" | ", fruits);
        System.out.println("Joined: " + joined);
        
        // 6. 正则表达式应用
        System.out.println("\n--- Regular Expressions ---");
        String email = "user@example.com";
        String emailPattern = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        boolean isValidEmail = email.matches(emailPattern);
        System.out.println("Email '" + email + "' is valid: " + isValidEmail);
        
        String phoneNumber = "123-456-7890";
        String phonePattern = "\\d{3}-\\d{3}-\\d{4}";
        boolean isValidPhone = phoneNumber.matches(phonePattern);
        System.out.println("Phone '" + phoneNumber + "' is valid: " + isValidPhone);
        
        // 替换操作
        String htmlText = "<p>Hello <b>World</b></p>";
        String plainText = htmlText.replaceAll("<[^>]+>", "");
        System.out.println("HTML: " + htmlText);
        System.out.println("Plain text: " + plainText);
    }
    
    /**
     * 面向对象特性详解
     * 
     * 重点：
     * 1. 封装（Encapsulation）
     * 2. 继承（Inheritance）
     * 3. 多态（Polymorphism）
     * 4. 抽象类和接口
     * 5. 方法重载和重写
     */
    
    // 封装示例
    static class BankAccount {
        private double balance; // 私有字段
        private String accountNumber;
        
        public BankAccount(String accountNumber, double initialBalance) {
            this.accountNumber = accountNumber;
            this.balance = Math.max(0, initialBalance); // 确保余额非负
        }
        
        // 受控访问方法
        public double getBalance() {
            return balance;
        }
        
        public String getAccountNumber() {
            return accountNumber;
        }
        
        public boolean deposit(double amount) {
            if (amount > 0) {
                balance += amount;
                return true;
            }
            return false;
        }
        
        public boolean withdraw(double amount) {
            if (amount > 0 && amount <= balance) {
                balance -= amount;
                return true;
            }
            return false;
        }
        
        @Override
        public String toString() {
            return "Account[" + accountNumber + "] Balance: $" + balance;
        }
    }
    
    // 继承示例
    static class SavingsAccount extends BankAccount {
        private double interestRate;
        
        public SavingsAccount(String accountNumber, double initialBalance, double interestRate) {
            super(accountNumber, initialBalance);
            this.interestRate = interestRate;
        }
        
        public void addInterest() {
            double interest = getBalance() * interestRate / 100;
            deposit(interest);
        }
        
        public double getInterestRate() {
            return interestRate;
        }
        
        @Override
        public String toString() {
            return super.toString() + " Interest Rate: " + interestRate + "%";
        }
    }
    
    // 抽象类示例
    static abstract class Shape {
        protected String color;
        
        public Shape(String color) {
            this.color = color;
        }
        
        // 抽象方法
        public abstract double calculateArea();
        public abstract double calculatePerimeter();
        
        // 具体方法
        public String getColor() {
            return color;
        }
        
        public void displayInfo() {
            System.out.println("Shape: " + getClass().getSimpleName() + 
                ", Color: " + color + 
                ", Area: " + calculateArea() + 
                ", Perimeter: " + calculatePerimeter());
        }
    }
    
    static class Circle extends Shape {
        private double radius;
        
        public Circle(String color, double radius) {
            super(color);
            this.radius = radius;
        }
        
        @Override
        public double calculateArea() {
            return Math.PI * radius * radius;
        }
        
        @Override
        public double calculatePerimeter() {
            return 2 * Math.PI * radius;
        }
    }
    
    static class Rectangle extends Shape {
        private double width, height;
        
        public Rectangle(String color, double width, double height) {
            super(color);
            this.width = width;
            this.height = height;
        }
        
        @Override
        public double calculateArea() {
            return width * height;
        }
        
        @Override
        public double calculatePerimeter() {
            return 2 * (width + height);
        }
    }
    
    // 接口示例
    interface Drawable {
        void draw();
        
        // 默认方法（JDK 8+）
        default void highlight() {
            System.out.println("Highlighting the drawable object");
        }
        
        // 静态方法（JDK 8+）
        static void printDrawingInfo() {
            System.out.println("Drawing interface - supports various drawable objects");
        }
    }
    
    interface Resizable {
        void resize(double factor);
    }
    
    // 实现多个接口
    static class DrawableCircle extends Circle implements Drawable, Resizable {
        public DrawableCircle(String color, double radius) {
            super(color, radius);
        }
        
        @Override
        public void draw() {
            System.out.println("Drawing a " + getColor() + " circle with radius " + 
                ((Circle)this).radius);
        }
        
        @Override
        public void resize(double factor) {
            ((Circle)this).radius *= factor;
            System.out.println("Resized circle, new radius: " + ((Circle)this).radius);
        }
    }
    
    public static void demonstrateOOP() {
        System.out.println("\n=== Object-Oriented Programming Features ===");
        
        // 1. 封装演示
        System.out.println("\n--- Encapsulation ---");
        BankAccount account = new BankAccount("ACC001", 1000.0);
        System.out.println("Initial: " + account);
        
        account.deposit(500.0);
        System.out.println("After deposit: " + account);
        
        boolean success = account.withdraw(200.0);
        System.out.println("Withdrawal success: " + success + ", " + account);
        
        // 2. 继承演示
        System.out.println("\n--- Inheritance ---");
        SavingsAccount savings = new SavingsAccount("SAV001", 2000.0, 2.5);
        System.out.println("Savings account: " + savings);
        
        savings.addInterest();
        System.out.println("After interest: " + savings);
        
        // 3. 多态演示
        System.out.println("\n--- Polymorphism ---");
        Shape[] shapes = {
            new Circle("Red", 5.0),
            new Rectangle("Blue", 4.0, 6.0),
            new Circle("Green", 3.0)
        };
        
        for (Shape shape : shapes) {
            shape.displayInfo(); // 多态调用
        }
        
        // 4. 接口演示
        System.out.println("\n--- Interface Implementation ---");
        DrawableCircle drawableCircle = new DrawableCircle("Yellow", 7.0);
        drawableCircle.draw();
        drawableCircle.highlight(); // 默认方法
        drawableCircle.resize(1.5);
        drawableCircle.draw();
        
        Drawable.printDrawingInfo(); // 静态方法
        
        // 5. instanceof 和类型转换
        System.out.println("\n--- Type Checking and Casting ---");
        Shape shape = new Circle("Purple", 4.0);
        
        if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            System.out.println("Shape is a Circle with area: " + circle.calculateArea());
        }
        
        if (shape instanceof Drawable) {
            System.out.println("Shape implements Drawable");
        } else {
            System.out.println("Shape does not implement Drawable");
        }
    }
    
    public static void main(String[] args) {
        demonstratePrimitiveTypes();
        demonstrateStringHandling();
        demonstrateOOP();
    }
}