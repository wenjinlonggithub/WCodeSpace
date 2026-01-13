package com.architecture.datastructure.linear.linkedlist;

import com.architecture.datastructure.common.Node;

/**
 * 单向链表实现 - Singly Linked List Implementation
 *
 * <p>原理说明:
 * 链表是一种线性数据结构，由节点组成，每个节点包含数据和指向下一个节点的引用。
 * 相比数组，链表在插入和删除操作上更高效，但访问元素需要遍历。
 *
 * <p>时间复杂度:
 * <ul>
 *   <li>访问: O(n) - 需要从头节点遍历</li>
 *   <li>插入头部: O(1) - 直接修改头指针</li>
 *   <li>插入尾部: O(n) - 需要遍历到尾部（可优化为O(1)通过维护tail指针）</li>
 *   <li>插入中间: O(n) - 需要遍历到指定位置</li>
 *   <li>删除头部: O(1) - 直接修改头指针</li>
 *   <li>删除中间: O(n) - 需要遍历找到前驱节点</li>
 *   <li>搜索: O(n) - 需要遍历查找</li>
 * </ul>
 *
 * <p>空间复杂度: O(n) - n个节点，每个节点需要额外存储指针
 *
 * <p>优点:
 * <ul>
 *   <li>动态大小，无需预分配空间</li>
 *   <li>插入/删除操作高效（在已知位置时）</li>
 *   <li>不需要连续内存空间</li>
 * </ul>
 *
 * <p>缺点:
 * <ul>
 *   <li>随机访问效率低</li>
 *   <li>额外存储空间存储指针</li>
 *   <li>遍历只能单向进行</li>
 *   <li>缓存不友好（节点内存分散）</li>
 * </ul>
 *
 * @param <T> 存储的数据类型
 * @author Architecture Team
 * @version 1.0
 * @since 2026-01-13
 */
public class LinkedListImplementation<T> {
    /**
     * 头节点
     * Head node of the linked list
     */
    private Node<T> head;

    /**
     * 链表大小
     * Size of the linked list
     */
    private int size;

    /**
     * 构造空链表
     * Constructs an empty linked list
     *
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    public LinkedListImplementation() {
        this.head = null;
        this.size = 0;
    }

    /**
     * 在链表头部插入元素
     * Inserts an element at the beginning of the list
     *
     * <p>算法步骤:
     * <ol>
     *   <li>创建新节点</li>
     *   <li>新节点的next指向当前头节点</li>
     *   <li>更新head为新节点</li>
     *   <li>size加1</li>
     * </ol>
     *
     * @param data 要插入的数据
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = head;
        head = newNode;
        size++;
    }

    /**
     * 在链表尾部插入元素
     * Inserts an element at the end of the list
     *
     * <p>算法步骤:
     * <ol>
     *   <li>创建新节点</li>
     *   <li>如果链表为空，新节点成为头节点</li>
     *   <li>否则遍历到尾节点，将尾节点的next指向新节点</li>
     *   <li>size加1</li>
     * </ol>
     *
     * @param data 要插入的数据
     * 时间复杂度: O(n) - 需要遍历到尾部
     * 空间复杂度: O(1)
     *
     * 优化建议: 可以维护一个tail指针，使插入操作变为O(1)
     */
    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);

        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    /**
     * 在指定位置插入元素
     * Inserts an element at the specified position
     *
     * <p>算法步骤:
     * <ol>
     *   <li>检查索引是否合法</li>
     *   <li>如果index为0，调用addFirst</li>
     *   <li>否则遍历到index-1位置</li>
     *   <li>插入新节点</li>
     * </ol>
     *
     * @param index 插入位置 (0-based)
     * @param data 要插入的数据
     * @throws IndexOutOfBoundsException 如果索引越界
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public void add(int index, T data) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (index == 0) {
            addFirst(data);
            return;
        }

        Node<T> newNode = new Node<>(data);
        Node<T> current = head;

        // 遍历到index-1位置
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }

        // 插入新节点
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    /**
     * 删除头节点
     * Removes and returns the first element
     *
     * @return 被删除的数据
     * @throws IllegalStateException 如果链表为空
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    public T removeFirst() {
        if (head == null) {
            throw new IllegalStateException("List is empty");
        }

        T data = head.data;
        head = head.next;
        size--;
        return data;
    }

    /**
     * 删除指定位置的元素
     * Removes and returns the element at the specified position
     *
     * @param index 要删除的位置
     * @return 被删除的数据
     * @throws IndexOutOfBoundsException 如果索引越界
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (index == 0) {
            return removeFirst();
        }

        Node<T> current = head;
        // 遍历到index-1位置（前驱节点）
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }

        T data = current.next.data;
        current.next = current.next.next;  // 跳过要删除的节点
        size--;
        return data;
    }

    /**
     * 删除第一个匹配的元素
     * Removes the first occurrence of the specified element
     *
     * @param data 要删除的数据
     * @return true if the element was removed, false otherwise
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public boolean removeElement(T data) {
        if (head == null) {
            return false;
        }

        // 如果头节点就是要删除的元素
        if (head.data.equals(data)) {
            head = head.next;
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(data)) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }

        return false;
    }

    /**
     * 获取指定位置的元素
     * Returns the element at the specified position
     *
     * @param index 位置索引
     * @return 该位置的数据
     * @throws IndexOutOfBoundsException 如果索引越界
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * 修改指定位置的元素
     * Replaces the element at the specified position
     *
     * @param index 位置索引
     * @param data 新数据
     * @return 原来的数据
     * @throws IndexOutOfBoundsException 如果索引越界
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public T set(int index, T data) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        T oldData = current.data;
        current.data = data;
        return oldData;
    }

    /**
     * 查找元素的索引
     * Returns the index of the first occurrence of the specified element
     *
     * @param data 要查找的数据
     * @return 元素的索引，如果不存在返回-1
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public int indexOf(T data) {
        Node<T> current = head;
        int index = 0;

        while (current != null) {
            if (current.data.equals(data)) {
                return index;
            }
            current = current.next;
            index++;
        }

        return -1;
    }

    /**
     * 检查链表是否包含指定元素
     * Returns true if this list contains the specified element
     *
     * @param data 要检查的数据
     * @return true if the element is present
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public boolean contains(T data) {
        return indexOf(data) != -1;
    }

    /**
     * 获取链表大小
     * Returns the number of elements in this list
     *
     * @return 链表大小
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    public int size() {
        return size;
    }

    /**
     * 检查链表是否为空
     * Returns true if this list contains no elements
     *
     * @return true if the list is empty
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 清空链表
     * Removes all elements from this list
     *
     * 时间复杂度: O(1) - Java的垃圾回收器会处理节点的内存释放
     * 空间复杂度: O(1)
     */
    public void clear() {
        head = null;
        size = 0;
    }

    /**
     * 反转链表 - 原地反转
     * Reverses the list in-place
     *
     * <p>算法步骤（迭代法）:
     * <ol>
     *   <li>使用三个指针: prev, current, next</li>
     *   <li>遍历链表，逐个反转节点的指向</li>
     *   <li>最后更新head为原来的尾节点</li>
     * </ol>
     *
     * <p>这是一个经典的面试题（LeetCode #206）
     *
     * 时间复杂度: O(n)
     * 空间复杂度: O(1) - 原地反转
     */
    public void reverse() {
        if (head == null || head.next == null) {
            return;
        }

        Node<T> prev = null;
        Node<T> current = head;
        Node<T> next = null;

        while (current != null) {
            next = current.next;      // 保存下一个节点
            current.next = prev;      // 反转当前节点的指针
            prev = current;           // prev前进一步
            current = next;           // current前进一步
        }

        head = prev;  // 更新头节点为原来的尾节点
    }

    /**
     * 获取头节点的数据（不删除）
     * Returns the first element without removing it
     *
     * @return 头节点的数据
     * @throws IllegalStateException 如果链表为空
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    public T getFirst() {
        if (head == null) {
            throw new IllegalStateException("List is empty");
        }
        return head.data;
    }

    /**
     * 获取尾节点的数据（不删除）
     * Returns the last element without removing it
     *
     * @return 尾节点的数据
     * @throws IllegalStateException 如果链表为空
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public T getLast() {
        if (head == null) {
            throw new IllegalStateException("List is empty");
        }

        Node<T> current = head;
        while (current.next != null) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * 转换为字符串表示
     * Returns a string representation of the list
     *
     * @return 链表的字符串表示，格式: [1 -> 2 -> 3]
     * 时间复杂度: O(n)
     * 空间复杂度: O(n) - 字符串构建需要额外空间
     */
    @Override
    public String toString() {
        if (head == null) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;

        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next;
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * 打印链表（用于调试）
     * Prints the list to console (for debugging)
     *
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public void print() {
        System.out.println(toString());
    }
}
