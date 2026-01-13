package com.architecture.datastructure.linear.linkedlist;

import com.architecture.datastructure.common.Node;

/**
 * LinkedList 面试高频题解析
 * LinkedList Common Interview Questions and Solutions
 *
 * <p>题目分类:
 * <ol>
 *   <li>链表反转 (简单-中等)</li>
 *   <li>链表环检测 (中等)</li>
 *   <li>合并有序链表 (简单)</li>
 *   <li>删除倒数第N个节点 (中等)</li>
 *   <li>链表中间节点 (简单)</li>
 *   <li>回文链表 (简单)</li>
 *   <li>相交链表 (简单)</li>
 * </ol>
 *
 * @author Architecture Team
 * @version 1.0
 * @since 2026-01-13
 */
public class LinkedListInterviewQuestions {

    /**
     * 【题目1】反转链表 - LeetCode #206
     * 难度: 简单 ⭐⭐
     *
     * <p>题目描述:
     * 给定单链表的头节点 head，请你反转链表，并返回反转后的链表。
     *
     * <p>示例:
     * <pre>
     * 输入: 1 -> 2 -> 3 -> 4 -> 5 -> null
     * 输出: 5 -> 4 -> 3 -> 2 -> 1 -> null
     * </pre>
     *
     * <p>解题思路:
     * 迭代法:
     * <ol>
     *   <li>使用三个指针 prev, current, next</li>
     *   <li>遍历链表，逐个反转节点的指向</li>
     *   <li>prev指向前一个节点，current指向当前节点，next保存下一个节点</li>
     * </ol>
     *
     * @param head 链表头节点
     * @return 反转后的链表头节点
     * 时间复杂度: O(n)
     * 空间复杂度: O(1) 迭代法
     */
    public static <T> Node<T> reverseList(Node<T> head) {
        Node<T> prev = null;
        Node<T> current = head;

        while (current != null) {
            Node<T> next = current.next;  // 保存下一个节点
            current.next = prev;          // 反转指针
            prev = current;               // prev前进一步
            current = next;               // current前进一步
        }

        return prev;  // prev是新的头节点
    }

    /**
     * 【题目1.2】反转链表 - 递归解法
     *
     * <p>解题思路:
     * 递归法:
     * <ol>
     *   <li>递归到链表尾部</li>
     *   <li>从尾部开始反转指针</li>
     *   <li>返回新的头节点（原尾节点）</li>
     * </ol>
     *
     * @param head 链表头节点
     * @return 反转后的链表头节点
     * 时间复杂度: O(n)
     * 空间复杂度: O(n) - 递归栈空间
     */
    public static <T> Node<T> reverseListRecursive(Node<T> head) {
        // 递归终止条件
        if (head == null || head.next == null) {
            return head;
        }

        // 递归反转后续节点
        Node<T> newHead = reverseListRecursive(head.next);

        // 反转当前节点
        head.next.next = head;
        head.next = null;

        return newHead;
    }

    /**
     * 【题目2】环形链表检测 - LeetCode #141
     * 难度: 简单 ⭐⭐
     *
     * <p>题目描述:
     * 给定一个链表，判断链表中是否有环。
     *
     * <p>解题思路:
     * 快慢指针法 (Floyd判圈算法/龟兔赛跑算法):
     * <ol>
     *   <li>定义两个指针：slow和fast，都从头节点开始</li>
     *   <li>slow指针每次移动1步，fast指针每次移动2步</li>
     *   <li>如果链表无环，fast指针最终会遇到null，说明链表无环</li>
     *   <li>如果链表有环，在某个时刻，fast指针一定会追上slow指针</li>
     *   <li>就像两个人在圆形跑道上跑步，速度快的人最终会追上速度慢的人</li>
     * </ol>
     *
     * <p>算法原理详解:
     * <pre>
     * 假设链表结构如下:
     * A -> B -> C -> D -> E
     *           ^         |
     *           |_________|
     *
     * slow指针每次走1步，fast指针每次走2步
     * - 如果没有环: fast会先走到null，返回false
     * - 如果有环: 
     *   - 当slow进入环时，fast已经在环中某处
     *   - 由于fast比slow快1步(相对速度)，所以fast会逐渐追上slow
     *   - 在环中，fast相对于slow以1步的速度前进
     *   - 最终fast会追上slow，此时返回true
     * </pre>
     *
     * @param head 链表头节点
     * @return true if the list has a cycle
     * 时间复杂度: O(n) - 在最坏情况下，每个节点最多被访问常数次
     * 空间复杂度: O(1) - 只使用了两个指针的额外空间
     *
     * 面试要点:
     * <ol>
     *   <li>快慢指针技巧：slow每次移动一步，fast每次移动两步</li>
     *   <li>时间复杂度O(n)，空间复杂度O(1)，是最优解</li>
     *   <li>关键在于理解为什么快指针能追上慢指针：在有环的情况下，快指针在环中相对于慢指针以1步/轮的速度追赶</li>
     *   <li>面试时可以画图辅助说明：画出链表结构，标出快慢指针的位置变化</li>
     *   <li>扩展问题：如何找到环的入口点？（可选）</li>
     * </ol>
     * 
     *
     */
    public static <T> boolean hasCycle(Node<T> head) {
        if (head == null || head.next == null) {
            return false;
        }

        Node<T> slow = head;
        Node<T> fast = head.next;

        while (slow != fast) {
            if (fast == null || fast.next == null) {
                return false;
            }
            slow = slow.next;
            fast = fast.next.next;
        }

        return true;
    }

    /**
     * 【题目3】合并两个有序链表 - LeetCode #21
     * 难度: 简单 ⭐⭐
     *
     * <p>题目描述:
     * 将两个升序链表合并为一个新的升序链表并返回。
     *
     * <p>示例:
     * <pre>
     * 输入: l1 = 1->2->4, l2 = 1->3->4
     * 输出: 1->1->2->3->4->4
     * </pre>
     *
     * <p>解题思路:
     * <ol>
     *   <li>使用哨兵节点(dummy node)简化边界处理</li>
     *   <li>双指针比较，选择较小的节点接入结果链表</li>
     *   <li>处理剩余节点</li>
     * </ol>
     *
     * @param l1 第一个有序链表
     * @param l2 第二个有序链表
     * @return 合并后的有序链表
     * 时间复杂度: O(m + n)
     * 空间复杂度: O(1)
     */
    public static Node<Integer> mergeTwoLists(Node<Integer> l1, Node<Integer> l2) {
        // 哨兵节点，简化头节点处理
        Node<Integer> dummy = new Node<>(0);
        Node<Integer> current = dummy;

        while (l1 != null && l2 != null) {
            if (l1.data <= l2.data) {
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
     * 【题目4】删除链表的倒数第N个节点 - LeetCode #19
     * 难度: 中等 ⭐⭐⭐
     *
     * <p>题目描述:
     * 给定一个链表，删除链表的倒数第 n 个节点，并返回链表的头节点。
     *
     * <p>示例:
     * <pre>
     * 输入: 1->2->3->4->5, n = 2
     * 输出: 1->2->3->5
     * </pre>
     *
     * <p>解题思路:
     * 双指针法（一次遍历）:
     * <ol>
     *   <li>使用哨兵节点</li>
     *   <li>fast指针先走n+1步</li>
     *   <li>fast和slow同时走，直到fast到达末尾</li>
     *   <li>此时slow.next就是要删除的节点</li>
     * </ol>
     *
     * @param head 链表头节点
     * @param n 倒数第n个节点
     * @return 删除节点后的链表头节点
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public static <T> Node<T> removeNthFromEnd(Node<T> head, int n) {
        Node<T> dummy = new Node<>(null);
        dummy.next = head;

        Node<T> fast = dummy;
        Node<T> slow = dummy;

        // fast先走n+1步
        for (int i = 0; i <= n; i++) {
            fast = fast.next;
        }

        // fast和slow同时走
        while (fast != null) {
            fast = fast.next;
            slow = slow.next;
        }

        // 删除节点
        slow.next = slow.next.next;

        return dummy.next;
    }

    /**
     * 【题目5】链表的中间节点 - LeetCode #876
     * 难度: 简单 ⭐⭐
     *
     * <p>题目描述:
     * 给定一个单链表，返回链表的中间节点。
     * 如果有两个中间节点，则返回第二个中间节点。
     *
     * <p>示例:
     * <pre>
     * 输入: 1->2->3->4->5
     * 输出: 3->4->5 (从节点3开始)
     * </pre>
     *
     * <p>解题思路:
     * 快慢指针:
     * <ol>
     *   <li>slow每次走1步</li>
     *   <li>fast每次走2步</li>
     *   <li>fast到达末尾时，slow在中间</li>
     * </ol>
     *
     * @param head 链表头节点
     * @return 中间节点
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public static <T> Node<T> findMiddle(Node<T> head) {
        Node<T> slow = head;
        Node<T> fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow;
    }

    /**
     * 【题目6】回文链表 - LeetCode #234
     * 难度: 简单 ⭐⭐
     *
     * <p>题目描述:
     * 判断一个链表是否为回文链表。
     *
     * <p>示例:
     * <pre>
     * 输入: 1->2->2->1
     * 输出: true
     * </pre>
     *
     * <p>解题思路:
     * <ol>
     *   <li>快慢指针找到中间节点</li>
     *   <li>反转后半部分链表</li>
     *   <li>比较前半部分和反转后的后半部分</li>
     * </ol>
     *
     * @param head 链表头节点
     * @return true if the list is palindrome
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public static <T> boolean isPalindrome(Node<T> head) {
        if (head == null || head.next == null) {
            return true;
        }

        // 1. 找到中间节点
        Node<T> slow = head;
        Node<T> fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // 2. 反转后半部分
        Node<T> secondHalf = reverseList(slow.next);

        // 3. 比较两部分
        Node<T> p1 = head;
        Node<T> p2 = secondHalf;
        boolean result = true;

        while (p2 != null) {
            if (!p1.data.equals(p2.data)) {
                result = false;
                break;
            }
            p1 = p1.next;
            p2 = p2.next;
        }

        // 4. 恢复链表（可选）
        slow.next = reverseList(secondHalf);

        return result;
    }

    /**
     * 【题目7】相交链表 - LeetCode #160
     * 难度: 简单 ⭐⭐
     *
     * <p>题目描述:
     * 找到两个单链表相交的起始节点。
     *
     * <p>解题思路:
     * 双指针法:
     * <ol>
     *   <li>两个指针分别从两个链表头开始</li>
     *   <li>到达末尾后跳到另一个链表的头</li>
     *   <li>相遇点就是交点（或同时为null）</li>
     * </ol>
     *
     * @param headA 链表A的头节点
     * @param headB 链表B的头节点
     * @return 相交节点，如果不相交返回null
     * 时间复杂度: O(m + n)
     * 空间复杂度: O(1)
     */
    public static <T> Node<T> getIntersectionNode(Node<T> headA, Node<T> headB) {
        if (headA == null || headB == null) {
            return null;
        }

        Node<T> pA = headA;
        Node<T> pB = headB;

        // 两个指针会在交点相遇，或同时到达null
        while (pA != pB) {
            pA = (pA == null) ? headB : pA.next;
            pB = (pB == null) ? headA : pB.next;
        }

        return pA;
    }

    // ==================== 测试驱动 ====================

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   LinkedList 面试题解析与测试                ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();

        testReverseList();
        testCycleDetection();
        testMergeLists();
        testRemoveNthFromEnd();
        testFindMiddle();
        testIsPalindrome();
    }

    private static void testReverseList() {
        System.out.println("【测试1: 反转链表】");
        Node<Integer> head = createList(1, 2, 3, 4, 5);
        System.out.println("原链表: " + printList(head));

        head = reverseList(head);
        System.out.println("迭代反转: " + printList(head));

        head = reverseListRecursive(head);
        System.out.println("递归反转: " + printList(head));

        System.out.println("✓ 测试通过\n");
    }

    private static void testCycleDetection() {
        System.out.println("【测试2: 环检测】");

        // 无环链表
        Node<Integer> head1 = createList(1, 2, 3, 4, 5);
        System.out.println("无环链表: " + printList(head1));
        System.out.println("是否有环: " + hasCycle(head1));

        // 有环链表
        Node<Integer> head2 = createList(1, 2, 3, 4, 5);
        Node<Integer> last = head2;
        while (last.next != null) last = last.next;
        last.next = head2.next;  // 尾部指向第二个节点，形成环
        System.out.println("有环链表（无法打印）");
        System.out.println("是否有环: " + hasCycle(head2));

        System.out.println("✓ 测试通过\n");
    }

    private static void testMergeLists() {
        System.out.println("【测试3: 合并有序链表】");
        Node<Integer> l1 = createList(1, 2, 4);
        Node<Integer> l2 = createList(1, 3, 4);

        System.out.println("链表1: " + printList(l1));
        System.out.println("链表2: " + printList(l2));

        Node<Integer> merged = mergeTwoLists(l1, l2);
        System.out.println("合并后: " + printList(merged));

        System.out.println("✓ 测试通过\n");
    }

    private static void testRemoveNthFromEnd() {
        System.out.println("【测试4: 删除倒数第N个节点】");
        Node<Integer> head = createList(1, 2, 3, 4, 5);
        System.out.println("原链表: " + printList(head));

        head = removeNthFromEnd(head, 2);
        System.out.println("删除倒数第2个: " + printList(head));

        System.out.println("✓ 测试通过\n");
    }

    private static void testFindMiddle() {
        System.out.println("【测试5: 找中间节点】");
        Node<Integer> head1 = createList(1, 2, 3, 4, 5);
        System.out.println("奇数链表: " + printList(head1));
        System.out.println("中间节点: " + findMiddle(head1).data);

        Node<Integer> head2 = createList(1, 2, 3, 4, 5, 6);
        System.out.println("偶数链表: " + printList(head2));
        System.out.println("中间节点: " + findMiddle(head2).data);

        System.out.println("✓ 测试通过\n");
    }

    private static void testIsPalindrome() {
        System.out.println("【测试6: 回文链表】");

        Node<Integer> head1 = createList(1, 2, 2, 1);
        System.out.println("链表: " + printList(head1));
        System.out.println("是否回文: " + isPalindrome(head1));

        Node<Integer> head2 = createList(1, 2, 3, 2, 1);
        System.out.println("链表: " + printList(head2));
        System.out.println("是否回文: " + isPalindrome(head2));

        Node<Integer> head3 = createList(1, 2, 3, 4, 5);
        System.out.println("链表: " + printList(head3));
        System.out.println("是否回文: " + isPalindrome(head3));

        System.out.println("✓ 测试通过\n");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建链表
     * Creates a linked list from the given values
     */
    @SafeVarargs
    private static <T> Node<T> createList(T... values) {
        if (values.length == 0) return null;

        Node<T> head = new Node<>(values[0]);
        Node<T> current = head;

        for (int i = 1; i < values.length; i++) {
            current.next = new Node<>(values[i]);
            current = current.next;
        }

        return head;
    }

    /**
     * 打印链表
     * Prints the linked list
     */
    private static <T> String printList(Node<T> head) {
        if (head == null) return "[]";

        StringBuilder sb = new StringBuilder();
        Node<T> current = head;

        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next;
        }

        return sb.toString();
    }
}
