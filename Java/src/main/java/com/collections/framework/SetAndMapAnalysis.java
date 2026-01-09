package com.collections.framework;

import java.util.*;
import java.util.concurrent.*;

/**
 * Set和Map集合深度分析
 * 
 * 详细分析：
 * 1. Set接口实现类（HashSet、LinkedHashSet、TreeSet）
 * 2. Map接口实现类（HashMap、LinkedHashMap、TreeMap）
 * 3. 哈希表实现原理
 * 4. 红黑树实现原理
 * 5. 负载因子和扩容机制
 * 6. 并发Map实现分析
 */

public class SetAndMapAnalysis {
    
    /**
     * Set实现类深度分析
     */
    
    static class SetImplementationAnalysis {
        
        /**
         * HashSet源码分析
         * 
         * 核心特点：
         * 1. 基于HashMap实现（值作为key，PRESENT作为value）
         * 2. 无序集合，不保证元素顺序
         * 3. 允许null值
         * 4. 非线程安全
         */
        public static void analyzeHashSet() {
            System.out.println("=== HashSet Implementation Analysis ===");
            
            // 1. 基本特性验证
            System.out.println("\n--- HashSet Basic Properties ---");
            
            Set<String> hashSet = new HashSet<>();
            
            // 添加元素
            hashSet.add("apple");
            hashSet.add("banana");
            hashSet.add("cherry");
            hashSet.add("apple"); // 重复元素
            hashSet.add(null);    // null值
            
            System.out.println("HashSet contents: " + hashSet);
            System.out.println("Size: " + hashSet.size()); // 4个元素（去重）
            System.out.println("Contains 'apple': " + hashSet.contains("apple"));
            System.out.println("Contains null: " + hashSet.contains(null));
            
            // 2. 无序性验证
            System.out.println("\n--- HashSet Ordering ---");
            
            Set<Integer> numbers = new HashSet<>();
            for (int i = 1; i <= 10; i++) {
                numbers.add(i);
            }
            
            System.out.println("Added 1-10 in order: " + numbers);
            
            // 多次遍历可能顺序不同（取决于哈希值）
            System.out.print("Iteration 1: ");
            for (Integer num : numbers) {
                System.out.print(num + " ");
            }
            System.out.println();
            
            // 3. 性能测试
            System.out.println("\n--- HashSet Performance ---");
            
            Set<Integer> performanceSet = new HashSet<>();
            
            // 添加性能
            long startTime = System.nanoTime();
            for (int i = 0; i < 100000; i++) {
                performanceSet.add(i);
            }
            long addTime = System.nanoTime() - startTime;
            
            // 查找性能
            startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                performanceSet.contains(i * 10);
            }
            long containsTime = System.nanoTime() - startTime;
            
            System.out.println("Add 100000 elements: " + addTime / 1_000_000.0 + " ms");
            System.out.println("Contains 10000 lookups: " + containsTime / 1_000_000.0 + " ms");
            
            // 4. 哈希冲突演示
            System.out.println("\n--- Hash Collision Demo ---");
            
            // 创建具有相同哈希值的对象
            Set<HashCollisionDemo> collisionSet = new HashSet<>();
            
            HashCollisionDemo obj1 = new HashCollisionDemo("A", 1);
            HashCollisionDemo obj2 = new HashCollisionDemo("B", 1); // 相同哈希值
            HashCollisionDemo obj3 = new HashCollisionDemo("A", 1); // 相同对象
            
            collisionSet.add(obj1);
            collisionSet.add(obj2);
            collisionSet.add(obj3);
            
            System.out.println("Objects with hash collision: " + collisionSet.size());
            System.out.println("Set contents: " + collisionSet);
        }
        
        /**
         * LinkedHashSet源码分析
         * 
         * 核心特点：
         * 1. 基于LinkedHashMap实现
         * 2. 维护插入顺序
         * 3. 性能略低于HashSet
         */
        public static void analyzeLinkedHashSet() {
            System.out.println("\n=== LinkedHashSet Implementation Analysis ===");
            
            // 1. 插入顺序维护
            System.out.println("\n--- LinkedHashSet Insertion Order ---");
            
            Set<String> linkedHashSet = new LinkedHashSet<>();
            linkedHashSet.add("third");
            linkedHashSet.add("first");
            linkedHashSet.add("second");
            linkedHashSet.add("first"); // 重复，不会改变顺序
            
            System.out.println("LinkedHashSet (insertion order): " + linkedHashSet);
            
            // 对比HashSet的无序性
            Set<String> hashSet = new HashSet<>();
            hashSet.add("third");
            hashSet.add("first");
            hashSet.add("second");
            
            System.out.println("HashSet (no order guarantee): " + hashSet);
            
            // 2. 性能对比
            System.out.println("\n--- Performance Comparison ---");
            
            int size = 100000;
            
            // LinkedHashSet性能
            Set<Integer> linkedSet = new LinkedHashSet<>();
            long startTime = System.nanoTime();
            for (int i = 0; i < size; i++) {
                linkedSet.add(i);
            }
            long linkedHashSetTime = System.nanoTime() - startTime;
            
            // HashSet性能
            Set<Integer> hashSetPerf = new HashSet<>();
            startTime = System.nanoTime();
            for (int i = 0; i < size; i++) {
                hashSetPerf.add(i);
            }
            long hashSetTime = System.nanoTime() - startTime;
            
            System.out.println("LinkedHashSet add time: " + linkedHashSetTime / 1_000_000.0 + " ms");
            System.out.println("HashSet add time: " + hashSetTime / 1_000_000.0 + " ms");
            System.out.println("Performance ratio: " + (linkedHashSetTime / (double) hashSetTime));
        }
        
        /**
         * TreeSet源码分析
         * 
         * 核心特点：
         * 1. 基于TreeMap实现（红黑树）
         * 2. 有序集合，支持自然排序和自定义排序
         * 3. 不允许null值
         * 4. 操作时间复杂度O(log n)
         */
        public static void analyzeTreeSet() {
            System.out.println("\n=== TreeSet Implementation Analysis ===");
            
            // 1. 自然排序
            System.out.println("\n--- TreeSet Natural Ordering ---");
            
            Set<Integer> treeSet = new TreeSet<>();
            treeSet.add(5);
            treeSet.add(2);
            treeSet.add(8);
            treeSet.add(1);
            treeSet.add(9);
            treeSet.add(3);
            
            System.out.println("TreeSet (natural order): " + treeSet);
            
            // 2. 自定义排序
            System.out.println("\n--- TreeSet Custom Ordering ---");
            
            // 降序排列
            Set<String> descendingSet = new TreeSet<>(Collections.reverseOrder());
            descendingSet.add("apple");
            descendingSet.add("banana");
            descendingSet.add("cherry");
            descendingSet.add("date");
            
            System.out.println("TreeSet (descending): " + descendingSet);
            
            // 按长度排序
            Set<String> lengthSet = new TreeSet<>((s1, s2) -> {
                int lengthCompare = Integer.compare(s1.length(), s2.length());
                return lengthCompare != 0 ? lengthCompare : s1.compareTo(s2);
            });
            
            lengthSet.add("a");
            lengthSet.add("bb");
            lengthSet.add("ccc");
            lengthSet.add("dd");
            lengthSet.add("eeee");
            
            System.out.println("TreeSet (by length): " + lengthSet);
            
            // 3. NavigableSet接口功能
            System.out.println("\n--- NavigableSet Operations ---");
            
            NavigableSet<Integer> navigableSet = new TreeSet<>();
            for (int i = 1; i <= 10; i++) {
                navigableSet.add(i * 10); // 10, 20, 30, ..., 100
            }
            
            System.out.println("NavigableSet: " + navigableSet);
            System.out.println("Lower than 55: " + navigableSet.lower(55)); // 50
            System.out.println("Floor of 55: " + navigableSet.floor(55));   // 50
            System.out.println("Ceiling of 55: " + navigableSet.ceiling(55)); // 60
            System.out.println("Higher than 55: " + navigableSet.higher(55)); // 60
            
            System.out.println("Head set (< 50): " + navigableSet.headSet(50));
            System.out.println("Tail set (>= 50): " + navigableSet.tailSet(50));
            System.out.println("Sub set [30, 70): " + navigableSet.subSet(30, 70));
            
            // 4. 性能测试
            System.out.println("\n--- TreeSet Performance ---");
            
            Set<Integer> treeSetPerf = new TreeSet<>();
            Set<Integer> hashSetPerf = new HashSet<>();
            
            int testSize = 100000;
            
            // TreeSet添加性能
            long startTime = System.nanoTime();
            for (int i = 0; i < testSize; i++) {
                treeSetPerf.add((int) (Math.random() * testSize));
            }
            long treeSetAddTime = System.nanoTime() - startTime;
            
            // HashSet添加性能
            startTime = System.nanoTime();
            for (int i = 0; i < testSize; i++) {
                hashSetPerf.add((int) (Math.random() * testSize));
            }
            long hashSetAddTime = System.nanoTime() - startTime;
            
            System.out.println("TreeSet add time: " + treeSetAddTime / 1_000_000.0 + " ms");
            System.out.println("HashSet add time: " + hashSetAddTime / 1_000_000.0 + " ms");
            System.out.println("TreeSet is " + (treeSetAddTime / (double) hashSetAddTime) + "x slower");
        }
    }
    
    /**
     * Map实现类深度分析
     */
    
    static class MapImplementationAnalysis {
        
        /**
         * HashMap源码深度分析
         * 
         * JDK 8重要改进：
         * 1. 数组 + 链表 + 红黑树
         * 2. 链表长度 > 8 转红黑树
         * 3. 红黑树节点 < 6 转链表
         * 4. 扩容优化
         */
        public static void analyzeHashMap() {
            System.out.println("\n=== HashMap Implementation Analysis ===");
            
            // 1. 基本操作和特性
            System.out.println("\n--- HashMap Basic Operations ---");
            
            Map<String, Integer> hashMap = new HashMap<>();
            
            // 添加元素
            hashMap.put("apple", 5);
            hashMap.put("banana", 3);
            hashMap.put("cherry", 8);
            hashMap.put(null, 0);      // 允许null key
            hashMap.put("date", null); // 允许null value
            
            System.out.println("HashMap contents: " + hashMap);
            System.out.println("Size: " + hashMap.size());
            
            // 更新值
            Integer oldValue = hashMap.put("apple", 10);
            System.out.println("Updated apple, old value: " + oldValue);
            
            // 条件操作
            hashMap.putIfAbsent("elderberry", 6);
            hashMap.putIfAbsent("apple", 15); // 不会更新
            
            System.out.println("After putIfAbsent: " + hashMap);
            
            // 2. 哈希冲突和链表转红黑树演示
            System.out.println("\n--- Hash Collision and Tree Conversion ---");
            
            Map<HashCollisionDemo, String> collisionMap = new HashMap<>();
            
            // 添加具有相同哈希值的对象
            for (int i = 0; i < 10; i++) {
                HashCollisionDemo key = new HashCollisionDemo("Key" + i, 1); // 相同哈希值
                collisionMap.put(key, "Value" + i);
            }
            
            System.out.println("Map with hash collisions size: " + collisionMap.size());
            
            // 3. 扩容机制演示
            System.out.println("\n--- HashMap Resize Mechanism ---");
            
            Map<Integer, String> resizeMap = new HashMap<>(4); // 初始容量4
            
            System.out.println("Demonstrating resize behavior:");
            for (int i = 0; i < 20; i++) {
                resizeMap.put(i, "Value" + i);
                
                if (i == 2 || i == 5 || i == 11) { // 可能的扩容点
                    System.out.println("Size " + resizeMap.size() + " - likely resize occurred");
                }
            }
            
            // 4. 性能测试
            System.out.println("\n--- HashMap Performance ---");
            
            Map<Integer, String> perfMap = new HashMap<>();
            int perfSize = 1000000;
            
            // 添加性能
            long startTime = System.nanoTime();
            for (int i = 0; i < perfSize; i++) {
                perfMap.put(i, "Value" + i);
            }
            long putTime = System.nanoTime() - startTime;
            
            // 查找性能
            startTime = System.nanoTime();
            for (int i = 0; i < 100000; i++) {
                perfMap.get(i * 10);
            }
            long getTime = System.nanoTime() - startTime;
            
            System.out.println("Put " + perfSize + " elements: " + putTime / 1_000_000.0 + " ms");
            System.out.println("Get 100000 lookups: " + getTime / 1_000_000.0 + " ms");
        }
        
        /**
         * LinkedHashMap源码分析
         * 
         * 核心特点：
         * 1. 继承自HashMap
         * 2. 维护双向链表保持顺序
         * 3. 支持访问顺序和插入顺序
         * 4. 可实现LRU缓存
         */
        public static void analyzeLinkedHashMap() {
            System.out.println("\n=== LinkedHashMap Implementation Analysis ===");
            
            // 1. 插入顺序维护
            System.out.println("\n--- LinkedHashMap Insertion Order ---");
            
            Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
            linkedHashMap.put("third", 3);
            linkedHashMap.put("first", 1);
            linkedHashMap.put("second", 2);
            
            System.out.println("LinkedHashMap (insertion order): " + linkedHashMap);
            
            // 对比HashMap的无序性
            Map<String, Integer> hashMap = new HashMap<>();
            hashMap.put("third", 3);
            hashMap.put("first", 1);
            hashMap.put("second", 2);
            
            System.out.println("HashMap (no order guarantee): " + hashMap);
            
            // 2. 访问顺序模式
            System.out.println("\n--- LinkedHashMap Access Order ---");
            
            Map<String, Integer> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true);
            accessOrderMap.put("A", 1);
            accessOrderMap.put("B", 2);
            accessOrderMap.put("C", 3);
            accessOrderMap.put("D", 4);
            
            System.out.println("Initial order: " + accessOrderMap);
            
            // 访问元素改变顺序
            accessOrderMap.get("B");
            System.out.println("After accessing B: " + accessOrderMap);
            
            accessOrderMap.get("A");
            System.out.println("After accessing A: " + accessOrderMap);
            
            // 3. LRU缓存实现
            System.out.println("\n--- LRU Cache Implementation ---");
            
            LRUCache<String, String> lruCache = new LRUCache<>(3);
            
            lruCache.put("1", "One");
            lruCache.put("2", "Two");
            lruCache.put("3", "Three");
            System.out.println("Cache after adding 3 items: " + lruCache);
            
            lruCache.get("1"); // 访问，移到最后
            System.out.println("After accessing '1': " + lruCache);
            
            lruCache.put("4", "Four"); // 添加新元素，应该移除最久未使用的
            System.out.println("After adding '4': " + lruCache);
        }
        
        /**
         * TreeMap源码分析
         * 
         * 核心特点：
         * 1. 基于红黑树实现
         * 2. 有序Map，支持自然排序和自定义排序
         * 3. 不允许null key
         * 4. 操作时间复杂度O(log n)
         */
        public static void analyzeTreeMap() {
            System.out.println("\n=== TreeMap Implementation Analysis ===");
            
            // 1. 自然排序
            System.out.println("\n--- TreeMap Natural Ordering ---");
            
            Map<Integer, String> treeMap = new TreeMap<>();
            treeMap.put(5, "Five");
            treeMap.put(2, "Two");
            treeMap.put(8, "Eight");
            treeMap.put(1, "One");
            treeMap.put(9, "Nine");
            
            System.out.println("TreeMap (natural order): " + treeMap);
            
            // 2. 自定义排序
            System.out.println("\n--- TreeMap Custom Ordering ---");
            
            Map<String, Integer> customTreeMap = new TreeMap<>((s1, s2) -> s2.compareTo(s1)); // 降序
            customTreeMap.put("apple", 5);
            customTreeMap.put("banana", 3);
            customTreeMap.put("cherry", 8);
            customTreeMap.put("date", 2);
            
            System.out.println("TreeMap (descending order): " + customTreeMap);
            
            // 3. NavigableMap接口功能
            System.out.println("\n--- NavigableMap Operations ---");
            
            NavigableMap<Integer, String> navigableMap = new TreeMap<>();
            for (int i = 1; i <= 10; i++) {
                navigableMap.put(i * 10, "Value" + (i * 10));
            }
            
            System.out.println("NavigableMap: " + navigableMap);
            
            System.out.println("Lower entry than 55: " + navigableMap.lowerEntry(55));
            System.out.println("Floor entry of 50: " + navigableMap.floorEntry(50));
            System.out.println("Ceiling entry of 55: " + navigableMap.ceilingEntry(55));
            System.out.println("Higher entry than 55: " + navigableMap.higherEntry(55));
            
            System.out.println("Head map (< 50): " + navigableMap.headMap(50));
            System.out.println("Tail map (>= 50): " + navigableMap.tailMap(50));
            System.out.println("Sub map [30, 70): " + navigableMap.subMap(30, 70));
            
            // 4. 性能对比
            System.out.println("\n--- TreeMap vs HashMap Performance ---");
            
            Map<Integer, String> treeMapPerf = new TreeMap<>();
            Map<Integer, String> hashMapPerf = new HashMap<>();
            
            int testSize = 100000;
            
            // TreeMap性能
            long startTime = System.nanoTime();
            for (int i = 0; i < testSize; i++) {
                treeMapPerf.put(i, "Value" + i);
            }
            long treeMapTime = System.nanoTime() - startTime;
            
            // HashMap性能
            startTime = System.nanoTime();
            for (int i = 0; i < testSize; i++) {
                hashMapPerf.put(i, "Value" + i);
            }
            long hashMapTime = System.nanoTime() - startTime;
            
            System.out.println("TreeMap put time: " + treeMapTime / 1_000_000.0 + " ms");
            System.out.println("HashMap put time: " + hashMapTime / 1_000_000.0 + " ms");
            System.out.println("TreeMap is " + (treeMapTime / (double) hashMapTime) + "x slower");
        }
    }
    
    /**
     * 哈希冲突演示类
     */
    static class HashCollisionDemo {
        private String name;
        private int fixedHash;
        
        public HashCollisionDemo(String name, int fixedHash) {
            this.name = name;
            this.fixedHash = fixedHash;
        }
        
        @Override
        public int hashCode() {
            return fixedHash; // 固定哈希值，制造冲突
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            HashCollisionDemo other = (HashCollisionDemo) obj;
            return Objects.equals(name, other.name) && fixedHash == other.fixedHash;
        }
        
        @Override
        public String toString() {
            return name + "(" + fixedHash + ")";
        }
    }
    
    /**
     * LRU缓存实现
     */
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        
        public LRUCache(int capacity) {
            super(16, 0.75f, true); // 访问顺序模式
            this.capacity = capacity;
        }
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
    
    public static void main(String[] args) {
        SetImplementationAnalysis.analyzeHashSet();
        SetImplementationAnalysis.analyzeLinkedHashSet();
        SetImplementationAnalysis.analyzeTreeSet();
        
        MapImplementationAnalysis.analyzeHashMap();
        MapImplementationAnalysis.analyzeLinkedHashMap();
        MapImplementationAnalysis.analyzeTreeMap();
    }
}