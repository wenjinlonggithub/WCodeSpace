package com.architecture.algorithm.demo;

import com.architecture.algorithm.AlgorithmMain;
import com.architecture.algorithm.graph.GraphAlgorithms;
import com.architecture.algorithm.search.SearchAlgorithms;
import com.architecture.algorithm.sort.SortingAlgorithms;
import com.architecture.algorithm.tree.BinaryTreeAlgorithms;
import com.architecture.algorithm.dynamicprogramming.DynamicProgrammingAlgorithms;
import com.architecture.algorithm.leetcode.TwoSumSolution;
import com.architecture.algorithm.util.AlgorithmVisualizer;

import java.util.Arrays;

/**
 * 算法综合演示类
 * 演示README中提到的所有算法类别
 */
public class AlgorithmDemo {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    算法与数据结构实战项目 - 综合演示");
        System.out.println("===========================================");
        
        // 1. 排序算法演示
        demonstrateSortingAlgorithms();
        
        // 2. 查找算法演示
        demonstrateSearchAlgorithms();
        
        // 3. 树算法演示
        demonstrateTreeAlgorithms();
        
        // 4. 图算法演示
        demonstrateGraphAlgorithms();
        
        // 5. 动态规划算法演示
        demonstrateDynamicProgrammingAlgorithms();
        
        // 6. LeetCode题解演示
        demonstrateLeetCodeSolutions();
        
        // 7. 算法可视化演示
        demonstrateAlgorithmVisualization();
        
        // 8. 复杂度分析
        demonstrateComplexityAnalysis();
        
        System.out.println("\n===========================================");
        System.out.println("    演示完成！感谢使用算法与数据结构库");
        System.out.println("===========================================");
    }
    
    private static void demonstrateSortingAlgorithms() {
        System.out.println("\n【排序算法演示】");
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("原数组: " + Arrays.toString(arr));
        
        int[] bubbleArr = arr.clone();
        SortingAlgorithms.bubbleSort(bubbleArr);
        System.out.println("冒泡排序: " + Arrays.toString(bubbleArr));
        
        int[] quickArr = arr.clone();
        SortingAlgorithms.quickSort(quickArr, 0, quickArr.length - 1);
        System.out.println("快速排序: " + Arrays.toString(quickArr));
        
        int[] mergeArr = arr.clone();
        SortingAlgorithms.mergeSort(mergeArr, 0, mergeArr.length - 1);
        System.out.println("归并排序: " + Arrays.toString(mergeArr));
        
        int[] heapArr = arr.clone();
        SortingAlgorithms.heapSort(heapArr);
        System.out.println("堆排序: " + Arrays.toString(heapArr));
    }
    
    private static void demonstrateSearchAlgorithms() {
        System.out.println("\n【查找算法演示】");
        int[] sortedArr = {2, 3, 4, 10, 40, 50, 80, 90, 100};
        int target = 10;
        
        System.out.println("有序数组: " + Arrays.toString(sortedArr));
        System.out.println("目标值: " + target);
        
        int result = SearchAlgorithms.binarySearch(sortedArr, target);
        System.out.println("二分查找结果: 索引 " + result + " (值: " + sortedArr[result] + ")");
        
        result = SearchAlgorithms.linearSearch(sortedArr, target);
        System.out.println("线性查找结果: 索引 " + result + " (值: " + sortedArr[result] + ")");
        
        result = SearchAlgorithms.jumpSearch(sortedArr, target);
        System.out.println("跳跃查找结果: 索引 " + result + " (值: " + sortedArr[result] + ")");
    }
    
    private static void demonstrateTreeAlgorithms() {
        System.out.println("\n【树算法演示】");
        BinaryTreeAlgorithms treeAlgo = new BinaryTreeAlgorithms();
        
        // 构建示例二叉树
        BinaryTreeAlgorithms.TreeNode root = new BinaryTreeAlgorithms.TreeNode(3);
        root.left = new BinaryTreeAlgorithms.TreeNode(9);
        root.right = new BinaryTreeAlgorithms.TreeNode(20);
        root.right.left = new BinaryTreeAlgorithms.TreeNode(15);
        root.right.right = new BinaryTreeAlgorithms.TreeNode(7);
        
        System.out.println("二叉树层序遍历: " + treeAlgo.levelOrder(root));
        System.out.println("二叉树最大深度: " + treeAlgo.maxDepth(root));
        System.out.println("二叉树最小深度: " + treeAlgo.minDepth(root));
        
        // 构建BST
        BinaryTreeAlgorithms.TreeNode bstRoot = null;
        int[] vals = {5, 3, 8, 2, 4, 7, 9};
        for (int val : vals) {
            bstRoot = treeAlgo.insertIntoBST(bstRoot, val);
        }
        System.out.println("BST中序遍历 (应为有序): " + treeAlgo.inorderTraversal(bstRoot));
    }
    
    private static void demonstrateGraphAlgorithms() {
        System.out.println("\n【图算法演示】");
        GraphAlgorithms ga = new GraphAlgorithms();
        
        // 创建示例图
        GraphAlgorithms.Graph graph = new GraphAlgorithms.Graph(5);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);
        
        System.out.print("BFS遍历结果: ");
        ga.bfs(graph, 0);
        
        // Dijkstra演示
        GraphAlgorithms.Graph weightedGraph = new GraphAlgorithms.Graph(4);
        weightedGraph.addUndirectedEdge(0, 1, 4);
        weightedGraph.addUndirectedEdge(0, 2, 3);
        weightedGraph.addUndirectedEdge(1, 2, 1);
        weightedGraph.addUndirectedEdge(1, 3, 2);
        weightedGraph.addUndirectedEdge(2, 3, 4);
        
        int[] distances = ga.dijkstra(weightedGraph, 0);
        System.out.println("Dijkstra最短路径 (从0开始): " + Arrays.toString(distances));
    }
    
    private static void demonstrateDynamicProgrammingAlgorithms() {
        System.out.println("\n【动态规划算法演示】");
        DynamicProgrammingAlgorithms dp = new DynamicProgrammingAlgorithms();
        
        System.out.println("斐波那契数列 (第10项): " + dp.fibonacciDP(10));
        System.out.println("爬楼梯 (5阶): " + dp.climbStairs(5));
        
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("最大子数组和 " + Arrays.toString(nums) + ": " + dp.maxSubArray(nums));
        
        int[] coins = {1, 3, 4};
        System.out.println("硬币找零 (amount=6, coins=[1,3,4]): " + dp.coinChange(coins, 6));
        
        int[] lisNums = {10, 9, 2, 5, 3, 7, 101, 18};
        System.out.println("最长递增子序列长度 " + Arrays.toString(lisNums) + ": " + dp.lengthOfLIS(lisNums));
    }
    
    private static void demonstrateLeetCodeSolutions() {
        System.out.println("\n【LeetCode题解演示】");
        TwoSumSolution solution = new TwoSumSolution();
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        
        int[] result = solution.twoSum(nums, target);
        System.out.println("两数之和: 输入 " + Arrays.toString(nums) + ", 目标 " + target);
        System.out.println("结果索引: [" + result[0] + ", " + result[1] + "]");
        System.out.println("对应值: [" + nums[result[0]] + ", " + nums[result[1]] + "]");
        System.out.println("验证: " + nums[result[0]] + " + " + nums[result[1]] + " = " + (nums[result[0]] + nums[result[1]]));
    }
    
    private static void demonstrateAlgorithmVisualization() {
        System.out.println("\n【算法可视化演示】");
        AlgorithmVisualizer.displayComplexityAnalysis();
    }
    
    private static void demonstrateComplexityAnalysis() {
        System.out.println("\n【复杂度分析指南】");
        System.out.println("时间复杂度: O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(2^n) < O(n!)");
        System.out.println("空间复杂度优化技术:");
        System.out.println("- 原地排序: 快排、堆排序空间优化");
        System.out.println("- 滚动数组: 动态规划空间压缩");
        System.out.println("- 双指针: 链表、数组问题空间优化");
        System.out.println("- 状态压缩: 位运算优化存储");
    }
}