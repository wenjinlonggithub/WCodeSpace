package com.architecture.core;

/**
 * Redis对象（RedisObject）核心类解析
 *
 * RedisObject是Redis中最核心的结构，用于表示所有的值对象
 *
 * 源码结构（server.h）：
 * typedef struct redisObject {
 *     unsigned type:4;        // 类型（4 bit）
 *     unsigned encoding:4;    // 编码（4 bit）
 *     unsigned lru:24;        // LRU时间或LFU数据（24 bit）
 *     int refcount;           // 引用计数
 *     void *ptr;              // 指向实际数据的指针
 * } robj;
 */
public class RedisObjectAnalysis {

    /**
     * 对象类型（type字段）
     *
     * #define OBJ_STRING 0    // 字符串对象
     * #define OBJ_LIST 1      // 列表对象
     * #define OBJ_SET 2       // 集合对象
     * #define OBJ_ZSET 3      // 有序集合对象
     * #define OBJ_HASH 4      // 哈希对象
     * #define OBJ_MODULE 5    // 模块对象
     * #define OBJ_STREAM 6    // 流对象
     */
    public static class ObjectType {
        public static final int STRING = 0;
        public static final int LIST = 1;
        public static final int SET = 2;
        public static final int ZSET = 3;
        public static final int HASH = 4;
        public static final int MODULE = 5;
        public static final int STREAM = 6;

        public static String getTypeName(int type) {
            switch (type) {
                case STRING: return "string";
                case LIST: return "list";
                case SET: return "set";
                case ZSET: return "zset";
                case HASH: return "hash";
                case MODULE: return "module";
                case STREAM: return "stream";
                default: return "unknown";
            }
        }
    }

    /**
     * 对象编码（encoding字段）
     *
     * 不同类型的对象可以有不同的编码方式，优化内存和性能
     */
    public static class ObjectEncoding {
        // 字符串编码
        public static final int RAW = 0;              // 简单动态字符串（SDS）
        public static final int INT = 1;              // 整数
        public static final int EMBSTR = 8;           // 嵌入式字符串（<=44字节）

        // 列表编码
        public static final int QUICKLIST = 9;        // 快速列表（ziplist + linkedlist）

        // 集合编码
        public static final int HT = 2;               // 哈希表（dict）
        public static final int INTSET = 6;           // 整数集合

        // 有序集合编码
        public static final int SKIPLIST = 7;         // 跳表 + 哈希表
        public static final int ZIPLIST = 5;          // 压缩列表（已废弃，用listpack）
        public static final int LISTPACK = 11;        // 列表包（Redis 7.0+）

        // 哈希编码
        // HT = 2 (同集合)
        // ZIPLIST = 5 (同上)
        // LISTPACK = 11 (同上)

        public static String getEncodingName(int encoding) {
            switch (encoding) {
                case RAW: return "raw";
                case INT: return "int";
                case HT: return "hashtable";
                case ZIPLIST: return "ziplist";
                case INTSET: return "intset";
                case SKIPLIST: return "skiplist";
                case EMBSTR: return "embstr";
                case QUICKLIST: return "quicklist";
                case LISTPACK: return "listpack";
                default: return "unknown";
            }
        }
    }

    /**
     * 编码转换规则
     *
     * 1. String对象：
     *    - 整数值 -> INT编码
     *    - 字符串长度 <= 44字节 -> EMBSTR编码
     *    - 字符串长度 > 44字节 -> RAW编码
     *
     * 2. List对象：
     *    - Redis 3.2之前：ziplist或linkedlist
     *    - Redis 3.2+：统一使用quicklist
     *
     * 3. Hash对象：
     *    - 元素数量 < hash-max-ziplist-entries（默认512）且
     *      所有键值长度 < hash-max-ziplist-value（默认64）-> ziplist/listpack
     *    - 否则 -> hashtable
     *
     * 4. Set对象：
     *    - 所有元素都是整数且元素数量 < set-max-intset-entries（默认512）-> intset
     *    - 否则 -> hashtable
     *
     * 5. ZSet对象：
     *    - 元素数量 < zset-max-ziplist-entries（默认128）且
     *      所有member长度 < zset-max-ziplist-value（默认64）-> ziplist/listpack
     *    - 否则 -> skiplist + hashtable
     */
    public void explainEncodingTransition() {
        System.out.println("=== Redis对象编码转换规则 ===\n");

        System.out.println("1. String对象:");
        System.out.println("   整数值 -> INT");
        System.out.println("   长度 <= 44字节 -> EMBSTR");
        System.out.println("   长度 > 44字节 -> RAW");

        System.out.println("\n2. List对象:");
        System.out.println("   Redis 3.2+ -> QUICKLIST（统一）");

        System.out.println("\n3. Hash对象:");
        System.out.println("   元素少且值小 -> ZIPLIST/LISTPACK");
        System.out.println("   否则 -> HASHTABLE");

        System.out.println("\n4. Set对象:");
        System.out.println("   全是整数且数量少 -> INTSET");
        System.out.println("   否则 -> HASHTABLE");

        System.out.println("\n5. ZSet对象:");
        System.out.println("   元素少且member短 -> ZIPLIST/LISTPACK");
        System.out.println("   否则 -> SKIPLIST + HASHTABLE");
    }

    /**
     * 引用计数（refcount字段）
     *
     * Redis使用引用计数进行内存管理：
     * - 创建对象时，refcount = 1
     * - 对象被新程序使用时，refcount++
     * - 对象不再被使用时，refcount--
     * - refcount = 0时，释放对象内存
     *
     * 对象共享：
     * - Redis会共享0-9999的整数对象
     * - 共享对象的refcount会增加
     */
    public void explainRefCount() {
        System.out.println("=== 引用计数机制 ===\n");

        System.out.println("作用：自动内存管理");
        System.out.println("规则：");
        System.out.println("  - 创建对象：refcount = 1");
        System.out.println("  - 使用对象：refcount++");
        System.out.println("  - 不再使用：refcount--");
        System.out.println("  - refcount = 0：释放内存");

        System.out.println("\n对象共享：");
        System.out.println("  - 整数0-9999会被共享");
        System.out.println("  - 节省内存");
        System.out.println("  - 示例：SET key1 100; SET key2 100");
        System.out.println("         key1和key2共享同一个整数对象");
    }

    /**
     * LRU/LFU字段
     *
     * 用于内存淘汰策略：
     * - LRU（Least Recently Used）：最近最少使用
     * - LFU（Least Frequently Used）：最不经常使用
     *
     * LRU模式（24 bit）：
     * - 记录对象最后一次被访问的时间（秒级）
     *
     * LFU模式（24 bit）：
     * - 高16位：最后降低频率的时间
     * - 低8位：访问频率计数器
     */
    public void explainLRU() {
        System.out.println("=== LRU/LFU机制 ===\n");

        System.out.println("LRU模式：");
        System.out.println("  - 记录最后访问时间");
        System.out.println("  - 淘汰最久未使用的key");
        System.out.println("  - 配置：maxmemory-policy allkeys-lru");

        System.out.println("\nLFU模式：");
        System.out.println("  - 记录访问频率");
        System.out.println("  - 淘汰访问频率最低的key");
        System.out.println("  - 配置：maxmemory-policy allkeys-lfu");
        System.out.println("  - 优势：更精确地反映热度");
    }

    /**
     * 为什么RedisObject这样设计？
     *
     * 1. 类型和编码分离：
     *    - type表示对象类型（用户视角）
     *    - encoding表示底层实现（内部优化）
     *    - 同一类型可以有多种编码，灵活优化
     *
     * 2. 节省内存：
     *    - type和encoding各占4位，共1字节
     *    - lru占24位，3字节
     *    - 头部总共16字节（64位系统）
     *
     * 3. 引用计数：
     *    - 自动内存管理
     *    - 支持对象共享
     *
     * 4. 淘汰策略：
     *    - LRU/LFU字段用于内存淘汰
     *    - 不需要额外的数据结构
     */
    public void explainDesign() {
        System.out.println("=== RedisObject设计理念 ===\n");

        System.out.println("1. 类型和编码分离");
        System.out.println("   优势：灵活优化，透明切换");

        System.out.println("\n2. 紧凑的内存布局");
        System.out.println("   type + encoding: 1字节");
        System.out.println("   lru: 3字节");
        System.out.println("   refcount: 4字节");
        System.out.println("   ptr: 8字节（64位）");
        System.out.println("   总计: 16字节");

        System.out.println("\n3. 引用计数");
        System.out.println("   自动内存管理 + 对象共享");

        System.out.println("\n4. 内置淘汰支持");
        System.out.println("   LRU/LFU字段直接支持淘汰策略");
    }

    public static void main(String[] args) {
        RedisObjectAnalysis analysis = new RedisObjectAnalysis();

        analysis.explainEncodingTransition();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainRefCount();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainLRU();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainDesign();
    }
}
