package com.architecture.datastructure.linear.queue;

/**
 * 循环队列实现 - Circular Queue Implementation
 */
public class CircularQueueImplementation<T> {
    private Object[] elements;
    private int front, rear, size, capacity;

    public CircularQueueImplementation(int capacity) {
        this.capacity = capacity;
        this.elements = new Object[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    public void enqueue(T element) {
        if (isFull()) throw new IllegalStateException("Queue is full");
        rear = (rear + 1) % capacity;
        elements[rear] = element;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new IllegalStateException("Queue is empty");
        T data = (T) elements[front];
        elements[front] = null;
        front = (front + 1) % capacity;
        size--;
        return data;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new IllegalStateException("Queue is empty");
        return (T) elements[front];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public int size() {
        return size;
    }
}
