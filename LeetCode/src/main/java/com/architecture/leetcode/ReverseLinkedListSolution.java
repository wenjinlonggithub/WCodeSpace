package com.architecture.leetcode;

import com.architecture.util.ListNode;

/**
 * LeetCode #206: Reverse Linked List
 * 题目描述：反转一个单链表
 * 解法：使用迭代或递归方法
 * 时间复杂度：O(n)
 * 空间复杂度：O(1) 迭代方法，O(n) 递归方法
 */
public class ReverseLinkedListSolution {
    
    /**
     * 使用迭代方法反转链表
     * @param head 链表头节点
     * @return 反转后的链表头节点
     */
    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode current = head;
        
        while (current != null) {
            ListNode nextTemp = current.next;  // 保存下一个节点
            current.next = prev;               // 当前节点指向前一个节点
            prev = current;                    // 移动prev指针
            current = nextTemp;                // 移动current指针
        }
        
        return prev;  // prev现在是新的头节点
    }
    
    /**
     * 使用递归方法反转链表
     * @param head 链表头节点
     * @return 反转后的链表头节点
     */
    public ListNode reverseListRecursive(ListNode head) {
        // 基础情况：如果链表为空或者只有一个节点
        if (head == null || head.next == null) {
            return head;
        }
        
        // 递归反转剩余部分
        ListNode newHead = reverseListRecursive(head.next);
        
        // 反转当前连接
        head.next.next = head;
        head.next = null;
        
        return newHead;
    }
    
    /**
     * 辅助方法：打印链表
     */
    public void printList(ListNode head) {
        ListNode current = head;
        while (current != null) {
            System.out.print(current.val);
            if (current.next != null) {
                System.out.print(" -> ");
            }
            current = current.next;
        }
        System.out.println();
    }
    
    /**
     * 辅助方法：创建链表
     */
    public ListNode createList(int[] values) {
        if (values.length == 0) return null;
        
        ListNode head = new ListNode(values[0]);
        ListNode current = head;
        
        for (int i = 1; i < values.length; i++) {
            current.next = new ListNode(values[i]);
            current = current.next;
        }
        
        return head;
    }
    
    public static void main(String[] args) {
        ReverseLinkedListSolution solution = new ReverseLinkedListSolution();
        
        // 测试用例
        int[] values = {1, 2, 3, 4, 5};
        ListNode head = solution.createList(values);
        
        System.out.println("Original List:");
        solution.printList(head);
        
        ListNode reversedHead = solution.reverseList(head);
        System.out.println("Reversed List (Iterative):");
        solution.printList(reversedHead);
        
        // 重新创建链表用于递归测试
        ListNode head2 = solution.createList(new int[]{1, 2, 3, 4, 5});
        System.out.println("Reversed List (Recursive):");
        ListNode reversedRecursive = solution.reverseListRecursive(head2);
        solution.printList(reversedRecursive);
    }
}