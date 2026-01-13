package com.architecture.datastructure.linear.array;

/**
 * 动态数组实现 - Dynamic Array Implementation
 *
 * <p>原理说明:
 * 动态数组（也称为可变数组、ArrayList）是一种可以自动调整大小的数组数据结构。
 * 它在底层使用固定大小的数组，当容量不足时会自动扩容，通常扩容为原来的2倍。
 *
 * <p>核心特性:
 * <ul>
 *   <li>支持随机访问 - 通过索引直接访问元素，时间复杂度O(1)</li>
 *   <li>自动扩容 - 当数组满时，创建新的更大数组并复制元素</li>
 *   <li>动态大小 - 无需预先知道确切大小</li>
 *   <li>连续内存 - 元素在内存中连续存储，缓存友好</li>
 * </ul>
 *
 * <p>时间复杂度:
 * <ul>
 *   <li>访问 get(index): O(1) - 直接通过索引访问</li>
 *   <li>尾部添加 add(element): O(1) 均摊 - 大部分情况O(1)，扩容时O(n)</li>
 *   <li>指定位置插入 add(index, element): O(n) - 需要移动后续元素</li>
 *   <li>删除 remove(index): O(n) - 需要移动后续元素</li>
 *   <li>查找 indexOf(element): O(n) - 需要遍历</li>
 * </ul>
 *
 * <p>空间复杂度: O(n)
 * - 实际分配空间可能大于存储元素数量（因为预留了扩容空间）
 * - 负载因子 = size / capacity，通常在0.5-1.0之间
 *
 * <p>扩容策略:
 * <pre>
 * 初始容量: 10
 * 扩容因子: 2（每次扩容为原来的2倍）
 *
 * 容量变化过程:
 * 10 -> 20 -> 40 -> 80 -> 160 -> ...
 *
 * 扩容成本分析:
 * - 单次扩容: O(n) - 需要复制所有元素
 * - 均摊成本: O(1) - 虽然单次扩容O(n)，但扩容频率随n增长而降低
 * </pre>
 *
 * <p>优点:
 * <ul>
 *   <li>随机访问快速 - O(1)时间复杂度</li>
 *   <li>尾部添加高效 - 均摊O(1)</li>
 *   <li>内存连续 - CPU缓存友好，遍历速度快</li>
 *   <li>实现简单 - 相比链表更容易实现和使用</li>
 * </ul>
 *
 * <p>缺点:
 * <ul>
 *   <li>插入/删除慢 - 中间位置操作需要移动大量元素</li>
 *   <li>扩容开销 - 扩容时需要复制所有元素</li>
 *   <li>空间浪费 - 为了减少扩容次数，通常会预留额外空间</li>
 * </ul>
 *
 * <p>与固定数组对比:
 * <table border="1">
 *   <tr><th>特性</th><th>固定数组</th><th>动态数组</th></tr>
 *   <tr><td>大小</td><td>固定不变</td><td>自动增长</td></tr>
 *   <tr><td>初始化</td><td>必须指定大小</td><td>可选，有默认值</td></tr>
 *   <tr><td>添加元素</td><td>可能数组越界</td><td>自动扩容</td></tr>
 *   <tr><td>内存效率</td><td>高（无浪费）</td><td>中（有预留空间）</td></tr>
 * </table>
 *
 * @param <T> 存储的元素类型
 * @author Architecture Team
 * @version 1.0
 * @since 2026-01-13
 */
public class DynamicArrayImplementation<T> {
    /**
     * 默认初始容量
     * Java ArrayList默认也是10
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 存储元素的数组
     * 使用Object[]以支持泛型
     */
    private Object[] elements;

    /**
     * 当前存储的元素数量
     */
    private int size;

    /**
     * 构造一个初始容量为10的空动态数组
     *
     * 时间复杂度: O(1)
     * 空间复杂度: O(1) - 仅分配初始数组
     */
    public DynamicArrayImplementation() {
        elements = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    /**
     * 构造一个指定初始容量的空动态数组
     *
     * @param initialCapacity 初始容量
     * @throws IllegalArgumentException 如果初始容量为负数
     * 时间复杂度: O(1)
     * 空间复杂度: O(capacity)
     */
    public DynamicArrayImplementation(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        elements = new Object[initialCapacity];
        size = 0;
    }

    /**
     * 在数组末尾添加元素
     *
     * <p>算法步骤:
     * <ol>
     *   <li>检查容量，如果已满则扩容</li>
     *   <li>将元素放入size位置</li>
     *   <li>size自增</li>
     * </ol>
     *
     * <p>扩容过程演示:
     * <pre>
     * 假设当前capacity=4, size=4, 添加新元素5:
     *
     * 步骤1: 检测到数组已满
     * [1, 2, 3, 4]  size=4, capacity=4
     *
     * 步骤2: 创建新数组，容量翻倍
     * [_, _, _, _, _, _, _, _]  capacity=8
     *
     * 步骤3: 复制旧数组元素
     * [1, 2, 3, 4, _, _, _, _]
     *
     * 步骤4: 添加新元素
     * [1, 2, 3, 4, 5, _, _, _]  size=5, capacity=8
     * </pre>
     *
     * @param element 要添加的元素
     * 时间复杂度: O(1) 均摊 - 大部分情况O(1)，扩容时O(n)
     * 空间复杂度: O(1) - 不扩容时不需要额外空间
     */
    public void add(T element) {
        ensureCapacity();
        elements[size++] = element;
    }

    /**
     * 在指定位置插入元素
     *
     * <p>算法步骤:
     * <ol>
     *   <li>检查索引是否合法</li>
     *   <li>检查容量，如果已满则扩容</li>
     *   <li>将index及之后的元素向后移动一位</li>
     *   <li>在index位置插入新元素</li>
     *   <li>size自增</li>
     * </ol>
     *
     * <p>插入过程演示:
     * <pre>
     * 在索引2的位置插入元素X:
     *
     * 原数组: [A, B, C, D, E, _, _]
     *          0  1  2  3  4  5  6
     *
     * 步骤1: 将索引2及之后的元素向后移动
     * [A, B, _, C, D, E, _]
     *
     * 步骤2: 在索引2插入X
     * [A, B, X, C, D, E, _]
     *
     * 使用System.arraycopy实现高效复制:
     * arraycopy(elements, 2, elements, 3, 3)
     * 将索引2-4的3个元素复制到索引3-5
     * </pre>
     *
     * @param index 插入位置
     * @param element 要插入的元素
     * @throws IndexOutOfBoundsException 如果索引越界
     * 时间复杂度: O(n) - 需要移动index之后的所有元素
     * 空间复杂度: O(1)
     */
    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity();
        // 将index及之后的元素向后移动一位
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
    }

    /**
     * 获取指定位置的元素
     *
     * <p>直接通过索引访问数组元素，这是数组的核心优势
     *
     * @param index 元素索引
     * @return 该位置的元素
     * @throws IndexOutOfBoundsException 如果索引越界
     * 时间复杂度: O(1) - 随机访问
     * 空间复杂度: O(1)
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (T) elements[index];
    }

    /**
     * 修改指定位置的元素
     *
     * @param index 元素索引
     * @param element 新元素
     * @return 原来的元素
     * @throws IndexOutOfBoundsException 如果索引越界
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    @SuppressWarnings("unchecked")
    public T set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T oldValue = (T) elements[index];
        elements[index] = element;
        return oldValue;
    }

    /**
     * 删除指定位置的元素
     *
     * <p>删除过程演示:
     * <pre>
     * 删除索引2的元素:
     *
     * 原数组: [A, B, C, D, E]
     *          0  1  2  3  4
     *
     * 步骤1: 保存要删除的元素C
     *
     * 步骤2: 将索引3及之后的元素向前移动
     * [A, B, D, E, E]
     *
     * 步骤3: 将最后一个位置设为null（防止内存泄漏）
     * [A, B, D, E, null]
     *
     * 步骤4: size减1
     * </pre>
     *
     * @param index 要删除的位置
     * @return 被删除的元素
     * @throws IndexOutOfBoundsException 如果索引越界
     * 时间复杂度: O(n) - 需要移动后续元素
     * 空间复杂度: O(1)
     */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T element = (T) elements[index];
        // 将index+1及之后的元素向前移动一位
        System.arraycopy(elements, index + 1, elements, index, size - index - 1);
        // 将最后一个元素设为null，帮助GC
        elements[--size] = null;
        return element;
    }

    /**
     * 删除第一个匹配的元素
     *
     * @param element 要删除的元素
     * @return 是否成功删除
     * 时间复杂度: O(n) - 需要查找和移动
     * 空间复杂度: O(1)
     */
    public boolean removeElement(T element) {
        int index = indexOf(element);
        if (index >= 0) {
            remove(index);
            return true;
        }
        return false;
    }

    /**
     * 查找元素第一次出现的索引
     *
     * @param element 要查找的元素
     * @return 元素索引，如果不存在返回-1
     * 时间复杂度: O(n) - 需要遍历
     * 空间复杂度: O(1)
     */
    public int indexOf(T element) {
        if (element == null) {
            for (int i = 0; i < size; i++) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (element.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 检查是否包含指定元素
     *
     * @param element 要检查的元素
     * @return 是否包含
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public boolean contains(T element) {
        return indexOf(element) >= 0;
    }

    /**
     * 获取数组中元素的数量
     *
     * @return 元素数量
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    public int size() {
        return size;
    }

    /**
     * 获取数组的当前容量
     *
     * @return 容量
     */
    public int capacity() {
        return elements.length;
    }

    /**
     * 检查数组是否为空
     *
     * @return 是否为空
     * 时间复杂度: O(1)
     * 空间复杂度: O(1)
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 清空数组
     *
     * <p>将所有元素设为null，帮助垃圾回收
     *
     * 时间复杂度: O(n) - 需要遍历清空
     * 空间复杂度: O(1)
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    /**
     * 确保容量足够，如果不够则扩容
     *
     * <p>扩容策略:
     * <ul>
     *   <li>当数组满时，创建一个容量为原来2倍的新数组</li>
     *   <li>将旧数组的所有元素复制到新数组</li>
     *   <li>用新数组替换旧数组</li>
     * </ul>
     *
     * <p>为什么扩容因子是2:
     * <ul>
     *   <li>太小（如1.5）: 频繁扩容，影响性能</li>
     *   <li>太大（如3）: 浪费空间</li>
     *   <li>2是一个较好的平衡点</li>
     * </ul>
     *
     * <p>均摊分析:
     * <pre>
     * 假设从空数组开始，连续添加n个元素:
     * - 扩容次数: log₂(n)
     * - 总复制次数: n + n/2 + n/4 + ... + 2 + 1 ≈ 2n
     * - 均摊到每个元素: 2n/n = 2 = O(1)
     * </pre>
     *
     * 时间复杂度: O(n) - 需要复制所有元素
     * 空间复杂度: O(n) - 需要创建新数组
     */
    private void ensureCapacity() {
        if (size == elements.length) {
            // 容量翻倍
            int newCapacity = elements.length * 2;
            Object[] newArray = new Object[newCapacity];
            // 使用System.arraycopy进行高效复制
            System.arraycopy(elements, 0, newArray, 0, size);
            elements = newArray;
        }
    }

    /**
     * 缩小容量以匹配当前大小
     *
     * <p>当删除大量元素后，可以调用此方法释放多余空间
     *
     * 时间复杂度: O(n)
     * 空间复杂度: O(n)
     */
    public void trimToSize() {
        if (size < elements.length) {
            Object[] newArray = new Object[size];
            System.arraycopy(elements, 0, newArray, 0, size);
            elements = newArray;
        }
    }

    /**
     * 转换为字符串表示
     *
     * @return 数组的字符串表示
     */
    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(elements[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
