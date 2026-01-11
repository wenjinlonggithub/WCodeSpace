package com.architecture.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * LeetCode第1题：两数之和
 * 根据README中的描述实现
 */
public class TwoSumSolution {
    
    /**
     * 使用哈希表解决两数之和问题
     * 时间复杂度: O(n)
     * 空间复杂度: O(n)
     * 
     * @param nums 整数数组
     * @param target 目标值
     * @return 返回两个数字的索引数组
     */
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                return new int[]{map.get(complement), i};
            }
            map.put(nums[i], i);
        }
        throw new IllegalArgumentException("No solution");
    }
    
    /**
     * 暴力解法（用于比较）
     * 时间复杂度: O(n²)
     * 空间复杂度: O(1)
     */
    public int[] twoSumBruteForce(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalArgumentException("No solution");
    }
    
    public static void main(String[] args) {
        TwoSumSolution solution = new TwoSumSolution();
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        
        int[] result = solution.twoSum(nums, target);
        System.out.println("输入数组: [2, 7, 11, 15]");
        System.out.println("目标值: " + target);
        System.out.println("结果索引: [" + result[0] + ", " + result[1] + "]");
        System.out.println("对应的值: [" + nums[result[0]] + ", " + nums[result[1]] + "]");
        System.out.println("验证: " + nums[result[0]] + " + " + nums[result[1]] + " = " + (nums[result[0]] + nums[result[1]]));
    }
}