package com.architecture.source;

/**
 * Redis命令执行流程源码分析
 *
 * 从客户端发送命令到接收回复的完整流程
 *
 * 源码文件：server.c, networking.c, db.c
 */
public class CommandExecutionAnalysis {

    /**
     * 命令执行完整流程
     *
     * 1. 客户端发送命令
     * 2. 服务器接收命令（readQueryFromClient）
     * 3. 解析命令（processInputBuffer）
     * 4. 执行命令（processCommand）
     * 5. 返回结果（addReply）
     * 6. 发送回复（sendReplyToClient）
     */
    public void explainFullFlow() {
        System.out.println("=== 命令执行完整流程 ===\n");

        System.out.println("客户端 -> 服务器");
        System.out.println("  ↓");
        System.out.println("1. readQueryFromClient");
        System.out.println("   - 从socket读取命令");
        System.out.println("   - 存入querybuf（查询缓冲区）");
        System.out.println("   ↓");
        System.out.println("2. processInputBuffer");
        System.out.println("   - 解析命令（RESP协议）");
        System.out.println("   - 分割命令和参数");
        System.out.println("   ↓");
        System.out.println("3. processCommand");
        System.out.println("   - 查找命令表（redisCommandTable）");
        System.out.println("   - 权限检查");
        System.out.println("   - 参数校验");
        System.out.println("   - 内存检查");
        System.out.println("   - 集群检查");
        System.out.println("   - 调用命令处理函数");
        System.out.println("   ↓");
        System.out.println("4. call");
        System.out.println("   - 执行命令");
        System.out.println("   - 慢查询日志");
        System.out.println("   - AOF追加");
        System.out.println("   - 主从复制传播");
        System.out.println("   ↓");
        System.out.println("5. addReply");
        System.out.println("   - 将结果写入输出缓冲区");
        System.out.println("   - 注册写事件（如果需要）");
        System.out.println("   ↓");
        System.out.println("6. sendReplyToClient");
        System.out.println("   - 发送回复给客户端");
        System.out.println("   - 清空输出缓冲区");
        System.out.println("   ↓");
        System.out.println("服务器 -> 客户端");
    }

    /**
     * 1. 命令表（Command Table）
     *
     * struct redisCommand {
     *     char *name;           // 命令名称
     *     redisCommandProc *proc; // 命令实现函数
     *     int arity;            // 参数个数，负数表示>=-arity
     *     char *sflags;         // 字符串标志
     *     uint64_t flags;       // 实际标志
     *     ...
     * };
     *
     * 示例：
     * {"set", setCommand, -3, "wm", ...}
     * - name: "set"
     * - proc: setCommand函数
     * - arity: -3（至少3个参数：SET key value）
     * - sflags: "wm"（write, may-replicate）
     */
    public void explainCommandTable() {
        System.out.println("=== 命令表 ===\n");

        System.out.println("命令注册：redisCommandTable数组");
        System.out.println("查找：O(1)，使用哈希表（dict）");

        System.out.println("\n命令结构：");
        System.out.println("  - name：命令名称");
        System.out.println("  - proc：实现函数");
        System.out.println("  - arity：参数个数");
        System.out.println("  - flags：命令标志");

        System.out.println("\n常见标志：");
        System.out.println("  w：写命令，会修改数据");
        System.out.println("  r：读命令");
        System.out.println("  m：可能占用大量内存");
        System.out.println("  a：管理命令");
        System.out.println("  s：统计命令");
        System.out.println("  R：随机命令（结果不确定）");
        System.out.println("  S：排序命令");
        System.out.println("  l：加载数据时可执行");
        System.out.println("  t：允许在从节点执行");
        System.out.println("  M：不自动传播");
        System.out.println("  F：快速命令");

        System.out.println("\n示例：");
        System.out.println("  {\"get\", getCommand, 2, \"rF\", ...}");
        System.out.println("  {\"set\", setCommand, -3, \"wm\", ...}");
        System.out.println("  {\"del\", delCommand, -2, \"w\", ...}");
    }

    /**
     * 2. processCommand详解
     *
     * 执行前的检查：
     * 1. 命令是否存在
     * 2. 参数个数是否正确
     * 3. 是否需要身份验证
     * 4. 内存是否足够
     * 5. BGSAVE错误时，是否允许写
     * 6. 订阅模式下，只允许特定命令
     * 7. 从节点是否允许该命令
     * 8. 集群模式下，key是否在本节点
     * 9. 最大内存限制
     */
    public void explainProcessCommand() {
        System.out.println("=== processCommand检查流程 ===\n");

        System.out.println("执行前的检查：");
        System.out.println("1. 命令查找");
        System.out.println("   if (!cmd) return \"unknown command\"");

        System.out.println("\n2. 参数校验");
        System.out.println("   if (cmd->arity != argc) return \"wrong number of arguments\"");

        System.out.println("\n3. 身份验证");
        System.out.println("   if (!authenticated && cmd->name != \"auth\")");
        System.out.println("      return \"NOAUTH Authentication required\"");

        System.out.println("\n4. 集群重定向");
        System.out.println("   if (cluster_enabled && key_not_in_slot)");
        System.out.println("      return \"MOVED 3999 127.0.0.1:6381\"");

        System.out.println("\n5. 内存检查");
        System.out.println("   if (out_of_memory && is_write_command)");
        System.out.println("      return \"OOM command not allowed\"");

        System.out.println("\n6. 持久化错误检查");
        System.out.println("   if (bgsave_error && is_write_command)");
        System.out.println("      return \"MISCONF Redis is configured to save RDB\"");

        System.out.println("\n7. 从节点只读检查");
        System.out.println("   if (server.masterhost && is_write_command)");
        System.out.println("      return \"READONLY You can't write against a read only replica\"");

        System.out.println("\n8. 发布订阅模式");
        System.out.println("   if (in_pubsub_mode && !is_pubsub_command)");
        System.out.println("      return \"only PING, SUBSCRIBE... allowed\"");

        System.out.println("\n通过所有检查后：");
        System.out.println("   call(c, CMD_CALL_FULL);");
    }

    /**
     * 3. call函数 - 实际执行
     *
     * int call(client *c, int flags) {
     *     // 1. 执行命令
     *     c->cmd->proc(c);
     *
     *     // 2. 慢查询日志
     *     slowlogPushEntryIfNeeded();
     *
     *     // 3. 统计信息
     *     c->cmd->microseconds += duration;
     *     c->cmd->calls++;
     *
     *     // 4. AOF持久化
     *     if (flags & CMD_CALL_PROPAGATE_AOF)
     *         feedAppendOnlyFile();
     *
     *     // 5. 主从复制
     *     if (flags & CMD_CALL_PROPAGATE_REPL)
     *         replicationFeedSlaves();
     * }
     */
    public void explainCall() {
        System.out.println("=== call执行流程 ===\n");

        System.out.println("1. 执行命令");
        System.out.println("   c->cmd->proc(c);");
        System.out.println("   调用实际的命令处理函数");

        System.out.println("\n2. 慢查询日志");
        System.out.println("   if (duration > slowlog_threshold)");
        System.out.println("      slowlogPushEntryIfNeeded();");

        System.out.println("\n3. 统计信息更新");
        System.out.println("   c->cmd->microseconds += duration;");
        System.out.println("   c->cmd->calls++;");
        System.out.println("   server.stat_numcommands++;");

        System.out.println("\n4. AOF追加");
        System.out.println("   if (server.aof_state == AOF_ON)");
        System.out.println("      feedAppendOnlyFile(c->cmd, c->db->id, c->argv, c->argc);");

        System.out.println("\n5. 主从复制传播");
        System.out.println("   if (listLength(server.slaves) > 0)");
        System.out.println("      replicationFeedSlaves(server.slaves, c->db->id, c->argv, c->argc);");

        System.out.println("\n6. 阻塞的客户端通知");
        System.out.println("   if (listLength(server.ready_keys))");
        System.out.println("      handleClientsBlockedOnKeys();");
    }

    /**
     * 4. 输出缓冲区管理
     *
     * 两种缓冲区：
     * - 固定缓冲区：buf[PROTO_REPLY_CHUNK_BYTES]（16KB）
     * - 可变缓冲区：reply链表（list）
     *
     * 使用策略：
     * - 小回复：使用固定缓冲区
     * - 大回复：使用可变缓冲区
     */
    public void explainOutputBuffer() {
        System.out.println("=== 输出缓冲区 ===\n");

        System.out.println("两种缓冲区：");
        System.out.println("1. 固定缓冲区（buf）");
        System.out.println("   - 大小：16KB");
        System.out.println("   - 用于：小回复");
        System.out.println("   - 优势：避免内存分配");

        System.out.println("\n2. 可变缓冲区（reply链表）");
        System.out.println("   - 大小：动态增长");
        System.out.println("   - 用于：大回复");
        System.out.println("   - 优势：支持任意大小");

        System.out.println("\n缓冲区限制：");
        System.out.println("  client-output-buffer-limit <class> <hard> <soft> <soft-seconds>");
        System.out.println("  normal: 0 0 0（无限制）");
        System.out.println("  replica: 256mb 64mb 60");
        System.out.println("  pubsub: 32mb 8mb 60");

        System.out.println("\n超限处理：");
        System.out.println("  - 硬限制：立即关闭客户端");
        System.out.println("  - 软限制：持续N秒后关闭");
    }

    /**
     * 5. RESP协议
     *
     * Redis序列化协议（REdis Serialization Protocol）
     *
     * 5种类型：
     * - 简单字符串（Simple Strings）：+OK\r\n
     * - 错误（Errors）：-ERR unknown command\r\n
     * - 整数（Integers）：:1000\r\n
     * - 批量字符串（Bulk Strings）：$6\r\nfoobar\r\n
     * - 数组（Arrays）：*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n
     */
    public void explainRESP() {
        System.out.println("=== RESP协议 ===\n");

        System.out.println("5种数据类型：");
        System.out.println("1. 简单字符串：+OK\\r\\n");
        System.out.println("2. 错误：-Error message\\r\\n");
        System.out.println("3. 整数：:1000\\r\\n");
        System.out.println("4. 批量字符串：$6\\r\\nfoobar\\r\\n");
        System.out.println("5. 数组：*2\\r\\n$3\\r\\nfoo\\r\\n$3\\r\\nbar\\r\\n");

        System.out.println("\n示例：SET key value");
        System.out.println("客户端发送：");
        System.out.println("  *3\\r\\n");
        System.out.println("  $3\\r\\nSET\\r\\n");
        System.out.println("  $3\\r\\nkey\\r\\n");
        System.out.println("  $5\\r\\nvalue\\r\\n");

        System.out.println("\n服务器回复：");
        System.out.println("  +OK\\r\\n");

        System.out.println("\n示例：GET key");
        System.out.println("客户端发送：");
        System.out.println("  *2\\r\\n$3\\r\\nGET\\r\\n$3\\r\\nkey\\r\\n");

        System.out.println("\n服务器回复：");
        System.out.println("  $5\\r\\nvalue\\r\\n");

        System.out.println("\nRESP优势：");
        System.out.println("  - 易于实现");
        System.out.println("  - 解析快速");
        System.out.println("  - 人类可读");
    }

    /**
     * 6. 管道（Pipeline）
     *
     * 客户端一次性发送多个命令，减少RTT（往返时间）
     *
     * 不使用Pipeline：
     * RTT1: SET key1 value1
     * RTT2: SET key2 value2
     * RTT3: SET key3 value3
     * 总耗时 = 3 * RTT
     *
     * 使用Pipeline：
     * 发送: SET key1 value1; SET key2 value2; SET key3 value3
     * 接收: OK; OK; OK
     * 总耗时 = 1 * RTT
     */
    public void explainPipeline() {
        System.out.println("=== Pipeline机制 ===\n");

        System.out.println("问题：");
        System.out.println("  - 每个命令都需要一次网络往返");
        System.out.println("  - RTT累积导致性能下降");

        System.out.println("\n解决：Pipeline");
        System.out.println("  - 批量发送命令");
        System.out.println("  - 批量接收回复");
        System.out.println("  - 减少网络往返");

        System.out.println("\n性能对比：");
        System.out.println("  不使用Pipeline：");
        System.out.println("    SET k1 v1 -> OK (RTT)");
        System.out.println("    SET k2 v2 -> OK (RTT)");
        System.out.println("    SET k3 v3 -> OK (RTT)");
        System.out.println("    总耗时 = 3 * RTT");

        System.out.println("\n  使用Pipeline：");
        System.out.println("    SET k1 v1; SET k2 v2; SET k3 v3 -> OK; OK; OK");
        System.out.println("    总耗时 = 1 * RTT");

        System.out.println("\n注意：");
        System.out.println("  - Pipeline不保证原子性");
        System.out.println("  - 命令之间不能有依赖");
        System.out.println("  - 控制批次大小，避免内存爆炸");
    }

    public static void main(String[] args) {
        CommandExecutionAnalysis analysis = new CommandExecutionAnalysis();

        analysis.explainFullFlow();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainCommandTable();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainProcessCommand();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainCall();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainRESP();
        System.out.println("\n" + "=".repeat(60) + "\n");

        analysis.explainPipeline();
    }
}
