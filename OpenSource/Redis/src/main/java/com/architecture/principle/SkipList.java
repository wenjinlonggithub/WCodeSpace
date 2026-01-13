package com.architecture.principle;

import java.util.Random;

/**
 * 跳表实现 - Redis ZSet底层数据结构之一
 * 跳表是一种有序数据结构，通过在每个节点中维持多个指向其他节点的指针，从而达到快速访问节点的目的
 * 时间复杂度：查找、插入、删除都是O(logN)
 */
public class SkipList<T extends Comparable<T>> {

    private static final int MAX_LEVEL = 32;
    private static final double P_FACTOR = 0.25;

    private Node<T> head;
    private int level;
    private Random random;

    static class Node<T> {
        T value;
        double score;
        Node<T>[] forward;

        @SuppressWarnings("unchecked")
        public Node(T value, double score, int level) {
            this.value = value;
            this.score = score;
            this.forward = new Node[level];
        }
    }

    public SkipList() {
        this.head = new Node<>(null, Double.MIN_VALUE, MAX_LEVEL);
        this.level = 1;
        this.random = new Random();
    }

    /**
     * 随机生成层数
     */
    private int randomLevel() {
        int level = 1;
        while (random.nextDouble() < P_FACTOR && level < MAX_LEVEL) {
            level++;
        }
        return level;
    }

    /**
     * 插入元素
     */
    public void insert(T value, double score) {
        Node<T>[] update = new Node[MAX_LEVEL];
        Node<T> current = head;

        // 从最高层开始查找插入位置
        for (int i = level - 1; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].score < score) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        // 随机生成新节点的层数
        int newLevel = randomLevel();
        if (newLevel > level) {
            for (int i = level; i < newLevel; i++) {
                update[i] = head;
            }
            level = newLevel;
        }

        // 创建新节点并插入
        Node<T> newNode = new Node<>(value, score, newLevel);
        for (int i = 0; i < newLevel; i++) {
            newNode.forward[i] = update[i].forward[i];
            update[i].forward[i] = newNode;
        }
    }

    /**
     * 删除元素
     */
    public boolean delete(T value, double score) {
        Node<T>[] update = new Node[MAX_LEVEL];
        Node<T> current = head;

        for (int i = level - 1; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].score < score) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        current = current.forward[0];
        if (current != null && current.score == score && current.value.equals(value)) {
            for (int i = 0; i < level; i++) {
                if (update[i].forward[i] != current) {
                    break;
                }
                update[i].forward[i] = current.forward[i];
            }

            while (level > 1 && head.forward[level - 1] == null) {
                level--;
            }
            return true;
        }
        return false;
    }

    /**
     * 查找元素
     */
    public Node<T> search(double score) {
        Node<T> current = head;
        for (int i = level - 1; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].score < score) {
                current = current.forward[i];
            }
        }
        current = current.forward[0];
        if (current != null && current.score == score) {
            return current;
        }
        return null;
    }

    /**
     * 打印跳表结构
     */
    public void print() {
        for (int i = level - 1; i >= 0; i--) {
            Node<T> current = head.forward[i];
            System.out.print("Level " + i + ": ");
            while (current != null) {
                System.out.print(current.value + "(" + current.score + ") ");
                current = current.forward[i];
            }
            System.out.println();
        }
    }
}
