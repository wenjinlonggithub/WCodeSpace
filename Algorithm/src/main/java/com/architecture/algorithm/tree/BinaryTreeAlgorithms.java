package com.architecture.algorithm.tree;

import java.util.*;

/**
 * 二叉树算法实现类
 * 根据README中的描述，实现二叉树遍历、二叉搜索树等算法
 */
public class BinaryTreeAlgorithms {
    
    /**
     * 二叉树节点定义
     */
    public static class TreeNode {
        public int val;
        public TreeNode left;
        public TreeNode right;
        
        TreeNode() {}
        
        public TreeNode(int val) {
            this.val = val;
        }
        
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }
    
    /**
     * 前序遍历 - 递归实现
     * 时间复杂度: O(n)
     * 空间复杂度: O(h) h为树的高度
     */
    public List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        preorderHelper(root, result);
        return result;
    }
    
    private void preorderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        
        result.add(node.val);           // 访问根节点
        preorderHelper(node.left, result);  // 遍历左子树
        preorderHelper(node.right, result); // 遍历右子树
    }
    
    /**
     * 中序遍历 - 递归实现
     */
    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        inorderHelper(root, result);
        return result;
    }
    
    private void inorderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        
        inorderHelper(node.left, result);   // 遍历左子树
        result.add(node.val);               // 访问根节点
        inorderHelper(node.right, result);  // 遍历右子树
    }
    
    /**
     * 后序遍历 - 递归实现
     */
    public List<Integer> postorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        postorderHelper(root, result);
        return result;
    }
    
    private void postorderHelper(TreeNode node, List<Integer> result) {
        if (node == null) return;
        
        postorderHelper(node.left, result);  // 遍历左子树
        postorderHelper(node.right, result); // 遍历右子树
        result.add(node.val);                // 访问根节点
    }
    
    /**
     * 前序遍历 - 迭代实现
     */
    public List<Integer> preorderTraversalIterative(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;
        
        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        
        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            result.add(node.val);
            
            if (node.right != null) {
                stack.push(node.right);
            }
            if (node.left != null) {
                stack.push(node.left);
            }
        }
        
        return result;
    }
    
    /**
     * 中序遍历 - 迭代实现
     */
    public List<Integer> inorderTraversalIterative(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode current = root;
        
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            
            current = stack.pop();
            result.add(current.val);
            current = current.right;
        }
        
        return result;
    }
    
    /**
     * 层序遍历（广度优先遍历）
     * 时间复杂度: O(n)
     * 空间复杂度: O(w) w为树的最大宽度
     */
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;
        
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        
        while (!queue.isEmpty()) {
            int size = queue.size();
            List<Integer> level = new ArrayList<>();
            
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                level.add(node.val);
                
                if (node.left != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }
            result.add(level);
        }
        return result;
    }
    
    /**
     * 二叉搜索树插入
     */
    public TreeNode insertIntoBST(TreeNode root, int val) {
        if (root == null) {
            return new TreeNode(val);
        }
        
        if (val < root.val) {
            root.left = insertIntoBST(root.left, val);
        } else {
            root.right = insertIntoBST(root.right, val);
        }
        
        return root;
    }
    
    /**
     * 二叉搜索树查找
     */
    public TreeNode searchBST(TreeNode root, int val) {
        if (root == null || root.val == val) {
            return root;
        }
        
        if (val < root.val) {
            return searchBST(root.left, val);
        } else {
            return searchBST(root.right, val);
        }
    }
    
    /**
     * 验证二叉搜索树
     */
    public boolean isValidBST(TreeNode root) {
        return isValidBSTHelper(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    
    private boolean isValidBSTHelper(TreeNode node, long minVal, long maxVal) {
        if (node == null) return true;
        
        if (node.val <= minVal || node.val >= maxVal) return false;
        
        return isValidBSTHelper(node.left, minVal, node.val) && 
               isValidBSTHelper(node.right, node.val, maxVal);
    }
    
    /**
     * 二叉树最大深度
     */
    public int maxDepth(TreeNode root) {
        if (root == null) return 0;
        
        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }
    
    /**
     * 二叉树最小深度
     */
    public int minDepth(TreeNode root) {
        if (root == null) return 0;
        
        if (root.left == null) return 1 + minDepth(root.right);
        if (root.right == null) return 1 + minDepth(root.left);
        
        return 1 + Math.min(minDepth(root.left), minDepth(root.right));
    }
    
    /**
     * 翻转二叉树
     */
    public TreeNode invertTree(TreeNode root) {
        if (root == null) return null;
        
        TreeNode left = invertTree(root.left);
        TreeNode right = invertTree(root.right);
        
        root.left = right;
        root.right = left;
        
        return root;
    }
    
    public static void main(String[] args) {
        BinaryTreeAlgorithms treeAlgo = new BinaryTreeAlgorithms();
        
        // 构建示例二叉树
        //       3
        //      / \
        //     9   20
        //        /  \
        //       15   7
        TreeNode root = new TreeNode(3);
        root.left = new TreeNode(9);
        root.right = new TreeNode(20);
        root.right.left = new TreeNode(15);
        root.right.right = new TreeNode(7);
        
        System.out.println("二叉树结构:");
        System.out.println("       3");
        System.out.println("      / \\");
        System.out.println("     9   20");
        System.out.println("        /  \\");
        System.out.println("       15   7");
        
        // 测试遍历算法
        System.out.println("\n前序遍历 (递归): " + treeAlgo.preorderTraversal(root));
        System.out.println("中序遍历 (递归): " + treeAlgo.inorderTraversal(root));
        System.out.println("后序遍历 (递归): " + treeAlgo.postorderTraversal(root));
        System.out.println("层序遍历: " + treeAlgo.levelOrder(root));
        
        System.out.println("\n前序遍历 (迭代): " + treeAlgo.preorderTraversalIterative(root));
        System.out.println("中序遍历 (迭代): " + treeAlgo.inorderTraversalIterative(root));
        
        // 测试树属性
        System.out.println("\n最大深度: " + treeAlgo.maxDepth(root));
        System.out.println("最小深度: " + treeAlgo.minDepth(root));
        
        // 测试二叉搜索树相关功能
        TreeNode bstRoot = null;
        int[] vals = {5, 3, 8, 2, 4, 7, 9};
        for (int val : vals) {
            bstRoot = treeAlgo.insertIntoBST(bstRoot, val);
        }
        
        System.out.println("\n构建的BST中序遍历: " + treeAlgo.inorderTraversal(bstRoot));
        System.out.println("BST有效性: " + treeAlgo.isValidBST(bstRoot));
        
        TreeNode searchedNode = treeAlgo.searchBST(bstRoot, 7);
        System.out.println("搜索节点7的值: " + (searchedNode != null ? searchedNode.val : "未找到"));
        
        // 测试翻转二叉树
        TreeNode invertedRoot = treeAlgo.invertTree(root);
        System.out.println("\n翻转后的层序遍历: " + treeAlgo.levelOrder(invertedRoot));
    }
}