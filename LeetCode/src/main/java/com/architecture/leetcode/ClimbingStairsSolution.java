package com.architecture.leetcode;

/**
 * LeetCode #70: Climbing Stairs
 * 题目描述：每次可以爬1或2个台阶，问有多少种方法到达第n阶
 * 解法：动态规划，斐波那契数列变形
 * 时间复杂度：O(n)
 * 空间复杂度：O(1)
 */
public class ClimbingStairsSolution {
    
    /**
     * 计算爬楼梯的方法数
     * @param n 楼梯数
     * @return 方法总数
     */
    public int climbStairs(int n) {
        if (n <= 2) {
            return n;
        }
        
        // 使用动态规划，只保存前两个状态
        int prev2 = 1;  // f(1) = 1
        int prev1 = 2;  // f(2) = 2
        int current = 0;
        
        for (int i = 3; i <= n; i++) {
            current = prev1 + prev2;  // f(i) = f(i-1) + f(i-2)
            prev2 = prev1;
            prev1 = current;
        }
        
        return current;
    }
    
    /**
     * 使用数组的动态规划解法（更容易理解）
     */
    public int climbStairsWithArray(int n) {
        if (n <= 2) {
            return n;
        }
        
        int[] dp = new int[n + 1];
        dp[1] = 1;
        dp[2] = 2;
        
        for (int i = 3; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        
        return dp[n];
    }
    
    /**
     * 递归解法（带记忆化，避免重复计算）
     */
    public int climbStairsMemoization(int n) {
        int[] memo = new int[n + 1];
        return climbStairsHelper(n, memo);
    }
    
    private int climbStairsHelper(int n, int[] memo) {
        if (n <= 2) {
            return n;
        }
        
        if (memo[n] != 0) {
            return memo[n];
        }
        
        memo[n] = climbStairsHelper(n - 1, memo) + climbStairsHelper(n - 2, memo);
        return memo[n];
    }
    
    public static void main(String[] args) {
        ClimbingStairsSolution solution = new ClimbingStairsSolution();
        
        // 测试用例
        int[] testCases = {1, 2, 3, 4, 5, 6};
        
        System.out.println("Climbing Stairs Solutions Comparison:");
        System.out.println("n\t| Iterative\t| With Array\t| Memoization");
        System.out.println("----------------------------------------");
        
        for (int n : testCases) {
            int result1 = solution.climbStairs(n);
            int result2 = solution.climbStairsWithArray(n);
            int result3 = solution.climbStairsMemoization(n);
            
            System.out.println(n + "\t| " + result1 + "\t\t| " + result2 + "\t\t| " + result3);
        }
        
        System.out.println("\nDetailed explanation for n=5:");
        System.out.println("To reach step 5, we can:");
        System.out.println("- Take 1 step from step 4 (f(4) ways)");
        System.out.println("- Take 2 steps from step 3 (f(3) ways)");
        System.out.println("So f(5) = f(4) + f(3) = " + solution.climbStairs(4) + " + " + 
                          solution.climbStairs(3) + " = " + solution.climbStairs(5));
    }
}