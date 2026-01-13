package com.architecture.source;

/**
 * Redis事件循环（Event Loop）源码分析
 *
 * Redis是单线程的事件驱动程序（Redis 6.0后I/O多线程）
 * 事件循环是Redis的核心，处理所有的I/O事件和时间事件
 *
 * 源码文件：ae.c, ae.h
 */
public class EventLoopAnalysis {

    /**
     * 事件循环结构（ae.h）
     *
     * typedef struct aeEventLoop {
     *     int maxfd;                  // 当前已注册的最大文件描述符
     *     int setsize;                // 文件描述符监听集合的大小
     *     long long timeEventNextId;  // 下一个时间事件的ID
     *     aeFileEvent *events;        // 文件事件数组
     *     aeFiredEvent *fired;        // 已就绪的文件事件
     *     aeTimeEvent *timeEventHead; // 时间事件链表头
     *     int stop;                   // 停止标志
     *     void *apidata;              // 多路复用库的私有数据
     *     aeBeforeSleepProc *beforesleep; // 进入阻塞前执行
     *     aeBeforeSleepProc *aftersleep;  // 阻塞后执行
     *     int flags;
     * } aeEventLoop;
     */

    /**
     * 1. 两种事件类型
     *
     * 文件事件（File Event）：
     * - 客户端连接、读写、关闭
     * - 基于Reactor模式
     * - 使用I/O多路复用（epoll/select/kqueue）
     *
     * 时间事件（Time Event）：
     * - 定时任务，如serverCron
     * - 过期键清理
     * - 统计信息更新
     */
    public void explainEventTypes() {
        System.out.println("=== Redis事件类型 ===\n");

        System.out.println("1. 文件事件（File Event）");
        System.out.println("   - 客户端连接请求");
        System.out.println("   - 客户端命令请求");
        System.out.println("   - 客户端断开连接");
        System.out.println("   - 主从复制");
        System.out.println("   实现：I/O多路复用");

        System.out.println("\n2. 时间事件（Time Event）");
        System.out.println("   - serverCron：定期任务");
        System.out.println("     * 过期键清理");
        System.out.println("     * 统计信息更新");
        System.out.println("     * 触发BGSAVE/BGREWRITEAOF");
        System.out.println("     * 关闭超时客户端");
        System.out.println("   实现：链表存储");
    }

    /**
     * 2. 文件事件处理器（Reactor模式）
     *
     * 组件：
     * - 套接字（Socket）：产生文件事件的源头
     * - I/O多路复用程序：监听多个套接字，产生事件
     * - 文件事件分派器：将事件分派给相应的处理器
     * - 事件处理器：处理具体的事件
     *
     * 处理器类型：
     * - 连接应答处理器：acceptTcpHandler
     * - 命令请求处理器：readQueryFromClient
     * - 命令回复处理器：sendReplyToClient
     */
    public void explainFileEventHandler() {
        System.out.println("=== 文件事件处理（Reactor模式）===\n");

        System.out.println("架构组件：");
        System.out.println("  Socket -> I/O多路复用 -> 事件分派器 -> 事件处理器");

        System.out.println("\n事件处理器类型：");
        System.out.println("1. acceptTcpHandler（连接应答）");
        System.out.println("   - 处理客户端连接请求");
        System.out.println("   - 创建客户端对象");
        System.out.println("   - 注册命令请求处理器");

        System.out.println("\n2. readQueryFromClient（命令请求）");
        System.out.println("   - 读取客户端命令");
        System.out.println("   - 解析命令");
        System.out.println("   - 执行命令");
        System.out.println("   - 注册命令回复处理器");

        System.out.println("\n3. sendReplyToClient（命令回复）");
        System.out.println("   - 将命令结果发送给客户端");
        System.out.println("   - 发送完成后注销自己");

        System.out.println("\n执行流程：");
        System.out.println("  1. 客户端连接 -> acceptTcpHandler");
        System.out.println("  2. 客户端发送命令 -> readQueryFromClient");
        System.out.println("  3. 返回结果 -> sendReplyToClient");
    }

    /**
     * 3. I/O多路复用
     *
     * Redis支持多种I/O多路复用库：
     * - evport（Solaris）
     * - epoll（Linux）
     * - kqueue（BSD）
     * - select（通用）
     *
     * 优先级：evport > epoll > kqueue > select
     *
     * 编译时选择：
     * #ifdef HAVE_EVPORT
     * #include "ae_evport.c"
     * #else
     *     #ifdef HAVE_EPOLL
     *     #include "ae_epoll.c"
     *     #else
     *         #ifdef HAVE_KQUEUE
     *         #include "ae_kqueue.c"
     *         #else
     *         #include "ae_select.c"
     *         #endif
     *     #endif
     * #endif
     */
    public void explainIOMultiplexing() {
        System.out.println("=== I/O多路复用 ===\n");

        System.out.println("支持的库（优先级从高到低）：");
        System.out.println("  1. evport（Solaris）");
        System.out.println("  2. epoll（Linux）");
        System.out.println("  3. kqueue（BSD/macOS）");
        System.out.println("  4. select（通用，兜底）");

        System.out.println("\nepoll优势（Linux）：");
        System.out.println("  - 时间复杂度O(1)");
        System.out.println("  - 无文件描述符数量限制");
        System.out.println("  - 边缘触发（ET）+ 水平触发（LT）");
        System.out.println("  - 通过回调避免遍历");

        System.out.println("\n为什么Redis这么快？");
        System.out.println("  - 单线程避免上下文切换");
        System.out.println("  - I/O多路复用处理并发");
        System.out.println("  - 非阻塞I/O");
        System.out.println("  - 基于内存操作");
    }

    /**
     * 4. 事件循环主流程
     *
     * void aeMain(aeEventLoop *eventLoop) {
     *     eventLoop->stop = 0;
     *     while (!eventLoop->stop) {
     *         // 执行beforesleep回调
     *         if (eventLoop->beforesleep != NULL)
     *             eventLoop->beforesleep(eventLoop);
     *
     *         // 处理事件
     *         aeProcessEvents(eventLoop, AE_ALL_EVENTS|AE_CALL_AFTER_SLEEP);
     *     }
     * }
     *
     * aeProcessEvents流程：
     * 1. 计算最近的时间事件触发时间
     * 2. 调用aeApiPoll等待文件事件（最多等待到时间事件触发）
     * 3. 处理已就绪的文件事件
     * 4. 处理已到达的时间事件
     */
    public void explainEventLoop() {
        System.out.println("=== 事件循环主流程 ===\n");

        System.out.println("主循环（aeMain）：");
        System.out.println("  while (!stop) {");
        System.out.println("      beforesleep();  // 执行前置任务");
        System.out.println("      processEvents(); // 处理事件");
        System.out.println("  }");

        System.out.println("\nprocessEvents流程：");
        System.out.println("  1. 计算最近时间事件的触发时间");
        System.out.println("  2. 调用aeApiPoll等待文件事件");
        System.out.println("     - 最多等待到时间事件触发");
        System.out.println("     - 有文件事件就绪则立即返回");
        System.out.println("  3. 执行aftersleep回调");
        System.out.println("  4. 处理所有就绪的文件事件");
        System.out.println("  5. 处理所有到达的时间事件");

        System.out.println("\n关键点：");
        System.out.println("  - 文件事件优先于时间事件");
        System.out.println("  - 时间事件决定最大等待时间");
        System.out.println("  - 单线程串行处理所有事件");
    }

    /**
     * 5. beforesleep和aftersleep
     *
     * beforesleep主要任务：
     * - 处理客户端的输出缓冲区
     * - 处理阻塞操作
     * - 执行AOF写入
     * - 处理unblock的客户端
     *
     * 为什么需要beforesleep？
     * - 集中处理非紧急任务
     * - 避免在事件处理中阻塞
     * - 优化批量操作
     */
    public void explainBeforeSleep() {
        System.out.println("=== beforesleep机制 ===\n");

        System.out.println("beforesleep任务：");
        System.out.println("  1. handleClientsWithPendingWrites");
        System.out.println("     - 尝试直接写回复（避免注册写事件）");
        System.out.println("  2. flushAppendOnlyFile");
        System.out.println("     - AOF缓冲区刷新");
        System.out.println("  3. handleBlockedClientsTimeout");
        System.out.println("     - 处理阻塞超时的客户端");
        System.out.println("  4. freeClientsInAsyncFreeQueue");
        System.out.println("     - 异步释放客户端");

        System.out.println("\n设计目的：");
        System.out.println("  - 集中处理批量任务");
        System.out.println("  - 避免频繁注册/注销事件");
        System.out.println("  - 优化性能");
    }

    /**
     * 6. serverCron时间事件
     *
     * 默认每100毫秒执行一次（hz=10）
     *
     * 主要任务：
     * - 更新统计信息（命令执行次数、内存使用等）
     * - 清理过期键
     * - 关闭超时客户端
     * - 尝试执行BGSAVE或AOF重写
     * - 主从同步检查
     * - 集群定期任务
     */
    public void explainServerCron() {
        System.out.println("=== serverCron定时任务 ===\n");

        System.out.println("执行频率：默认每100ms（hz=10）");

        System.out.println("\n主要任务：");
        System.out.println("1. 更新统计信息");
        System.out.println("   - ops_sec_samples：每秒操作数");
        System.out.println("   - stat_net_input/output_bytes");

        System.out.println("\n2. 数据库维护");
        System.out.println("   - activeExpireCycle：清理过期键");
        System.out.println("   - 触发BGSAVE/BGREWRITEAOF");

        System.out.println("\n3. 客户端管理");
        System.out.println("   - 关闭超时客户端");
        System.out.println("   - 释放查询缓冲区");

        System.out.println("\n4. 主从复制");
        System.out.println("   - 发送PING给从节点");
        System.out.println("   - 检测主从连接");

        System.out.println("\n5. 集群维护");
        System.out.println("   - clusterCron：集群定时任务");

        System.out.println("\n6. AOF重写");
        System.out.println("   - 检查是否需要重写");
        System.out.println("   - 管理重写进程");
    }

    /**
     * 7. Redis 6.0的I/O多线程
     *
     * 改进：
     * - 命令执行仍然是单线程
     * - I/O读写使用多线程
     * - 降低I/O瓶颈
     *
     * 配置：
     * io-threads 4
     * io-threads-do-reads yes
     */
    public void explainIOThreads() {
        System.out.println("=== Redis 6.0 I/O多线程 ===\n");

        System.out.println("改进点：");
        System.out.println("  - 命令执行：仍然单线程");
        System.out.println("  - I/O读写：多线程");
        System.out.println("  - 解析命令：多线程");
        System.out.println("  - 写回复：多线程");

        System.out.println("\n为什么这样设计？");
        System.out.println("  - I/O是瓶颈，不是CPU");
        System.out.println("  - 命令执行单线程保持简单");
        System.out.println("  - 避免复杂的锁机制");

        System.out.println("\n配置：");
        System.out.println("  io-threads 4");
        System.out.println("  io-threads-do-reads yes");

        System.out.println("\n注意：");
        System.out.println("  - 适用于I/O密集场景");
        System.out.println("  - CPU密集场景效果不明显");
    }

    public static void main(String[] args) {
        EventLoopAnalysis analysis = new EventLoopAnalysis();

        analysis.explainEventTypes();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainFileEventHandler();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainIOMultiplexing();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainEventLoop();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainServerCron();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainIOThreads();
    }
}
