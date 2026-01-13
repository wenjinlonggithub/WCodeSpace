# LinkedList - 链表详解

## 概述

链表（Linked List）是一种线性数据结构，由一系列节点（Node）组成。每个节点包含两部分：
1. **数据域（Data）**：存储实际数据
2. **指针域（Pointer/Reference）**：指向下一个节点的引用

链表在内存中**不需要连续存储**，可以动态分配空间，这是它与数组最大的区别。

---

## 基本原理

### 1. 节点结构

```
┌─────────┬──────┐
│  Data   │ Next │──> null
└─────────┴──────┘
   单个节点
```

### 2. 链表类型

#### (1) 单向链表 (Singly Linked List)
```
head -> [1|•] -> [2|•] -> [3|•] -> [4|null]
```
- 每个节点只有一个指向下一个节点的指针
- 只能单向遍历（从头到尾）
- **应用**：栈、队列的实现

#### (2) 双向链表 (Doubly Linked List)
```
head <-> [•|1|•] <-> [•|2|•] <-> [•|3|•] <-> null
        prev data next
```
- 每个节点有两个指针：prev（前驱）和next（后继）
- 可以双向遍历
- **应用**：浏览器历史记录、LRU缓存

#### (3) 循环链表 (Circular Linked List)
```
head -> [1|•] -> [2|•] -> [3|•] -> [4|•]
         ↑___________________________|
```
- 尾节点的next指向头节点，形成环
- **应用**：约瑟夫环问题、循环播放列表

---

## 时间复杂度分析

| 操作 | 数组 | 链表 | 说明 |
|------|------|------|------|
| **访问（按索引）** | O(1) | **O(n)** | 链表需要从头遍历 |
| **查找（按值）** | O(n) | O(n) | 都需要遍历 |
| **插入头部** | **O(n)** | **O(1)** | 链表只需改指针 |
| **插入尾部** | O(1) | O(n) | 链表需遍历（可优化至O(1)） |
| **插入中间** | O(n) | O(n) | 都需要移动/遍历 |
| **删除头部** | O(n) | **O(1)** | 链表只需改指针 |
| **删除尾部** | O(1) | O(n) | 链表需遍历（双向链表O(1)） |
| **删除中间** | O(n) | O(n) | 都需要移动/遍历 |

### 复杂度说明

#### 为什么链表访问是O(n)？
链表不支持随机访问，必须从头节点开始遍历到目标位置。

#### 为什么链表插入/删除是O(1)？
**前提**：已知插入/删除位置的节点引用。
- 插入：只需修改指针指向
- 删除：只需修改前驱节点的指针

#### 链表尾部插入的优化
维护一个tail指针指向尾节点，使尾部插入从O(n)优化为O(1)：
```java
private Node<T> tail;

public void addLast(T data) {
    Node<T> newNode = new Node<>(data);
    if (tail != null) {
        tail.next = newNode;
    }
    tail = newNode;
}
```

---

## 空间复杂度

- **存储空间**：O(n) - n个节点
- **额外开销**：
  - 单向链表：每个节点额外存储1个指针（4-8字节）
  - 双向链表：每个节点额外存储2个指针（8-16字节）

**对比数组**：
- 数组不需要额外指针，内存更紧凑
- 但数组可能有预分配空间浪费
- 链表按需分配，无浪费

---

## 优缺点对比

### ✅ 优点

1. **动态大小**
   - 无需预先指定大小，可随时扩展
   - 没有数组的resize开销

2. **插入/删除高效**
   - 在已知位置插入/删除只需O(1)
   - 特别适合频繁插入删除的场景

3. **内存灵活**
   - 不需要连续内存空间
   - 适合内存碎片化的环境

4. **易于实现特殊结构**
   - 栈、队列、双端队列
   - 循环结构

### ❌ 缺点

1. **随机访问慢**
   - 不支持O(1)的索引访问
   - 必须从头遍历

2. **额外空间**
   - 每个节点需要存储指针
   - 小数据时开销占比大

3. **缓存不友好**
   - 节点内存分散，局部性差
   - CPU缓存命中率低

4. **遍历单向**
   - 单向链表只能从头到尾
   - 需要双向遍历时必须用双向链表

---

## 实际应用场景

### 1. LRU缓存 (Least Recently Used)

**实现方式**：双向链表 + 哈希表

```
HashMap: key -> Node
双向链表: [最新] <-> ... <-> [最旧]
```

- **访问**：移到链表头部（O(1)）
- **淘汰**：删除链表尾部（O(1)）
- **查找**：通过HashMap（O(1)）

**Java实现**：`LinkedHashMap` with `accessOrder=true`

### 2. 浏览器历史记录

- **前进/后退**：双向链表支持双向遍历
- **新页面**：当前位置后的记录被清除
- **实现**：双向链表

### 3. 文本编辑器撤销/重做

```
[操作1] <-> [操作2] <-> [操作3] <-> 当前位置
                              ↑
```

- **撤销**：向前移动（undo）
- **重做**：向后移动（redo）
- **新操作**：删除当前位置后的节点

### 4. 音乐播放列表

- **循环播放**：循环链表
- **随机播放**：需要支持快速访问（不适合纯链表）
- **动态添加/删除**：链表方便

### 5. 操作系统进程调度

- **就绪队列**：使用链表管理进程
- **动态调度**：频繁插入/删除进程
- **优先级队列**：可使用多个链表

### 6. 多项式计算

```java
// 表示多项式: 3x² + 5x + 7
Node -> [3, 2] -> [5, 1] -> [7, 0]
      (系数,指数)
```

- 每个节点存储一项的系数和指数
- 易于实现多项式加法、乘法

---

## 常见面试题

### ⭐⭐ 简单题

#### 1. 反转链表 (LeetCode #206)

**解法1：迭代**
```java
public Node reverse(Node head) {
    Node prev = null, curr = head;
    while (curr != null) {
        Node next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}
```
- **时间**：O(n)
- **空间**：O(1)

**解法2：递归**
```java
public Node reverse(Node head) {
    if (head == null || head.next == null) return head;
    Node newHead = reverse(head.next);
    head.next.next = head;
    head.next = null;
    return newHead;
}
```
- **时间**：O(n)
- **空间**：O(n) - 递归栈

#### 2. 检测环 (LeetCode #141)

**Floyd判圈算法（快慢指针）**
```java
public boolean hasCycle(Node head) {
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}
```
- **时间**：O(n)
- **空间**：O(1)

#### 3. 合并两个有序链表 (LeetCode #21)

```java
public Node mergeTwoLists(Node l1, Node l2) {
    Node dummy = new Node(0);
    Node curr = dummy;
    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) {
            curr.next = l1;
            l1 = l1.next;
        } else {
            curr.next = l2;
            l2 = l2.next;
        }
        curr = curr.next;
    }
    curr.next = (l1 != null) ? l1 : l2;
    return dummy.next;
}
```

#### 4. 链表的中间节点 (LeetCode #876)

**快慢指针**
```java
public Node findMiddle(Node head) {
    Node slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow;
}
```

#### 5. 回文链表 (LeetCode #234)

**步骤**：
1. 快慢指针找中点
2. 反转后半部分
3. 比较前后两部分

---

### ⭐⭐⭐ 中等题

#### 6. 删除倒数第N个节点 (LeetCode #19)

**双指针（一次遍历）**
```java
public Node removeNthFromEnd(Node head, int n) {
    Node dummy = new Node(0);
    dummy.next = head;
    Node fast = dummy, slow = dummy;

    // fast先走n+1步
    for (int i = 0; i <= n; i++) {
        fast = fast.next;
    }

    // 同时移动
    while (fast != null) {
        fast = fast.next;
        slow = slow.next;
    }

    slow.next = slow.next.next;
    return dummy.next;
}
```

#### 7. 重排链表 (LeetCode #143)

**题目**：`1->2->3->4` 变为 `1->4->2->3`

**步骤**：
1. 找中点（快慢指针）
2. 反转后半部分
3. 交替合并两个链表

#### 8. 环形链表II - 找环入口 (LeetCode #142)

**Floyd判圈 + 数学推导**

---

### ⭐⭐⭐⭐ 困难题

#### 9. 合并K个有序链表 (LeetCode #23)

**解法**：优先队列（最小堆）
- **时间**：O(N log K) - N是总节点数，K是链表数
- **空间**：O(K) - 堆大小

#### 10. 反转链表II - 反转部分 (LeetCode #92)

**题目**：反转从位置m到n的节点

---

## 与Java标准库对比

### LinkedList vs ArrayList

| 特性 | LinkedList | ArrayList | 推荐场景 |
|------|-----------|-----------|---------|
| **底层实现** | 双向链表 | 动态数组 | - |
| **随机访问** | O(n) | **O(1)** | 频繁访问用ArrayList |
| **头部插入** | **O(1)** | O(n) | 频繁头部操作用LinkedList |
| **尾部插入** | **O(1)** | **O(1)** 均摊 | 都可以 |
| **中间插入** | O(n) | O(n) | 都一样 |
| **内存占用** | 更多（指针） | 更少（连续） | 内存敏感用ArrayList |
| **缓存友好** | 否 | **是** | 性能敏感用ArrayList |
| **Deque接口** | **实现** | 未实现 | 双端队列用LinkedList |

### Java LinkedList实现细节

```java
public class LinkedList<E> implements List<E>, Deque<E> {
    private Node<E> first;  // 头节点
    private Node<E> last;   // 尾节点（优化！）
    private int size;

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;  // 双向链表
    }
}
```

**关键特性**：
1. **双向链表**：支持高效的前后遍历
2. **维护tail指针**：尾部插入O(1)
3. **实现Deque接口**：可作为双端队列使用
4. **非线程安全**：需要外部同步

### 使用建议

#### 使用ArrayList的场景
- ✅ 频繁随机访问元素
- ✅ 主要在尾部添加元素
- ✅ 内存敏感的场景
- ✅ 需要高性能的场景（缓存友好）

#### 使用LinkedList的场景
- ✅ 频繁在头部插入/删除
- ✅ 需要双端队列功能
- ✅ 实现LRU缓存等特殊数据结构
- ✅ 不需要随机访问

**经验法则**：
> 90%的情况下应该使用ArrayList，除非有明确的理由使用LinkedList。

---

## 实现技巧

### 1. 哨兵节点 (Dummy Node)

简化头节点的边界处理：

```java
public Node mergeTwoLists(Node l1, Node l2) {
    Node dummy = new Node(0);  // 哨兵节点
    Node curr = dummy;
    // ... 操作
    return dummy.next;  // 返回真正的头节点
}
```

**优点**：
- 统一处理头节点和其他节点
- 避免大量if判断
- 代码更简洁

### 2. 快慢指针

**应用**：
- 找中间节点
- 检测环
- 找倒数第K个节点

```java
// 慢指针走1步，快指针走2步
Node slow = head, fast = head;
while (fast != null && fast.next != null) {
    slow = slow.next;
    fast = fast.next.next;
}
```

### 3. 递归处理

**适用于**：
- 反转链表
- 合并链表
- 删除节点

**模板**：
```java
public Node recursive(Node head) {
    // 1. 递归终止条件
    if (head == null || head.next == null) {
        return head;
    }

    // 2. 递归处理子问题
    Node result = recursive(head.next);

    // 3. 处理当前节点
    // ...

    return result;
}
```

### 4. 原地反转 vs 新建链表

**原地反转**：O(1)空间
```java
// 只修改指针，不创建新节点
curr.next = prev;
```

**新建链表**：O(n)空间
```java
// 创建新节点
Node newNode = new Node(curr.data);
```

---

## 性能优化技巧

### 1. 维护tail指针

```java
private Node<T> head;
private Node<T> tail;  // 维护尾指针

public void addLast(T data) {
    Node<T> newNode = new Node<>(data);
    if (tail != null) {
        tail.next = newNode;
    } else {
        head = newNode;
    }
    tail = newNode;
    size++;
}
```

**收益**：尾部插入从O(n)优化至O(1)

### 2. 缓存size

```java
private int size;  // 缓存链表大小

public int size() {
    return size;  // O(1) 而不是O(n)遍历
}
```

### 3. 使用双向链表

**场景**：需要频繁删除尾部或反向遍历

```java
class Node<T> {
    T data;
    Node<T> prev;  // 添加前驱指针
    Node<T> next;
}
```

**优势**：
- 删除尾部：O(1)
- 反向遍历：O(n)
- 删除指定节点：O(1)（已知节点引用）

### 4. 循环链表优化

**场景**：约瑟夫环、循环调度

```java
// 尾节点指向头节点
tail.next = head;
```

---

## 调试技巧

### 1. 打印链表

```java
public void print() {
    Node curr = head;
    while (curr != null) {
        System.out.print(curr.data);
        if (curr.next != null) System.out.print(" -> ");
        curr = curr.next;
    }
    System.out.println();
}
```

### 2. 检测环

```java
// 防止无限循环打印
Set<Node> visited = new HashSet<>();
while (curr != null && !visited.contains(curr)) {
    visited.add(curr);
    System.out.print(curr.data + " -> ");
    curr = curr.next;
}
if (curr != null) {
    System.out.println("(环检测到)");
}
```

### 3. 可视化

```
head -> [1] -> [2] -> [3] -> null
         ↑            ↓
       prev        current
```

---

## 参考资料

### 书籍
- 《算法导论》第10章 - 基本数据结构
- 《数据结构与算法分析》- 链表章节
- 《编程珠玑》- 链表技巧

### 在线资源
- **LeetCode链表专题**：https://leetcode.com/tag/linked-list/
- **VisuAlgo链表可视化**：https://visualgo.net/en/list
- **Java LinkedList源码**：`java.util.LinkedList`

### 相关项目文件
- 实现：`LinkedListImplementation.java`
- 演示：`LinkedListDemo.java`
- 面试题：`LinkedListInterviewQuestions.java`
- 测试：`LinkedListTest.java`

---

## 总结

链表是一种**基础但重要**的数据结构，它的核心优势在于：
1. ✅ 动态大小，按需分配
2. ✅ 插入/删除高效（O(1)）
3. ✅ 内存不需要连续

但也有明显缺陷：
1. ❌ 随机访问慢（O(n)）
2. ❌ 额外指针开销
3. ❌ 缓存不友好

**使用原则**：
- 需要频繁插入/删除且很少随机访问 → **使用链表**
- 需要频繁随机访问 → **使用数组**
- 大多数场景 → **优先考虑ArrayList**

**面试重点**：
- 反转链表（迭代 + 递归）
- 快慢指针技巧
- 环检测
- 合并链表
- 哨兵节点技巧

掌握链表不仅是为了应对面试，更重要的是理解**指针/引用**的本质，这对学习更复杂的数据结构（树、图）至关重要。

---

*文档版本：1.0*
*最后更新：2026-01-13*
*作者：Architecture Team*
