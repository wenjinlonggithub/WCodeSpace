package com.architecture.principle;

import java.util.*;
import java.util.concurrent.*;

/**
 * Redis 主从复制 - 简化演示版
 *
 * 用最简单的方式演示主从复制的核心概念
 */
public class SimpleReplicationDemo {

    /**
     * 简化版主节点
     */
    static class SimpleMaster {
        private String replicationId;                    // 复制ID
        private long offset;                             // 复制偏移量
        private Map<String, String> data;                // 数据存储
        private List<SimpleSlave> slaves;                // 从节点列表
        private LinkedList<String> commandLog;           // 命令日志（模拟backlog）
        private static final int MAX_LOG_SIZE = 100;     // 最大日志条数

        public SimpleMaster() {
            this.replicationId = "master-" + UUID.randomUUID().toString().substring(0, 8);
            this.offset = 0;
            this.data = new ConcurrentHashMap<>();
            this.slaves = new CopyOnWriteArrayList<>();
            this.commandLog = new LinkedList<>();

            System.out.println("🔴 主节点已启动");
            System.out.println("   Replication ID: " + replicationId);
        }

        /**
         * SET命令
         */
        public void set(String key, String value) {
            // 1. 执行命令
            data.put(key, value);
            offset++;

            System.out.println("\n📝 [主节点] SET " + key + " = " + value);
            System.out.println("   Offset: " + offset);

            // 2. 记录到命令日志
            String command = "SET " + key + " " + value + " " + offset;
            commandLog.addLast(command);

            // 保持日志大小
            if (commandLog.size() > MAX_LOG_SIZE) {
                commandLog.removeFirst();
            }

            // 3. 传播给所有从节点
            propagateCommand(command);
        }

        /**
         * 传播命令给从节点
         */
        private void propagateCommand(String command) {
            if (slaves.isEmpty()) {
                return;
            }

            System.out.println("📡 [主节点] 传播命令给 " + slaves.size() + " 个从节点");
            for (SimpleSlave slave : slaves) {
                slave.receiveCommand(command);
            }
        }

        /**
         * 从节点连接（PSYNC）
         */
        public void handlePsync(SimpleSlave slave, String slaveReplId, long slaveOffset) {
            System.out.println("\n📥 [主节点] 收到PSYNC请求");
            System.out.println("   从节点ID: " + slaveReplId);
            System.out.println("   从节点Offset: " + slaveOffset);

            // 判断全量复制还是增量复制
            if (slaveReplId.equals("?") || slaveOffset == -1) {
                // 全量复制
                fullResync(slave);
            } else if (slaveReplId.equals(replicationId) && canPartialResync(slaveOffset)) {
                // 增量复制
                partialResync(slave, slaveOffset);
            } else {
                // 无法增量复制，执行全量复制
                System.out.println("⚠️ [主节点] 无法增量复制，执行全量复制");
                fullResync(slave);
            }

            // 添加到从节点列表
            if (!slaves.contains(slave)) {
                slaves.add(slave);
            }
        }

        /**
         * 全量复制
         */
        private void fullResync(SimpleSlave slave) {
            System.out.println("🔄 [主节点] 执行全量复制");

            // 1. 发送FULLRESYNC响应
            slave.handleFullResync(replicationId, offset);

            // 2. 发送所有数据
            System.out.println("📤 [主节点] 发送所有数据 (" + data.size() + " 条)");
            for (Map.Entry<String, String> entry : data.entrySet()) {
                slave.receiveData(entry.getKey(), entry.getValue());
            }

            System.out.println("✅ [主节点] 全量复制完成");
        }

        /**
         * 增量复制
         */
        private void partialResync(SimpleSlave slave, long slaveOffset) {
            System.out.println("🔄 [主节点] 执行增量复制");

            // 1. 发送CONTINUE响应
            slave.handlePartialResync();

            // 2. 发送缺失的命令
            List<String> missingCommands = getCommandsSince(slaveOffset);
            System.out.println("📤 [主节点] 发送增量命令 (" + missingCommands.size() + " 条)");

            for (String command : missingCommands) {
                slave.receiveCommand(command);
            }

            System.out.println("✅ [主节点] 增量复制完成");
        }

        /**
         * 判断是否可以部分复制
         */
        private boolean canPartialResync(long slaveOffset) {
            if (commandLog.isEmpty()) {
                return false;
            }

            // 获取最早命令的offset
            String firstCmd = commandLog.getFirst();
            long firstOffset = extractOffset(firstCmd);

            // 从节点的offset必须在日志范围内
            return slaveOffset >= firstOffset && slaveOffset < offset;
        }

        /**
         * 获取从指定offset开始的所有命令
         */
        private List<String> getCommandsSince(long slaveOffset) {
            List<String> result = new ArrayList<>();

            for (String command : commandLog) {
                long cmdOffset = extractOffset(command);
                if (cmdOffset > slaveOffset) {
                    result.add(command);
                }
            }

            return result;
        }

        /**
         * 从命令中提取offset
         */
        private long extractOffset(String command) {
            String[] parts = command.split(" ");
            if (parts.length > 3) {
                try {
                    return Long.parseLong(parts[3]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        }

        public Map<String, String> getData() {
            return new HashMap<>(data);
        }

        public String getReplicationId() {
            return replicationId;
        }

        public long getOffset() {
            return offset;
        }

        public int getSlaveCount() {
            return slaves.size();
        }
    }

    /**
     * 简化版从节点
     */
    static class SimpleSlave {
        private String name;
        private String masterReplId;                     // 主节点ID
        private long offset;                             // 复制偏移量
        private Map<String, String> data;                // 数据存储

        public SimpleSlave(String name) {
            this.name = name;
            this.masterReplId = "?";
            this.offset = -1;
            this.data = new ConcurrentHashMap<>();

            System.out.println("\n🔵 从节点 [" + name + "] 已启动");
        }

        /**
         * 连接到主节点
         */
        public void connectToMaster(SimpleMaster master) {
            System.out.println("\n🔌 [" + name + "] 连接到主节点...");
            System.out.println("   发送PSYNC: " + masterReplId + " " + offset);

            // 发送PSYNC命令
            master.handlePsync(this, masterReplId, offset);
        }

        /**
         * 处理全量复制响应
         */
        public void handleFullResync(String replId, long replOffset) {
            System.out.println("📥 [" + name + "] 收到FULLRESYNC");
            System.out.println("   新的Replication ID: " + replId);
            System.out.println("   初始Offset: " + replOffset);

            // 清空旧数据
            data.clear();

            // 更新复制信息
            this.masterReplId = replId;
            this.offset = replOffset;
        }

        /**
         * 处理增量复制响应
         */
        public void handlePartialResync() {
            System.out.println("📥 [" + name + "] 收到CONTINUE，开始增量复制");
        }

        /**
         * 接收数据（全量复制时）
         */
        public void receiveData(String key, String value) {
            data.put(key, value);
        }

        /**
         * 接收命令（增量复制/命令传播）
         */
        public void receiveCommand(String command) {
            String[] parts = command.split(" ");

            if (parts.length >= 3 && parts[0].equals("SET")) {
                String key = parts[1];
                String value = parts[2];
                long cmdOffset = parts.length > 3 ? Long.parseLong(parts[3]) : 0;

                // 执行命令
                data.put(key, value);
                this.offset = cmdOffset;

                System.out.println("📥 [" + name + "] 执行: SET " + key + " = " + value + ", Offset: " + offset);
            }
        }

        public Map<String, String> getData() {
            return new HashMap<>(data);
        }

        public String getName() {
            return name;
        }

        public long getOffset() {
            return offset;
        }
    }

    // ==================== 演示程序 ====================

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(70));
        System.out.println("          Redis 主从复制 - 简化演示");
        System.out.println("=".repeat(70));

        // 步骤1：创建主节点
        System.out.println("\n【步骤1】创建主节点");
        SimpleMaster master = new SimpleMaster();

        Thread.sleep(1000);

        // 步骤2：主节点写入初始数据
        System.out.println("\n【步骤2】主节点写入初始数据");
        master.set("name", "Redis");
        master.set("version", "7.0");
        master.set("author", "Salvatore");

        Thread.sleep(1000);

        // 步骤3：从节点1连接（全量复制）
        System.out.println("\n【步骤3】从节点1连接 - 触发全量复制");
        SimpleSlave slave1 = new SimpleSlave("Slave-1");
        slave1.connectToMaster(master);

        Thread.sleep(1000);

        // 步骤4：验证从节点1数据
        System.out.println("\n【步骤4】验证从节点1数据");
        System.out.println("主节点数据: " + master.getData());
        System.out.println("从节点1数据: " + slave1.getData());

        Thread.sleep(1000);

        // 步骤5：主节点继续写入（命令传播）
        System.out.println("\n【步骤5】主节点继续写入 - 触发命令传播");
        master.set("language", "C");
        master.set("license", "BSD");

        Thread.sleep(1000);

        // 步骤6：验证命令传播效果
        System.out.println("\n【步骤6】验证命令传播效果");
        System.out.println("主节点数据: " + master.getData());
        System.out.println("从节点1数据: " + slave1.getData());
        System.out.println("从节点1 Offset: " + slave1.getOffset());

        Thread.sleep(1000);

        // 步骤7：从节点2连接（全量复制）
        System.out.println("\n【步骤7】从节点2连接 - 触发全量复制");
        SimpleSlave slave2 = new SimpleSlave("Slave-2");
        slave2.connectToMaster(master);

        Thread.sleep(1000);

        // 步骤8：主节点继续写入
        System.out.println("\n【步骤8】主节点继续写入 - 传播给所有从节点");
        master.set("type", "NoSQL");
        master.set("port", "6379");

        Thread.sleep(1000);

        // 步骤9：验证所有节点数据
        System.out.println("\n【步骤9】验证所有节点数据");
        System.out.println("主节点数据 (Offset=" + master.getOffset() + "): " + master.getData());
        System.out.println("从节点1数据 (Offset=" + slave1.getOffset() + "): " + slave1.getData());
        System.out.println("从节点2数据 (Offset=" + slave2.getOffset() + "): " + slave2.getData());

        Thread.sleep(1000);

        // 步骤10：模拟从节点断线重连（增量复制）
        System.out.println("\n【步骤10】模拟从节点3断线重连 - 触发增量复制");
        SimpleSlave slave3 = new SimpleSlave("Slave-3");

        // 模拟之前连接过，有旧的复制信息
        slave3.masterReplId = master.getReplicationId();
        slave3.offset = master.getOffset() - 2; // 落后2个命令

        System.out.println("从节点3之前的Offset: " + slave3.offset);
        System.out.println("主节点当前Offset: " + master.getOffset());

        slave3.connectToMaster(master);

        Thread.sleep(1000);

        // 步骤11：最终统计
        System.out.println("\n【步骤11】最终统计");
        System.out.println("=".repeat(70));
        System.out.println("主节点信息:");
        System.out.println("  Replication ID: " + master.getReplicationId());
        System.out.println("  Offset: " + master.getOffset());
        System.out.println("  从节点数量: " + master.getSlaveCount());
        System.out.println("  数据量: " + master.getData().size());

        System.out.println("\n从节点信息:");
        System.out.println("  Slave-1 Offset: " + slave1.getOffset() + ", 数据量: " + slave1.getData().size());
        System.out.println("  Slave-2 Offset: " + slave2.getOffset() + ", 数据量: " + slave2.getData().size());
        System.out.println("  Slave-3 Offset: " + slave3.getOffset() + ", 数据量: " + slave3.getData().size());

        System.out.println("\n" + "=".repeat(70));
        System.out.println("          演示完成");
        System.out.println("=".repeat(70));

        // 总结
        System.out.println("\n💡 总结:");
        System.out.println("1. 全量复制：首次连接或无法增量复制时，传输所有数据");
        System.out.println("2. 增量复制：短暂断线重连，只传输缺失的命令");
        System.out.println("3. 命令传播：复制完成后，持续同步写命令");
        System.out.println("4. Offset：记录复制进度，判断数据是否一致");
        System.out.println("5. Replication ID：标识数据集，判断是否同一个主节点");
    }
}
