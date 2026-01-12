package com.architecture.leetcode;

import java.util.*;

/**
 * LeetCode #15: 3Sum
 * 题目描述：给定一个包含n个整数的数组nums，判断nums中是否存在三个元素a、b、c
 *          使得a + b + c = 0？找出所有满足条件且不重复的三元组
 * 公司：字节跳动、阿里巴巴高频题
 *
 * 解法：排序 + 双指针
 * 时间复杂度：O(n²)
 * 空间复杂度：O(1) - 不计算结果集的空间
 *
 * 关键点：
 * 1. 先对数组排序
 * 2. 固定一个数，用双指针找另外两个数
 * 3. 通过跳过重复元素来去重
 */
public class ThreeSumSolution {

    /**
     * 找出所有和为0的不重复三元组
     * @param nums 整数数组
     * @return 所有满足条件的三元组列表
     */
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();

        // 边界条件检查
        if (nums == null || nums.length < 3) {
            return result;
        }

        // 排序数组，方便使用双指针
        Arrays.sort(nums);

        // 固定第一个数，用双指针找另外两个数
        for (int i = 0; i < nums.length - 2; i++) {
            // 如果第一个数已经大于0，后面不可能有解
            if (nums[i] > 0) {
                break;
            }

            // 跳过重复的第一个数
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }

            int left = i + 1;
            int right = nums.length - 1;
            int target = -nums[i];

            while (left < right) {
                int sum = nums[left] + nums[right];

                if (sum == target) {
                    // 找到一组解
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));

                    // 跳过重复的第二个数
                    while (left < right && nums[left] == nums[left + 1]) {
                        left++;
                    }

                    // 跳过重复的第三个数
                    while (left < right && nums[right] == nums[right - 1]) {
                        right--;
                    }

                    // 移动指针继续查找
                    left++;
                    right--;
                } else if (sum < target) {
                    // 和太小，左指针右移
                    left++;
                } else {
                    // 和太大，右指针左移
                    right--;
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        ThreeSumSolution solution = new ThreeSumSolution();

        // 测试用例1
        int[] nums1 = {-1, 0, 1, 2, -1, -4};
        System.out.println("Input: nums = [-1, 0, 1, 2, -1, -4]");
        List<List<Integer>> result1 = solution.threeSum(nums1);
        System.out.println("Output: " + result1);
        System.out.println("Explanation: 满足条件的三元组为 [[-1, -1, 2], [-1, 0, 1]]\n");

        // 测试用例2
        int[] nums2 = {0, 1, 1};
        System.out.println("Input: nums = [0, 1, 1]");
        List<List<Integer>> result2 = solution.threeSum(nums2);
        System.out.println("Output: " + result2);
        System.out.println("Explanation: 没有和为0的三元组\n");

        // 测试用例3
        int[] nums3 = {0, 0, 0};
        System.out.println("Input: nums = [0, 0, 0]");
        List<List<Integer>> result3 = solution.threeSum(nums3);
        System.out.println("Output: " + result3);
        System.out.println("Explanation: 唯一的三元组是 [[0, 0, 0]]");
    }
}
