package com.architecture.principle;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Redis主从复制原理实现
 *
 * 主从复制是Redis高可用的基础，包括：
 * 1. 全量复制（Full Resynchronization）
 * 2. 增量复制（Partial Resynchronization）
 * 3. 命令传播（Command Propagation）
 *
 * 核心概念：
 * - Replication ID：标识数据集的唯一ID
 * - Replication Offset：复制偏移量，记录已复制的字节数
 * - Replication Backlog：复制积压缓冲区，环形缓冲区，默认1MB
 */
public class RedisReplication {

    /**
     * Redis主节点（Master）
     */
    static class RedisMaster {
        private String replicationId;           // 复制ID
        private long replicationOffset;         // 复制偏移量
        private Map<String, String> dataStore;  // 数据存储
        private ReplicationBacklog backlog;     // 复制积压缓冲区
        private List<RedisSlaveConnection> slaves; // 从节点连接列表
        private ServerSocket serverSocket;
        private boolean running;

        // 写命令缓冲区（待发送给从节点）
        private Queue<ReplicationCommand> commandBuffer;

        public RedisMaster(int port) throws IOException {
            this.replicationId = UUID.randomUUID().toString();
            this.replicationOffset = 0;
            this.dataStore = new ConcurrentHashMap<>();
            this.backlog = new ReplicationBacklog(1024 * 1024); // 1MB
            this.slaves = new CopyOnWriteArrayList<>();
            this.commandBuffer = new ConcurrentLinkedQueue<>();
            this.serverSocket = new ServerSocket(port);
            this.running = false;

            System.out.println("🔴 [Master] 启动完成");
            System.out.println("    Replication ID: " + replicationId);
            System.out.println("    Port: " + port);
        }

        /**
         * 启动主节点，监听从节点连接
         */
        public void start() {
            running = true;

            // 监听从节点连接
            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleSlaveConnection(clientSocket);
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("❌ [Master] 接受连接失败: " + e.getMessage());
                        }
                    }
                }
            }, "master-accept-thread").start();

            // 命令传播线程
            new Thread(this::propagateCommands, "master-propagate-thread").start();

            System.out.println("✅ [Master] 开始监听从节点连接");
        }

        /**
         * 处理从节点连接
         */
        private void handleSlaveConnection(Socket socket) {
            try {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("\n📡 [Master] 收到从节点连接: " + socket.getRemoteSocketAddress());

                // 读取PSYNC命令
                String line = in.readLine();
                if (line != null && line.startsWith("PSYNC")) {
                    handlePsync(line, socket, in, out);
                }
            } catch (IOException e) {
                System.err.println("❌ [Master] 处理从节点连接失败: " + e.getMessage());
            }
        }

        /**
         * 处理PSYNC命令（主从复制核心）
         *
         * PSYNC replicationId offset
         *
         * 响应：
         * - FULLRESYNC replicationId offset：需要全量复制
         * - CONTINUE：可以增量复制
         */
        private void handlePsync(String psyncCmd, Socket socket,
                                  BufferedReader in, PrintWriter out) throws IOException {
            String[] parts = psyncCmd.split(" ");
            String slaveReplId = parts.length > 1 ? parts[1] : "?";
            long slaveOffset = parts.length > 2 ? Long.parseLong(parts[2]) : -1;

            System.out.println("📥 [Master] 收到PSYNC命令");
            System.out.println("    从节点 Replication ID: " + slaveReplId);
            System.out.println("    从节点 Offset: " + slaveOffset);

            // 判断是全量复制还是增量复制
            if (slaveReplId.equals("?") || slaveOffset == -1 ||
                !slaveReplId.equals(replicationId) ||
                !backlog.canPartialResync(slaveOffset)) {

                // 全量复制
                System.out.println("🔄 [Master] 执行全量复制 (FULLRESYNC)");
                fullResync(socket, out);
            } else {
                // 增量复制
                System.out.println("🔄 [Master] 执行增量复制 (CONTINUE)");
                partialResync(socket, out, slaveOffset);
            }

            // 添加从节点连接
            RedisSlaveConnection slaveConn = new RedisSlaveConnection(socket, out);
            slaves.add(slaveConn);
            System.out.println("✅ [Master] 从节点已连接，当前从节点数: " + slaves.size());
        }

        /**
         * 全量复制
         *
         * 流程：
         * 1. 响应 +FULLRESYNC replicationId offset
         * 2. 执行BGSAVE生成RDB文件
         * 3. 发送RDB文件给从节点
         * 4. 发送缓冲区中的写命令
         */
        private void fullResync(Socket socket, PrintWriter out) {
            // 1. 响应FULLRESYNC
            out.println("+FULLRESYNC " + replicationId + " " + replicationOffset);
            out.flush();

            System.out.println("    📤 发送: +FULLRESYNC " + replicationId + " " + replicationOffset);

            // 2. 生成RDB快照（模拟）
            System.out.println("    💾 生成RDB快照...");
            byte[] rdbData = generateRDB();

            // 3. 发送RDB数据
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(rdbData);
                outputStream.flush();
                System.out.println("    📤 RDB数据发送完成，大小: " + rdbData.length + " bytes");
            } catch (IOException e) {
                System.err.println("    ❌ 发送RDB失败: " + e.getMessage());
            }

            // 4. 发送缓冲区中的命令
            System.out.println("    📤 发送缓冲区命令...");
        }

        /**
         * 增量复制
         *
         * 流程：
         * 1. 响应 +CONTINUE
         * 2. 从复制积压缓冲区中获取从slaveOffset到当前的命令
         * 3. 发送这些命令给从节点
         */
        private void partialResync(Socket socket, PrintWriter out, long slaveOffset) {
            // 1. 响应CONTINUE
            out.println("+CONTINUE");
            out.flush();

            System.out.println("    📤 发送: +CONTINUE");

            // 2. 获取积压缓冲区中的命令
            List<ReplicationCommand> commands = backlog.getCommandsSince(slaveOffset);

            System.out.println("    📤 发送增量命令，数量: " + commands.size());

            // 3. 发送命令
            for (ReplicationCommand cmd : commands) {
                out.println(cmd.serialize());
                out.flush();
            }

            System.out.println("    ✅ 增量复制完成");
        }

        /**
         * 生成RDB快照（简化实现）
         */
        private byte[] generateRDB() {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {

                // 写入魔数和版本
                oos.writeUTF("REDIS");
                oos.writeInt(9); // RDB版本

                // 写入数据
                oos.writeObject(new HashMap<>(dataStore));

                // 写入EOF和校验和
                oos.writeUTF("EOF");
                oos.writeLong(System.currentTimeMillis());

                return baos.toByteArray();
            } catch (IOException e) {
                System.err.println("❌ 生成RDB失败: " + e.getMessage());
                return new byte[0];
            }
        }

        /**
         * 执行SET命令
         */
        public void set(String key, String value) {
            dataStore.put(key, value);

            System.out.println("\n📝 [Master] SET " + key + " = " + value);

            // 创建复制命令
            ReplicationCommand cmd = new ReplicationCommand(
                "SET",
                Arrays.asList(key, value),
                replicationOffset
            );

            // 添加到积压缓冲区
            backlog.append(cmd);
            replicationOffset += cmd.getSize();

            // 添加到命令缓冲区（待传播给从节点）
            commandBuffer.offer(cmd);

            System.out.println("    Replication Offset: " + replicationOffset);
        }

        /**
         * 命令传播（异步发送给所有从节点）
         */
        private void propagateCommands() {
            while (running) {
                try {
                    ReplicationCommand cmd = commandBuffer.poll();
                    if (cmd != null) {
                        propagateToSlaves(cmd);
                    } else {
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        /**
         * 将命令传播给所有从节点
         */
        private void propagateToSlaves(ReplicationCommand cmd) {
            if (slaves.isEmpty()) {
                return;
            }

            System.out.println("📡 [Master] 传播命令给 " + slaves.size() + " 个从节点");

            Iterator<RedisSlaveConnection> iterator = slaves.iterator();
            while (iterator.hasNext()) {
                RedisSlaveConnection slave = iterator.next();
                try {
                    slave.out.println(cmd.serialize());
                    slave.out.flush();
                    System.out.println("    ✅ 发送到从节点: " + slave.socket.getRemoteSocketAddress());
                } catch (Exception e) {
                    System.err.println("    ❌ 发送失败: " + e.getMessage());
                    iterator.remove();
                    try {
                        slave.socket.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }

        public String get(String key) {
            return dataStore.get(key);
        }

        public void stop() throws IOException {
            running = false;
            serverSocket.close();
            for (RedisSlaveConnection slave : slaves) {
                slave.socket.close();
            }
        }

        public String getReplicationId() {
            return replicationId;
        }

        public long getReplicationOffset() {
            return replicationOffset;
        }

        public int getSlaveCount() {
            return slaves.size();
        }
    }

    /**
     * Redis从节点（Slave/Replica）
     */
    static class RedisSlave {
        private String masterHost;
        private int masterPort;
        private String replicationId;
        private long replicationOffset;
        private Map<String, String> dataStore;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private boolean running;

        public RedisSlave(String masterHost, int masterPort) {
            this.masterHost = masterHost;
            this.masterPort = masterPort;
            this.replicationId = "?";
            this.replicationOffset = -1;
            this.dataStore = new ConcurrentHashMap<>();
            this.running = false;

            System.out.println("🔵 [Slave] 初始化完成");
            System.out.println("    Master: " + masterHost + ":" + masterPort);
        }

        /**
         * 连接到主节点并开始复制
         */
        public void connect() throws IOException {
            System.out.println("\n🔌 [Slave] 连接到主节点...");

            socket = new Socket(masterHost, masterPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("✅ [Slave] 连接成功");

            // 发送PSYNC命令
            sendPsync();

            // 启动接收线程
            running = true;
            new Thread(this::receiveCommands, "slave-receive-thread").start();
        }

        /**
         * 发送PSYNC命令
         *
         * 第一次连接：PSYNC ? -1
         * 断线重连：PSYNC replicationId offset
         */
        private void sendPsync() throws IOException {
            String psyncCmd = "PSYNC " + replicationId + " " + replicationOffset;
            out.println(psyncCmd);
            out.flush();

            System.out.println("📤 [Slave] 发送PSYNC: " + psyncCmd);

            // 读取响应
            String response = in.readLine();
            System.out.println("📥 [Slave] 收到响应: " + response);

            if (response.startsWith("+FULLRESYNC")) {
                handleFullResync(response);
            } else if (response.startsWith("+CONTINUE")) {
                handlePartialResync();
            }
        }

        /**
         * 处理全量复制
         */
        private void handleFullResync(String response) throws IOException {
            String[] parts = response.split(" ");
            this.replicationId = parts[1];
            this.replicationOffset = Long.parseLong(parts[2]);

            System.out.println("🔄 [Slave] 开始全量复制");
            System.out.println("    新的 Replication ID: " + replicationId);
            System.out.println("    初始 Offset: " + replicationOffset);

            // 接收RDB数据
            System.out.println("📥 [Slave] 接收RDB数据...");
            byte[] rdbData = receiveRDB();

            System.out.println("💾 [Slave] 加载RDB数据，大小: " + rdbData.length + " bytes");
            loadRDB(rdbData);

            System.out.println("✅ [Slave] 全量复制完成");
        }

        /**
         * 处理增量复制
         */
        private void handlePartialResync() {
            System.out.println("🔄 [Slave] 开始增量复制");
            System.out.println("    当前 Replication ID: " + replicationId);
            System.out.println("    当前 Offset: " + replicationOffset);
        }

        /**
         * 接收RDB数据
         */
        private byte[] receiveRDB() throws IOException {
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;

            // 简化实现：读取固定大小
            // 实际Redis会先发送RDB大小
            inputStream.read(buffer, 0, Math.min(buffer.length, inputStream.available()));

            while (inputStream.available() > 0) {
                bytesRead = inputStream.read(buffer);
                if (bytesRead > 0) {
                    baos.write(buffer, 0, bytesRead);
                }
            }

            return baos.toByteArray();
        }

        /**
         * 加载RDB数据
         */
        @SuppressWarnings("unchecked")
        private void loadRDB(byte[] rdbData) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(rdbData);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {

                // 读取魔数和版本
                String magic = ois.readUTF();
                int version = ois.readInt();

                System.out.println("    RDB Magic: " + magic + ", Version: " + version);

                // 读取数据
                Map<String, String> data = (Map<String, String>) ois.readObject();
                dataStore.putAll(data);

                System.out.println("    加载数据量: " + data.size() + " 条");

            } catch (Exception e) {
                System.err.println("❌ [Slave] 加载RDB失败: " + e.getMessage());
            }
        }

        /**
         * 接收主节点传播的命令
         */
        private void receiveCommands() {
            while (running) {
                try {
                    String line = in.readLine();
                    if (line == null) {
                        System.out.println("⚠️ [Slave] 与主节点断开连接");
                        break;
                    }

                    if (line.startsWith("SET")) {
                        handleSetCommand(line);
                    }

                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ [Slave] 接收命令失败: " + e.getMessage());
                    }
                    break;
                }
            }
        }

        /**
         * 处理SET命令
         */
        private void handleSetCommand(String cmdLine) {
            String[] parts = cmdLine.split(" ");
            if (parts.length >= 3) {
                String key = parts[1];
                String value = parts[2];
                long offset = parts.length > 3 ? Long.parseLong(parts[3]) : 0;

                dataStore.put(key, value);
                replicationOffset = offset;

                System.out.println("📥 [Slave] 执行命令: SET " + key + " = " + value);
                System.out.println("    Offset: " + replicationOffset);
            }
        }

        public String get(String key) {
            return dataStore.get(key);
        }

        public void stop() throws IOException {
            running = false;
            if (socket != null) {
                socket.close();
            }
        }

        public Map<String, String> getDataStore() {
            return new HashMap<>(dataStore);
        }
    }

    /**
     * 复制积压缓冲区（环形缓冲区）
     */
    static class ReplicationBacklog {
        private final int capacity;
        private final LinkedList<ReplicationCommand> buffer;
        private long minOffset;
        private long maxOffset;

        public ReplicationBacklog(int capacity) {
            this.capacity = capacity;
            this.buffer = new LinkedList<>();
            this.minOffset = 0;
            this.maxOffset = 0;
        }

        /**
         * 添加命令到缓冲区
         */
        public synchronized void append(ReplicationCommand cmd) {
            buffer.addLast(cmd);
            maxOffset += cmd.getSize();

            // 如果超过容量，删除最旧的命令
            while (getCurrentSize() > capacity && !buffer.isEmpty()) {
                ReplicationCommand removed = buffer.removeFirst();
                minOffset += removed.getSize();
            }
        }

        /**
         * 判断是否可以进行部分复制
         */
        public synchronized boolean canPartialResync(long slaveOffset) {
            return slaveOffset >= minOffset && slaveOffset <= maxOffset;
        }

        /**
         * 获取从指定偏移量开始的所有命令
         */
        public synchronized List<ReplicationCommand> getCommandsSince(long offset) {
            List<ReplicationCommand> result = new ArrayList<>();
            long currentOffset = minOffset;

            for (ReplicationCommand cmd : buffer) {
                if (currentOffset >= offset) {
                    result.add(cmd);
                }
                currentOffset += cmd.getSize();
            }

            return result;
        }

        private int getCurrentSize() {
            return buffer.stream().mapToInt(ReplicationCommand::getSize).sum();
        }
    }

    /**
     * 复制命令
     */
    static class ReplicationCommand {
        private final String command;
        private final List<String> args;
        private final long offset;

        public ReplicationCommand(String command, List<String> args, long offset) {
            this.command = command;
            this.args = args;
            this.offset = offset;
        }

        public String serialize() {
            StringBuilder sb = new StringBuilder(command);
            for (String arg : args) {
                sb.append(" ").append(arg);
            }
            sb.append(" ").append(offset);
            return sb.toString();
        }

        public int getSize() {
            return serialize().length();
        }

        public long getOffset() {
            return offset;
        }
    }

    /**
     * 从节点连接
     */
    static class RedisSlaveConnection {
        final Socket socket;
        final PrintWriter out;

        public RedisSlaveConnection(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
        }
    }

    // ==================== 演示和测试 ====================

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(70));
        System.out.println("          Redis 主从复制原理演示");
        System.out.println("=".repeat(70));

        // 1. 启动主节点
        System.out.println("\n【步骤1】启动主节点");
        RedisMaster master = new RedisMaster(6379);
        master.start();

        Thread.sleep(1000);

        // 2. 启动从节点1
        System.out.println("\n【步骤2】启动从节点1 - 全量复制");
        RedisSlave slave1 = new RedisSlave("localhost", 6379);
        slave1.connect();

        Thread.sleep(2000);

        // 3. 主节点写入数据
        System.out.println("\n【步骤3】主节点写入数据");
        master.set("name", "Redis");
        master.set("version", "7.0");
        master.set("type", "database");

        Thread.sleep(1000);

        // 4. 验证从节点数据
        System.out.println("\n【步骤4】验证从节点1数据");
        System.out.println("从节点数据: " + slave1.getDataStore());

        Thread.sleep(1000);

        // 5. 启动从节点2 - 测试全量复制
        System.out.println("\n【步骤5】启动从节点2 - 测试已有数据的全量复制");
        RedisSlave slave2 = new RedisSlave("localhost", 6379);
        slave2.connect();

        Thread.sleep(2000);

        // 6. 主节点继续写入
        System.out.println("\n【步骤6】主节点继续写入数据");
        master.set("author", "Salvatore");
        master.set("language", "C");

        Thread.sleep(1000);

        // 7. 验证所有从节点数据
        System.out.println("\n【步骤7】验证所有从节点数据");
        System.out.println("从节点1数据: " + slave1.getDataStore());
        System.out.println("从节点2数据: " + slave2.getDataStore());

        // 8. 显示统计信息
        System.out.println("\n【步骤8】统计信息");
        System.out.println("主节点 Replication ID: " + master.getReplicationId());
        System.out.println("主节点 Replication Offset: " + master.getReplicationOffset());
        System.out.println("主节点 Slave 数量: " + master.getSlaveCount());

        // 9. 清理
        System.out.println("\n【步骤9】关闭连接");
        slave1.stop();
        slave2.stop();
        master.stop();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("          演示完成");
        System.out.println("=".repeat(70));
    }
}
