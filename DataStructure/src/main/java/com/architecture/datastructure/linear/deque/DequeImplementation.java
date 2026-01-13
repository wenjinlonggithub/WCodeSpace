package com.architecture.datastructure.linear.deque;

/**
 * 双端队列 - Deque (Double-Ended Queue)
 * 支持两端插入和删除
 */
public class DequeImplementation<T> {
    private Object[] elements;
    private int front, rear, size, capacity;

    public DequeImplementation(int capacity) {
        this.capacity = capacity;
        this.elements = new Object[capacity];
        this.front = 0;
        this.rear = capacity - 1;
        this.size = 0;
    }

    public void addFirst(T element) {
        if (isFull()) throw new IllegalStateException("Deque is full");
        front = (front - 1 + capacity) % capacity;
        elements[front] = element;
        size++;
    }

    public void addLast(T element) {
        if (isFull()) throw new IllegalStateException("Deque is full");
        rear = (rear + 1) % capacity;
        elements[rear] = element;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T removeFirst() {
        if (isEmpty()) throw new IllegalStateException("Deque is empty");
        T element = (T) elements[front];
        elements[front] = null;
        front = (front + 1) % capacity;
        size--;
        return element;
    }

    @SuppressWarnings("unchecked")
    public T removeLast() {
        if (isEmpty()) throw new IllegalStateException("Deque is empty");
        T element = (T) elements[rear];
        elements[rear] = null;
        rear = (rear - 1 + capacity) % capacity;
        size--;
        return element;
    }

    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == capacity; }
    public int size() { return size; }
}
