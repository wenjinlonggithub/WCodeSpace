package com.architecture.datastructure.tree.binarytree;

import com.architecture.datastructure.common.TreeNode;
import java.util.*;

/**
 * 二叉树实现 - Binary Tree
 */
public class BinaryTreeImplementation<T> {
    private TreeNode<T> root;

    public BinaryTreeImplementation() {
        this.root = null;
    }

    public BinaryTreeImplementation(TreeNode<T> root) {
        this.root = root;
    }

    /** 前序遍历 - Preorder: Root -> Left -> Right */
    public List<T> preorderTraversal() {
        List<T> result = new ArrayList<>();
        preorderHelper(root, result);
        return result;
    }

    private void preorderHelper(TreeNode<T> node, List<T> result) {
        if (node == null) return;
        result.add(node.data);
        preorderHelper(node.left, result);
        preorderHelper(node.right, result);
    }

    /** 中序遍历 - Inorder: Left -> Root -> Right */
    public List<T> inorderTraversal() {
        List<T> result = new ArrayList<>();
        inorderHelper(root, result);
        return result;
    }

    private void inorderHelper(TreeNode<T> node, List<T> result) {
        if (node == null) return;
        inorderHelper(node.left, result);
        result.add(node.data);
        inorderHelper(node.right, result);
    }

    /** 后序遍历 - Postorder: Left -> Right -> Root */
    public List<T> postorderTraversal() {
        List<T> result = new ArrayList<>();
        postorderHelper(root, result);
        return result;
    }

    private void postorderHelper(TreeNode<T> node, List<T> result) {
        if (node == null) return;
        postorderHelper(node.left, result);
        postorderHelper(node.right, result);
        result.add(node.data);
    }

    /** 层序遍历 - Level Order (BFS) */
    public List<T> levelOrderTraversal() {
        List<T> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode<T>> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode<T> node = queue.poll();
            result.add(node.data);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        return result;
    }

    /** 获取树的高度 */
    public int height() {
        return heightHelper(root);
    }

    private int heightHelper(TreeNode<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(heightHelper(node.left), heightHelper(node.right));
    }

    public TreeNode<T> getRoot() { return root; }
    public void setRoot(TreeNode<T> root) { this.root = root; }
}
