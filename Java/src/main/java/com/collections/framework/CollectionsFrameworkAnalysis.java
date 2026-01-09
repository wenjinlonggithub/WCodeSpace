package com.collections.framework;

import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;

/**
 * Java集合框架深度解析
 * 
 * 详细分析：
 * 1. Collection接口层次结构
 * 2. List实现类源码分析（ArrayList、LinkedList、Vector）
 * 3. Set实现类源码分析（HashSet、LinkedHashSet、TreeSet）
 * 4. Map实现类源码分析（HashMap、LinkedHashMap、TreeMap）
 * 5. Queue和Deque实现分析
 * 6. 并发集合类分析
 * 7. 集合性能对比和选择策略
 */

public class CollectionsFrameworkAnalysis {
    
    /**
     * Collection接口层次结构分析
     * 
     * Collection (interface)
     * ├── List (interface)
     * │   ├── ArrayList (class)
     * │   ├── LinkedList (class)
     * │   └── Vector (class)
     * ├── Set (interface)
     * │   ├── HashSet (class)
     * │   ├── LinkedHashSet (class)
     * │   └── TreeSet (class)
     * └── Queue (interface)
     *     ├── PriorityQueue (class)
     *     └── Deque (interface)
     *         └── ArrayDeque (class)
     */
    
    public static void analyzeCollectionHierarchy() {
        System.out.println("=== Collection Framework Hierarchy Analysis ===");
        
        // 1. 接口继承关系演示
        System.out.println("\n--- Interface Inheritance ---");
        
        List<String> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        Queue<String> queue = new PriorityQueue<>();
        
        // 所有集合都实现了Collection接口
        Collection<String> col1 = list;
        Collection<String> col2 = set;
        Collection<String> col3 = queue;
        
        System.out.println("ArrayList implements Collection: " + (col1 instanceof Collection));
        System.out.println("HashSet implements Collection: " + (col2 instanceof Collection));
        System.out.println("PriorityQueue implements Collection: " + (col3 instanceof Collection));
        
        // 2. 通用操作演示
        System.out.println("\n--- Common Collection Operations ---");
        
        Collection<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        
        System.out.println("Size: " + numbers.size());
        System.out.println("Contains 2: " + numbers.contains(2));
        System.out.println("Is empty: " + numbers.isEmpty());
        
        // 批量操作
        Collection<Integer> moreNumbers = Arrays.asList(4, 5, 6);
        numbers.addAll(moreNumbers);
        System.out.println("After addAll: " + numbers);
        
        numbers.removeAll(Arrays.asList(2, 4));
        System.out.println("After removeAll: " + numbers);
        
        // 3. 迭代器模式
        System.out.println("\n--- Iterator Pattern ---");
        
        Iterator<Integer> iterator = numbers.iterator();
        System.out.print("Iterator traversal: ");
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
        
        // 增强for循环（语法糖）
        System.out.print("Enhanced for loop: ");
        for (Integer num : numbers) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
    
    /**
     * List实现类深度分析
     */
    
    static class ListImplementationAnalysis {
        
        /**
         * ArrayList源码分析
         * 
         * 核心特点：
         * 1. 基于动态数组实现
         * 2. 随机访问时间复杂度O(1)
         * 3. 插入删除时间复杂度O(n)
         * 4. 默认初始容量10，扩容因子1.5
         */
        public static void analyzeArrayList() {
            System.out.println("\n=== ArrayList Implementation Analysis ===");
            
            // 1. 容量和扩容机制
            System.out.println("\n--- ArrayList Capacity and Growth ---");
            
            List<Integer> arrayList = new ArrayList<>();
            
            // 使用反射观察内部数组
            try {
                Field elementDataField = ArrayList.class.getDeclaredField("elementData");
                elementDataField.setAccessible(true);
                
                System.out.println("Initial capacity observation:");
                Object[] elementData = (Object[]) elementDataField.get(arrayList);
                System.out.println("Empty list capacity: " + elementData.length);
                
                // 添加第一个元素触发初始化
                arrayList.add(1);
                elementData = (Object[]) elementDataField.get(arrayList);
                System.out.println("After first add capacity: " + elementData.length);
                
                // 观察扩容过程
                for (int i = 2; i <= 15; i++) {
                    arrayList.add(i);
                    Object[] currentData = (Object[]) elementDataField.get(arrayList);
                    if (currentData.length != elementData.length) {
                        System.out.println("Capacity changed at size " + arrayList.size() + 
                            ": " + elementData.length + " -> " + currentData.length);
                        elementData = currentData;
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Reflection failed: " + e.getMessage());
            }
            
            // 2. 性能特性测试
            System.out.println("\n--- ArrayList Performance Characteristics ---");
            
            List<Integer> list = new ArrayList<>();
            
            // 随机访问性能
            for (int i = 0; i < 100000; i++) {
                list.add(i);
            }
            
            long startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                int index = (int) (Math.random() * list.size());
                list.get(index);
            }
            long randomAccessTime = System.nanoTime() - startTime;
            
            // 顺序访问性能
            startTime = System.nanoTime();
            for (Integer value : list) {
                // 访问元素
            }
            long sequentialAccessTime = System.nanoTime() - startTime;
            
            System.out.println("Random access time: " + randomAccessTime / 1_000_000.0 + " ms");
            System.out.println("Sequential access time: " + sequentialAccessTime / 1_000_000.0 + " ms");
            
            // 插入性能测试
            List<Integer> insertList = new ArrayList<>();
            
            // 尾部插入
            startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                insertList.add(i);
            }
            long tailInsertTime = System.nanoTime() - startTime;
            
            // 头部插入
            List<Integer> headInsertList = new ArrayList<>();
            startTime = System.nanoTime();
            for (int i = 0; i < 1000; i++) { // 减少数量，因为头部插入很慢
                headInsertList.add(0, i);
            }
            long headInsertTime = System.nanoTime() - startTime;
            
            System.out.println("Tail insert time (10000): " + tailInsertTime / 1_000_000.0 + " ms");
            System.out.println("Head insert time (1000): " + headInsertTime / 1_000_000.0 + " ms");
        }
        
        /**
         * LinkedList源码分析
         * 
         * 核心特点：
         * 1. 基于双向链表实现
         * 2. 插入删除时间复杂度O(1)
         * 3. 随机访问时间复杂度O(n)
         * 4. 实现了List和Deque接口
         */
        public static void analyzeLinkedList() {
            System.out.println("\n=== LinkedList Implementation Analysis ===");
            
            // 1. 内部结构分析
            System.out.println("\n--- LinkedList Internal Structure ---");
            
            LinkedList<String> linkedList = new LinkedList<>();
            linkedList.add("First");
            linkedList.add("Second");
            linkedList.add("Third");
            
            try {
                // 观察内部节点结构
                Field firstField = LinkedList.class.getDeclaredField("first");
                Field lastField = LinkedList.class.getDeclaredField("last");
                Field sizeField = LinkedList.class.getDeclaredField("size");
                
                firstField.setAccessible(true);
                lastField.setAccessible(true);
                sizeField.setAccessible(true);
                
                Object firstNode = firstField.get(linkedList);
                Object lastNode = lastField.get(linkedList);
                int size = (Integer) sizeField.get(linkedList);
                
                System.out.println("Size: " + size);
                System.out.println("First node: " + (firstNode != null ? "exists" : "null"));
                System.out.println("Last node: " + (lastNode != null ? "exists" : "null"));
                
            } catch (Exception e) {
                System.out.println("Reflection failed: " + e.getMessage());
            }
            
            // 2. 性能特性测试
            System.out.println("\n--- LinkedList Performance Characteristics ---");
            
            LinkedList<Integer> list = new LinkedList<>();
            
            // 头部插入性能
            long startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                list.addFirst(i);
            }
            long headInsertTime = System.nanoTime() - startTime;
            
            // 尾部插入性能
            list.clear();
            startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                list.addLast(i);
            }
            long tailInsertTime = System.nanoTime() - startTime;
            
            // 随机访问性能
            startTime = System.nanoTime();
            for (int i = 0; i < 1000; i++) { // 减少数量，因为随机访问很慢
                int index = (int) (Math.random() * list.size());
                list.get(index);
            }
            long randomAccessTime = System.nanoTime() - startTime;
            
            System.out.println("Head insert time (10000): " + headInsertTime / 1_000_000.0 + " ms");
            System.out.println("Tail insert time (10000): " + tailInsertTime / 1_000_000.0 + " ms");
            System.out.println("Random access time (1000): " + randomAccessTime / 1_000_000.0 + " ms");
            
            // 3. Deque接口功能
            System.out.println("\n--- LinkedList as Deque ---");
            
            Deque<String> deque = new LinkedList<>();
            
            // 双端队列操作
            deque.addFirst("First");
            deque.addLast("Last");
            deque.addFirst("New First");
            deque.addLast("New Last");
            
            System.out.println("Deque contents: " + deque);
            System.out.println("Remove first: " + deque.removeFirst());
            System.out.println("Remove last: " + deque.removeLast());
            System.out.println("Final contents: " + deque);
            
            // 栈操作
            deque.push("Stack Top");
            System.out.println("After push: " + deque);
            System.out.println("Pop: " + deque.pop());
            System.out.println("After pop: " + deque);
        }
        
        /**
         * Vector源码分析
         * 
         * 核心特点：
         * 1. 基于动态数组实现（类似ArrayList）
         * 2. 线程安全（所有方法都是synchronized）
         * 3. 默认扩容因子2.0
         * 4. 历史遗留类，现在很少使用
         */
        public static void analyzeVector() {
            System.out.println("\n=== Vector Implementation Analysis ===");
            
            // 1. 线程安全性验证
            System.out.println("\n--- Vector Thread Safety ---");
            
            Vector<Integer> vector = new Vector<>();
            int threadCount = 10;
            int operationsPerThread = 1000;
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            vector.add(threadId * operationsPerThread + j);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await();
                System.out.println("Vector final size: " + vector.size());
                System.out.println("Expected size: " + (threadCount * operationsPerThread));
                System.out.println("Thread safety: " + 
                    (vector.size() == threadCount * operationsPerThread ? "PASS" : "FAIL"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            executor.shutdown();
            
            // 2. 扩容机制对比
            System.out.println("\n--- Vector vs ArrayList Growth ---");
            
            // Vector扩容（2倍）
            Vector<Integer> vectorGrowth = new Vector<>(5);
            System.out.println("Vector growth pattern:");
            for (int i = 0; i < 20; i++) {
                vectorGrowth.add(i);
                if (i == 4 || i == 9 || i == 19) { // 关键扩容点
                    System.out.println("Size " + vectorGrowth.size() + ", Capacity: " + vectorGrowth.capacity());
                }
            }
            
            // ArrayList扩容（1.5倍）
            ArrayList<Integer> arrayListGrowth = new ArrayList<>(5);
            System.out.println("\nArrayList growth pattern (for comparison):");
            try {
                Field elementDataField = ArrayList.class.getDeclaredField("elementData");
                elementDataField.setAccessible(true);
                
                for (int i = 0; i < 20; i++) {
                    arrayListGrowth.add(i);
                    if (i == 4 || i == 9 || i == 19) { // 关键扩容点
                        Object[] elementData = (Object[]) elementDataField.get(arrayListGrowth);
                        System.out.println("Size " + arrayListGrowth.size() + 
                            ", Capacity: " + elementData.length);
                    }
                }
            } catch (Exception e) {
                System.out.println("Reflection failed: " + e.getMessage());
            }
        }
        
        /**
         * List实现类性能对比
         */
        public static void performanceComparison() {
            System.out.println("\n=== List Implementations Performance Comparison ===");
            
            int size = 50000;
            
            // 1. 随机访问性能对比
            System.out.println("\n--- Random Access Performance ---");
            
            List<Integer> arrayList = new ArrayList<>();
            List<Integer> linkedList = new LinkedList<>();
            List<Integer> vector = new Vector<>();
            
            // 填充数据
            for (int i = 0; i < size; i++) {
                arrayList.add(i);
                linkedList.add(i);
                vector.add(i);
            }
            
            int accessCount = 10000;
            Random random = new Random();
            
            // ArrayList随机访问
            long startTime = System.nanoTime();
            for (int i = 0; i < accessCount; i++) {
                int index = random.nextInt(size);
                arrayList.get(index);
            }
            long arrayListTime = System.nanoTime() - startTime;
            
            // LinkedList随机访问
            startTime = System.nanoTime();
            for (int i = 0; i < accessCount / 10; i++) { // 减少测试次数
                int index = random.nextInt(size);
                linkedList.get(index);
            }
            long linkedListTime = (System.nanoTime() - startTime) * 10; // 调整为相同规模
            
            // Vector随机访问
            startTime = System.nanoTime();
            for (int i = 0; i < accessCount; i++) {
                int index = random.nextInt(size);
                vector.get(index);
            }
            long vectorTime = System.nanoTime() - startTime;
            
            System.out.println("ArrayList random access: " + arrayListTime / 1_000_000.0 + " ms");
            System.out.println("LinkedList random access: " + linkedListTime / 1_000_000.0 + " ms");
            System.out.println("Vector random access: " + vectorTime / 1_000_000.0 + " ms");
            
            // 2. 插入性能对比
            System.out.println("\n--- Insertion Performance ---");
            
            List<Integer> arrayListInsert = new ArrayList<>();
            List<Integer> linkedListInsert = new LinkedList<>();
            
            int insertCount = 10000;
            
            // ArrayList头部插入
            startTime = System.nanoTime();
            for (int i = 0; i < insertCount / 10; i++) { // 减少测试次数
                arrayListInsert.add(0, i);
            }
            long arrayListInsertTime = (System.nanoTime() - startTime) * 10;
            
            // LinkedList头部插入
            startTime = System.nanoTime();
            for (int i = 0; i < insertCount; i++) {
                linkedListInsert.add(0, i);
            }
            long linkedListInsertTime = System.nanoTime() - startTime;
            
            System.out.println("ArrayList head insert: " + arrayListInsertTime / 1_000_000.0 + " ms");
            System.out.println("LinkedList head insert: " + linkedListInsertTime / 1_000_000.0 + " ms");
            
            System.out.println("\nPerformance Summary:");
            System.out.println("- ArrayList: 最佳随机访问性能，尾部插入快，头部插入慢");
            System.out.println("- LinkedList: 最佳插入删除性能，随机访问慢");
            System.out.println("- Vector: 类似ArrayList但有同步开销，性能较差");
        }
    }
    
    public static void main(String[] args) {
        analyzeCollectionHierarchy();
        
        ListImplementationAnalysis.analyzeArrayList();
        ListImplementationAnalysis.analyzeLinkedList();
        ListImplementationAnalysis.analyzeVector();
        ListImplementationAnalysis.performanceComparison();
    }
}