package com.architecture.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * LeetCode #1: Two Sum
 * 题目描述：给定一个整数数组和一个目标值，找出数组中和为目标值的两个数的下标
 * 解法：使用HashMap存储已遍历的数值及其索引，一次遍历即可找到答案
 * 时间复杂度：O(n)
 * 空间复杂度：O(n)
 */
public class TwoSumSolution {
    
    /**
     * 找出数组中两个数之和等于目标值的索引
     * @param nums 整数数组
     * @param target 目标值
     * @return 两个数的索引组成的数组
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
        
        throw new IllegalArgumentException("No two sum solution");
    }
    
    public static void main(String[] args) {
        TwoSumSolution solution = new TwoSumSolution();
        
        // 测试用例
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        
        int[] result = solution.twoSum(nums, target);
        System.out.println("Input: nums = [2, 7, 11, 15], target = 9");
        System.out.println("Output: [" + result[0] + ", " + result[1] + "]");
        System.out.println("Explanation: nums[" + result[0] + "] + nums[" + result[1] + "] = " + 
                          nums[result[0]] + " + " + nums[result[1]] + " = " + target);
    }
}