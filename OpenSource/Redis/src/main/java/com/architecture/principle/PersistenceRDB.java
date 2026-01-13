package com.architecture.principle;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis RDB持久化机制模拟
 *
 * RDB持久化：将某个时间点的数据库快照保存到磁盘
 *
 * 触发方式：
 * 1. SAVE命令：阻塞Redis服务器进程，直到RDB文件创建完成
 * 2. BGSAVE命令：fork子进程来创建RDB文件，不阻塞主进程
 * 3. 自动触发：根据配置规则（如save 900 1）
 *
 * 优点：
 * - 适合大规模数据恢复
 * - 数据恢复速度快
 * - 对性能影响最小化
 *
 * 缺点：
 * - 可能丢失最后一次快照之后的数据
 * - fork子进程时，如果数据集较大，可能导致服务暂停
 */
public class PersistenceRDB {

    private Map<String, String> dataStore;
    private String rdbFilePath;
    private long lastSaveTime;

    public PersistenceRDB(String rdbFilePath) {
        this.dataStore = new HashMap<>();
        this.rdbFilePath = rdbFilePath;
        this.lastSaveTime = System.currentTimeMillis();
    }

    /**
     * 模拟SET命令
     */
    public void set(String key, String value) {
        dataStore.put(key, value);
    }

    /**
     * 模拟GET命令
     */
    public String get(String key) {
        return dataStore.get(key);
    }

    /**
     * SAVE命令 - 同步保存（阻塞）
     */
    public void save() throws IOException {
        System.out.println("[RDB] 开始SAVE操作，服务器阻塞中...");
        long startTime = System.currentTimeMillis();

        saveToFile();

        lastSaveTime = System.currentTimeMillis();
        System.out.println("[RDB] SAVE完成，耗时: " + (lastSaveTime - startTime) + "ms");
    }

    /**
     * BGSAVE命令 - 异步保存（非阻塞）
     */
    public void bgsave() {
        System.out.println("[RDB] 开始BGSAVE操作，fork子进程...");

        // 模拟fork子进程
        Thread bgThread = new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();

                // 子进程执行保存操作
                saveToFile();

                long endTime = System.currentTimeMillis();
                lastSaveTime = endTime;
                System.out.println("[RDB] BGSAVE完成，耗时: " + (endTime - startTime) + "ms");
            } catch (IOException e) {
                System.err.println("[RDB] BGSAVE失败: " + e.getMessage());
            }
        });

        bgThread.setName("redis-rdb-bgsave");
        bgThread.start();
        System.out.println("[RDB] 主进程继续处理客户端请求...");
    }

    /**
     * 自动保存 - 根据配置规则
     * 示例规则：save 900 1（900秒内至少1次修改）
     */
    public void autoSave(long seconds, int changes) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime >= TimeUnit.SECONDS.toMillis(seconds)) {
            if (dataStore.size() >= changes) {
                System.out.println("[RDB] 触发自动保存规则: " + seconds + "秒 " + changes + "次修改");
                bgsave();
            }
        }
    }

    /**
     * 保存到文件
     */
    private void saveToFile() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(rdbFilePath))) {

            // 写入魔数和版本信息
            oos.writeUTF("REDIS");
            oos.writeInt(1); // 版本号

            // 写入数据
            oos.writeObject(dataStore);

            // 写入校验和（简化实现）
            oos.writeLong(System.currentTimeMillis());

            oos.flush();
        }
    }

    /**
     * 从RDB文件加载数据
     */
    @SuppressWarnings("unchecked")
    public void loadFromFile() throws IOException, ClassNotFoundException {
        File file = new File(rdbFilePath);
        if (!file.exists()) {
            System.out.println("[RDB] RDB文件不存在，跳过加载");
            return;
        }

        System.out.println("[RDB] 开始加载RDB文件...");
        long startTime = System.currentTimeMillis();

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(rdbFilePath))) {

            // 读取魔数和版本
            String magic = ois.readUTF();
            int version = ois.readInt();

            if (!"REDIS".equals(magic)) {
                throw new IOException("无效的RDB文件格式");
            }

            System.out.println("[RDB] RDB文件版本: " + version);

            // 读取数据
            dataStore = (Map<String, String>) ois.readObject();

            // 读取校验和
            long checksum = ois.readLong();

            long endTime = System.currentTimeMillis();
            System.out.println("[RDB] 加载完成，耗时: " + (endTime - startTime) + "ms, " +
                    "数据量: " + dataStore.size());
        }
    }

    /**
     * 获取数据存储信息
     */
    public String getInfo() {
        return String.format("数据量: %d, 最后保存时间: %d",
                dataStore.size(), lastSaveTime);
    }
}
