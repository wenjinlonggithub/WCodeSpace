package com.architecture.leetcode;

/**
 * LeetCode #53: Maximum Subarray
 * 题目描述：给定一个整数数组nums，找到一个具有最大和的连续子数组（至少包含一个元素），返回其最大和
 * 公司：阿里巴巴、字节跳动、腾讯高频题
 *
 * 解法1：Kadane算法（动态规划）
 * 时间复杂度：O(n)
 * 空间复杂度：O(1)
 *
 * 解法2：分治法
 * 时间复杂度：O(n log n)
 * 空间复杂度：O(log n) - 递归栈空间
 */
public class MaxSubArraySolution {

    /**
     * 方案1：Kadane算法（推荐）
     * 核心思想：当前位置的最大子数组和 = max(当前元素, 前一个位置的最大子数组和 + 当前元素)
     *
     * @param nums 整数数组
     * @return 最大子数组和
     */
    public int maxSubArray(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // currentSum: 以当前位置结尾的最大子数组和
        // maxSum: 全局最大子数组和
        int currentSum = nums[0];
        int maxSum = nums[0];

        for (int i = 1; i < nums.length; i++) {
            // 如果前面的和是负数，不如重新开始
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            // 更新全局最大值
            maxSum = Math.max(maxSum, currentSum);
        }

        return maxSum;
    }

    /**
     * 方案2：动态规划（更清晰的DP写法）
     * dp[i] 表示以 nums[i] 结尾的最大子数组和
     *
     * @param nums 整数数组
     * @return 最大子数组和
     */
    public int maxSubArrayDP(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        int maxSum = dp[0];

        for (int i = 1; i < nums.length; i++) {
            // 状态转移方程
            dp[i] = Math.max(nums[i], dp[i - 1] + nums[i]);
            maxSum = Math.max(maxSum, dp[i]);
        }

        return maxSum;
    }

    /**
     * 方案3：分治法
     * 将数组分为左右两部分，最大子数组和可能出现在：
     * 1. 左半部分
     * 2. 右半部分
     * 3. 跨越中点（左半部分的最大后缀 + 右半部分的最大前缀）
     *
     * @param nums 整数数组
     * @return 最大子数组和
     */
    public int maxSubArrayDivideConquer(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        return maxSubArrayHelper(nums, 0, nums.length - 1);
    }

    private int maxSubArrayHelper(int[] nums, int left, int right) {
        // 基础情况
        if (left == right) {
            return nums[left];
        }

        int mid = left + (right - left) / 2;

        // 递归求解左右两部分
        int leftMax = maxSubArrayHelper(nums, left, mid);
        int rightMax = maxSubArrayHelper(nums, mid + 1, right);

        // 求跨越中点的最大子数组和
        int crossMax = maxCrossingSum(nums, left, mid, right);

        // 返回三者中的最大值
        return Math.max(Math.max(leftMax, rightMax), crossMax);
    }

    private int maxCrossingSum(int[] nums, int left, int mid, int right) {
        // 计算左半部分的最大后缀和
        int leftSum = Integer.MIN_VALUE;
        int sum = 0;
        for (int i = mid; i >= left; i--) {
            sum += nums[i];
            leftSum = Math.max(leftSum, sum);
        }

        // 计算右半部分的最大前缀和
        int rightSum = Integer.MIN_VALUE;
        sum = 0;
        for (int i = mid + 1; i <= right; i++) {
            sum += nums[i];
            rightSum = Math.max(rightSum, sum);
        }

        // 返回跨越中点的最大和
        return leftSum + rightSum;
    }

    /**
     * 扩展：返回最大子数组的起始和结束位置
     */
    public int[] maxSubArrayWithIndices(int[] nums) {
        if (nums == null || nums.length == 0) {
            return new int[]{0, 0, 0}; // {maxSum, start, end}
        }

        int currentSum = nums[0];
        int maxSum = nums[0];
        int start = 0;
        int end = 0;
        int tempStart = 0;

        for (int i = 1; i < nums.length; i++) {
            if (currentSum < 0) {
                currentSum = nums[i];
                tempStart = i;
            } else {
                currentSum += nums[i];
            }

            if (currentSum > maxSum) {
                maxSum = currentSum;
                start = tempStart;
                end = i;
            }
        }

        return new int[]{maxSum, start, end};
    }

    public static void main(String[] args) {
        MaxSubArraySolution solution = new MaxSubArraySolution();

        // 测试用例1
        int[] nums1 = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.println("Input: nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]");
        System.out.println("Output (Kadane): " + solution.maxSubArray(nums1));
        System.out.println("Output (DP): " + solution.maxSubArrayDP(nums1));
        System.out.println("Output (Divide & Conquer): " + solution.maxSubArrayDivideConquer(nums1));

        int[] result1 = solution.maxSubArrayWithIndices(nums1);
        System.out.println("Max subarray: [" + result1[1] + ", " + result1[2] + "], sum = " + result1[0]);
        System.out.println("Explanation: 最大子数组是 [4, -1, 2, 1]，和为 6\n");

        // 测试用例2
        int[] nums2 = {1};
        System.out.println("Input: nums = [1]");
        System.out.println("Output: " + solution.maxSubArray(nums2));
        System.out.println("Explanation: 只有一个元素\n");

        // 测试用例3
        int[] nums3 = {5, 4, -1, 7, 8};
        System.out.println("Input: nums = [5, 4, -1, 7, 8]");
        System.out.println("Output: " + solution.maxSubArray(nums3));
        System.out.println("Explanation: 最大子数组是整个数组，和为 23");
    }
}
