package com.architecture.interview;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.lang.reflect.*;

/**
 * Java核心面试题详解
 * 
 * 涵盖领域：
 * 1. JVM内存模型和垃圾回收
 * 2. 集合框架深入理解
 * 3. 多线程和并发编程
 * 4. 反射和注解
 * 5. 设计模式应用
 * 6. 性能优化和最佳实践
 */

public class JavaInterviewQuestions {
    
    /**
     * 面试题1: 解释Java内存模型(JMM)
     * 
     * 答案要点：
     * 1. 堆内存：存储对象实例，分为年轻代(Eden、S0、S1)和老年代
     * 2. 方法区：存储类信息、常量池、静态变量
     * 3. 栈内存：存储局部变量、方法调用
     * 4. 程序计数器：记录当前执行指令位置
     * 5. 本地方法栈：native方法调用
     */
    public static void demonstrateMemoryModel() {
        System.out.println("=== Java Memory Model Demo ===");
        
        // 堆内存中的对象
        String heapString = new String("Heap Object");
        
        // 方法区中的字符串常量池
        String poolString = "Pool String";
        
        // 栈内存中的局部变量
        int stackVariable = 42;
        
        System.out.println("Heap String: " + heapString);
        System.out.println("Pool String: " + poolString);
        System.out.println("Stack Variable: " + stackVariable);
        
        // 演示字符串常量池
        String s1 = "Hello";
        String s2 = "Hello";
        String s3 = new String("Hello");
        
        System.out.println("s1 == s2: " + (s1 == s2)); // true - 同一个常量池对象
        System.out.println("s1 == s3: " + (s1 == s3)); // false - s3在堆中
        System.out.println("s1.equals(s3): " + s1.equals(s3)); // true - 内容相同
    }
    
    /**
     * 面试题2: HashMap的实现原理和线程安全问题
     * 
     * 答案要点：
     * 1. 底层数据结构：数组 + 链表 + 红黑树(JDK8+)
     * 2. 哈希冲突解决：链地址法
     * 3. 扩容机制：负载因子0.75，容量翻倍
     * 4. 线程不安全：并发修改可能导致死循环
     * 5. 线程安全替代：ConcurrentHashMap、Collections.synchronizedMap
     */
    public static void demonstrateHashMapInternals() {
        System.out.println("\n=== HashMap Internals Demo ===");
        
        Map<String, Integer> hashMap = new HashMap<>();
        
        // 添加元素演示哈希分布
        String[] keys = {"apple", "banana", "cherry", "date", "elderberry"};
        for (int i = 0; i < keys.length; i++) {
            hashMap.put(keys[i], i);
            System.out.println("Added: " + keys[i] + " -> " + i + 
                " (hashCode: " + keys[i].hashCode() + ")");
        }
        
        // 演示HashMap的线程不安全性
        Map<Integer, String> unsafeMap = new HashMap<>();
        
        // 多线程并发修改HashMap可能导致问题
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    unsafeMap.put(threadId * 1000 + j, "Thread-" + threadId + "-Value-" + j);
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Unsafe HashMap size: " + unsafeMap.size());
        System.out.println("Expected size: 3000");
        
        // 线程安全的替代方案
        Map<Integer, String> safeMap = new ConcurrentHashMap<>();
        Map<Integer, String> syncMap = Collections.synchronizedMap(new HashMap<>());
        
        System.out.println("ConcurrentHashMap created: " + safeMap.getClass().getSimpleName());
        System.out.println("SynchronizedMap created: " + syncMap.getClass().getSimpleName());
    }
    
    /**
     * 面试题3: ArrayList vs LinkedList 性能对比
     * 
     * 答案要点：
     * 1. ArrayList：基于数组，随机访问O(1)，插入删除O(n)
     * 2. LinkedList：基于双向链表，随机访问O(n)，插入删除O(1)
     * 3. 内存占用：ArrayList更紧凑，LinkedList有额外指针开销
     * 4. 缓存友好性：ArrayList更好
     */
    public static void compareListPerformance() {
        System.out.println("\n=== ArrayList vs LinkedList Performance ===");
        
        int size = 100000;
        
        // 测试随机访问性能
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        
        // 填充数据
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
        
        // 测试随机访问
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            int index = (int) (Math.random() * size);
            arrayList.get(index);
        }
        long arrayListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            int index = (int) (Math.random() * size);
            linkedList.get(index);
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        System.out.println("Random Access Performance:");
        System.out.println("ArrayList: " + arrayListTime / 1000000.0 + " ms");
        System.out.println("LinkedList: " + linkedListTime / 1000000.0 + " ms");
        
        // 测试头部插入性能
        List<Integer> arrayListInsert = new ArrayList<>();
        List<Integer> linkedListInsert = new LinkedList<>();
        
        startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            arrayListInsert.add(0, i); // 头部插入
        }
        long arrayListInsertTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            linkedListInsert.add(0, i); // 头部插入
        }
        long linkedListInsertTime = System.nanoTime() - startTime;
        
        System.out.println("\nHead Insertion Performance:");
        System.out.println("ArrayList: " + arrayListInsertTime / 1000000.0 + " ms");
        System.out.println("LinkedList: " + linkedListInsertTime / 1000000.0 + " ms");
    }
    
    /**
     * 面试题4: 深拷贝 vs 浅拷贝
     * 
     * 答案要点：
     * 1. 浅拷贝：只复制对象的引用，共享内部对象
     * 2. 深拷贝：递归复制所有对象，完全独立
     * 3. 实现方式：Cloneable接口、序列化、手动复制
     */
    static class Person implements Cloneable {
        private String name;
        private Address address;
        
        public Person(String name, Address address) {
            this.name = name;
            this.address = address;
        }
        
        // 浅拷贝
        @Override
        public Person clone() throws CloneNotSupportedException {
            return (Person) super.clone();
        }
        
        // 深拷贝
        public Person deepClone() throws CloneNotSupportedException {
            Person cloned = (Person) super.clone();
            cloned.address = this.address.clone();
            return cloned;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
        
        @Override
        public String toString() {
            return "Person{name='" + name + "', address=" + address + "}";
        }
    }
    
    static class Address implements Cloneable {
        private String city;
        private String street;
        
        public Address(String city, String street) {
            this.city = city;
            this.street = street;
        }
        
        @Override
        public Address clone() throws CloneNotSupportedException {
            return (Address) super.clone();
        }
        
        // Getters and setters
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        
        @Override
        public String toString() {
            return "Address{city='" + city + "', street='" + street + "'}";
        }
    }
    
    public static void demonstrateCloning() throws CloneNotSupportedException {
        System.out.println("\n=== Shallow vs Deep Copy Demo ===");
        
        Address address = new Address("Beijing", "Zhongguancun");
        Person original = new Person("John", address);
        
        // 浅拷贝
        Person shallowCopy = original.clone();
        
        // 深拷贝
        Person deepCopy = original.deepClone();
        
        System.out.println("Original: " + original);
        System.out.println("Shallow Copy: " + shallowCopy);
        System.out.println("Deep Copy: " + deepCopy);
        
        // 修改原始对象的地址
        original.getAddress().setCity("Shanghai");
        
        System.out.println("\nAfter modifying original address:");
        System.out.println("Original: " + original);
        System.out.println("Shallow Copy: " + shallowCopy); // 地址也被修改了
        System.out.println("Deep Copy: " + deepCopy);       // 地址没有被修改
    }
    
    /**
     * 面试题5: Java反射机制详解
     * 
     * 答案要点：
     * 1. 反射定义：运行时检查和操作类、方法、字段的能力
     * 2. 主要类：Class、Method、Field、Constructor
     * 3. 应用场景：框架开发、动态代理、序列化
     * 4. 性能影响：比直接调用慢，但提供了灵活性
     */
    static class ReflectionExample {
        private String privateField = "Private Value";
        public String publicField = "Public Value";
        
        private void privateMethod() {
            System.out.println("Private method called");
        }
        
        public void publicMethod(String param) {
            System.out.println("Public method called with: " + param);
        }
        
        public ReflectionExample() {}
        
        public ReflectionExample(String value) {
            this.publicField = value;
        }
    }
    
    public static void demonstrateReflection() throws Exception {
        System.out.println("\n=== Reflection Demo ===");
        
        Class<?> clazz = ReflectionExample.class;
        
        // 获取类信息
        System.out.println("Class name: " + clazz.getName());
        System.out.println("Simple name: " + clazz.getSimpleName());
        System.out.println("Package: " + clazz.getPackage().getName());
        
        // 创建实例
        Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
        Object instance1 = defaultConstructor.newInstance();
        
        Constructor<?> paramConstructor = clazz.getDeclaredConstructor(String.class);
        Object instance2 = paramConstructor.newInstance("Reflection Created");
        
        // 访问字段
        Field[] fields = clazz.getDeclaredFields();
        System.out.println("\nFields:");
        for (Field field : fields) {
            System.out.println("- " + field.getName() + " (" + field.getType().getSimpleName() + ")");
            
            field.setAccessible(true); // 访问私有字段
            Object value = field.get(instance1);
            System.out.println("  Value: " + value);
        }
        
        // 调用方法
        Method[] methods = clazz.getDeclaredMethods();
        System.out.println("\nMethods:");
        for (Method method : methods) {
            System.out.println("- " + method.getName() + 
                " (Parameters: " + method.getParameterCount() + ")");
            
            if (method.getName().equals("privateMethod")) {
                method.setAccessible(true); // 访问私有方法
                method.invoke(instance1);
            } else if (method.getName().equals("publicMethod")) {
                method.invoke(instance1, "Reflection Parameter");
            }
        }
        
        // 修改私有字段
        Field privateField = clazz.getDeclaredField("privateField");
        privateField.setAccessible(true);
        privateField.set(instance1, "Modified by Reflection");
        System.out.println("\nModified private field: " + privateField.get(instance1));
    }
    
    /**
     * 面试题6: 异常处理最佳实践
     * 
     * 答案要点：
     * 1. 异常分类：Checked Exception、Unchecked Exception、Error
     * 2. 异常处理原则：早抛出、晚捕获、具体处理
     * 3. 自定义异常：继承合适的异常类，提供有意义的信息
     * 4. 资源管理：try-with-resources语句
     */
    static class CustomException extends Exception {
        public CustomException(String message) {
            super(message);
        }
        
        public CustomException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static void demonstrateExceptionHandling() {
        System.out.println("\n=== Exception Handling Demo ===");
        
        // 1. 基本异常处理
        try {
            int result = divide(10, 0);
            System.out.println("Result: " + result);
        } catch (ArithmeticException e) {
            System.out.println("Arithmetic error: " + e.getMessage());
        }
        
        // 2. 多重异常处理
        try {
            processArray(null);
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Array processing error: " + e.getClass().getSimpleName());
        }
        
        // 3. 自定义异常
        try {
            validateAge(-5);
        } catch (CustomException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
        
        // 4. try-with-resources
        try (AutoCloseable resource = createResource()) {
            System.out.println("Using resource");
            // 资源会自动关闭
        } catch (Exception e) {
            System.out.println("Resource error: " + e.getMessage());
        }
        
        // 5. 异常链
        try {
            methodWithExceptionChain();
        } catch (Exception e) {
            System.out.println("Exception chain:");
            Throwable current = e;
            while (current != null) {
                System.out.println("- " + current.getClass().getSimpleName() + ": " + current.getMessage());
                current = current.getCause();
            }
        }
    }
    
    private static int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }
    
    private static void processArray(int[] array) {
        System.out.println("Array length: " + array.length);
        System.out.println("First element: " + array[0]);
    }
    
    private static void validateAge(int age) throws CustomException {
        if (age < 0) {
            throw new CustomException("Age cannot be negative: " + age);
        }
        if (age > 150) {
            throw new CustomException("Age seems unrealistic: " + age);
        }
    }
    
    private static AutoCloseable createResource() {
        return () -> System.out.println("Resource closed");
    }
    
    private static void methodWithExceptionChain() throws Exception {
        try {
            throw new RuntimeException("Original exception");
        } catch (RuntimeException e) {
            throw new CustomException("Wrapped exception", e);
        }
    }
    
    public static void main(String[] args) throws Exception {
        demonstrateMemoryModel();
        demonstrateHashMapInternals();
        compareListPerformance();
        demonstrateCloning();
        demonstrateReflection();
        demonstrateExceptionHandling();
    }
}