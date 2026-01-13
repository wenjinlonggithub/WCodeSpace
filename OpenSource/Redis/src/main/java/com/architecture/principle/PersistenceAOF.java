package com.architecture.principle;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis AOF持久化机制模拟
 *
 * AOF（Append Only File）：以日志形式记录每个写操作
 *
 * 同步策略：
 * 1. always：每个写命令都同步写入磁盘（最安全，性能最差）
 * 2. everysec：每秒同步一次（折中方案，默认）
 * 3. no：由操作系统决定何时同步（性能最好，安全性最差）
 *
 * AOF重写：
 * - 当AOF文件过大时，可以重写来压缩文件大小
 * - 不是读取旧AOF文件，而是直接从内存中读取数据生成新AOF
 *
 * 优点：
 * - 数据安全性高
 * - AOF文件是纯文本，易于理解和处理
 * - 可以处理误操作（通过编辑AOF文件）
 *
 * 缺点：
 * - AOF文件通常比RDB文件大
 * - 恢复速度比RDB慢
 * - 可能存在bug导致数据不一致
 */
public class PersistenceAOF {

    private Map<String, String> dataStore;
    private String aofFilePath;
    private BufferedWriter aofWriter;
    private SyncPolicy syncPolicy;
    private List<String> commandBuffer;
    private long lastSyncTime;

    public enum SyncPolicy {
        ALWAYS,     // 每次写命令都同步
        EVERYSEC,   // 每秒同步
        NO          // 不主动同步
    }

    public PersistenceAOF(String aofFilePath, SyncPolicy syncPolicy) throws IOException {
        this.dataStore = new HashMap<>();
        this.aofFilePath = aofFilePath;
        this.syncPolicy = syncPolicy;
        this.commandBuffer = new ArrayList<>();
        this.lastSyncTime = System.currentTimeMillis();

        File file = new File(aofFilePath);
        this.aofWriter = new BufferedWriter(new FileWriter(file, true));
    }

    /**
     * 模拟SET命令
     */
    public void set(String key, String value) throws IOException {
        dataStore.put(key, value);

        // 写入AOF
        String command = String.format("SET %s %s", key, value);
        appendCommand(command);
    }

    /**
     * 模拟DEL命令
     */
    public void del(String key) throws IOException {
        dataStore.remove(key);

        String command = String.format("DEL %s", key);
        appendCommand(command);
    }

    /**
     * 追加命令到AOF
     */
    private void appendCommand(String command) throws IOException {
        commandBuffer.add(command);

        // 根据同步策略决定何时写入磁盘
        switch (syncPolicy) {
            case ALWAYS:
                flushToDisk();
                break;
            case EVERYSEC:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSyncTime >= 1000) {
                    flushToDisk();
                    lastSyncTime = currentTime;
                }
                break;
            case NO:
                // 不主动同步，由操作系统决定
                break;
        }
    }

    /**
     * 刷新到磁盘
     */
    private void flushToDisk() throws IOException {
        for (String command : commandBuffer) {
            aofWriter.write(command);
            aofWriter.newLine();
        }
        aofWriter.flush();
        commandBuffer.clear();
        System.out.println("[AOF] 已同步到磁盘，策略: " + syncPolicy);
    }

    /**
     * AOF重写
     * 从内存中的数据集生成新的AOF文件
     */
    public void rewrite() throws IOException {
        System.out.println("[AOF] 开始AOF重写...");
        long startTime = System.currentTimeMillis();

        String tempFile = aofFilePath + ".temp";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            // 遍历内存中的数据，生成SET命令
            for (Map.Entry<String, String> entry : dataStore.entrySet()) {
                String command = String.format("SET %s %s", entry.getKey(), entry.getValue());
                writer.write(command);
                writer.newLine();
            }
            writer.flush();
        }

        // 替换旧AOF文件
        File oldFile = new File(aofFilePath);
        File newFile = new File(tempFile);

        aofWriter.close();
        oldFile.delete();
        newFile.renameTo(oldFile);

        // 重新打开AOF文件
        aofWriter = new BufferedWriter(new FileWriter(aofFilePath, true));

        long endTime = System.currentTimeMillis();
        System.out.println("[AOF] 重写完成，耗时: " + (endTime - startTime) + "ms");
    }

    /**
     * 从AOF文件加载数据
     */
    public void loadFromFile() throws IOException {
        File file = new File(aofFilePath);
        if (!file.exists()) {
            System.out.println("[AOF] AOF文件不存在，跳过加载");
            return;
        }

        System.out.println("[AOF] 开始加载AOF文件...");
        long startTime = System.currentTimeMillis();
        int commandCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(aofFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                executeCommand(line);
                commandCount++;
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("[AOF] 加载完成，耗时: " + (endTime - startTime) + "ms, " +
                "执行命令数: " + commandCount);
    }

    /**
     * 执行命令
     */
    private void executeCommand(String command) {
        String[] parts = command.split(" ", 3);
        if (parts.length < 2) {
            return;
        }

        String cmd = parts[0];
        String key = parts[1];

        switch (cmd) {
            case "SET":
                if (parts.length >= 3) {
                    dataStore.put(key, parts[2]);
                }
                break;
            case "DEL":
                dataStore.remove(key);
                break;
        }
    }

    /**
     * 关闭AOF
     */
    public void close() throws IOException {
        if (!commandBuffer.isEmpty()) {
            flushToDisk();
        }
        if (aofWriter != null) {
            aofWriter.close();
        }
    }

    /**
     * 获取数据
     */
    public String get(String key) {
        return dataStore.get(key);
    }

    /**
     * 获取信息
     */
    public String getInfo() throws IOException {
        File file = new File(aofFilePath);
        return String.format("数据量: %d, AOF文件大小: %d bytes, 同步策略: %s",
                dataStore.size(), file.length(), syncPolicy);
    }
}
