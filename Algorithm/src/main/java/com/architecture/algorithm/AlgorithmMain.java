package com.architecture.algorithm;

/**
 * 算法与数据结构主类
 * 根据README中的描述，实现经典算法、LeetCode题解、复杂度分析等
 */
public class AlgorithmMain {
    public static void main(String[] args) {
        System.out.println("欢迎来到算法与数据结构实战项目！");
        System.out.println("本项目包含多种经典算法的Java实现");
        System.out.println("包括排序算法、查找算法、树算法、图算法、动态规划等");
        
        // 演示两数之和算法
        System.out.println("\n示例：两数之和算法演示");
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        com.architecture.algorithm.leetcode.TwoSumSolution solution = new com.architecture.algorithm.leetcode.TwoSumSolution();
        int[] result = solution.twoSum(nums, target);
        System.out.println("输入数组: [2, 7, 11, 15], 目标值: " + target);
        System.out.println("返回索引: [" + result[0] + ", " + result[1] + "]");
    }
    
    /**
     * 复杂度分析枚举
     */
    public enum Complexity {
        O1("O(1) - 常数时间"),
        OLOGN("O(log n) - 对数时间"),
        ON("O(n) - 线性时间"),
        ONLOGN("O(n log n) - 线性对数时间"),
        ON2("O(n²) - 平方时间"),
        O2N("O(2^n) - 指数时间"),
        ON_("O(n!) - 阶乘时间");
        
        private final String description;
        
        Complexity(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}