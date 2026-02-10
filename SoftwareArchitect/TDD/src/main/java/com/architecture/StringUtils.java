package com.architecture;

/**
 * 字符串工具类
 *
 * 【TDD实践：从测试到实现】
 * 这个类的每个方法都是先写测试，再写实现
 * 这种方式带来的好处：
 * 1. 方法签名由使用场景决定，而不是凭空设计
 * 2. 每个方法都有测试保护，重构时更有信心
 * 3. 测试即文档，展示了方法的正确用法
 *
 * 【重构的安全性】
 * 有了测试保护，我们可以放心地重构代码
 * 例如：reverse方法可以有多种实现方式
 * - 使用StringBuilder.reverse()
 * - 使用递归
 * - 使用循环
 * 只要测试通过，任何实现都是可接受的
 */
public class StringUtils {

    /**
     * 反转字符串
     *
     * TDD开发过程：
     * 1. 先写测试：shouldReverseString
     * 2. 看到红灯（方法不存在）
     * 3. 创建方法，实现最简单的逻辑
     * 4. 测试通过（绿灯）
     * 5. 添加边界测试：空字符串
     * 6. 完善实现，处理边界情况
     *
     * @param input 待反转的字符串
     * @return 反转后的字符串
     */
    public String reverse(String input) {
        // 边界处理：这是在添加边界测试后补充的
        if (input == null || input.isEmpty()) {
            return input;
        }
        // 核心实现：使用StringBuilder的reverse方法
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * 判断是否为回文字符串
     *
     * TDD的代码复用：
     * 这个方法复用了reverse方法
     * 因为reverse方法已经有测试保护，所以可以放心使用
     *
     * @param input 待检测的字符串
     * @return 如果是回文返回true，否则返回false
     */
    public boolean isPalindrome(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        String reversed = reverse(input);
        return input.equals(reversed);
    }

    /**
     * 统计字符串中的元音字母数量
     *
     * TDD的需求驱动：
     * 测试明确了需求："hello world" 应该返回 3
     * 这让我们知道：
     * - 需要统计 a, e, i, o, u
     * - 空格不算
     * - 大小写如何处理（可以通过添加测试来明确）
     *
     * @param input 待统计的字符串
     * @return 元音字母的数量
     */
    public int countVowels(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        int count = 0;
        String vowels = "aeiouAEIOU";
        for (char c : input.toCharArray()) {
            if (vowels.indexOf(c) != -1) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将字符串中每个单词的首字母大写
     *
     * TDD的渐进式开发：
     * 1. 先实现基本功能（单个单词）
     * 2. 测试通过
     * 3. 扩展到多个单词
     * 4. 添加更多测试（连续空格、首尾空格等）
     * 5. 完善实现
     *
     * @param input 待处理的字符串
     * @return 每个单词首字母大写后的字符串
     */
    public String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)))
                      .append(words[i].substring(1));
            }
            if (i < words.length - 1) {
                result.append(" ");
            }
        }
        return result.toString();
    }
}
