package com.architecture.datastructure.tree.bst;

import com.architecture.datastructure.common.TreeNode;

/**
 * 二叉搜索树 - Binary Search Tree
 * 性质: 左子树所有节点 < 根节点 < 右子树所有节点
 */
public class BSTImplementation<T extends Comparable<T>> {
    private TreeNode<T> root;

    public void insert(T data) {
        root = insertHelper(root, data);
    }

    private TreeNode<T> insertHelper(TreeNode<T> node, T data) {
        if (node == null) return new TreeNode<>(data);

        int cmp = data.compareTo(node.data);
        if (cmp < 0) {
            node.left = insertHelper(node.left, data);
        } else if (cmp > 0) {
            node.right = insertHelper(node.right, data);
        }
        return node;
    }

    public boolean search(T data) {
        return searchHelper(root, data);
    }

    private boolean searchHelper(TreeNode<T> node, T data) {
        if (node == null) return false;

        int cmp = data.compareTo(node.data);
        if (cmp == 0) return true;
        if (cmp < 0) return searchHelper(node.left, data);
        return searchHelper(node.right, data);
    }

    public void delete(T data) {
        root = deleteHelper(root, data);
    }

    private TreeNode<T> deleteHelper(TreeNode<T> node, T data) {
        if (node == null) return null;

        int cmp = data.compareTo(node.data);
        if (cmp < 0) {
            node.left = deleteHelper(node.left, data);
        } else if (cmp > 0) {
            node.right = deleteHelper(node.right, data);
        } else {
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            TreeNode<T> minRight = findMin(node.right);
            node.data = minRight.data;
            node.right = deleteHelper(node.right, minRight.data);
        }
        return node;
    }

    private TreeNode<T> findMin(TreeNode<T> node) {
        while (node.left != null) node = node.left;
        return node;
    }

    public TreeNode<T> getRoot() { return root; }
}
