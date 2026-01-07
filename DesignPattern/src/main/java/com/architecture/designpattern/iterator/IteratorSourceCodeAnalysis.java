package com.architecture.designpattern.iterator;

import org.springframework.stereotype.Component;

@Component
public class IteratorSourceCodeAnalysis {

    /**
     * ====================
     * 迭代器模式源码分析
     * ====================
     */

    /**
     * 1. Java ArrayList迭代器实现
     */
    public void analyzeArrayListIterator() {
        System.out.println("ArrayList迭代器：基于索引的迭代，modCount检查");
    }

    /**
     * 2. Java LinkedList迭代器实现
     */
    public void analyzeLinkedListIterator() {
        System.out.println("LinkedList迭代器：双向链表节点遍历，双向迭代");
    }

    /**
     * 3. Java Stream API
     */
    public void analyzeStreamAPI() {
        System.out.println("Stream API：内部迭代器，懒加载，并行处理");
    }

    /**
     * 4. 自定义迭代器实现
     */
    public void analyzeCustomIterator() {
        System.out.println("自定义迭代器：状态管理，异常处理，性能优化");
    }

    /**
     * 5. 并发环境下的迭代器
     */
    public void analyzeConcurrentIterator() {
        System.out.println("并发迭代器：CopyOnWriteArrayList，弱一致性，线程安全");
    }
}