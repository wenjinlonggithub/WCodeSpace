package com.architecture.datastructure.common;

/**
 * 通用节点类 - Generic Node for Linked Structures
 *
 * 用于链表、栈、队列等线性链式数据结构
 * Used for linked lists, stacks, queues, and other linear linked structures
 *
 * @param <T> 节点存储的数据类型 / Type of data stored in the node
 *
 * @author Architecture Team
 * @since 1.0
 */
public class Node<T> {
    /**
     * 节点存储的数据
     * Data stored in this node
     */
    public T data;

    /**
     * 指向下一个节点的引用
     * Reference to the next node
     */
    public Node<T> next;

    /**
     * 构造一个新节点
     * Constructs a new node with the given data
     *
     * @param data 要存储的数据 / The data to store
     */
    public Node(T data) {
        this.data = data;
        this.next = null;
    }

    /**
     * 构造一个新节点，并指定下一个节点
     * Constructs a new node with data and next reference
     *
     * @param data 要存储的数据 / The data to store
     * @param next 下一个节点 / The next node
     */
    public Node(T data, Node<T> next) {
        this.data = data;
        this.next = next;
    }

    @Override
    public String toString() {
        return data != null ? data.toString() : "null";
    }
}
