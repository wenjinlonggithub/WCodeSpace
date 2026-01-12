package com.architecture.leetcode;

import java.util.*;

/**
 * LeetCode #3: Longest Substring Without Repeating Characters
 * 题目描述：给定一个字符串s，找出其中不含有重复字符的最长子串的长度
 * 公司：字节跳动、腾讯高频题
 *
 * 解法：滑动窗口 + HashSet/HashMap
 * 时间复杂度：O(n)
 * 空间复杂度：O(min(n, m))，其中m是字符集大小
 *
 * 关键点：
 * 1. 使用滑动窗口维护一个无重复字符的子串
 * 2. 用HashSet记录窗口中的字符
 * 3. 遇到重复字符时，移动左指针直到窗口中不再有重复字符
 */
public class LongestSubstringSolution {

    /**
     * 方案1：使用HashSet的滑动窗口
     * @param s 输入字符串
     * @return 最长无重复子串的长度
     */
    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        Set<Character> window = new HashSet<>();
        int maxLen = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);

            // 如果窗口中已经有这个字符，移动左指针
            while (window.contains(c)) {
                window.remove(s.charAt(left));
                left++;
            }

            // 将当前字符加入窗口
            window.add(c);

            // 更新最大长度
            maxLen = Math.max(maxLen, right - left + 1);
        }

        return maxLen;
    }

    /**
     * 方案2：使用HashMap优化的滑动窗口
     * HashMap存储字符及其最新位置，可以直接跳到重复字符的下一个位置
     * @param s 输入字符串
     * @return 最长无重复子串的长度
     */
    public int lengthOfLongestSubstringOptimized(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        Map<Character, Integer> charIndex = new HashMap<>();
        int maxLen = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);

            // 如果字符已存在且在当前窗口内
            if (charIndex.containsKey(c) && charIndex.get(c) >= left) {
                // 直接跳到重复字符的下一个位置
                left = charIndex.get(c) + 1;
            }

            // 更新字符的最新位置
            charIndex.put(c, right);

            // 更新最大长度
            maxLen = Math.max(maxLen, right - left + 1);
        }

        return maxLen;
    }

    /**
     * 方案3：使用数组代替HashMap（适用于ASCII字符）
     * @param s 输入字符串
     * @return 最长无重复子串的长度
     */
    public int lengthOfLongestSubstringArray(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        // 假设字符集为ASCII，共128个字符
        int[] charIndex = new int[128];
        Arrays.fill(charIndex, -1);

        int maxLen = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);

            // 如果字符已存在且在当前窗口内
            if (charIndex[c] >= left) {
                left = charIndex[c] + 1;
            }

            // 更新字符的最新位置
            charIndex[c] = right;

            // 更新最大长度
            maxLen = Math.max(maxLen, right - left + 1);
        }

        return maxLen;
    }

    public static void main(String[] args) {
        LongestSubstringSolution solution = new LongestSubstringSolution();

        // 测试用例1
        String s1 = "abcabcbb";
        System.out.println("Input: s = \"" + s1 + "\"");
        System.out.println("Output (HashSet): " + solution.lengthOfLongestSubstring(s1));
        System.out.println("Output (HashMap): " + solution.lengthOfLongestSubstringOptimized(s1));
        System.out.println("Output (Array): " + solution.lengthOfLongestSubstringArray(s1));
        System.out.println("Explanation: 最长无重复子串是 \"abc\"，长度为 3\n");

        // 测试用例2
        String s2 = "bbbbb";
        System.out.println("Input: s = \"" + s2 + "\"");
        System.out.println("Output (HashSet): " + solution.lengthOfLongestSubstring(s2));
        System.out.println("Explanation: 最长无重复子串是 \"b\"，长度为 1\n");

        // 测试用例3
        String s3 = "pwwkew";
        System.out.println("Input: s = \"" + s3 + "\"");
        System.out.println("Output (HashSet): " + solution.lengthOfLongestSubstring(s3));
        System.out.println("Explanation: 最长无重复子串是 \"wke\"，长度为 3\n");

        // 测试用例4
        String s4 = "abba";
        System.out.println("Input: s = \"" + s4 + "\"");
        System.out.println("Output (HashMap): " + solution.lengthOfLongestSubstringOptimized(s4));
        System.out.println("Explanation: 最长无重复子串是 \"ab\" 或 \"ba\"，长度为 2");
    }
}
