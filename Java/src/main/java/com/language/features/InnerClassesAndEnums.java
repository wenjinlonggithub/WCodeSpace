package com.language.features;

import java.util.*;
import java.util.function.*;

/**
 * Java内部类和枚举详解
 * 
 * 涵盖内容：
 * 1. 静态内部类（Static Nested Class）
 * 2. 成员内部类（Member Inner Class）
 * 3. 局部内部类（Local Inner Class）
 * 4. 匿名内部类（Anonymous Inner Class）
 * 5. 枚举类型详解
 * 6. 函数式接口和Lambda表达式
 */

public class InnerClassesAndEnums {
    
    private String outerField = "Outer Field";
    private static String staticOuterField = "Static Outer Field";
    
    /**
     * 静态内部类
     * 
     * 特点：
     * 1. 不需要外部类实例就可以创建
     * 2. 只能访问外部类的静态成员
     * 3. 可以有静态成员
     */
    static class StaticNestedClass {
        private String nestedField = "Static Nested Field";
        private static String staticNestedField = "Static Nested Static Field";
        
        public void display() {
            System.out.println("Static Nested Class:");
            System.out.println("  Nested field: " + nestedField);
            System.out.println("  Static nested field: " + staticNestedField);
            System.out.println("  Outer static field: " + staticOuterField);
            // System.out.println(outerField); // 编译错误：无法访问非静态外部成员
        }
        
        public static void staticMethod() {
            System.out.println("Static method in static nested class");
        }
    }
    
    /**
     * 成员内部类
     * 
     * 特点：
     * 1. 需要外部类实例才能创建
     * 2. 可以访问外部类的所有成员（包括私有成员）
     * 3. 不能有静态成员（除了静态常量）
     */
    class MemberInnerClass {
        private String innerField = "Member Inner Field";
        // private static String staticField = "Error"; // 编译错误
        private static final String CONSTANT = "Inner Constant"; // 允许静态常量
        
        public void display() {
            System.out.println("Member Inner Class:");
            System.out.println("  Inner field: " + innerField);
            System.out.println("  Outer field: " + outerField); // 可以直接访问
            System.out.println("  Outer static field: " + staticOuterField);
            System.out.println("  Outer this: " + InnerClassesAndEnums.this.outerField);
        }
        
        public void modifyOuter() {
            outerField = "Modified by Inner Class";
        }
    }
    
    /**
     * 局部内部类演示
     */
    public void demonstrateLocalInnerClass() {
        final String localVariable = "Local Variable";
        int effectivelyFinalVar = 42;
        
        /**
         * 局部内部类
         * 
         * 特点：
         * 1. 定义在方法内部
         * 2. 只能在定义它的方法内使用
         * 3. 可以访问外部类成员和方法的final/effectively final变量
         */
        class LocalInnerClass {
            private String localInnerField = "Local Inner Field";
            
            public void display() {
                System.out.println("Local Inner Class:");
                System.out.println("  Local inner field: " + localInnerField);
                System.out.println("  Outer field: " + outerField);
                System.out.println("  Local variable: " + localVariable);
                System.out.println("  Effectively final var: " + effectivelyFinalVar);
            }
        }
        
        LocalInnerClass localInner = new LocalInnerClass();
        localInner.display();
    }
    
    /**
     * 匿名内部类演示
     */
    public void demonstrateAnonymousInnerClass() {
        System.out.println("\n--- Anonymous Inner Classes ---");
        
        // 1. 继承抽象类的匿名内部类
        abstract class AbstractProcessor {
            protected String name;
            
            public AbstractProcessor(String name) {
                this.name = name;
            }
            
            public abstract void process();
            
            public void displayInfo() {
                System.out.println("Processor: " + name);
            }
        }
        
        AbstractProcessor processor = new AbstractProcessor("Data Processor") {
            @Override
            public void process() {
                System.out.println("Processing data in anonymous class");
                displayInfo();
            }
        };
        
        processor.process();
        
        // 2. 实现接口的匿名内部类
        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous Runnable task executed");
                System.out.println("Accessing outer field: " + outerField);
            }
        };
        
        task.run();
        
        // 3. 事件处理器示例
        interface EventHandler {
            void handle(String event);
        }
        
        EventHandler clickHandler = new EventHandler() {
            private int clickCount = 0;
            
            @Override
            public void handle(String event) {
                clickCount++;
                System.out.println("Handling event: " + event + " (Count: " + clickCount + ")");
            }
        };
        
        clickHandler.handle("Button Click");
        clickHandler.handle("Button Click");
        
        // 4. 比较：匿名内部类 vs Lambda表达式
        System.out.println("\nAnonymous Class vs Lambda:");
        
        // 匿名内部类
        Comparator<String> anonymousComparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.length() - s2.length();
            }
        };
        
        // Lambda表达式
        Comparator<String> lambdaComparator = (s1, s2) -> s1.length() - s2.length();
        
        List<String> words = Arrays.asList("apple", "pie", "banana", "cherry");
        
        List<String> sortedByAnonymous = new ArrayList<>(words);
        sortedByAnonymous.sort(anonymousComparator);
        System.out.println("Sorted by anonymous class: " + sortedByAnonymous);
        
        List<String> sortedByLambda = new ArrayList<>(words);
        sortedByLambda.sort(lambdaComparator);
        System.out.println("Sorted by lambda: " + sortedByLambda);
    }
    
    /**
     * 枚举类型详解
     */
    
    // 1. 简单枚举
    enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
    
    // 2. 带构造函数和方法的枚举
    enum Planet {
        MERCURY(3.303e+23, 2.4397e6),
        VENUS(4.869e+24, 6.0518e6),
        EARTH(5.976e+24, 6.37814e6),
        MARS(6.421e+23, 3.3972e6),
        JUPITER(1.9e+27, 7.1492e7),
        SATURN(5.688e+26, 6.0268e7),
        URANUS(8.686e+25, 2.5559e7),
        NEPTUNE(1.024e+26, 2.4746e7);
        
        private final double mass;   // 质量（千克）
        private final double radius; // 半径（米）
        
        Planet(double mass, double radius) {
            this.mass = mass;
            this.radius = radius;
        }
        
        public double getMass() { return mass; }
        public double getRadius() { return radius; }
        
        // 通用重力常数 (m3 kg-1 s-2)
        public static final double G = 6.67300E-11;
        
        public double surfaceGravity() {
            return G * mass / (radius * radius);
        }
        
        public double surfaceWeight(double otherMass) {
            return otherMass * surfaceGravity();
        }
    }
    
    // 3. 实现接口的枚举
    interface Operation {
        double apply(double x, double y);
    }
    
    enum BasicOperation implements Operation {
        PLUS("+") {
            @Override
            public double apply(double x, double y) { return x + y; }
        },
        MINUS("-") {
            @Override
            public double apply(double x, double y) { return x - y; }
        },
        TIMES("*") {
            @Override
            public double apply(double x, double y) { return x * y; }
        },
        DIVIDE("/") {
            @Override
            public double apply(double x, double y) { return x / y; }
        };
        
        private final String symbol;
        
        BasicOperation(String symbol) {
            this.symbol = symbol;
        }
        
        @Override
        public String toString() {
            return symbol;
        }
    }
    
    // 4. 策略枚举模式
    enum PayrollDay {
        MONDAY(PayType.WEEKDAY),
        TUESDAY(PayType.WEEKDAY),
        WEDNESDAY(PayType.WEEKDAY),
        THURSDAY(PayType.WEEKDAY),
        FRIDAY(PayType.WEEKDAY),
        SATURDAY(PayType.WEEKEND),
        SUNDAY(PayType.WEEKEND);
        
        private final PayType payType;
        
        PayrollDay(PayType payType) {
            this.payType = payType;
        }
        
        public int pay(int minutesWorked, int payRate) {
            return payType.pay(minutesWorked, payRate);
        }
        
        // 策略枚举
        private enum PayType {
            WEEKDAY {
                @Override
                int overtimePay(int minutesWorked, int payRate) {
                    return minutesWorked <= MINUTES_PER_SHIFT ? 0 :
                        (minutesWorked - MINUTES_PER_SHIFT) * payRate / 2;
                }
            },
            WEEKEND {
                @Override
                int overtimePay(int minutesWorked, int payRate) {
                    return minutesWorked * payRate / 2;
                }
            };
            
            abstract int overtimePay(int minutesWorked, int payRate);
            private static final int MINUTES_PER_SHIFT = 8 * 60;
            
            int pay(int minutesWorked, int payRate) {
                int basePay = minutesWorked * payRate;
                return basePay + overtimePay(minutesWorked, payRate);
            }
        }
    }
    
    public static void demonstrateEnums() {
        System.out.println("\n=== Enum Demonstrations ===");
        
        // 1. 基本枚举使用
        System.out.println("\n--- Basic Enum Usage ---");
        Day today = Day.FRIDAY;
        System.out.println("Today is: " + today);
        System.out.println("Ordinal: " + today.ordinal());
        System.out.println("Name: " + today.name());
        
        // 枚举比较
        System.out.println("Is today Friday? " + (today == Day.FRIDAY));
        System.out.println("Compare with Monday: " + today.compareTo(Day.MONDAY));
        
        // 遍历枚举值
        System.out.println("All days:");
        for (Day day : Day.values()) {
            System.out.println("  " + day + " (ordinal: " + day.ordinal() + ")");
        }
        
        // 2. 复杂枚举使用
        System.out.println("\n--- Complex Enum Usage ---");
        double earthWeight = 70.0; // 70公斤
        
        for (Planet planet : Planet.values()) {
            System.out.printf("Weight on %s: %.2f kg%n",
                planet, planet.surfaceWeight(earthWeight));
        }
        
        // 3. 枚举实现接口
        System.out.println("\n--- Enum Implementing Interface ---");
        double x = 10.0, y = 5.0;
        
        for (BasicOperation op : BasicOperation.values()) {
            System.out.printf("%.1f %s %.1f = %.1f%n",
                x, op, y, op.apply(x, y));
        }
        
        // 4. 策略枚举模式
        System.out.println("\n--- Strategy Enum Pattern ---");
        int minutesWorked = 10 * 60; // 10小时
        int payRate = 100; // 每小时100元
        
        for (PayrollDay day : PayrollDay.values()) {
            System.out.printf("Pay for %s: %d yuan%n",
                day, day.pay(minutesWorked, payRate));
        }
        
        // 5. 枚举的switch语句
        System.out.println("\n--- Enum in Switch Statement ---");
        Day someDay = Day.SATURDAY;
        
        switch (someDay) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                System.out.println(someDay + " is a weekday");
                break;
            case SATURDAY:
            case SUNDAY:
                System.out.println(someDay + " is a weekend");
                break;
        }
        
        // 6. EnumSet和EnumMap
        System.out.println("\n--- EnumSet and EnumMap ---");
        EnumSet<Day> weekdays = EnumSet.range(Day.MONDAY, Day.FRIDAY);
        EnumSet<Day> weekend = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
        
        System.out.println("Weekdays: " + weekdays);
        System.out.println("Weekend: " + weekend);
        
        EnumMap<Day, String> activities = new EnumMap<>(Day.class);
        activities.put(Day.MONDAY, "Work");
        activities.put(Day.FRIDAY, "TGIF");
        activities.put(Day.SATURDAY, "Relax");
        activities.put(Day.SUNDAY, "Rest");
        
        System.out.println("Activities: " + activities);
    }
    
    public static void demonstrateInnerClasses() {
        System.out.println("=== Inner Classes Demonstration ===");
        
        InnerClassesAndEnums outer = new InnerClassesAndEnums();
        
        // 1. 静态内部类
        System.out.println("\n--- Static Nested Class ---");
        StaticNestedClass staticNested = new StaticNestedClass();
        staticNested.display();
        StaticNestedClass.staticMethod();
        
        // 2. 成员内部类
        System.out.println("\n--- Member Inner Class ---");
        MemberInnerClass memberInner = outer.new MemberInnerClass();
        memberInner.display();
        
        System.out.println("Before modification: " + outer.outerField);
        memberInner.modifyOuter();
        System.out.println("After modification: " + outer.outerField);
        
        // 3. 局部内部类
        System.out.println("\n--- Local Inner Class ---");
        outer.demonstrateLocalInnerClass();
        
        // 4. 匿名内部类
        outer.demonstrateAnonymousInnerClass();
    }
    
    public static void main(String[] args) {
        demonstrateInnerClasses();
        demonstrateEnums();
    }
}