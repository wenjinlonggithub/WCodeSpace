package com.architecture.datastructure.linear.queue;

import com.architecture.datastructure.common.Node;

/**
 * 队列实现 - Queue Implementation (LinkedList-based)
 *
 * 队列是先进先出(FIFO - First In First Out)的线性数据结构
 *
 * 时间复杂度:
 * - enqueue: O(1)
 * - dequeue: O(1)
 * - peek: O(1)
 *
 * @param <T> 元素类型
 */
public class QueueImplementation<T> {
    private Node<T> front;
    private Node<T> rear;
    private int size;

    public QueueImplementation() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    public void enqueue(T element) {
        Node<T> newNode = new Node<>(element);
        if (rear == null) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) throw new IllegalStateException("Queue is empty");
        T data = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) throw new IllegalStateException("Queue is empty");
        return front.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
