package com.architecture.datastructure.linear.linkedlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LinkedList 单元测试
 * LinkedList Unit Tests
 *
 * 测试覆盖:
 * - 所有公共方法的正常功能
 * - 边界情况（空链表、单元素、多元素）
 * - 异常情况（索引越界、空链表操作）
 * - 特殊操作（反转、查找等）
 *
 * @author Architecture Team
 * @version 1.0
 * @since 2026-01-13
 */
@DisplayName("LinkedList Implementation Tests")
public class LinkedListTest {

    private LinkedListImplementation<Integer> list;

    @BeforeEach
    @DisplayName("初始化测试环境")
    public void setUp() {
        list = new LinkedListImplementation<>();
    }

    // ==================== 构造和基本属性测试 ====================

    @Test
    @DisplayName("测试空链表初始化")
    public void testEmptyListInitialization() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    // ==================== 插入操作测试 ====================

    @Test
    @DisplayName("测试在头部添加元素")
    public void testAddFirst() {
        list.addFirst(3);
        list.addFirst(2);
        list.addFirst(1);

        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    @DisplayName("测试在尾部添加元素")
    public void testAddLast() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    @DisplayName("测试在指定位置插入元素")
    public void testAddAtIndex() {
        list.addLast(1);
        list.addLast(3);
        list.add(1, 2);  // 在索引1插入2

        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    @DisplayName("测试在索引0插入元素")
    public void testAddAtIndexZero() {
        list.addLast(2);
        list.addLast(3);
        list.add(0, 1);  // 在索引0插入

        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
    }

    @Test
    @DisplayName("测试在最后位置插入元素")
    public void testAddAtIndexEnd() {
        list.addLast(1);
        list.addLast(2);
        list.add(2, 3);  // 在末尾插入

        assertEquals(3, list.size());
        assertEquals(3, list.get(2));
    }

    @Test
    @DisplayName("测试插入时索引越界")
    public void testAddAtInvalidIndex() {
        list.addLast(1);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.add(5, 10);  // 索引越界
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.add(-1, 10);  // 负索引
        });
    }

    // ==================== 删除操作测试 ====================

    @Test
    @DisplayName("测试删除头节点")
    public void testRemoveFirst() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        int removed = list.removeFirst();

        assertEquals(1, removed);
        assertEquals(2, list.size());
        assertEquals(2, list.get(0));
    }

    @Test
    @DisplayName("测试删除指定位置元素")
    public void testRemoveAtIndex() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        int removed = list.remove(1);

        assertEquals(2, removed);
        assertEquals(2, list.size());
        assertEquals(1, list.get(0));
        assertEquals(3, list.get(1));
    }

    @Test
    @DisplayName("测试删除最后一个元素")
    public void testRemoveLastElement() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        int removed = list.remove(2);

        assertEquals(3, removed);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("测试删除元素by值")
    public void testRemoveElement() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        boolean removed = list.removeElement(2);

        assertTrue(removed);
        assertEquals(2, list.size());
        assertFalse(list.contains(2));
    }

    @Test
    @DisplayName("测试删除不存在的元素")
    public void testRemoveNonExistentElement() {
        list.addLast(1);
        list.addLast(2);

        boolean removed = list.removeElement(99);

        assertFalse(removed);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("测试空链表删除异常")
    public void testRemoveFromEmptyList() {
        assertThrows(IllegalStateException.class, () -> {
            list.removeFirst();
        });
    }

    @Test
    @DisplayName("测试删除时索引越界")
    public void testRemoveAtInvalidIndex() {
        list.addLast(1);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.remove(5);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.remove(-1);
        });
    }

    // ==================== 访问操作测试 ====================

    @Test
    @DisplayName("测试获取元素")
    public void testGet() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    @DisplayName("测试获取头元素")
    public void testGetFirst() {
        list.addLast(1);
        list.addLast(2);

        assertEquals(1, list.getFirst());
    }

    @Test
    @DisplayName("测试获取尾元素")
    public void testGetLast() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        assertEquals(3, list.getLast());
    }

    @Test
    @DisplayName("测试空链表获取元素异常")
    public void testGetFromEmptyList() {
        assertThrows(IllegalStateException.class, () -> {
            list.getFirst();
        });

        assertThrows(IllegalStateException.class, () -> {
            list.getLast();
        });
    }

    @Test
    @DisplayName("测试获取元素时索引越界")
    public void testGetAtInvalidIndex() {
        list.addLast(1);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.get(5);
        });
    }

    // ==================== 修改操作测试 ====================

    @Test
    @DisplayName("测试修改元素")
    public void testSet() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        int oldValue = list.set(1, 20);

        assertEquals(2, oldValue);
        assertEquals(20, list.get(1));
        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("测试修改元素时索引越界")
    public void testSetAtInvalidIndex() {
        list.addLast(1);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            list.set(5, 10);
        });
    }

    // ==================== 查找操作测试 ====================

    @Test
    @DisplayName("测试查找元素索引")
    public void testIndexOf() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        assertEquals(0, list.indexOf(1));
        assertEquals(1, list.indexOf(2));
        assertEquals(2, list.indexOf(3));
        assertEquals(-1, list.indexOf(99));
    }

    @Test
    @DisplayName("测试包含元素")
    public void testContains() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        assertTrue(list.contains(1));
        assertTrue(list.contains(2));
        assertTrue(list.contains(3));
        assertFalse(list.contains(99));
    }

    @Test
    @DisplayName("测试空链表查找")
    public void testSearchInEmptyList() {
        assertEquals(-1, list.indexOf(1));
        assertFalse(list.contains(1));
    }

    // ==================== 特殊操作测试 ====================

    @Test
    @DisplayName("测试反转链表")
    public void testReverse() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.addLast(4);
        list.addLast(5);

        list.reverse();

        assertEquals(5, list.get(0));
        assertEquals(4, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(2, list.get(3));
        assertEquals(1, list.get(4));
    }

    @Test
    @DisplayName("测试反转单元素链表")
    public void testReverseSingleElement() {
        list.addLast(1);

        list.reverse();

        assertEquals(1, list.size());
        assertEquals(1, list.get(0));
    }

    @Test
    @DisplayName("测试反转空链表")
    public void testReverseEmptyList() {
        list.reverse();  // 不应该抛出异常

        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("测试清空链表")
    public void testClear() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        list.clear();

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    // ==================== 复杂场景测试 ====================

    @Test
    @DisplayName("测试混合操作")
    public void testMixedOperations() {
        // 添加元素
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        assertEquals(3, list.size());

        // 插入元素
        list.addFirst(0);
        assertEquals(4, list.size());
        assertEquals(0, list.get(0));

        // 删除元素
        list.removeFirst();
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));

        // 修改元素
        list.set(1, 20);
        assertEquals(20, list.get(1));

        // 反转
        list.reverse();
        assertEquals(3, list.get(0));
        assertEquals(20, list.get(1));
        assertEquals(1, list.get(2));
    }

    @Test
    @DisplayName("测试大量数据")
    public void testLargeDataSet() {
        int size = 1000;

        // 添加大量元素
        for (int i = 0; i < size; i++) {
            list.addLast(i);
        }

        assertEquals(size, list.size());

        // 验证元素
        for (int i = 0; i < size; i++) {
            assertEquals(i, list.get(i));
        }

        // 反转
        list.reverse();

        // 验证反转后的元素
        for (int i = 0; i < size; i++) {
            assertEquals(size - 1 - i, list.get(i));
        }
    }

    @Test
    @DisplayName("测试toString方法")
    public void testToString() {
        assertEquals("[]", list.toString());

        list.addLast(1);
        assertEquals("[1]", list.toString());

        list.addLast(2);
        list.addLast(3);
        assertEquals("[1 -> 2 -> 3]", list.toString());
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试单元素链表的所有操作")
    public void testSingleElementOperations() {
        list.addLast(1);

        assertEquals(1, list.size());
        assertEquals(1, list.get(0));
        assertEquals(1, list.getFirst());
        assertEquals(1, list.getLast());
        assertTrue(list.contains(1));

        int removed = list.removeFirst();
        assertEquals(1, removed);
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("测试两元素链表的反转")
    public void testTwoElementReverse() {
        list.addLast(1);
        list.addLast(2);

        list.reverse();

        assertEquals(2, list.get(0));
        assertEquals(1, list.get(1));
    }
}
