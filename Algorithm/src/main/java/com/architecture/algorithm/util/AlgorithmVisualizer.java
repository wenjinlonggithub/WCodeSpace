package com.architecture.algorithm.util;

import com.architecture.algorithm.sort.SortingAlgorithms;
import com.architecture.algorithm.tree.BinaryTreeAlgorithms;

import java.util.List;

/**
 * 算法可视化工具类
 * 根据README中的描述，提供算法过程的可视化展示
 */
public class AlgorithmVisualizer {
    
    /**
     * 可视化冒泡排序过程
     */
    public static void visualizeBubbleSort(int[] arr) {
        System.out.println("冒泡排序可视化过程:");
        int n = arr.length;
        
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            System.out.print("第 " + (i + 1) + " 轮: ");
            
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换元素
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                    
                    // 打印当前状态
                    System.out.print("[交换 " + arr[j+1] + " 和 " + arr[j] + "] ");
                    printArray(arr);
                }
            }
            
            if (!swapped) {
                System.out.println("数组已排序完成");
                break;
            }
        }
        
        System.out.println("最终结果: ");
        printArray(arr);
    }
    
    /**
     * 打印数组
     */
    public static void printArray(int[] arr) {
        for (int value : arr) {
            System.out.print(value + " ");
        }
        System.out.println();
    }
    
    /**
     * 可视化二叉树结构
     */
    public static void visualizeBinaryTree(BinaryTreeAlgorithms.TreeNode root) {
        System.out.println("二叉树结构可视化:");
        printBinaryTree(root, "", true);
    }
    
    private static void printBinaryTree(BinaryTreeAlgorithms.TreeNode node, String prefix, boolean isLast) {
        if (node != null) {
            System.out.println(prefix + (isLast ? "└── " : "├── ") + node.val);
            
            // 计算子节点的前缀
            String childPrefix = prefix + (isLast ? "    " : "|   ");
            
            // 先打印左子树，再打印右子树
            if (node.left != null || node.right != null) {
                if (node.left != null) {
                    printBinaryTree(node.left, childPrefix, node.right == null);
                }
                if (node.right != null) {
                    printBinaryTree(node.right, childPrefix, true);
                }
            }
        }
    }
    
    /**
     * 可视化递归过程（以斐波那契为例）
     */
    public static long visualizeFibonacci(int n, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "计算 fibonacci(" + n + ")");
        
        if (n <= 1) {
            System.out.println(indent + "返回 " + n);
            return n;
        }
        
        long result = visualizeFibonacci(n - 1, depth + 1) + visualizeFibonacci(n - 2, depth + 1);
        System.out.println(indent + "返回 fibonacci(" + n + ") = " + result);
        return result;
    }
    
    /**
     * 显示复杂度分析
     */
    public static void displayComplexityAnalysis() {
        System.out.println("\n=== 算法复杂度分析 ===");
        System.out.println("时间复杂度等级:");
        System.out.println("O(1) - 常数时间");
        System.out.println("O(log n) - 对数时间");
        System.out.println("O(n) - 线性时间");
        System.out.println("O(n log n) - 线性对数时间");
        System.out.println("O(n²) - 平方时间");
        System.out.println("O(2^n) - 指数时间");
        System.out.println("O(n!) - 阶乘时间");
        
        System.out.println("\n排序算法复杂度对比:");
        System.out.println("┌─────────────┬─────────┬─────────┬──────────┬──────────┐");
        System.out.println("│    算法     │   最好  │  平均   │   最坏   │ 空间复杂度 │");
        System.out.println("├─────────────┼─────────┼─────────┼──────────┼──────────┤");
        System.out.println("│ 冒泡排序    │  O(n)   │  O(n²)  │  O(n²)   │   O(1)   │");
        System.out.println("│ 选择排序    │  O(n²)  │  O(n²)  │  O(n²)   │   O(1)   │");
        System.out.println("│ 插入排序    │  O(n)   │  O(n²)  │  O(n²)   │   O(1)   │");
        System.out.println("│ 快速排序    │O(nlogn) │O(nlogn) │  O(n²)   │ O(logn)  │");
        System.out.println("│ 归并排序    │O(nlogn) │O(nlogn) │O(nlogn)  │   O(n)   │");
        System.out.println("│ 堆排序      │O(nlogn) │O(nlogn) │O(nlogn)  │   O(1)   │");
        System.out.println("│ 计数排序    │ O(n+k)  │ O(n+k)  │ O(n+k)   │   O(k)   │");
        System.out.println("│ 桶排序      │ O(n+k)  │ O(n+k)  │  O(n²)   │  O(n+k)  │");
        System.out.println("│ 基数排序    │ O(nk)   │ O(nk)   │  O(nk)   │  O(n+k)  │");
        System.out.println("└─────────────┴─────────┴─────────┴──────────┴──────────┘");
    }
    
    public static void main(String[] args) {
        // 演示冒泡排序可视化
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("原数组: ");
        printArray(arr);
        
        int[] bubbleArr = arr.clone();
        visualizeBubbleSort(bubbleArr);
        
        // 演示二叉树可视化
        BinaryTreeAlgorithms treeAlgo = new BinaryTreeAlgorithms();
        BinaryTreeAlgorithms.TreeNode root = new BinaryTreeAlgorithms.TreeNode(1);
        root.left = new BinaryTreeAlgorithms.TreeNode(2);
        root.right = new BinaryTreeAlgorithms.TreeNode(3);
        root.left.left = new BinaryTreeAlgorithms.TreeNode(4);
        root.left.right = new BinaryTreeAlgorithms.TreeNode(5);
        root.right.left = new BinaryTreeAlgorithms.TreeNode(6);
        root.right.right = new BinaryTreeAlgorithms.TreeNode(7);
        
        visualizeBinaryTree(root);
        
        // 演示递归可视化
        System.out.println("\n斐波那契递归过程可视化 (n=5):");
        long result = visualizeFibonacci(5, 0);
        System.out.println("最终结果: " + result);
        
        // 显示复杂度分析
        displayComplexityAnalysis();
        
        // 演示遍历算法可视化
        System.out.println("\n二叉树遍历结果:");
        System.out.println("前序遍历: " + treeAlgo.preorderTraversal(root));
        System.out.println("中序遍历: " + treeAlgo.inorderTraversal(root));
        System.out.println("后序遍历: " + treeAlgo.postorderTraversal(root));
        System.out.println("层序遍历: " + treeAlgo.levelOrder(root));
    }
}