package com.architecture.core;

/**
 * Redis字典（Dict）核心类解析
 *
 * 字典是Redis最重要的数据结构之一：
 * - Redis数据库就是用字典实现的
 * - Hash类型底层也是字典
 *
 * 特点：
 * - 渐进式rehash：避免阻塞
 * - 两个哈希表：用于rehash过程
 */
public class DictAnalysis {

    /**
     * 字典结构（dict.h）
     *
     * typedef struct dict {
     *     dictType *type;          // 类型特定函数
     *     void *privdata;          // 私有数据
     *     dictht ht[2];            // 两个哈希表
     *     long rehashidx;          // rehash索引，-1表示未进行rehash
     *     int16_t pauserehash;     // 暂停rehash的计数
     * } dict;
     *
     * typedef struct dictht {
     *     dictEntry **table;       // 哈希表数组
     *     unsigned long size;      // 哈希表大小
     *     unsigned long sizemask;  // 哈希表大小掩码（size-1）
     *     unsigned long used;      // 已使用节点数量
     * } dictht;
     *
     * typedef struct dictEntry {
     *     void *key;               // 键
     *     union {
     *         void *val;
     *         uint64_t u64;
     *         int64_t s64;
     *         double d;
     *     } v;                     // 值
     *     struct dictEntry *next;  // 链表指针（解决哈希冲突）
     * } dictEntry;
     */

    /**
     * 1. 哈希算法
     *
     * Redis使用MurmurHash2算法：
     * - 速度快
     * - 分布均匀
     * - 适合字符串键
     *
     * 计算索引：
     * hash = dictHashKey(key);
     * index = hash & dict->ht[table].sizemask;
     */
    public void explainHashAlgorithm() {
        System.out.println("=== 哈希算法 ===\n");

        System.out.println("算法：MurmurHash2");
        System.out.println("特点：");
        System.out.println("  - 速度快");
        System.out.println("  - 分布均匀");
        System.out.println("  - 碰撞率低");

        System.out.println("\n索引计算：");
        System.out.println("  hash = MurmurHash2(key)");
        System.out.println("  index = hash & (size - 1)");
        System.out.println("\n使用位运算&代替%，性能更好");
    }

    /**
     * 2. 哈希冲突解决
     *
     * 使用链地址法（Separate Chaining）：
     * - 每个桶是一个链表
     * - 新节点插入到链表头部（O(1)）
     * - 查找需要遍历链表
     */
    public void explainHashCollision() {
        System.out.println("=== 哈希冲突解决 ===\n");

        System.out.println("方法：链地址法");
        System.out.println("实现：");
        System.out.println("  - 每个桶维护一个链表");
        System.out.println("  - 冲突时插入链表头部");
        System.out.println("  - 查找时遍历链表");

        System.out.println("\n时间复杂度：");
        System.out.println("  - 插入：O(1)");
        System.out.println("  - 查找：O(N)，N为链表长度");
        System.out.println("  - 平均：O(1)，当负载因子合理时");
    }

    /**
     * 3. Rehash机制
     *
     * 什么时候触发rehash？
     * - 扩容：负载因子 >= 1（未执行BGSAVE/BGREWRITEAOF）
     *        负载因子 >= 5（正在执行BGSAVE/BGREWRITEAOF）
     * - 收缩：负载因子 < 0.1
     *
     * 负载因子 = used / size
     */
    public void explainRehash() {
        System.out.println("=== Rehash机制 ===\n");

        System.out.println("触发条件：");
        System.out.println("扩容：");
        System.out.println("  - 负载因子 >= 1（无持久化）");
        System.out.println("  - 负载因子 >= 5（执行持久化）");
        System.out.println("  - 为什么持久化时阈值更高？");
        System.out.println("    避免fork时的写时复制开销");

        System.out.println("\n收缩：");
        System.out.println("  - 负载因子 < 0.1");
        System.out.println("  - 节省内存");

        System.out.println("\n大小计算：");
        System.out.println("  - 扩容：第一个 >= used * 2 的 2^n");
        System.out.println("  - 收缩：第一个 >= used 的 2^n");
    }

    /**
     * 4. 渐进式Rehash
     *
     * 为什么需要渐进式？
     * - 如果数据量很大（百万、千万级别）
     * - 一次性rehash会导致长时间阻塞
     * - 影响Redis的响应性能
     *
     * 渐进式rehash步骤：
     * 1. 为ht[1]分配空间，让字典同时持有ht[0]和ht[1]
     * 2. 将rehashidx设置为0，表示rehash开始
     * 3. 在rehash期间，每次对字典的增删改查操作，顺带将ht[0]在rehashidx索引上的所有键值对rehash到ht[1]
     * 4. 随着操作的进行，ht[0]的数据会逐渐迁移到ht[1]
     * 5. 当ht[0]全部迁移完毕，将rehashidx设置为-1，表示rehash完成
     *
     * 期间的操作：
     * - 查找：先在ht[0]查找，找不到再在ht[1]查找
     * - 新增：直接在ht[1]上添加
     * - 删除和更新：在两个表中查找
     */
    public void explainProgressiveRehash() {
        System.out.println("=== 渐进式Rehash ===\n");

        System.out.println("为什么需要？");
        System.out.println("  - 数据量大时，一次性rehash耗时长");
        System.out.println("  - 会阻塞服务，影响性能");
        System.out.println("  - 渐进式避免长时间阻塞");

        System.out.println("\n实现步骤：");
        System.out.println("  1. 为ht[1]分配空间");
        System.out.println("  2. 设置rehashidx = 0");
        System.out.println("  3. 每次操作时，顺带迁移一个桶");
        System.out.println("  4. 逐步将ht[0]数据迁移到ht[1]");
        System.out.println("  5. 完成后，rehashidx = -1");

        System.out.println("\nrehash期间的操作：");
        System.out.println("  - 查找：ht[0] -> ht[1]");
        System.out.println("  - 新增：只在ht[1]添加");
        System.out.println("  - 删除/更新：在两个表中操作");

        System.out.println("\n优势：");
        System.out.println("  - 分摊耗时");
        System.out.println("  - 不阻塞服务");
        System.out.println("  - 对用户透明");
    }

    /**
     * 5. 字典迭代器
     *
     * 两种迭代器：
     * 1. 安全迭代器（safe iterator）：
     *    - 迭代期间可以修改字典
     *    - 会暂停rehash
     *
     * 2. 非安全迭代器（unsafe iterator）：
     *    - 迭代期间不能修改字典
     *    - 不暂停rehash
     *    - 性能更好
     */
    public void explainIterator() {
        System.out.println("=== 字典迭代器 ===\n");

        System.out.println("安全迭代器：");
        System.out.println("  - 可以修改字典");
        System.out.println("  - 暂停rehash（pauserehash++）");
        System.out.println("  - 用于SCAN命令");

        System.out.println("\n非安全迭代器：");
        System.out.println("  - 不能修改字典");
        System.out.println("  - 不暂停rehash");
        System.out.println("  - 性能更好");
        System.out.println("  - 用于只读操作");
    }

    /**
     * 6. 字典的应用
     */
    public void explainApplications() {
        System.out.println("=== 字典的应用 ===\n");

        System.out.println("1. Redis数据库");
        System.out.println("   - 整个数据库就是一个字典");
        System.out.println("   - key -> value映射");

        System.out.println("\n2. Hash类型");
        System.out.println("   - 当元素较多时使用字典");
        System.out.println("   - field -> value映射");

        System.out.println("\n3. Set类型");
        System.out.println("   - 使用字典实现");
        System.out.println("   - value为NULL，只存储key");

        System.out.println("\n4. ZSet类型");
        System.out.println("   - member -> score映射");
        System.out.println("   - 配合跳表使用");

        System.out.println("\n5. 过期字典");
        System.out.println("   - key -> 过期时间映射");
        System.out.println("   - 用于管理键的过期");
    }

    /**
     * 7. 性能分析
     */
    public void explainPerformance() {
        System.out.println("=== 性能分析 ===\n");

        System.out.println("时间复杂度：");
        System.out.println("  - 查找：平均O(1)，最坏O(N)");
        System.out.println("  - 插入：平均O(1)");
        System.out.println("  - 删除：平均O(1)");

        System.out.println("\n空间复杂度：");
        System.out.println("  - O(N)，N为键值对数量");
        System.out.println("  - rehash期间临时翻倍");

        System.out.println("\n优化点：");
        System.out.println("  - 渐进式rehash：避免阻塞");
        System.out.println("  - 链表头插：O(1)插入");
        System.out.println("  - 位运算：快速计算索引");
        System.out.println("  - 2的幂次方大小：优化取模运算");
    }

    public static void main(String[] args) {
        DictAnalysis analysis = new DictAnalysis();

        analysis.explainHashAlgorithm();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainHashCollision();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainRehash();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainProgressiveRehash();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainApplications();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainPerformance();
    }
}
