package com.architecture.datastructure.tree.avl;

/**
 * AVL树 - 自平衡二叉搜索树
 * 保证左右子树高度差不超过1
 */
public class AVLTreeImplementation<T extends Comparable<T>> {

    static class AVLNode<T> {
        T data;
        AVLNode<T> left, right;
        int height;

        AVLNode(T data) {
            this.data = data;
            this.height = 1;
        }
    }

    private AVLNode<T> root;

    public void insert(T data) {
        root = insertHelper(root, data);
    }

    private AVLNode<T> insertHelper(AVLNode<T> node, T data) {
        if (node == null) return new AVLNode<>(data);

        int cmp = data.compareTo(node.data);
        if (cmp < 0) {
            node.left = insertHelper(node.left, data);
        } else if (cmp > 0) {
            node.right = insertHelper(node.right, data);
        } else {
            return node;  // 不插入重复元素
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));
        return balance(node);
    }

    private AVLNode<T> balance(AVLNode<T> node) {
        int balanceFactor = getBalance(node);

        // Left Left Case
        if (balanceFactor > 1 && getBalance(node.left) >= 0) {
            return rotateRight(node);
        }

        // Left Right Case
        if (balanceFactor > 1 && getBalance(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Right Right Case
        if (balanceFactor < -1 && getBalance(node.right) <= 0) {
            return rotateLeft(node);
        }

        // Right Left Case
        if (balanceFactor < -1 && getBalance(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    private AVLNode<T> rotateRight(AVLNode<T> y) {
        AVLNode<T> x = y.left;
        AVLNode<T> T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    private AVLNode<T> rotateLeft(AVLNode<T> x) {
        AVLNode<T> y = x.right;
        AVLNode<T> T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    private int height(AVLNode<T> node) {
        return node == null ? 0 : node.height;
    }

    private int getBalance(AVLNode<T> node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    public boolean search(T data) {
        return searchHelper(root, data);
    }

    private boolean searchHelper(AVLNode<T> node, T data) {
        if (node == null) return false;

        int cmp = data.compareTo(node.data);
        if (cmp == 0) return true;
        if (cmp < 0) return searchHelper(node.left, data);
        return searchHelper(node.right, data);
    }
}
