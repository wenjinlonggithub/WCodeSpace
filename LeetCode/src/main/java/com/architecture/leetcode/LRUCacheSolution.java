package com.architecture.leetcode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LeetCode #146: LRU Cache
 * 题目描述：设计一个LRU（最近最少使用）缓存机制，支持get和put操作
 * 公司：字节跳动高频题
 *
 * 解法1：使用LinkedHashMap（推荐）
 * 时间复杂度：O(1) for both get and put
 * 空间复杂度：O(capacity)
 *
 * 解法2：手动实现双向链表 + HashMap
 */
public class LRUCacheSolution {

    /**
     * 方案1：使用LinkedHashMap实现LRU缓存
     * LinkedHashMap特点：
     * 1. 继承自HashMap，具有HashMap的所有特性
     * 2. 内部维护了一个双向链表，可以按插入顺序或访问顺序排序
     * 3. 构造函数第三个参数accessOrder=true时，按访问顺序排序（LRU的核心）
     * 4. 每次get/put操作都会将元素移动到链表末尾，实现访问顺序排序
     * 5. 重写removeEldestEntry方法可控制缓存容量，当size > capacity时自动删除最老元素
     * LinkedHashMap可以保持插入顺序或访问顺序
     */
    public static class LRUCache {
        private final int capacity;
        private final LinkedHashMap<Integer, Integer> cache;

        public LRUCache(int capacity) {
            this.capacity = capacity;
            // 第三个参数true表示按访问顺序排序（accessOrder）
            this.cache = new LinkedHashMap<Integer, Integer>(capacity, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                    // 当size超过capacity时，自动删除最老的元素
                    return size() > LRUCache.this.capacity;
                }
            };
        }

        public int get(int key) {
            return cache.getOrDefault(key, -1);
        }

        public void put(int key, int value) {
            cache.put(key, value);
        }
    }

    /**
     * 方案2：手动实现双向链表 + HashMap
     * 更加底层，面试官可能要求手写
     */
    static class LRUCacheManual {
        // 双向链表节点
        class DLinkedNode {
            int key;
            int value;
            DLinkedNode prev;
            DLinkedNode next;

            public DLinkedNode() {}

            public DLinkedNode(int key, int value) {
                this.key = key;
                this.value = value;
            }
        }

        private Map<Integer, DLinkedNode> cache = new LinkedHashMap<>();
        private int size;
        private int capacity;
        private DLinkedNode head, tail;

        public LRUCacheManual(int capacity) {
            this.size = 0;
            this.capacity = capacity;
            // 使用伪头部和伪尾部节点
            head = new DLinkedNode();
            tail = new DLinkedNode();
            head.next = tail;
            tail.prev = head;
        }

        public int get(int key) {
            DLinkedNode node = cache.get(key);
            if (node == null) {
                return -1;
            }
            // 如果key存在，先通过哈希表定位，再移到头部
            moveToHead(node);
            return node.value;
        }

        public void put(int key, int value) {
            DLinkedNode node = cache.get(key);

            if (node == null) {
                // 如果key不存在，创建一个新的节点
                DLinkedNode newNode = new DLinkedNode(key, value);
                // 添加进哈希表
                cache.put(key, newNode);
                // 添加至双向链表的头部
                addToHead(newNode);
                ++size;

                if (size > capacity) {
                    // 如果超出容量，删除双向链表的尾部节点
                    DLinkedNode tailNode = removeTail();
                    // 删除哈希表中对应的项
                    cache.remove(tailNode.key);
                    --size;
                }
            } else {
                // 如果key存在，修改value，并移到头部
                node.value = value;
                moveToHead(node);
            }
        }

        private void addToHead(DLinkedNode node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
        }

        private void removeNode(DLinkedNode node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private void moveToHead(DLinkedNode node) {
            removeNode(node);
            addToHead(node);
        }

        private DLinkedNode removeTail() {
            DLinkedNode res = tail.prev;
            removeNode(res);
            return res;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== LRU Cache Solution 1: LinkedHashMap ===");
        LRUCache cache1 = new LRUCache(2);

        cache1.put(1, 1);
        cache1.put(2, 2);
        System.out.println("get(1): " + cache1.get(1));       // 返回 1
        cache1.put(3, 3);                                      // 该操作会使得key 2作废
        System.out.println("get(2): " + cache1.get(2));       // 返回 -1 (未找到)
        cache1.put(4, 4);                                      // 该操作会使得key 1作废
        System.out.println("get(1): " + cache1.get(1));       // 返回 -1 (未找到)
        System.out.println("get(3): " + cache1.get(3));       // 返回 3
        System.out.println("get(4): " + cache1.get(4));       // 返回 4

        System.out.println("\n=== LRU Cache Solution 2: Manual Implementation ===");
        LRUCacheManual cache2 = new LRUCacheManual(2);

        cache2.put(1, 1);
        cache2.put(2, 2);
        System.out.println("get(1): " + cache2.get(1));       // 返回 1
        cache2.put(3, 3);                                      // 该操作会使得key 2作废
        System.out.println("get(2): " + cache2.get(2));       // 返回 -1 (未找到)
        cache2.put(4, 4);                                      // 该操作会使得key 1作废
        System.out.println("get(1): " + cache2.get(1));       // 返回 -1 (未找到)
        System.out.println("get(3): " + cache2.get(3));       // 返回 3
        System.out.println("get(4): " + cache2.get(4));       // 返回 4
    }
}
