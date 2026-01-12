package com.architecture.leetcode;

import com.architecture.util.ListNode;
import java.util.PriorityQueue;

/**
 * LeetCode #23: Merge k Sorted Lists
 * 题目描述：给定k个排序链表，将它们合并为一个排序链表
 * 公司：字节跳动、阿里巴巴、腾讯高频题
 *
 * 解法1：优先队列（最小堆）
 * 时间复杂度：O(N log k)，其中N是所有链表的节点总数，k是链表数量
 * 空间复杂度：O(k) - 堆的大小
 *
 * 解法2：分治合并
 * 时间复杂度：O(N log k)
 * 空间复杂度：O(log k) - 递归栈空间
 *
 * 解法3：逐一合并
 * 时间复杂度：O(kN)
 * 空间复杂度：O(1)
 */
public class MergeKSortedListsSolution {

    /**
     * 方案1：使用优先队列（最小堆）
     * 将所有链表的头节点放入最小堆，每次取出最小的节点，并将该节点的下一个节点放入堆
     *
     * @param lists k个排序链表数组
     * @return 合并后的链表头节点
     */
    public ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }

        // 创建最小堆，按节点值排序
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>(
            (a, b) -> a.val - b.val
        );

        // 将所有链表的头节点加入堆
        for (ListNode head : lists) {
            if (head != null) {
                minHeap.offer(head);
            }
        }

        // 创建哑节点，简化边界处理
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        // 每次取出堆顶最小节点，加入结果链表
        while (!minHeap.isEmpty()) {
            ListNode minNode = minHeap.poll();
            current.next = minNode;
            current = current.next;

            // 如果该节点还有后续节点，将其加入堆
            if (minNode.next != null) {
                minHeap.offer(minNode.next);
            }
        }

        return dummy.next;
    }

    /**
     * 方案2：分治合并（推荐）
     * 将k个链表配对并将同一对中的链表合并
     * 第一轮合并后，k个链表被合并成k/2个链表
     * 重复这一过程，直到得到最终的有序链表
     *
     * @param lists k个排序链表数组
     * @return 合并后的链表头节点
     */
    public ListNode mergeKListsDivideConquer(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }
        return merge(lists, 0, lists.length - 1);
    }

    private ListNode merge(ListNode[] lists, int left, int right) {
        // 基础情况
        if (left == right) {
            return lists[left];
        }

        int mid = left + (right - left) / 2;

        // 递归合并左右两部分
        ListNode l1 = merge(lists, left, mid);
        ListNode l2 = merge(lists, mid + 1, right);

        // 合并两个有序链表
        return mergeTwoLists(l1, l2);
    }

    /**
     * 合并两个有序链表
     */
    private ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null) return l2;
        if (l2 == null) return l1;

        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        while (l1 != null && l2 != null) {
            if (l1.val <= l2.val) {
                current.next = l1;
                l1 = l1.next;
            } else {
                current.next = l2;
                l2 = l2.next;
            }
            current = current.next;
        }

        // 连接剩余节点
        current.next = (l1 != null) ? l1 : l2;

        return dummy.next;
    }

    /**
     * 方案3：逐一合并
     * 依次将每个链表合并到结果链表中
     *
     * @param lists k个排序链表数组
     * @return 合并后的链表头节点
     */
    public ListNode mergeKListsSequential(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }

        ListNode result = lists[0];
        for (int i = 1; i < lists.length; i++) {
            result = mergeTwoLists(result, lists[i]);
        }

        return result;
    }

    // 辅助方法：创建链表
    public ListNode createList(int[] values) {
        if (values == null || values.length == 0) {
            return null;
        }

        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        for (int val : values) {
            current.next = new ListNode(val);
            current = current.next;
        }

        return dummy.next;
    }

    // 辅助方法：打印链表
    public void printList(ListNode head) {
        if (head == null) {
            System.out.println("Empty list");
            return;
        }

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

    public static void main(String[] args) {
        MergeKSortedListsSolution solution = new MergeKSortedListsSolution();

        // 测试用例1
        System.out.println("=== Test Case 1: Priority Queue ===");
        ListNode[] lists1 = new ListNode[3];
        lists1[0] = solution.createList(new int[]{1, 4, 5});
        lists1[1] = solution.createList(new int[]{1, 3, 4});
        lists1[2] = solution.createList(new int[]{2, 6});

        System.out.println("Input:");
        for (int i = 0; i < lists1.length; i++) {
            System.out.print("List " + (i + 1) + ": ");
            solution.printList(lists1[i]);
        }

        ListNode result1 = solution.mergeKLists(lists1);
        System.out.print("Output: ");
        solution.printList(result1);

        // 测试用例2
        System.out.println("\n=== Test Case 2: Divide and Conquer ===");
        ListNode[] lists2 = new ListNode[3];
        lists2[0] = solution.createList(new int[]{1, 4, 5});
        lists2[1] = solution.createList(new int[]{1, 3, 4});
        lists2[2] = solution.createList(new int[]{2, 6});

        ListNode result2 = solution.mergeKListsDivideConquer(lists2);
        System.out.print("Output: ");
        solution.printList(result2);

        // 测试用例3：空链表
        System.out.println("\n=== Test Case 3: Empty Lists ===");
        ListNode[] lists3 = new ListNode[0];
        ListNode result3 = solution.mergeKLists(lists3);
        System.out.print("Output: ");
        solution.printList(result3);

        // 测试用例4：单个链表
        System.out.println("\n=== Test Case 4: Single List ===");
        ListNode[] lists4 = new ListNode[1];
        lists4[0] = solution.createList(new int[]{1, 2, 3});
        ListNode result4 = solution.mergeKLists(lists4);
        System.out.print("Output: ");
        solution.printList(result4);
    }
}
