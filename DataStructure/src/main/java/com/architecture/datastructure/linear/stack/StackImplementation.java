package com.architecture.datastructure.linear.stack;

/**
 * 栈实现 - Stack Implementation (Array-based)
 *
 * 栈是后进先出(LIFO - Last In First Out)的线性数据结构
 *
 * 时间复杂度:
 * - push: O(1) 均摊
 * - pop: O(1)
 * - peek: O(1)
 *
 * 空间复杂度: O(n)
 *
 * @param <T> 元素类型
 */
public class StackImplementation<T> {
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elements;
    private int size;

    public StackImplementation() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public void push(T element) {
        ensureCapacity();
        elements[size++] = element;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new IllegalStateException("Stack is empty");
        T element = (T) elements[--size];
        elements[size] = null;
        return element;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new IllegalStateException("Stack is empty");
        return (T) elements[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    private void ensureCapacity() {
        if (size == elements.length) {
            Object[] newArray = new Object[elements.length * 2];
            System.arraycopy(elements, 0, newArray, 0, size);
            elements = newArray;
        }
    }
}
