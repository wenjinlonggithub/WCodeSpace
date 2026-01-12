# LeetCode Solutions - 大厂算法面试题集

本项目包含各种LeetCode经典题目的Java解决方案，特别聚焦于字节跳动、阿里巴巴等大厂高频面试题，旨在帮助学习算法和数据结构。

## 包含的题目类型

### 数组与字符串
- Two Sum (#1) - 使用HashMap优化查找
- Three Sum (#15) - **字节跳动高频** - 排序+双指针
- Longest Substring Without Repeating Characters (#3) - **字节跳动高频** - 滑动窗口

### 链表
- Reverse Linked List (#206) - 迭代和递归两种解法
- Merge K Sorted Lists (#23) - **字节跳动高频** - 优先队列/分治法

### 动态规划
- Climbing Stairs (#70) - 斐波那契数列变形
- Maximum Subarray (#53) - **阿里巴巴高频** - Kadane算法

### 设计题
- LRU Cache (#146) - **字节跳动高频** - LinkedHashMap/双向链表+HashMap

### 图论与搜索
- Number of Islands (#200) - **通用高频** - DFS/BFS/并查集

## 设计特点

1. **时间复杂度优化** - 每个解法都标注了时间复杂度
2. **空间复杂度优化** - 注明了额外空间使用情况
3. **多种解法** - 同一题目提供不同解法对比
4. **详细注释** - 每个方法都有完整的文档说明
5. **测试用例** - 每个类都包含演示和测试代码

## 运行方式

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.architecture.App"
```

或者直接运行：

```bash
java -cp target/classes com.architecture.App
```

## 算法分类

### 按解法分类
- **双指针法** - 用于数组和链表问题
- **动态规划** - 用于最优化问题
- **递归与分治** - 用于树和图问题
- **哈希表** - 用于快速查找问题

### 按数据结构分类
- **数组** - 索引操作和遍历
- **链表** - 指针操作和重构
- **栈与队列** - 特殊顺序处理
- **树与图** - 递归遍历和搜索

## 学习建议

1. 先理解题目要求和约束
2. 分析时间/空间复杂度要求
3. 选择合适的数据结构和算法
4. 编写代码并验证边界情况
5. 优化解法并思考其他可能解法

## 大厂面试题分类

### 字节跳动高频题
1. **LRU Cache (#146)** - 设计题，考察数据结构设计能力
   - 解法1：LinkedHashMap（推荐）
   - 解法2：双向链表 + HashMap

2. **Three Sum (#15)** - 双指针经典题
   - 排序 + 双指针
   - 时间复杂度 O(n²)

3. **Longest Substring Without Repeating Characters (#3)** - 滑动窗口经典题
   - 解法1：HashSet + 滑动窗口
   - 解法2：HashMap优化
   - 解法3：数组优化（ASCII）

4. **Merge K Sorted Lists (#23)** - 链表 + 堆
   - 解法1：优先队列（最小堆）
   - 解法2：分治合并（推荐）
   - 解法3：逐一合并

### 阿里巴巴高频题
1. **Maximum Subarray (#53)** - 动态规划经典题
   - 解法1：Kadane算法（推荐）
   - 解法2：动态规划
   - 解法3：分治法

### 通用高频题
1. **Number of Islands (#200)** - DFS/BFS经典题
   - 解法1：深度优先搜索（DFS）
   - 解法2：广度优先搜索（BFS）
   - 解法3：并查集（Union-Find）

## 更新日志

- **2026-01-12** - 大厂算法面试题更新
  - 新增字节跳动高频题：LRU Cache、Three Sum、Longest Substring、Merge K Sorted Lists
  - 新增阿里巴巴高频题：Maximum Subarray
  - 新增通用高频题：Number of Islands
  - 每道题提供多种解法和详细注释

- **2026-01-12** - 创建项目，添加基础经典题目解决方案
  - Two Sum
  - Reverse Linked List
  - Climbing Stairs