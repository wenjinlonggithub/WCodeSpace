package com.architecture;

import com.architecture.leetcode.*;
import com.architecture.util.ListNode;
import java.util.List;

/**
 * LeetCode Solutions Main Class
 * 演示各种LeetCode经典题目的解决方案
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Welcome to LeetCode Solutions - 大厂算法面试题集!" );
        System.out.println("=======================================================");

        // 演示Two Sum解决方案
        System.out.println("\n1. Two Sum Solution:");
        TwoSumSolution twoSumSolution = new TwoSumSolution();
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        int[] result = twoSumSolution.twoSum(nums, target);
        System.out.println("Result: [" + result[0] + ", " + result[1] + "]");

        // 演示Reverse Linked List解决方案
        System.out.println("\n2. Reverse Linked List Solution:");
        ReverseLinkedListSolution reverseSolution = new ReverseLinkedListSolution();

        // 创建链表并演示反转
        ListNode head = reverseSolution.createList(new int[]{1, 2, 3, 4, 5});
        System.out.print("Original: ");
        reverseSolution.printList(head);

        // 重新创建链表，因为上面的链表已经被修改
        ListNode head2 = reverseSolution.createList(new int[]{1, 2, 3, 4, 5});
        ListNode reversed = reverseSolution.reverseList(head2);
        System.out.print("Reversed: ");
        reverseSolution.printList(reversed);

        // 演示Climbing Stairs解决方案
        System.out.println("\n3. Climbing Stairs Solution:");
        ClimbingStairsSolution climbingSolution = new ClimbingStairsSolution();
        int stairsResult = climbingSolution.climbStairs(5);
        System.out.println("Number of ways to climb 5 stairs: " + stairsResult);

        System.out.println("\n=== 字节跳动高频题 ===");

        // 演示LRU Cache解决方案
        System.out.println("\n4. LRU Cache Solution (字节高频):");
        LRUCacheSolution.LRUCache cache = new LRUCacheSolution.LRUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        System.out.println("get(1): " + cache.get(1));
        cache.put(3, 3);
        System.out.println("get(2): " + cache.get(2));
        System.out.println("get(3): " + cache.get(3));

        // 演示Three Sum解决方案
        System.out.println("\n5. Three Sum Solution (字节高频):");
        ThreeSumSolution threeSumSolution = new ThreeSumSolution();
        int[] nums3 = {-1, 0, 1, 2, -1, -4};
        List<List<Integer>> threeResult = threeSumSolution.threeSum(nums3);
        System.out.println("Three sum result: " + threeResult);

        // 演示Longest Substring Without Repeating Characters解决方案
        System.out.println("\n6. Longest Substring Without Repeating Characters (字节高频):");
        LongestSubstringSolution longestSolution = new LongestSubstringSolution();
        String s = "abcabcbb";
        int lengthResult = longestSolution.lengthOfLongestSubstring(s);
        System.out.println("Length of longest substring: " + lengthResult);

        // 演示Merge K Sorted Lists解决方案
        System.out.println("\n7. Merge K Sorted Lists (字节高频):");
        MergeKSortedListsSolution mergeSolution = new MergeKSortedListsSolution();
        ListNode[] lists = new ListNode[3];
        lists[0] = mergeSolution.createList(new int[]{1, 4, 5});
        lists[1] = mergeSolution.createList(new int[]{1, 3, 4});
        lists[2] = mergeSolution.createList(new int[]{2, 6});
        ListNode mergedResult = mergeSolution.mergeKLists(lists);
        System.out.print("Merged list: ");
        mergeSolution.printList(mergedResult);

        System.out.println("\n=== 阿里巴巴高频题 ===");

        // 演示Maximum Subarray解决方案
        System.out.println("\n8. Maximum Subarray (阿里高频):");
        MaxSubArraySolution maxSubSolution = new MaxSubArraySolution();
        int[] numsMax = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        int maxResult = maxSubSolution.maxSubArray(numsMax);
        System.out.println("Maximum subarray sum: " + maxResult);

        System.out.println("\n=== 通用高频题 ===");

        // 演示Number of Islands解决方案
        System.out.println("\n9. Number of Islands (DFS/BFS经典):");
        NumberOfIslandsSolution islandsSolution = new NumberOfIslandsSolution();
        char[][] grid = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
        };
        int islandsResult = islandsSolution.numIslands(grid);
        System.out.println("Number of islands: " + islandsResult);

        System.out.println("\n=======================================================");
        System.out.println("All LeetCode solutions demonstrated!");
        System.out.println("涵盖：数组、链表、字符串、动态规划、设计题、图论等多个领域");
    }
}