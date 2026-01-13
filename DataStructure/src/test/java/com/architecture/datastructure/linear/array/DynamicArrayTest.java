package com.architecture.datastructure.linear.array;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DynamicArray 单元测试
 * DynamicArray Unit Tests
 *
 * 测试覆盖:
 * - 所有公共方法的正常功能
 * - 边界情况（空数组、单元素、扩容）
 * - 异常情况（索引越界）
 *
 * @author Architecture Team
 * @version 1.0
 * @since 2026-01-13
 */
@DisplayName("DynamicArray Implementation Tests")
public class DynamicArrayTest {

    private DynamicArrayImplementation<Integer> array;

    @BeforeEach
    @DisplayName("初始化测试环境")
    public void setUp() {
        array = new DynamicArrayImplementation<>();
    }

    // ==================== 构造和基本属性测试 ====================

    @Test
    @DisplayName("测试默认构造函数")
    public void testDefaultConstructor() {
        assertTrue(array.isEmpty());
        assertEquals(0, array.size());
        assertEquals(10, array.capacity());
    }

    @Test
    @DisplayName("测试指定容量构造函数")
    public void testConstructorWithCapacity() {
        DynamicArrayImplementation<String> customArray = new DynamicArrayImplementation<>(20);
        assertEquals(0, customArray.size());
        assertEquals(20, customArray.capacity());
    }

    @Test
    @DisplayName("测试负数容量抛出异常")
    public void testNegativeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DynamicArrayImplementation<>(-1);
        });
    }

    // ==================== 添加操作测试 ====================

    @Test
    @DisplayName("测试尾部添加元素")
    public void testAdd() {
        array.add(1);
        array.add(2);
        array.add(3);

        assertEquals(3, array.size());
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
    }

    @Test
    @DisplayName("测试在指定位置插入")
    public void testAddAtIndex() {
        array.add(1);
        array.add(3);
        array.add(1, 2);  // 在索引1插入2

        assertEquals(3, array.size());
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
    }

    @Test
    @DisplayName("测试在开头插入")
    public void testAddAtBeginning() {
        array.add(2);
        array.add(3);
        array.add(0, 1);

        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
    }

    @Test
    @DisplayName("测试在末尾插入")
    public void testAddAtEnd() {
        array.add(1);
        array.add(2);
        array.add(2, 3);

        assertEquals(3, array.size());
        assertEquals(3, array.get(2));
    }

    @Test
    @DisplayName("测试插入时索引越界")
    public void testAddAtInvalidIndex() {
        array.add(1);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            array.add(5, 10);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            array.add(-1, 10);
        });
    }

    // ==================== 访问操作测试 ====================

    @Test
    @DisplayName("测试获取元素")
    public void testGet() {
        array.add(10);
        array.add(20);
        array.add(30);

        assertEquals(10, array.get(0));
        assertEquals(20, array.get(1));
        assertEquals(30, array.get(2));
    }

    @Test
    @DisplayName("测试获取元素时索引越界")
    public void testGetAtInvalidIndex() {
        array.add(1);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            array.get(5);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            array.get(-1);
        });
    }

    @Test
    @DisplayName("测试修改元素")
    public void testSet() {
        array.add(1);
        array.add(2);
        array.add(3);

        int oldValue = array.set(1, 20);

        assertEquals(2, oldValue);
        assertEquals(20, array.get(1));
        assertEquals(3, array.size());
    }

    @Test
    @DisplayName("测试修改元素时索引越界")
    public void testSetAtInvalidIndex() {
        array.add(1);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            array.set(5, 10);
        });
    }

    // ==================== 删除操作测试 ====================

    @Test
    @DisplayName("测试删除元素")
    public void testRemove() {
        array.add(1);
        array.add(2);
        array.add(3);

        int removed = array.remove(1);

        assertEquals(2, removed);
        assertEquals(2, array.size());
        assertEquals(1, array.get(0));
        assertEquals(3, array.get(1));
    }

    @Test
    @DisplayName("测试删除第一个元素")
    public void testRemoveFirst() {
        array.add(1);
        array.add(2);
        array.add(3);

        array.remove(0);

        assertEquals(2, array.size());
        assertEquals(2, array.get(0));
    }

    @Test
    @DisplayName("测试删除最后一个元素")
    public void testRemoveLast() {
        array.add(1);
        array.add(2);
        array.add(3);

        array.remove(2);

        assertEquals(2, array.size());
        assertEquals(2, array.get(1));
    }

    @Test
    @DisplayName("测试删除元素by值")
    public void testRemoveElement() {
        array.add(10);
        array.add(20);
        array.add(30);

        boolean removed = array.removeElement(20);

        assertTrue(removed);
        assertEquals(2, array.size());
        assertFalse(array.contains(20));
    }

    @Test
    @DisplayName("测试删除不存在的元素")
    public void testRemoveNonExistentElement() {
        array.add(1);
        array.add(2);

        boolean removed = array.removeElement(99);

        assertFalse(removed);
        assertEquals(2, array.size());
    }

    @Test
    @DisplayName("测试删除时索引越界")
    public void testRemoveAtInvalidIndex() {
        array.add(1);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            array.remove(5);
        });
    }

    // ==================== 查找操作测试 ====================

    @Test
    @DisplayName("测试查找元素索引")
    public void testIndexOf() {
        array.add(10);
        array.add(20);
        array.add(30);

        assertEquals(0, array.indexOf(10));
        assertEquals(1, array.indexOf(20));
        assertEquals(2, array.indexOf(30));
        assertEquals(-1, array.indexOf(99));
    }

    @Test
    @DisplayName("测试包含元素")
    public void testContains() {
        array.add(10);
        array.add(20);

        assertTrue(array.contains(10));
        assertTrue(array.contains(20));
        assertFalse(array.contains(99));
    }

    // ==================== 容量和大小测试 ====================

    @Test
    @DisplayName("测试size方法")
    public void testSize() {
        assertEquals(0, array.size());

        array.add(1);
        assertEquals(1, array.size());

        array.add(2);
        assertEquals(2, array.size());

        array.remove(0);
        assertEquals(1, array.size());
    }

    @Test
    @DisplayName("测试isEmpty方法")
    public void testIsEmpty() {
        assertTrue(array.isEmpty());

        array.add(1);
        assertFalse(array.isEmpty());

        array.remove(0);
        assertTrue(array.isEmpty());
    }

    @Test
    @DisplayName("测试capacity方法")
    public void testCapacity() {
        assertEquals(10, array.capacity());

        // 添加元素直到扩容
        for (int i = 0; i < 11; i++) {
            array.add(i);
        }

        assertEquals(20, array.capacity());  // 容量应该翻倍
    }

    // ==================== 扩容测试 ====================

    @Test
    @DisplayName("测试自动扩容")
    public void testAutoResize() {
        // 创建初始容量为4的数组
        DynamicArrayImplementation<Integer> smallArray = new DynamicArrayImplementation<>(4);

        assertEquals(4, smallArray.capacity());

        // 添加5个元素，触发扩容
        for (int i = 0; i < 5; i++) {
            smallArray.add(i);
        }

        assertEquals(8, smallArray.capacity());  // 容量应该翻倍
        assertEquals(5, smallArray.size());

        // 验证所有元素都正确
        for (int i = 0; i < 5; i++) {
            assertEquals(i, smallArray.get(i));
        }
    }

    @Test
    @DisplayName("测试多次扩容")
    public void testMultipleResize() {
        DynamicArrayImplementation<Integer> smallArray = new DynamicArrayImplementation<>(2);

        for (int i = 0; i < 10; i++) {
            smallArray.add(i);
        }

        // 容量变化: 2 -> 4 -> 8 -> 16
        assertEquals(16, smallArray.capacity());
        assertEquals(10, smallArray.size());
    }

    @Test
    @DisplayName("测试trimToSize")
    public void testTrimToSize() {
        // 添加5个元素
        for (int i = 0; i < 5; i++) {
            array.add(i);
        }

        assertEquals(10, array.capacity());
        assertEquals(5, array.size());

        // 缩小容量
        array.trimToSize();

        assertEquals(5, array.capacity());
        assertEquals(5, array.size());

        // 验证数据完整性
        for (int i = 0; i < 5; i++) {
            assertEquals(i, array.get(i));
        }
    }

    // ==================== 其他操作测试 ====================

    @Test
    @DisplayName("测试clear方法")
    public void testClear() {
        array.add(1);
        array.add(2);
        array.add(3);

        array.clear();

        assertTrue(array.isEmpty());
        assertEquals(0, array.size());
    }

    @Test
    @DisplayName("测试toString方法")
    public void testToString() {
        assertEquals("[]", array.toString());

        array.add(1);
        assertEquals("[1]", array.toString());

        array.add(2);
        array.add(3);
        assertEquals("[1, 2, 3]", array.toString());
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("测试空数组操作")
    public void testEmptyArray() {
        assertTrue(array.isEmpty());
        assertEquals(0, array.size());
        assertEquals(-1, array.indexOf(1));
        assertFalse(array.contains(1));
    }

    @Test
    @DisplayName("测试单元素数组")
    public void testSingleElement() {
        array.add(42);

        assertEquals(1, array.size());
        assertEquals(42, array.get(0));
        assertEquals(0, array.indexOf(42));
        assertTrue(array.contains(42));

        int removed = array.remove(0);
        assertEquals(42, removed);
        assertTrue(array.isEmpty());
    }

    @Test
    @DisplayName("测试大量数据")
    public void testLargeDataSet() {
        int size = 1000;

        // 添加大量元素
        for (int i = 0; i < size; i++) {
            array.add(i);
        }

        assertEquals(size, array.size());

        // 验证元素
        for (int i = 0; i < size; i++) {
            assertEquals(i, array.get(i));
        }

        // 验证容量是2的幂次
        int capacity = array.capacity();
        assertTrue((capacity & (capacity - 1)) == 0);  // 2的幂次的特性
    }

    @Test
    @DisplayName("测试null元素")
    public void testNullElement() {
        DynamicArrayImplementation<String> stringArray = new DynamicArrayImplementation<>();

        stringArray.add("A");
        stringArray.add(null);
        stringArray.add("B");

        assertEquals(3, stringArray.size());
        assertNull(stringArray.get(1));
        assertEquals(1, stringArray.indexOf(null));
        assertTrue(stringArray.contains(null));
    }
}
