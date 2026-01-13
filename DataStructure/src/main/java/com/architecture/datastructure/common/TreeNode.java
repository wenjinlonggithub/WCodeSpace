package com.architecture.datastructure.common;

/**
 * 树节点类 - Generic TreeNode for Binary Tree Structures
 *
 * 用于二叉树及其变种（BST、AVL、红黑树等）
 * Used for binary trees and variants (BST, AVL, Red-Black Tree, etc.)
 *
 * @param <T> 节点存储的数据类型 / Type of data stored in the node
 *
 * @author Architecture Team
 * @since 1.0
 */
public class TreeNode<T> {
    /**
     * 节点存储的数据
     * Data stored in this node
     */
    public T data;

    /**
     * 左子节点
     * Left child node
     */
    public TreeNode<T> left;

    /**
     * 右子节点
     * Right child node
     */
    public TreeNode<T> right;

    /**
     * 构造一个新的树节点
     * Constructs a new tree node with the given data
     *
     * @param data 要存储的数据 / The data to store
     */
    public TreeNode(T data) {
        this.data = data;
        this.left = null;
        this.right = null;
    }

    /**
     * 构造一个新的树节点，并指定子节点
     * Constructs a new tree node with data and children
     *
     * @param data 要存储的数据 / The data to store
     * @param left 左子节点 / Left child
     * @param right 右子节点 / Right child
     */
    public TreeNode(T data, TreeNode<T> left, TreeNode<T> right) {
        this.data = data;
        this.left = left;
        this.right = right;
    }

    /**
     * 判断是否为叶子节点
     * Check if this is a leaf node
     *
     * @return true if this node has no children
     */
    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public String toString() {
        return data != null ? data.toString() : "null";
    }
}
