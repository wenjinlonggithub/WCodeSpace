package com.jdk.source;

import java.util.*;
import java.util.concurrent.*;
import java.lang.ref.*;
import java.lang.reflect.*;

/**
 * JDK源码分析和核心实现原理
 * 
 * 深入分析：
 * 1. Object类核心方法实现原理
 * 2. String类内部实现机制
 * 3. Integer缓存机制源码分析
 * 4. HashMap核心源码解析
 * 5. ArrayList动态扩容机制
 * 6. ThreadLocal实现原理
 * 7. 引用类型和垃圾回收
 */

public class JDKSourceCodeAnalysis {
    
    /**
     * Object类核心方法分析
     * 
     * Object类是所有Java类的根类，提供了基础的方法：
     * 1. equals() - 对象相等性比较
     * 2. hashCode() - 哈希码计算
     * 3. toString() - 字符串表示
     * 4. clone() - 对象克隆
     * 5. wait()/notify()/notifyAll() - 线程同步
     * 6. finalize() - 垃圾回收前调用
     */
    
    static class ObjectMethodsDemo {
        private String name;
        private int value;
        
        public ObjectMethodsDemo(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        /**
         * equals()方法的正确实现
         * 
         * 源码分析：Object.equals()默认实现是 return (this == other);
         * 重写时需要满足：
         * 1. 自反性：x.equals(x) == true
         * 2. 对称性：x.equals(y) == y.equals(x)
         * 3. 传递性：x.equals(y) && y.equals(z) => x.equals(z)
         * 4. 一致性：多次调用结果相同
         * 5. 非空性：x.equals(null) == false
         */
        @Override
        public boolean equals(Object obj) {
            // 1. 检查引用相等性（性能优化）
            if (this == obj) return true;
            
            // 2. 检查null和类型
            if (obj == null || getClass() != obj.getClass()) return false;
            
            // 3. 类型转换和字段比较
            ObjectMethodsDemo other = (ObjectMethodsDemo) obj;
            return value == other.value && 
                   Objects.equals(name, other.name);
        }
        
        /**
         * hashCode()方法的正确实现
         * 
         * 源码分析：Object.hashCode()返回对象的内存地址（经过处理）
         * 重写规则：
         * 1. equals()相等的对象必须有相同的hashCode()
         * 2. hashCode()相等的对象不一定equals()相等
         * 3. 对象的hashCode()在程序执行期间应保持不变
         */
        @Override
        public int hashCode() {
            // 使用Objects.hash()工具方法，内部实现：
            // return Arrays.hashCode(values);
            return Objects.hash(name, value);
            
            // 手动实现的等价代码：
            // int result = 17;
            // result = 31 * result + (name != null ? name.hashCode() : 0);
            // result = 31 * result + value;
            // return result;
        }
        
        @Override
        public String toString() {
            // Object.toString()默认实现：
            // return getClass().getName() + "@" + Integer.toHexString(hashCode());
            return "ObjectMethodsDemo{name='" + name + "', value=" + value + "}";
        }
        
        /**
         * clone()方法实现
         * 
         * 源码分析：Object.clone()是native方法，执行浅拷贝
         * 使用条件：
         * 1. 实现Cloneable接口
         * 2. 重写clone()方法
         * 3. 处理CloneNotSupportedException
         */
        @Override
        protected Object clone() throws CloneNotSupportedException {
            // 调用Object.clone()执行浅拷贝
            return super.clone();
        }
        
        // Getters
        public String getName() { return name; }
        public int getValue() { return value; }
    }
    
    /**
     * String类内部实现分析
     * 
     * JDK 9之前：char[] value
     * JDK 9之后：byte[] value + byte coder (LATIN1/UTF16)
     */
    public static void analyzeStringImplementation() {
        System.out.println("=== String Implementation Analysis ===");
        
        // 1. String的不可变性实现
        System.out.println("\n--- String Immutability ---");
        String str1 = "Hello";
        String str2 = str1.concat(" World");
        
        System.out.println("Original: " + str1);     // "Hello" - 未改变
        System.out.println("Concatenated: " + str2); // "Hello World" - 新对象
        System.out.println("Same object? " + (str1 == str2)); // false
        
        // 2. 字符串常量池机制
        System.out.println("\n--- String Constant Pool ---");
        
        // 编译时常量会进入常量池
        String literal1 = "Java";
        String literal2 = "Java";
        System.out.println("literal1 == literal2: " + (literal1 == literal2)); // true
        
        // new String()创建堆对象
        String heap1 = new String("Java");
        String heap2 = new String("Java");
        System.out.println("heap1 == heap2: " + (heap1 == heap2)); // false
        System.out.println("heap1.equals(heap2): " + heap1.equals(heap2)); // true
        
        // intern()方法返回常量池中的引用
        String interned = heap1.intern();
        System.out.println("literal1 == interned: " + (literal1 == interned)); // true
        
        // 3. String.hashCode()实现分析
        System.out.println("\n--- String hashCode Implementation ---");
        
        /*
         * String.hashCode()源码实现：
         * 
         * public int hashCode() {
         *     int h = hash;  // 缓存的哈希值
         *     if (h == 0 && value.length > 0) {
         *         char val[] = value;
         *         for (int i = 0; i < value.length; i++) {
         *             h = 31 * h + val[i];  // 31是质数，减少冲突
         *         }
         *         hash = h;  // 缓存计算结果
         *     }
         *     return h;
         * }
         */
        
        String test = "ABC";
        int expectedHash = 'A' * 31 * 31 + 'B' * 31 + 'C'; // 手动计算
        System.out.println("String 'ABC' hashCode: " + test.hashCode());
        System.out.println("Expected hashCode: " + expectedHash);
        System.out.println("Match: " + (test.hashCode() == expectedHash));
        
        // 4. StringBuilder内部实现
        System.out.println("\n--- StringBuilder Implementation ---");
        
        /*
         * StringBuilder核心字段：
         * char[] value;     // 字符数组（JDK 9后是byte[]）
         * int count;        // 当前长度
         * 
         * 扩容策略：
         * int newCapacity = (oldCapacity << 1) + 2;  // 大约2倍扩容
         */
        
        StringBuilder sb = new StringBuilder(5); // 初始容量5
        System.out.println("Initial capacity: 5");
        
        sb.append("Hello"); // 长度5，不需要扩容
        System.out.println("After 'Hello': length=" + sb.length());
        
        sb.append(" World!"); // 长度12，需要扩容
        System.out.println("After ' World!': length=" + sb.length());
        System.out.println("Result: " + sb.toString());
    }
    
    /**
     * Integer缓存机制源码分析
     * 
     * Integer.valueOf()方法会缓存-128到127之间的Integer对象
     */
    public static void analyzeIntegerCaching() {
        System.out.println("\n=== Integer Caching Analysis ===");
        
        /*
         * Integer.valueOf()源码：
         * 
         * public static Integer valueOf(int i) {
         *     if (i >= IntegerCache.low && i <= IntegerCache.high)
         *         return IntegerCache.cache[i + (-IntegerCache.low)];
         *     return new Integer(i);
         * }
         * 
         * IntegerCache类：
         * private static class IntegerCache {
         *     static final int low = -128;
         *     static final int high = 127;  // 可通过-XX:AutoBoxCacheMax=<size>调整
         *     static final Integer cache[];
         * }
         */
        
        System.out.println("\n--- Integer Cache Range Test ---");
        
        // 缓存范围内的测试
        Integer a1 = 127;
        Integer a2 = 127;
        Integer a3 = Integer.valueOf(127);
        
        System.out.println("127 == 127 (autobox): " + (a1 == a2)); // true
        System.out.println("127 == valueOf(127): " + (a1 == a3)); // true
        
        // 超出缓存范围的测试
        Integer b1 = 128;
        Integer b2 = 128;
        Integer b3 = Integer.valueOf(128);
        
        System.out.println("128 == 128 (autobox): " + (b1 == b2)); // false
        System.out.println("128 == valueOf(128): " + (b1 == b3)); // false
        System.out.println("128.equals(128): " + b1.equals(b2)); // true
        
        // new Integer()总是创建新对象
        Integer c1 = new Integer(127);
        Integer c2 = new Integer(127);
        System.out.println("new Integer(127) == new Integer(127): " + (c1 == c2)); // false
        
        // 其他包装类的缓存情况
        System.out.println("\n--- Other Wrapper Class Caching ---");
        
        // Boolean: TRUE和FALSE
        Boolean bool1 = true;
        Boolean bool2 = Boolean.valueOf(true);
        System.out.println("Boolean true caching: " + (bool1 == bool2)); // true
        
        // Byte: 全部缓存(-128到127)
        Byte byte1 = 100;
        Byte byte2 = Byte.valueOf((byte)100);
        System.out.println("Byte caching: " + (byte1 == byte2)); // true
        
        // Character: 0到127
        Character char1 = 'A';
        Character char2 = Character.valueOf('A');
        System.out.println("Character caching: " + (char1 == char2)); // true
        
        // Short: -128到127
        Short short1 = 100;
        Short short2 = Short.valueOf((short)100);
        System.out.println("Short caching: " + (short1 == short2)); // true
        
        // Long: -128到127
        Long long1 = 100L;
        Long long2 = Long.valueOf(100L);
        System.out.println("Long caching: " + (long1 == long2)); // true
        
        // Float和Double: 不缓存
        Float float1 = 1.0f;
        Float float2 = Float.valueOf(1.0f);
        System.out.println("Float caching: " + (float1 == float2)); // false
    }
    
    /**
     * HashMap核心源码解析
     * 
     * JDK 8的重要改进：
     * 1. 数组 + 链表 + 红黑树结构
     * 2. 链表长度超过8转换为红黑树
     * 3. 红黑树节点少于6转换回链表
     */
    
    static class HashMapAnalysis<K, V> {
        
        /**
         * 模拟HashMap的hash()方法实现
         * 
         * 源码：
         * static final int hash(Object key) {
         *     int h;
         *     return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
         * }
         * 
         * 目的：减少哈希冲突，让高位也参与运算
         */
        public static int hash(Object key) {
            int h;
            return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
        }
        
        /**
         * 模拟HashMap的索引计算
         * 
         * 源码中使用：(n - 1) & hash
         * 其中n是数组长度，必须是2的幂次方
         */
        public static int indexFor(int hash, int length) {
            return hash & (length - 1); // 等价于 hash % length，但更高效
        }
        
        /**
         * 模拟HashMap的扩容阈值计算
         * 
         * threshold = capacity * loadFactor
         * 默认loadFactor = 0.75
         */
        public static int threshold(int capacity, float loadFactor) {
            return (int) (capacity * loadFactor);
        }
        
        /**
         * 模拟HashMap的容量计算（向上取最近的2的幂次方）
         * 
         * 源码：tableSizeFor()方法
         */
        public static int tableSizeFor(int cap) {
            int n = cap - 1;
            n |= n >>> 1;
            n |= n >>> 2;
            n |= n >>> 4;
            n |= n >>> 8;
            n |= n >>> 16;
            return (n < 0) ? 1 : (n >= (1 << 30)) ? (1 << 30) : n + 1;
        }
        
        public static void demonstrateHashMapInternals() {
            System.out.println("\n=== HashMap Internals Analysis ===");
            
            // 1. 哈希函数测试
            System.out.println("\n--- Hash Function Test ---");
            String[] keys = {"apple", "banana", "cherry", "date"};
            
            for (String key : keys) {
                int originalHash = key.hashCode();
                int improvedHash = hash(key);
                System.out.printf("Key: %s, Original: %d, Improved: %d%n", 
                    key, originalHash, improvedHash);
            }
            
            // 2. 索引计算测试
            System.out.println("\n--- Index Calculation Test ---");
            int capacity = 16; // HashMap默认初始容量
            
            for (String key : keys) {
                int hashValue = hash(key);
                int index = indexFor(hashValue, capacity);
                System.out.printf("Key: %s, Hash: %d, Index: %d%n", 
                    key, hashValue, index);
            }
            
            // 3. 容量计算测试
            System.out.println("\n--- Capacity Calculation Test ---");
            int[] testCapacities = {5, 10, 17, 33, 100};
            
            for (int cap : testCapacities) {
                int actualCapacity = tableSizeFor(cap);
                System.out.printf("Requested: %d, Actual: %d%n", cap, actualCapacity);
            }
            
            // 4. 扩容阈值测试
            System.out.println("\n--- Resize Threshold Test ---");
            float loadFactor = 0.75f;
            
            for (int cap = 16; cap <= 128; cap *= 2) {
                int thresh = threshold(cap, loadFactor);
                System.out.printf("Capacity: %d, Threshold: %d%n", cap, thresh);
            }
            
            // 5. 实际HashMap行为验证
            System.out.println("\n--- Actual HashMap Behavior ---");
            Map<String, Integer> map = new HashMap<>();
            
            // 添加元素并观察行为
            for (int i = 0; i < 20; i++) {
                String key = "key" + i;
                map.put(key, i);
                
                if (i % 5 == 4) { // 每5个元素打印一次状态
                    System.out.printf("Size: %d, Elements added: %d%n", map.size(), i + 1);
                }
            }
        }
    }
    
    /**
     * ArrayList动态扩容机制分析
     */
    
    static class ArrayListAnalysis {
        
        /**
         * 模拟ArrayList的扩容逻辑
         * 
         * 源码中的grow()方法：
         * int newCapacity = oldCapacity + (oldCapacity >> 1); // 1.5倍扩容
         */
        public static int calculateNewCapacity(int oldCapacity) {
            return oldCapacity + (oldCapacity >> 1); // 右移1位相当于除以2
        }
        
        /**
         * 模拟ArrayList的最小容量计算
         */
        public static int calculateMinCapacity(int minCapacity) {
            final int DEFAULT_CAPACITY = 10;
            return Math.max(minCapacity, DEFAULT_CAPACITY);
        }
        
        public static void demonstrateArrayListGrowth() {
            System.out.println("\n=== ArrayList Growth Analysis ===");
            
            // 1. 扩容策略测试
            System.out.println("\n--- Growth Strategy Test ---");
            int capacity = 10; // ArrayList默认容量
            
            System.out.println("ArrayList capacity growth:");
            for (int i = 0; i < 5; i++) {
                System.out.printf("Current: %d, Next: %d%n", 
                    capacity, calculateNewCapacity(capacity));
                capacity = calculateNewCapacity(capacity);
            }
            
            // 2. 实际ArrayList行为测试
            System.out.println("\n--- Actual ArrayList Behavior ---");
            
            // 使用反射观察ArrayList内部状态
            List<Integer> list = new ArrayList<>();
            
            try {
                Field elementDataField = ArrayList.class.getDeclaredField("elementData");
                elementDataField.setAccessible(true);
                
                System.out.println("ArrayList growth observation:");
                for (int i = 0; i < 25; i++) {
                    list.add(i);
                    
                    Object[] elementData = (Object[]) elementDataField.get(list);
                    int currentCapacity = elementData.length;
                    
                    if (i == 0 || i == 9 || i == 15 || i == 22) { // 关键扩容点
                        System.out.printf("Size: %d, Capacity: %d%n", list.size(), currentCapacity);
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Reflection access failed: " + e.getMessage());
            }
            
            // 3. 不同初始化方式的容量对比
            System.out.println("\n--- Different Initialization Capacities ---");
            
            List<String> list1 = new ArrayList<>(); // 默认构造
            List<String> list2 = new ArrayList<>(5); // 指定初始容量
            List<String> list3 = new ArrayList<>(Arrays.asList("a", "b", "c")); // 从集合构造
            
            System.out.println("Default constructor: lazy initialization");
            System.out.println("Specified capacity: immediate allocation");
            System.out.println("From collection: capacity equals collection size");
        }
    }
    
    public static void main(String[] args) {
        // 1. Object方法分析
        System.out.println("=== Object Methods Analysis ===");
        ObjectMethodsDemo obj1 = new ObjectMethodsDemo("Test", 42);
        ObjectMethodsDemo obj2 = new ObjectMethodsDemo("Test", 42);
        ObjectMethodsDemo obj3 = new ObjectMethodsDemo("Different", 100);
        
        System.out.println("obj1.equals(obj2): " + obj1.equals(obj2)); // true
        System.out.println("obj1.equals(obj3): " + obj1.equals(obj3)); // false
        System.out.println("obj1.hashCode(): " + obj1.hashCode());
        System.out.println("obj2.hashCode(): " + obj2.hashCode());
        System.out.println("Same hashCode: " + (obj1.hashCode() == obj2.hashCode()));
        
        // 2. String实现分析
        analyzeStringImplementation();
        
        // 3. Integer缓存分析
        analyzeIntegerCaching();
        
        // 4. HashMap内部机制分析
        HashMapAnalysis.demonstrateHashMapInternals();
        
        // 5. ArrayList扩容机制分析
        ArrayListAnalysis.demonstrateArrayListGrowth();
    }
}