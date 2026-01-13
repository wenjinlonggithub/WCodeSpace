package com.architecture.datastructure.advanced.skiplist;

import java.util.Random;

/**
 * 跳表 - Skip List
 * 概率性平衡数据结构，类似于平衡树
 */
public class SkipListImplementation<T extends Comparable<T>> {
    private static final int MAX_LEVEL = 16;
    private static final double P = 0.5;

    static class Node<T> {
        T value;
        Node<T>[] forward;

        @SuppressWarnings("unchecked")
        Node(T value, int level) {
            this.value = value;
            this.forward = new Node[level + 1];
        }
    }

    private Node<T> head;
    private int level;
    private Random random;

    public SkipListImplementation() {
        head = new Node<>(null, MAX_LEVEL);
        level = 0;
        random = new Random();
    }

    public void insert(T value) {
        @SuppressWarnings("unchecked")
        Node<T>[] update = new Node[MAX_LEVEL + 1];
        Node<T> current = head;

        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null &&
                   current.forward[i].value.compareTo(value) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        int newLevel = randomLevel();
        if (newLevel > level) {
            for (int i = level + 1; i <= newLevel; i++) {
                update[i] = head;
            }
            level = newLevel;
        }

        Node<T> newNode = new Node<>(value, newLevel);
        for (int i = 0; i <= newLevel; i++) {
            newNode.forward[i] = update[i].forward[i];
            update[i].forward[i] = newNode;
        }
    }

    public boolean search(T value) {
        Node<T> current = head;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null &&
                   current.forward[i].value.compareTo(value) < 0) {
                current = current.forward[i];
            }
        }
        current = current.forward[0];
        return current != null && current.value.equals(value);
    }

    private int randomLevel() {
        int lvl = 0;
        while (random.nextDouble() < P && lvl < MAX_LEVEL) {
            lvl++;
        }
        return lvl;
    }
}
