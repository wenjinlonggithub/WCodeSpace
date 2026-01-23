package com.architecture.nio.channel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.file.*;

/**
 * Java NIO Channel 核心组件示例
 *
 * Channel（通道）是NIO的核心组件之一，类似于传统I/O中的流（Stream）
 * 但有以下重要区别：
 * 1. Channel可以读也可以写，Stream通常是单向的（InputStream或OutputStream）
 * 2. Channel可以异步读写
 * 3. Channel总是基于Buffer进行读写
 *
 * 主要的Channel实现：
 * - FileChannel：文件I/O
 * - DatagramChannel：UDP网络I/O
 * - SocketChannel：TCP客户端网络I/O
 * - ServerSocketChannel：TCP服务器端网络I/O
 */
public class ChannelDemo {

    private static final String TEST_FILE = "test-channel.txt";
    private static final String COPY_FILE = "test-channel-copy.txt";

    /**
     * 演示FileChannel的基本读写操作
     */
    public static void demonstrateFileChannel() throws IOException {
        System.out.println("=== FileChannel 基本操作 ===");

        // 1. 写入文件
        try (RandomAccessFile file = new RandomAccessFile(TEST_FILE, "rw");
             FileChannel channel = file.getChannel()) {

            String data = "Java NIO Channel 示例数据\n";
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 将数据写入Buffer
            buffer.put(data.getBytes("UTF-8"));

            // 切换到读模式
            buffer.flip();

            // 从Buffer写入Channel
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            System.out.println("数据已写入文件: " + TEST_FILE);
            System.out.println("文件大小: " + channel.size() + " 字节");
        }

        // 2. 读取文件
        try (RandomAccessFile file = new RandomAccessFile(TEST_FILE, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 从Channel读取到Buffer
            int bytesRead = channel.read(buffer);
            System.out.println("读取字节数: " + bytesRead);

            // 切换到读模式
            buffer.flip();

            // 从Buffer中读取数据
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);

            System.out.println("读取的内容: " + new String(data, "UTF-8"));
        }
    }

    /**
     * 演示FileChannel的position操作
     */
    public static void demonstratePosition() throws IOException {
        System.out.println("\n=== FileChannel Position 操作 ===");

        try (RandomAccessFile file = new RandomAccessFile(TEST_FILE, "rw");
             FileChannel channel = file.getChannel()) {

            // 获取当前位置
            long position = channel.position();
            System.out.println("当前位置: " + position);

            // 设置位置到文件末尾
            channel.position(channel.size());
            System.out.println("移动到文件末尾: " + channel.position());

            // 追加数据
            String appendData = "追加的数据\n";
            ByteBuffer buffer = ByteBuffer.wrap(appendData.getBytes("UTF-8"));
            channel.write(buffer);

            // 重置到开始位置
            channel.position(0);
            System.out.println("重置到开始: " + channel.position());
        }
    }

    /**
     * 演示FileChannel的transferTo和transferFrom
     * 这是高效的零拷贝文件传输方法
     */
    public static void demonstrateTransfer() throws IOException {
        System.out.println("\n=== FileChannel Transfer 操作（零拷贝）===");

        // 使用transferTo复制文件
        try (RandomAccessFile sourceFile = new RandomAccessFile(TEST_FILE, "r");
             FileChannel sourceChannel = sourceFile.getChannel();
             RandomAccessFile destFile = new RandomAccessFile(COPY_FILE, "rw");
             FileChannel destChannel = destFile.getChannel()) {

            long startTime = System.nanoTime();

            // transferTo: 从源通道传输到目标通道
            // 这个方法使用操作系统的零拷贝优化，直接在内核空间完成传输
            long bytesTransferred = sourceChannel.transferTo(0, sourceChannel.size(), destChannel);

            long endTime = System.nanoTime();

            System.out.println("复制字节数: " + bytesTransferred);
            System.out.println("耗时: " + (endTime - startTime) / 1000 + " 微秒");
            System.out.println("零拷贝技术避免了数据在用户空间和内核空间之间的拷贝");
        }

        // 验证复制结果
        try (RandomAccessFile file = new RandomAccessFile(COPY_FILE, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();

            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            System.out.println("复制文件内容: " + new String(data, "UTF-8"));
        }
    }

    /**
     * 演示FileChannel的scatter和gather操作
     * Scatter: 分散读取 - 从一个Channel读取数据到多个Buffer
     * Gather: 聚集写入 - 将多个Buffer的数据写入一个Channel
     */
    public static void demonstrateScatterGather() throws IOException {
        System.out.println("\n=== Scatter/Gather 操作 ===");

        // Gather Write - 聚集写入
        try (RandomAccessFile file = new RandomAccessFile("scatter-gather.txt", "rw");
             FileChannel channel = file.getChannel()) {

            // 准备多个Buffer
            ByteBuffer header = ByteBuffer.allocate(128);
            ByteBuffer body = ByteBuffer.allocate(1024);

            header.put("Header: NIO Scatter/Gather\n".getBytes("UTF-8"));
            body.put("Body: 这是消息体内容\n".getBytes("UTF-8"));

            header.flip();
            body.flip();

            // 聚集写入：将多个Buffer的数据一次性写入Channel
            ByteBuffer[] buffers = {header, body};
            long bytesWritten = channel.write(buffers);
            System.out.println("Gather写入字节数: " + bytesWritten);
        }

        // Scatter Read - 分散读取
        try (RandomAccessFile file = new RandomAccessFile("scatter-gather.txt", "r");
             FileChannel channel = file.getChannel()) {

            // 准备多个Buffer
            ByteBuffer header = ByteBuffer.allocate(128);
            ByteBuffer body = ByteBuffer.allocate(1024);

            // 分散读取：从Channel读取数据到多个Buffer
            ByteBuffer[] buffers = {header, body};
            long bytesRead = channel.read(buffers);
            System.out.println("Scatter读取字节数: " + bytesRead);

            header.flip();
            body.flip();

            System.out.println("Header Buffer: " + new String(header.array(), 0, header.limit(), "UTF-8").trim());
            System.out.println("Body Buffer: " + new String(body.array(), 0, body.limit(), "UTF-8").trim());
        }
    }

    /**
     * 演示FileChannel的FileLock
     */
    public static void demonstrateFileLock() throws IOException {
        System.out.println("\n=== FileChannel FileLock 操作 ===");

        try (RandomAccessFile file = new RandomAccessFile(TEST_FILE, "rw");
             FileChannel channel = file.getChannel()) {

            // 获取文件锁（阻塞式）
            // 参数：position, size, shared
            // shared=true: 共享锁（读锁）
            // shared=false: 独占锁（写锁）
            System.out.println("尝试获取文件锁...");
            FileLock lock = channel.lock();

            System.out.println("获取到文件锁");
            System.out.println("  是否共享锁: " + lock.isShared());
            System.out.println("  是否有效: " + lock.isValid());

            // 在持有锁期间进行操作
            ByteBuffer buffer = ByteBuffer.wrap("锁定期间写入的数据\n".getBytes("UTF-8"));
            channel.write(buffer);

            // 释放锁
            lock.release();
            System.out.println("文件锁已释放");

            // 尝试获取非阻塞锁
            FileLock tryLock = channel.tryLock();
            if (tryLock != null) {
                System.out.println("tryLock成功获取锁");
                tryLock.release();
            } else {
                System.out.println("tryLock未能获取锁");
            }
        }
    }

    /**
     * 演示内存映射文件（MappedByteBuffer）
     * 这是一种高效的文件I/O方式
     */
    public static void demonstrateMappedByteBuffer() throws IOException {
        System.out.println("\n=== 内存映射文件 MappedByteBuffer ===");

        try (RandomAccessFile file = new RandomAccessFile("mapped-file.txt", "rw");
             FileChannel channel = file.getChannel()) {

            // 创建内存映射文件
            // 将文件的一部分或全部映射到内存中
            MappedByteBuffer mappedBuffer = channel.map(
                FileChannel.MapMode.READ_WRITE,  // 映射模式
                0,                                // 起始位置
                1024                              // 映射大小
            );

            System.out.println("创建内存映射缓冲区，大小: 1024 字节");

            // 直接操作映射的内存
            mappedBuffer.put("Memory Mapped File - 内存映射文件\n".getBytes("UTF-8"));
            mappedBuffer.put("这种方式避免了系统调用，提高了I/O性能\n".getBytes("UTF-8"));

            // 强制刷新到磁盘
            mappedBuffer.force();
            System.out.println("数据已通过内存映射写入文件");

            System.out.println("\n内存映射文件的优点：");
            System.out.println("1. 文件内容直接映射到进程地址空间");
            System.out.println("2. 减少了数据拷贝次数");
            System.out.println("3. 适合大文件的随机访问");
            System.out.println("4. 多个进程可以共享同一个映射");
        }
    }

    /**
     * 清理测试文件
     */
    public static void cleanup() {
        try {
            Files.deleteIfExists(Paths.get(TEST_FILE));
            Files.deleteIfExists(Paths.get(COPY_FILE));
            Files.deleteIfExists(Paths.get("scatter-gather.txt"));
            Files.deleteIfExists(Paths.get("mapped-file.txt"));
            System.out.println("\n测试文件已清理");
        } catch (IOException e) {
            System.err.println("清理文件失败: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            demonstrateFileChannel();
            demonstratePosition();
            demonstrateTransfer();
            demonstrateScatterGather();
            demonstrateFileLock();
            demonstrateMappedByteBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
}
