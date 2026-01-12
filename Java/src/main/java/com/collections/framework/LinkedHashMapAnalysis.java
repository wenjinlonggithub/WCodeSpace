package com.collections.framework;

import java.util.*;
import java.lang.reflect.*;

/**
 * LinkedHashMap核心原理深度剖析
 *
 * 本文详细分析：
 * 1. LinkedHashMap的内部数据结构（HashMap + 双向链表）
 * 2. Entry节点的扩展（增加before和after指针）
 * 3. 插入顺序和访问顺序的实现原理
 * 4. 核心方法源码级分析（put、get、remove等）
 * 5. LRU缓存的完整实现原理
 * 6. 与HashMap的性能对比
 * 7. 实际应用场景分析
 *
 * @author Architecture Team
 */
public class LinkedHashMapAnalysis {

    /**
     * ========================================
     * 第一部分：LinkedHashMap核心原理概述
     * ========================================
     *
     * LinkedHashMap = HashMap + 双向链表
     *
     * 继承关系：
     * LinkedHashMap extends HashMap
     *
     * 核心特点：
     * 1. 在HashMap的基础上增加了双向链表
     * 2. 维护元素的插入顺序或访问顺序
     * 3. 通过accessOrder参数控制顺序类型
     *    - false（默认）：插入顺序
     *    - true：访问顺序（适合实现LRU）
     * 4. 性能略低于HashMap，但仍然是O(1)
     *
     * 数据结构：
     * +------------------+
     * | HashMap 哈希表   |  ← 快速查找（O(1)）
     * +------------------+
     *         +
     *         |
     * +------------------+
     * | 双向链表         |  ← 维护顺序
     * +------------------+
     */

    public static void demonstrateCoreStructure() {
        System.out.println("=== LinkedHashMap核心结构演示 ===\n");

        // 1. 继承关系验证
        System.out.println("--- 1. 继承关系验证 ---");
        LinkedHashMap<String, Integer> linkedMap = new LinkedHashMap<>();

        System.out.println("LinkedHashMap是HashMap的子类: " + (linkedMap instanceof HashMap));
        System.out.println("LinkedHashMap是Map的实现: " + (linkedMap instanceof Map));

        // 2. 插入顺序维护（默认行为）
        System.out.println("\n--- 2. 插入顺序维护 ---");
        linkedMap.put("C", 3);
        linkedMap.put("A", 1);
        linkedMap.put("D", 4);
        linkedMap.put("B", 2);

        System.out.println("插入顺序：C, A, D, B");
        System.out.print("遍历顺序：");
        linkedMap.forEach((k, v) -> System.out.print(k + " "));
        System.out.println("\n说明：LinkedHashMap维护了插入顺序");

        // 对比HashMap的无序性
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("C", 3);
        hashMap.put("A", 1);
        hashMap.put("D", 4);
        hashMap.put("B", 2);

        System.out.print("HashMap遍历顺序：");
        hashMap.forEach((k, v) -> System.out.print(k + " "));
        System.out.println("\n说明：HashMap不保证顺序");

        // 3. 访问顺序模式
        System.out.println("\n--- 3. 访问顺序模式 ---");
        LinkedHashMap<String, Integer> accessOrderMap =
            new LinkedHashMap<>(16, 0.75f, true); // accessOrder=true

        accessOrderMap.put("A", 1);
        accessOrderMap.put("B", 2);
        accessOrderMap.put("C", 3);
        accessOrderMap.put("D", 4);

        System.out.println("初始顺序：" + accessOrderMap.keySet());

        // 访问元素会改变顺序
        accessOrderMap.get("B");
        System.out.println("访问B后：" + accessOrderMap.keySet());

        accessOrderMap.get("A");
        System.out.println("访问A后：" + accessOrderMap.keySet());

        System.out.println("说明：访问顺序模式下，被访问的元素会移到链表末尾");
    }

    /**
     * ========================================
     * 第二部分：LinkedHashMap内部数据结构深度剖析
     * ========================================
     *
     * HashMap.Node（普通节点）：
     * class Node<K,V> {
     *     final int hash;
     *     final K key;
     *     V value;
     *     Node<K,V> next;  // 用于链表或红黑树
     * }
     *
     * LinkedHashMap.Entry（扩展节点）：
     * class Entry<K,V> extends HashMap.Node<K,V> {
     *     Entry<K,V> before;  // 双向链表的前驱指针
     *     Entry<K,V> after;   // 双向链表的后继指针
     * }
     *
     * LinkedHashMap的关键字段：
     * - transient Entry<K,V> head;  // 双向链表头节点
     * - transient Entry<K,V> tail;  // 双向链表尾节点
     * - final boolean accessOrder;  // 顺序模式（false=插入顺序，true=访问顺序）
     */

    public static void analyzeInternalStructure() {
        System.out.println("\n\n=== LinkedHashMap内部结构深度分析 ===\n");

        // 1. Entry节点结构分析
        System.out.println("--- 1. Entry节点结构分析 ---");

        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("First", 1);
        map.put("Second", 2);
        map.put("Third", 3);

        try {
            // 使用反射查看内部结构
            Field headField = LinkedHashMap.class.getDeclaredField("head");
            Field tailField = LinkedHashMap.class.getDeclaredField("tail");
            headField.setAccessible(true);
            tailField.setAccessible(true);

            Object head = headField.get(map);
            Object tail = tailField.get(map);

            System.out.println("链表头节点: " + (head != null ? "存在" : "null"));
            System.out.println("链表尾节点: " + (tail != null ? "存在" : "null"));

            if (head != null) {
                Class<?> entryClass = head.getClass();
                System.out.println("节点类型: " + entryClass.getName());

                // 检查是否有before和after字段
                try {
                    Field beforeField = entryClass.getDeclaredField("before");
                    Field afterField = entryClass.getDeclaredField("after");
                    System.out.println("包含before指针: " + (beforeField != null));
                    System.out.println("包含after指针: " + (afterField != null));
                } catch (NoSuchFieldException e) {
                    System.out.println("before/after字段未找到（可能是私有内部类）");
                }

                // 遍历链表
                System.out.println("\n双向链表遍历（从头到尾）：");
                traverseLinkedList(head);
            }

        } catch (Exception e) {
            System.out.println("反射访问失败: " + e.getMessage());
        }

        // 2. 数据结构示意图
        System.out.println("\n--- 2. 数据结构示意图 ---");
        System.out.println("""
            LinkedHashMap内部结构：

            哈希表数组（从HashMap继承）：
            +-------+-------+-------+-------+
            | [0]   | [1]   | [2]   | [3]   | ... (数组，用于快速查找)
            +-------+-------+-------+-------+
                |       |       |       |
                ↓       ↓       ↓       ↓
              Entry   Entry   Entry   Entry  (HashMap.Node链表或红黑树)

            双向链表（LinkedHashMap新增）：
            head → Entry ⇄ Entry ⇄ Entry ⇄ Entry ← tail
                   (First)  (Second) (Third)  ...

            每个Entry节点同时属于：
            1. 哈希表的某个桶（通过hash和next指针）
            2. 双向链表（通过before和after指针）

            这种设计实现了：
            - O(1)的查找、插入、删除（通过哈希表）
            - O(1)的顺序维护（通过双向链表）
            """);
    }

    /**
     * 辅助方法：遍历LinkedHashMap的内部双向链表
     */
    private static void traverseLinkedList(Object headNode) {
        try {
            Object current = headNode;
            int count = 0;

            while (current != null && count < 20) { // 限制最多20个节点，防止无限循环
                Class<?> nodeClass = current.getClass();

                // 获取key和value
                Field keyField = findFieldInHierarchy(nodeClass, "key");
                Field valueField = findFieldInHierarchy(nodeClass, "value");
                Field afterField = findFieldInHierarchy(nodeClass, "after");

                if (keyField != null && valueField != null) {
                    keyField.setAccessible(true);
                    valueField.setAccessible(true);

                    Object key = keyField.get(current);
                    Object value = valueField.get(current);

                    System.out.printf("  [%d] %s = %s%n", count, key, value);
                }

                // 移动到下一个节点
                if (afterField != null) {
                    afterField.setAccessible(true);
                    current = afterField.get(current);
                } else {
                    break;
                }

                count++;
            }
        } catch (Exception e) {
            System.out.println("链表遍历失败: " + e.getMessage());
        }
    }

    /**
     * 辅助方法：在类层次结构中查找字段
     */
    private static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * ========================================
     * 第三部分：核心方法源码级分析
     * ========================================
     *
     * LinkedHashMap重写了HashMap的关键方法，
     * 在保持哈希表功能的同时维护双向链表。
     */

    public static void analyzeCoreMethods() {
        System.out.println("\n\n=== LinkedHashMap核心方法源码分析 ===\n");

        // 1. put方法分析
        System.out.println("--- 1. put方法分析 ---");
        System.out.println("""
            LinkedHashMap的put操作流程：

            1. 调用HashMap.put(key, value)
               - 计算hash值
               - 在哈希表中查找或创建节点

            2. 如果是新节点，调用LinkedHashMap.newNode()
               - 创建LinkedHashMap.Entry对象（而非HashMap.Node）
               - 通过linkNodeLast()将新节点链接到双向链表尾部

            3. 如果是更新，不改变链表顺序（插入顺序模式）
               或移动到链表尾部（访问顺序模式）

            关键方法：

            Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
                LinkedHashMap.Entry<K,V> p =
                    new LinkedHashMap.Entry<>(hash, key, value, e);
                linkNodeLast(p);  // 链接到链表尾部
                return p;
            }

            private void linkNodeLast(Entry<K,V> p) {
                Entry<K,V> last = tail;
                tail = p;
                if (last == null)
                    head = p;  // 第一个节点
                else {
                    p.before = last;
                    last.after = p;
                }
            }
            """);

        // 演示put操作
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        System.out.println("执行操作序列：");
        map.put("A", "Apple");
        System.out.println("1. put(A, Apple) - 创建新节点，链接到链表尾部");

        map.put("B", "Banana");
        System.out.println("2. put(B, Banana) - 创建新节点，链接到链表尾部");

        map.put("C", "Cherry");
        System.out.println("3. put(C, Cherry) - 创建新节点，链接到链表尾部");

        map.put("A", "Avocado");
        System.out.println("4. put(A, Avocado) - 更新值，不改变链表顺序（插入顺序模式）");

        System.out.println("\n当前map内容: " + map);
        System.out.println("说明：A仍然在第一位，虽然值被更新了");

        // 2. get方法分析
        System.out.println("\n--- 2. get方法分析 ---");
        System.out.println("""
            LinkedHashMap的get操作流程：

            插入顺序模式（accessOrder=false）：
            1. 调用HashMap.get(key) 获取值
            2. 返回值，不改变链表结构

            访问顺序模式（accessOrder=true）：
            1. 调用HashMap.get(key) 获取值
            2. 调用afterNodeAccess(node) 将节点移到链表尾部
            3. 返回值

            关键方法：

            void afterNodeAccess(Node<K,V> e) {
                Entry<K,V> last;
                if (accessOrder && (last = tail) != e) {
                    Entry<K,V> p = (Entry<K,V>)e;
                    Entry<K,V> b = p.before;
                    Entry<K,V> a = p.after;

                    // 从链表中移除p
                    p.after = null;
                    if (b == null)
                        head = a;
                    else
                        b.after = a;
                    if (a != null)
                        a.before = b;
                    else
                        last = b;

                    // 将p移到链表尾部
                    if (last == null)
                        head = p;
                    else {
                        p.before = last;
                        last.after = p;
                    }
                    tail = p;
                }
            }
            """);

        // 演示访问顺序模式
        LinkedHashMap<String, String> accessMap = new LinkedHashMap<>(16, 0.75f, true);

        accessMap.put("A", "Apple");
        accessMap.put("B", "Banana");
        accessMap.put("C", "Cherry");
        accessMap.put("D", "Date");

        System.out.println("访问顺序模式演示：");
        System.out.println("初始顺序: " + accessMap.keySet());

        accessMap.get("B");
        System.out.println("get(B)后: " + accessMap.keySet());
        System.out.println("说明：B被移到了末尾");

        accessMap.get("A");
        System.out.println("get(A)后: " + accessMap.keySet());
        System.out.println("说明：A被移到了末尾");

        // 3. remove方法分析
        System.out.println("\n--- 3. remove方法分析 ---");
        System.out.println("""
            LinkedHashMap的remove操作流程：

            1. 调用HashMap.remove(key)
               - 从哈希表中删除节点

            2. 调用afterNodeRemoval(node)
               - 从双向链表中移除节点
               - 更新before和after指针

            关键方法：

            void afterNodeRemoval(Node<K,V> e) {
                Entry<K,V> p = (Entry<K,V>)e;
                Entry<K,V> b = p.before;
                Entry<K,V> a = p.after;

                // 清空指针
                p.before = p.after = null;

                // 更新链表连接
                if (b == null)
                    head = a;
                else
                    b.after = a;

                if (a == null)
                    tail = b;
                else
                    a.before = b;
            }
            """);

        // 演示remove操作
        LinkedHashMap<String, Integer> removeMap = new LinkedHashMap<>();
        removeMap.put("A", 1);
        removeMap.put("B", 2);
        removeMap.put("C", 3);
        removeMap.put("D", 4);

        System.out.println("删除操作演示：");
        System.out.println("初始: " + removeMap.keySet());

        removeMap.remove("B");
        System.out.println("remove(B)后: " + removeMap.keySet());
        System.out.println("说明：B从链表中被移除，A和C现在直接相连");

        removeMap.remove("A");
        System.out.println("remove(A)后: " + removeMap.keySet());
        System.out.println("说明：头节点被移除，C成为新的头节点");
    }

    /**
     * ========================================
     * 第四部分：插入顺序 vs 访问顺序详细对比
     * ========================================
     */

    public static void compareOrderModes() {
        System.out.println("\n\n=== 插入顺序 vs 访问顺序详细对比 ===\n");

        // 1. 插入顺序模式
        System.out.println("--- 1. 插入顺序模式（accessOrder=false，默认） ---");
        LinkedHashMap<String, Integer> insertionOrderMap = new LinkedHashMap<>();

        insertionOrderMap.put("C", 3);
        insertionOrderMap.put("A", 1);
        insertionOrderMap.put("D", 4);
        insertionOrderMap.put("B", 2);

        System.out.println("插入顺序：C, A, D, B");
        System.out.println("当前顺序: " + insertionOrderMap.keySet());

        // 访问元素
        insertionOrderMap.get("A");
        insertionOrderMap.get("C");
        System.out.println("访问A和C后: " + insertionOrderMap.keySet());
        System.out.println("说明：顺序不变，仍然是插入顺序");

        // 更新元素
        insertionOrderMap.put("A", 10);
        System.out.println("更新A后: " + insertionOrderMap.keySet());
        System.out.println("说明：更新不改变顺序");

        // 2. 访问顺序模式
        System.out.println("\n--- 2. 访问顺序模式（accessOrder=true） ---");
        LinkedHashMap<String, Integer> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true);

        accessOrderMap.put("C", 3);
        accessOrderMap.put("A", 1);
        accessOrderMap.put("D", 4);
        accessOrderMap.put("B", 2);

        System.out.println("插入顺序：C, A, D, B");
        System.out.println("当前顺序: " + accessOrderMap.keySet());

        // 访问元素
        accessOrderMap.get("A");
        System.out.println("访问A后: " + accessOrderMap.keySet());
        System.out.println("说明：A移到了末尾");

        accessOrderMap.get("C");
        System.out.println("访问C后: " + accessOrderMap.keySet());
        System.out.println("说明：C移到了末尾");

        // 更新元素
        accessOrderMap.put("D", 40);
        System.out.println("更新D后: " + accessOrderMap.keySet());
        System.out.println("说明：put操作也会触发访问，D移到了末尾");

        // 3. 什么操作会触发"访问"？
        System.out.println("\n--- 3. 触发访问的操作（accessOrder=true时） ---");
        LinkedHashMap<String, Integer> testMap = new LinkedHashMap<>(16, 0.75f, true);
        testMap.put("A", 1);
        testMap.put("B", 2);
        testMap.put("C", 3);

        System.out.println("初始顺序: " + testMap.keySet());

        testMap.get("A");
        System.out.println("get(A)后: " + testMap.keySet());

        testMap.put("B", 20);
        System.out.println("put(B, 20)后: " + testMap.keySet());

        testMap.getOrDefault("C", 0);
        System.out.println("getOrDefault(C)后: " + testMap.keySet());

        testMap.containsKey("A");
        System.out.println("containsKey(A)后: " + testMap.keySet());
        System.out.println("说明：containsKey不会触发访问");

        testMap.replace("A", 10);
        System.out.println("replace(A, 10)后: " + testMap.keySet());
        System.out.println("说明：replace会触发访问");
    }

    /**
     * ========================================
     * 第五部分：LRU缓存完整实现和原理
     * ========================================
     *
     * LRU (Least Recently Used) 最近最少使用缓存
     *
     * 原理：
     * 1. 使用LinkedHashMap的访问顺序模式
     * 2. 每次访问元素时，该元素移到链表末尾
     * 3. 链表头部的元素就是最久未使用的
     * 4. 当缓存满时，删除链表头部元素
     */

    public static void analyzeLRUCache() {
        System.out.println("\n\n=== LRU缓存实现原理和实战 ===\n");

        // 1. LRU缓存原理
        System.out.println("--- 1. LRU缓存原理 ---");
        System.out.println("""
            LRU缓存的核心思想：

            1. 保留最近使用的数据
            2. 淘汰最久未使用的数据

            LinkedHashMap实现LRU的关键：

            1. 使用访问顺序模式（accessOrder=true）
               - 最近访问的元素在链表尾部
               - 最久未访问的元素在链表头部

            2. 重写removeEldestEntry()方法
               - 在插入新元素后被调用
               - 返回true表示应该删除最老的元素
               - 可以根据size()判断是否超过容量

            源码示例：

            protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
                return size() > capacity;  // 超过容量就删除最老元素
            }

            数据结构示意：

            head → [最久未使用] ⇄ ... ⇄ [最近使用] ← tail
                   ↑ 淘汰候选           ↑ 最近访问
            """);

        // 2. LRU缓存实现
        System.out.println("\n--- 2. LRU缓存实现演示 ---");

        LRUCache<Integer, String> lruCache = new LRUCache<>(3); // 容量为3

        System.out.println("创建容量为3的LRU缓存");

        lruCache.put(1, "One");
        System.out.println("put(1, One): " + lruCache);

        lruCache.put(2, "Two");
        System.out.println("put(2, Two): " + lruCache);

        lruCache.put(3, "Three");
        System.out.println("put(3, Three): " + lruCache);
        System.out.println("缓存已满，当前顺序：1 → 2 → 3");

        lruCache.get(1);
        System.out.println("\nget(1): " + lruCache);
        System.out.println("访问1后，顺序变为：2 → 3 → 1");

        lruCache.put(4, "Four");
        System.out.println("\nput(4, Four): " + lruCache);
        System.out.println("容量满，删除最久未使用的2，顺序：3 → 1 → 4");

        lruCache.get(3);
        System.out.println("\nget(3): " + lruCache);
        System.out.println("访问3后，顺序：1 → 4 → 3");

        lruCache.put(5, "Five");
        System.out.println("\nput(5, Five): " + lruCache);
        System.out.println("容量满，删除最久未使用的1，顺序：4 → 3 → 5");

        // 3. LRU缓存高级特性
        System.out.println("\n--- 3. LRU缓存高级特性 ---");

        // 3.1 带过期时间的LRU缓存
        System.out.println("\n3.1 带统计信息的LRU缓存：");
        StatisticsLRUCache<String, String> statsCache = new StatisticsLRUCache<>(3);

        statsCache.put("A", "Apple");
        statsCache.put("B", "Banana");
        statsCache.put("C", "Cherry");

        statsCache.get("A");
        statsCache.get("B");
        statsCache.get("A");
        statsCache.get("Missing"); // 缓存未命中

        statsCache.put("D", "Date"); // 触发淘汰

        System.out.println("缓存状态：" + statsCache);
        System.out.println(statsCache.getStatistics());

        // 4. LRU缓存的实际应用场景
        System.out.println("\n--- 4. LRU缓存实际应用场景 ---");
        System.out.println("""
            LRU缓存的典型应用：

            1. 数据库查询缓存
               - 缓存最近查询的结果
               - 减少数据库访问

            2. 页面缓存
               - Web服务器缓存热门页面
               - 提高响应速度

            3. 图片缓存
               - 移动应用中缓存最近查看的图片
               - 节省内存和流量

            4. DNS缓存
               - 缓存最近解析的域名
               - 加快网络访问

            5. 分页数据缓存
               - 缓存最近访问的分页数据
               - 提升用户体验

            优点：
            - 实现简单（基于LinkedHashMap）
            - 性能高（O(1)操作）
            - 自动管理内存

            缺点：
            - 不适合所有场景（有些场景需要LFU等其他策略）
            - 线程不安全（需要外部同步）
            """);
    }

    /**
     * 基础LRU缓存实现
     */
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public LRUCache(int capacity) {
            super(capacity, 0.75f, true); // accessOrder=true
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    /**
     * 带统计信息的LRU缓存
     */
    static class StatisticsLRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        private long hits = 0;      // 命中次数
        private long misses = 0;    // 未命中次数
        private long evictions = 0; // 淘汰次数

        public StatisticsLRUCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        public V get(Object key) {
            V value = super.get(key);
            if (value != null) {
                hits++;
            } else {
                misses++;
            }
            return value;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            boolean shouldRemove = size() > capacity;
            if (shouldRemove) {
                evictions++;
                System.out.println("  [淘汰] 移除最久未使用的元素: " + eldest.getKey());
            }
            return shouldRemove;
        }

        public String getStatistics() {
            long total = hits + misses;
            double hitRate = total > 0 ? (hits * 100.0 / total) : 0;

            return String.format("""
                缓存统计：
                - 命中次数: %d
                - 未命中次数: %d
                - 命中率: %.2f%%
                - 淘汰次数: %d
                - 当前大小: %d/%d
                """, hits, misses, hitRate, evictions, size(), capacity);
        }
    }

    /**
     * ========================================
     * 第六部分：性能分析和对比
     * ========================================
     */

    public static void performanceAnalysis() {
        System.out.println("\n\n=== LinkedHashMap性能分析 ===\n");

        int testSize = 100000;
        int accessCount = 10000;

        // 1. 插入性能对比
        System.out.println("--- 1. 插入性能对比 ---");

        // HashMap
        HashMap<Integer, String> hashMap = new HashMap<>();
        long startTime = System.nanoTime();
        for (int i = 0; i < testSize; i++) {
            hashMap.put(i, "Value" + i);
        }
        long hashMapPutTime = System.nanoTime() - startTime;

        // LinkedHashMap（插入顺序）
        LinkedHashMap<Integer, String> linkedHashMap = new LinkedHashMap<>();
        startTime = System.nanoTime();
        for (int i = 0; i < testSize; i++) {
            linkedHashMap.put(i, "Value" + i);
        }
        long linkedHashMapPutTime = System.nanoTime() - startTime;

        // LinkedHashMap（访问顺序）
        LinkedHashMap<Integer, String> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true);
        startTime = System.nanoTime();
        for (int i = 0; i < testSize; i++) {
            accessOrderMap.put(i, "Value" + i);
        }
        long accessOrderPutTime = System.nanoTime() - startTime;

        System.out.printf("HashMap插入%d个元素: %.2f ms%n", testSize, hashMapPutTime / 1_000_000.0);
        System.out.printf("LinkedHashMap（插入顺序）: %.2f ms (%.2fx)%n",
            linkedHashMapPutTime / 1_000_000.0,
            linkedHashMapPutTime / (double) hashMapPutTime);
        System.out.printf("LinkedHashMap（访问顺序）: %.2f ms (%.2fx)%n",
            accessOrderPutTime / 1_000_000.0,
            accessOrderPutTime / (double) hashMapPutTime);

        // 2. 查找性能对比
        System.out.println("\n--- 2. 查找性能对比 ---");

        Random random = new Random(42);

        // HashMap查找
        startTime = System.nanoTime();
        for (int i = 0; i < accessCount; i++) {
            int key = random.nextInt(testSize);
            hashMap.get(key);
        }
        long hashMapGetTime = System.nanoTime() - startTime;

        // LinkedHashMap（插入顺序）查找
        random.setSeed(42);
        startTime = System.nanoTime();
        for (int i = 0; i < accessCount; i++) {
            int key = random.nextInt(testSize);
            linkedHashMap.get(key);
        }
        long linkedHashMapGetTime = System.nanoTime() - startTime;

        // LinkedHashMap（访问顺序）查找
        random.setSeed(42);
        startTime = System.nanoTime();
        for (int i = 0; i < accessCount; i++) {
            int key = random.nextInt(testSize);
            accessOrderMap.get(key);
        }
        long accessOrderGetTime = System.nanoTime() - startTime;

        System.out.printf("HashMap查找%d次: %.2f ms%n", accessCount, hashMapGetTime / 1_000_000.0);
        System.out.printf("LinkedHashMap（插入顺序）: %.2f ms (%.2fx)%n",
            linkedHashMapGetTime / 1_000_000.0,
            linkedHashMapGetTime / (double) hashMapGetTime);
        System.out.printf("LinkedHashMap（访问顺序）: %.2f ms (%.2fx)%n",
            accessOrderGetTime / 1_000_000.0,
            accessOrderGetTime / (double) hashMapGetTime);

        // 3. 遍历性能对比
        System.out.println("\n--- 3. 遍历性能对比 ---");

        // HashMap遍历
        startTime = System.nanoTime();
        for (Map.Entry<Integer, String> entry : hashMap.entrySet()) {
            // 访问entry
        }
        long hashMapIterTime = System.nanoTime() - startTime;

        // LinkedHashMap遍历
        startTime = System.nanoTime();
        for (Map.Entry<Integer, String> entry : linkedHashMap.entrySet()) {
            // 访问entry
        }
        long linkedHashMapIterTime = System.nanoTime() - startTime;

        System.out.printf("HashMap遍历: %.2f ms%n", hashMapIterTime / 1_000_000.0);
        System.out.printf("LinkedHashMap遍历: %.2f ms (%.2fx)%n",
            linkedHashMapIterTime / 1_000_000.0,
            linkedHashMapIterTime / (double) hashMapIterTime);

        // 4. 内存占用分析
        System.out.println("\n--- 4. 内存占用分析 ---");
        System.out.println("""
            内存占用对比：

            HashMap.Node节点：
            - int hash (4 bytes)
            - K key (引用，8 bytes)
            - V value (引用，8 bytes)
            - Node<K,V> next (引用，8 bytes)
            - 对象头 (12-16 bytes)
            总计：约 40-44 bytes/节点

            LinkedHashMap.Entry节点：
            - 继承Node的所有字段
            - Entry<K,V> before (引用，8 bytes)
            - Entry<K,V> after (引用，8 bytes)
            总计：约 56-60 bytes/节点

            内存开销：LinkedHashMap比HashMap多约 40%

            结论：
            - LinkedHashMap的内存占用明显高于HashMap
            - 需要在顺序维护和内存占用之间权衡
            """);

        // 5. 性能总结
        System.out.println("\n--- 5. 性能总结 ---");
        System.out.println("""
            性能特征总结：

            时间复杂度：
            - get()：O(1)
            - put()：O(1)
            - remove()：O(1)
            - 遍历：O(n)

            与HashMap对比：
            - 插入：慢 10-20%（需要维护链表）
            - 查找：
              * 插入顺序模式：慢 5-10%（额外的Entry对象）
              * 访问顺序模式：慢 20-30%（需要调整链表）
            - 遍历：快 10-20%（顺序遍历链表，缓存友好）
            - 内存：多 40%（额外的before和after指针）

            适用场景：
            - 需要维护插入顺序
            - 需要实现LRU缓存
            - 遍历性能比随机访问更重要
            - 内存占用不是主要瓶颈

            不适用场景：
            - 只需要基本的键值对存储（用HashMap）
            - 内存受限的环境
            - 高频的随机访问（访问顺序模式）
            """);
    }

    /**
     * ========================================
     * 第七部分：实际应用场景和最佳实践
     * ========================================
     */

    public static void practicalUseCases() {
        System.out.println("\n\n=== LinkedHashMap实际应用场景 ===\n");

        // 1. 场景1：HTTP响应头排序
        System.out.println("--- 场景1：HTTP响应头（保持顺序） ---");

        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Content-Length", "1234");
        headers.put("Server", "MyServer/1.0");
        headers.put("Date", "2026-01-12");

        System.out.println("HTTP响应头（按添加顺序）：");
        headers.forEach((key, value) ->
            System.out.println(key + ": " + value));

        // 2. 场景2：配置文件的有序加载
        System.out.println("\n--- 场景2：配置文件（保持加载顺序） ---");

        LinkedHashMap<String, String> config = new LinkedHashMap<>();
        config.put("db.url", "jdbc:mysql://localhost:3306/mydb");
        config.put("db.username", "root");
        config.put("db.password", "password");
        config.put("app.name", "MyApp");
        config.put("app.version", "1.0.0");

        System.out.println("配置项（按加载顺序）：");
        config.forEach((key, value) ->
            System.out.println(key + " = " + value));

        // 3. 场景3：数据库结果集缓存
        System.out.println("\n--- 场景3：数据库查询结果缓存 ---");

        DatabaseCache dbCache = new DatabaseCache(5);

        // 模拟数据库查询
        dbCache.query("SELECT * FROM users WHERE id=1");
        dbCache.query("SELECT * FROM orders WHERE user_id=1");
        dbCache.query("SELECT * FROM products WHERE id=100");
        dbCache.query("SELECT * FROM users WHERE id=2");
        dbCache.query("SELECT * FROM orders WHERE user_id=2");

        System.out.println("当前缓存：" + dbCache.getCacheKeys());

        // 再次查询id=1的用户（缓存命中）
        dbCache.query("SELECT * FROM users WHERE id=1");
        System.out.println("查询id=1后（移到末尾）：" + dbCache.getCacheKeys());

        // 新查询导致淘汰
        dbCache.query("SELECT * FROM products WHERE id=200");
        System.out.println("新查询后（淘汰最久未用）：" + dbCache.getCacheKeys());

        // 4. 场景4：JSON对象的字段顺序保持
        System.out.println("\n--- 场景4：JSON对象字段顺序 ---");

        LinkedHashMap<String, Object> jsonObject = new LinkedHashMap<>();
        jsonObject.put("id", 1);
        jsonObject.put("name", "John Doe");
        jsonObject.put("email", "john@example.com");
        jsonObject.put("age", 30);
        jsonObject.put("city", "New York");

        System.out.println("JSON对象（保持字段顺序）：");
        System.out.println("{");
        List<String> keys = new ArrayList<>(jsonObject.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = jsonObject.get(key);
            String valueStr = value instanceof String ? "\"" + value + "\"" : value.toString();
            System.out.print("  \"" + key + "\": " + valueStr);
            if (i < keys.size() - 1) {
                System.out.println(",");
            } else {
                System.out.println();
            }
        }
        System.out.println("}");

        // 5. 最佳实践
        System.out.println("\n--- 最佳实践 ---");
        System.out.println("""
            LinkedHashMap使用最佳实践：

            1. 选择合适的顺序模式
               - 需要保持插入顺序：accessOrder=false（默认）
               - 需要实现LRU：accessOrder=true

            2. 初始容量设置
               - 预知元素数量：initialCapacity = expectedSize / 0.75 + 1
               - 避免频繁扩容

            3. 线程安全
               - LinkedHashMap不是线程安全的
               - 多线程环境使用：
                 * Collections.synchronizedMap()
                 * ConcurrentHashMap（但不保证顺序）

            4. 内存管理
               - 大量数据时考虑内存占用（比HashMap多40%）
               - LRU缓存要设置合理的容量上限

            5. 序列化
               - LinkedHashMap是可序列化的
               - 顺序信息会被保留

            6. 性能优化
               - 避免在访问顺序模式下频繁get操作
               - 遍历操作比HashMap更快

            7. 替代方案
               - 只需顺序：ArrayList<Entry>
               - 需要排序：TreeMap
               - 高并发：ConcurrentHashMap
            """);
    }

    /**
     * 数据库查询缓存示例
     */
    static class DatabaseCache {
        private final LRUCache<String, String> cache;

        public DatabaseCache(int capacity) {
            this.cache = new LRUCache<>(capacity);
        }

        public String query(String sql) {
            String result = cache.get(sql);
            if (result != null) {
                System.out.println("[缓存命中] " + sql.substring(0, Math.min(40, sql.length())) + "...");
                return result;
            }

            // 模拟数据库查询
            System.out.println("[数据库查询] " + sql.substring(0, Math.min(40, sql.length())) + "...");
            result = "Result for: " + sql;
            cache.put(sql, result);
            return result;
        }

        public Set<String> getCacheKeys() {
            return cache.keySet();
        }
    }

    /**
     * ========================================
     * 主方法：运行所有演示
     * ========================================
     */

    public static void main(String[] args) {
        System.out.println("""
            ╔════════════════════════════════════════════════════════════╗
            ║                                                            ║
            ║     LinkedHashMap 核心原理深度剖析                          ║
            ║                                                            ║
            ║     本程序将全面分析 LinkedHashMap 的实现原理和应用         ║
            ║                                                            ║
            ╚════════════════════════════════════════════════════════════╝
            """);

        // 第一部分：核心结构
        demonstrateCoreStructure();

        // 第二部分：内部数据结构
        analyzeInternalStructure();

        // 第三部分：核心方法
        analyzeCoreMethods();

        // 第四部分：顺序模式对比
        compareOrderModes();

        // 第五部分：LRU缓存
        analyzeLRUCache();

        // 第六部分：性能分析
        performanceAnalysis();

        // 第七部分：实际应用
        practicalUseCases();

        System.out.println("""

            ╔════════════════════════════════════════════════════════════╗
            ║                                                            ║
            ║     LinkedHashMap 分析完成！                               ║
            ║                                                            ║
            ║     关键要点：                                             ║
            ║     1. HashMap + 双向链表                                  ║
            ║     2. 支持插入顺序和访问顺序                               ║
            ║     3. 实现LRU缓存的最佳选择                               ║
            ║     4. 性能略低于HashMap但提供顺序保证                      ║
            ║                                                            ║
            ╚════════════════════════════════════════════════════════════╝
            """);
    }
}
