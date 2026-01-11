package com.architecture.algorithm.dynamicprogramming;

import java.util.Arrays;

/**
 * 动态规划算法实现类
 * 根据README中的描述，实现斐波那契、爬楼梯、背包问题等经典DP问题
 */
public class DynamicProgrammingAlgorithms {
    
    /**
     * 斐波那契数列 - 递归实现（低效）
     * 时间复杂度: O(2^n)
     * 空间复杂度: O(n)
     */
    public long fibonacciRecursive(int n) {
        if (n <= 1) return n;
        return fibonacciRecursive(n - 1) + fibonacciRecursive(n - 2);
    }
    
    /**
     * 斐波那契数列 - 记忆化递归实现
     * 时间复杂度: O(n)
     * 空间复杂度: O(n)
     */
    public long fibonacciMemoization(int n) {
        long[] memo = new long[n + 1];
        Arrays.fill(memo, -1);
        return fibonacciMemoHelper(n, memo);
    }
    
    private long fibonacciMemoHelper(int n, long[] memo) {
        if (n <= 1) return n;
        if (memo[n] != -1) return memo[n];
        
        memo[n] = fibonacciMemoHelper(n - 1, memo) + fibonacciMemoHelper(n - 2, memo);
        return memo[n];
    }
    
    /**
     * 斐波那契数列 - 动态规划实现
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public long fibonacciDP(int n) {
        if (n <= 1) return n;
        
        long prev2 = 0;
        long prev1 = 1;
        long current = 0;
        
        for (int i = 2; i <= n; i++) {
            current = prev1 + prev2;
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
    
    /**
     * 爬楼梯问题
     * 每次可以爬1或2个台阶，求到达n阶的不同方法数
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public int climbStairs(int n) {
        if (n <= 2) return n;
        
        int prev2 = 1; // 到达第1阶的方法数
        int prev1 = 2; // 到达第2阶的方法数
        int current = 0;
        
        for (int i = 3; i <= n; i++) {
            current = prev1 + prev2;
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
    
    /**
     * 最大子数组和（Kadane算法）
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public int maxSubArray(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        
        int maxSoFar = nums[0];
        int maxEndingHere = nums[0];
        
        for (int i = 1; i < nums.length; i++) {
            maxEndingHere = Math.max(nums[i], maxEndingHere + nums[i]);
            maxSoFar = Math.max(maxSoFar, maxEndingHere);
        }
        
        return maxSoFar;
    }
    
    /**
     * 0-1背包问题
     * 时间复杂度: O(nW) n为物品数量，W为背包容量
     * 空间复杂度: O(nW)
     */
    public int knapsack01(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];
        
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                if (weights[i - 1] <= w) {
                    // 可以选择当前物品，取选与不选的最大值
                    dp[i][w] = Math.max(
                        dp[i - 1][w],                           // 不选当前物品
                        dp[i - 1][w - weights[i - 1]] + values[i - 1]  // 选当前物品
                    );
                } else {
                    // 无法选择当前物品
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }
        
        return dp[n][capacity];
    }
    
    /**
     * 完全背包问题
     * 时间复杂度: O(nW)
     * 空间复杂度: O(nW)
     */
    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1); // 使用一个较大值初始化
        dp[0] = 0;
        
        for (int i = 1; i <= amount; i++) {
            for (int coin : coins) {
                if (coin <= i) {
                    dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                }
            }
        }
        
        return dp[amount] > amount ? -1 : dp[amount];
    }
    
    /**
     * 最长递增子序列（LIS）
     * 时间复杂度: O(n²)
     * 空间复杂度: O(n)
     */
    public int lengthOfLIS(int[] nums) {
        if (nums == null || nums.length == 0) return 0;
        
        int[] dp = new int[nums.length];
        Arrays.fill(dp, 1);
        int maxLength = 1;
        
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxLength = Math.max(maxLength, dp[i]);
        }
        
        return maxLength;
    }
    
    /**
     * 编辑距离（Levenshtein距离）
     * 时间复杂度: O(mn) m,n为两个字符串长度
     * 空间复杂度: O(mn)
     */
    public int minDistance(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        // 初始化边界条件
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // 字符相同，无需操作
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j],     // 删除
                                dp[i][j - 1]),     // 插入
                        dp[i - 1][j - 1]          // 替换
                    );
                }
            }
        }
        
        return dp[m][n];
    }
    
    /**
     * 矩阵链乘法
     * 时间复杂度: O(n³)
     * 空间复杂度: O(n²)
     */
    public int matrixChainMultiplication(int[] dimensions) {
        int n = dimensions.length - 1;
        int[][] dp = new int[n][n];
        
        // len是矩阵链的长度
        for (int len = 2; len <= n; len++) {
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                dp[i][j] = Integer.MAX_VALUE;
                
                for (int k = i; k < j; k++) {
                    int cost = dp[i][k] + dp[k + 1][j] + 
                              dimensions[i] * dimensions[k + 1] * dimensions[j + 1];
                    dp[i][j] = Math.min(dp[i][j], cost);
                }
            }
        }
        
        return dp[0][n - 1];
    }
    
    public static void main(String[] args) {
        DynamicProgrammingAlgorithms dp = new DynamicProgrammingAlgorithms();
        
        // 测试斐波那契数列
        System.out.println("斐波那契数列测试:");
        int n = 10;
        System.out.println("第" + n + "个斐波那契数 (DP): " + dp.fibonacciDP(n));
        
        // 测试爬楼梯
        System.out.println("\n爬楼梯问题:");
        int stairs = 5;
        System.out.println("爬" + stairs + "阶楼梯的方法数: " + dp.climbStairs(stairs));
        
        // 测试最大子数组和
        System.out.println("\n最大子数组和:");
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("数组: " + Arrays.toString(nums));
        System.out.println("最大子数组和: " + dp.maxSubArray(nums));
        
        // 测试0-1背包
        System.out.println("\n0-1背包问题:");
        int[] weights = {1, 3, 4};
        int[] values = {1, 4, 5};
        int capacity = 7;
        System.out.println("物品重量: " + Arrays.toString(weights));
        System.out.println("物品价值: " + Arrays.toString(values));
        System.out.println("背包容量: " + capacity);
        System.out.println("最大价值: " + dp.knapsack01(weights, values, capacity));
        
        // 测试硬币找零（完全背包）
        System.out.println("\n硬币找零问题:");
        int[] coins = {1, 3, 4};
        int amount = 6;
        System.out.println("硬币面额: " + Arrays.toString(coins));
        System.out.println("目标金额: " + amount);
        System.out.println("最少硬币数: " + dp.coinChange(coins, amount));
        
        // 测试最长递增子序列
        System.out.println("\n最长递增子序列:");
        int[] lisNums = {10, 9, 2, 5, 3, 7, 101, 18};
        System.out.println("数组: " + Arrays.toString(lisNums));
        System.out.println("LIS长度: " + dp.lengthOfLIS(lisNums));
        
        // 测试编辑距离
        System.out.println("\n编辑距离:");
        String word1 = "horse";
        String word2 = "ros";
        System.out.println("单词1: " + word1);
        System.out.println("单词2: " + word2);
        System.out.println("编辑距离: " + dp.minDistance(word1, word2));
        
        // 测试矩阵链乘法
        System.out.println("\n矩阵链乘法:");
        int[] dimensions = {1, 2, 3, 4, 5}; // 表示矩阵A1(1x2), A2(2x3), A3(3x4), A4(4x5)
        System.out.println("矩阵维度: " + Arrays.toString(dimensions));
        System.out.println("最小乘法次数: " + dp.matrixChainMultiplication(dimensions));
    }
}