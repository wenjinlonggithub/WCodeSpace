package com.architecture.jvm.runtime;

import java.io.*;
import java.lang.management.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * 直接内存深度演示
 *
 * 直接内存(Direct Memory)不是JVM运行时数据区的一部分，也不是Java虚拟机规范中定义的内存区域
 * 但这部分内存也被频繁使用，也可能导致OutOfMemoryError异常
 *
 * 特点：
 * 1. 在Java堆外分配内存
 * 2. 通过Native函数库直接分配
 * 3. 通过DirectByteBuffer对象操作
 * 4. 避免Java堆和Native堆之间的数据复制
 * 5. 适合IO操作，提高性能
 *
 * 应用场景：
 * - NIO操作
 * - 网络数据传输
 * - 文件读写
 * - 共享内存
 *
 * @author Architecture
 */
public class DirectMemoryDemo {

    private static final int BUFFER_SIZE = 1024 * 1024; // 1MB
    private static final String TEST_FILE = "direct_memory_test.txt";

    public static void main(String[] args) {
        System.out.println("=== 直接内存深度演示 ===\n");

        // 1. 直接内存基础
        introduceDirectMemory();

        // 2. DirectByteBuffer演示
        demonstrateDirectByteBuffer();

        // 3. 堆内存 vs 直接内存
        compareHeapVsDirectMemory();

        // 4. NIO中的直接内存应用
        demonstrateNIOWithDirectMemory();

        // 5. 直接内存的分配和回收
        demonstrateAllocationAndDeallocation();

        // 6. 直接内存监控
        monitorDirectMemory();

        // 7. 直接内存OOM
        // demonstrateDirectMemoryOOM(); // 注释掉，避免实际OOM

        // 8. 直接内存参数
        displayDirectMemoryParameters();

        // 清理测试文件
        cleanupTestFile();
    }

    /**
     * 1. 直接内存基础介绍
     */
    private static void introduceDirectMemory() {
        System.out.println("【1. 直接内存基础】\n");

        System.out.println("什么是直接内存？");
        System.out.println("  • 在Java堆外分配的内存");
        System.out.println("  • 通过JNI调用操作系统的malloc/free函数分配");
        System.out.println("  • 由DirectByteBuffer对象引用");
        System.out.println("  • 不受GC直接管理，但通过虚引用间接回收");

        System.out.println("\n为什么使用直接内存？");
        System.out.println("  优势:");
        System.out.println("    1. 避免Java堆和本地I/O之间的数据复制");
        System.out.println("    2. 提高I/O操作性能");
        System.out.println("    3. 不占用Java堆空间");
        System.out.println("    4. 适合大内存操作");
        System.out.println("  劣势:");
        System.out.println("    1. 分配和释放成本高");
        System.out.println("    2. 不受JVM内存管理，难以追踪");
        System.out.println("    3. 可能导致OOM (受-XX:MaxDirectMemorySize限制)");

        System.out.println("\n传统I/O vs NIO直接内存:");
        System.out.println("  传统I/O (使用堆内存):");
        System.out.println("    应用 → Java堆 → OS缓冲区 → 磁盘/网络");
        System.out.println("    ↑          ↑");
        System.out.println("    └─ 需要复制 ─┘");
        System.out.println();
        System.out.println("  NIO (使用直接内存):");
        System.out.println("    应用 → 直接内存(OS缓冲区) → 磁盘/网络");
        System.out.println("           (零拷贝)");
        System.out.println();
    }

    /**
     * 2. DirectByteBuffer演示
     */
    private static void demonstrateDirectByteBuffer() {
        System.out.println("【2. DirectByteBuffer演示】\n");

        System.out.println("创建DirectByteBuffer:");

        // 创建直接内存缓冲区
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        System.out.println("  ByteBuffer.allocateDirect(1024)");
        System.out.println("  • 分配1KB直接内存");
        System.out.println("  • 返回DirectByteBuffer对象");

        // 创建堆内存缓冲区对比
        ByteBuffer heapBuffer = ByteBuffer.allocate(1024);
        System.out.println("\n  ByteBuffer.allocate(1024)");
        System.out.println("  • 分配1KB堆内存");
        System.out.println("  • 返回HeapByteBuffer对象");

        // 检查是否是直接内存
        System.out.println("\n检查缓冲区类型:");
        System.out.println("  directBuffer.isDirect() = " + directBuffer.isDirect());
        System.out.println("  heapBuffer.isDirect() = " + heapBuffer.isDirect());

        // 写入数据
        System.out.println("\n写入数据:");
        directBuffer.put("Hello Direct Memory!".getBytes());
        System.out.println("  写入: \"Hello Direct Memory!\"");
        System.out.println("  position: " + directBuffer.position());
        System.out.println("  limit: " + directBuffer.limit());
        System.out.println("  capacity: " + directBuffer.capacity());

        // 读取数据
        directBuffer.flip(); // 切换到读模式
        byte[] data = new byte[directBuffer.remaining()];
        directBuffer.get(data);
        System.out.println("  读取: \"" + new String(data) + "\"");

        System.out.println("\nDirectByteBuffer的内存布局:");
        System.out.println("  Java堆中:");
        System.out.println("    └─ DirectByteBuffer对象 (引用)");
        System.out.println("  直接内存中:");
        System.out.println("    └─ 实际数据存储");
        System.out.println();
    }

    /**
     * 3. 堆内存 vs 直接内存性能对比
     */
    private static void compareHeapVsDirectMemory() {
        System.out.println("【3. 堆内存 vs 直接内存性能对比】\n");

        int iterations = 100000;
        int bufferSize = 1024;

        // 测试堆内存
        long heapTime = testBufferPerformance(false, iterations, bufferSize);

        // 测试直接内存
        long directTime = testBufferPerformance(true, iterations, bufferSize);

        System.out.println("\n性能对比结果:");
        System.out.println("  堆内存耗时  : " + heapTime + " ms");
        System.out.println("  直接内存耗时: " + directTime + " ms");
        System.out.println("  性能提升    : " +
            String.format("%.2f%%", (heapTime - directTime) * 100.0 / heapTime));

        System.out.println("\n分析:");
        System.out.println("  • 直接内存避免了堆内存和系统内存之间的拷贝");
        System.out.println("  • 大量I/O操作时，直接内存性能优势明显");
        System.out.println("  • 但直接内存分配和回收成本更高");
        System.out.println();
    }

    private static long testBufferPerformance(boolean direct, int iterations, int bufferSize) {
        System.out.println("测试 " + (direct ? "直接内存" : "堆内存") + "...");

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            ByteBuffer buffer = direct ?
                ByteBuffer.allocateDirect(bufferSize) :
                ByteBuffer.allocate(bufferSize);

            // 写入数据
            for (int j = 0; j < bufferSize / 4; j++) {
                buffer.putInt(j);
            }

            // 读取数据
            buffer.flip();
            while (buffer.hasRemaining()) {
                buffer.getInt();
            }

            buffer.clear();
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    /**
     * 4. NIO中的直接内存应用
     */
    private static void demonstrateNIOWithDirectMemory() {
        System.out.println("【4. NIO中的直接内存应用】\n");

        System.out.println("创建测试文件...");
        createTestFile();

        try {
            // 使用直接内存读取文件
            long directTime = readFileWithDirectBuffer();

            // 使用堆内存读取文件
            long heapTime = readFileWithHeapBuffer();

            System.out.println("\nNIO文件读取性能对比:");
            System.out.println("  直接内存: " + directTime + " ms");
            System.out.println("  堆内存  : " + heapTime + " ms");

            if (directTime < heapTime) {
                System.out.println("  直接内存快 " +
                    String.format("%.2f%%", (heapTime - directTime) * 100.0 / heapTime));
            }

        } catch (IOException e) {
            System.err.println("IO错误: " + e.getMessage());
        }
        System.out.println();
    }

    private static void createTestFile() {
        try (FileOutputStream fos = new FileOutputStream(TEST_FILE)) {
            byte[] data = new byte[BUFFER_SIZE * 10]; // 10MB
            Arrays.fill(data, (byte) 'A');
            fos.write(data);
        } catch (IOException e) {
            System.err.println("创建测试文件失败: " + e.getMessage());
        }
    }

    private static long readFileWithDirectBuffer() throws IOException {
        long startTime = System.currentTimeMillis();

        try (FileInputStream fis = new FileInputStream(TEST_FILE);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            while (channel.read(buffer) > 0) {
                buffer.flip();
                // 处理数据
                buffer.clear();
            }
        }

        return System.currentTimeMillis() - startTime;
    }

    private static long readFileWithHeapBuffer() throws IOException {
        long startTime = System.currentTimeMillis();

        try (FileInputStream fis = new FileInputStream(TEST_FILE);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (channel.read(buffer) > 0) {
                buffer.flip();
                // 处理数据
                buffer.clear();
            }
        }

        return System.currentTimeMillis() - startTime;
    }

    /**
     * 5. 直接内存的分配和回收
     */
    private static void demonstrateAllocationAndDeallocation() {
        System.out.println("【5. 直接内存的分配和回收】\n");

        System.out.println("直接内存分配:");
        System.out.println("  1. 调用ByteBuffer.allocateDirect()");
        System.out.println("  2. 创建DirectByteBuffer对象 (在堆中)");
        System.out.println("  3. 通过Unsafe.allocateMemory()分配本地内存");
        System.out.println("  4. 创建Cleaner对象关联DirectByteBuffer");

        System.out.println("\n直接内存回收:");
        System.out.println("  1. DirectByteBuffer对象被GC回收");
        System.out.println("  2. Cleaner的clean()方法被调用");
        System.out.println("  3. Unsafe.freeMemory()释放本地内存");

        System.out.println("\n演示：分配和回收");

        // 获取初始直接内存使用量
        long initialMemory = getDirectMemoryUsed();
        System.out.println("  初始直接内存: " + formatBytes(initialMemory));

        // 分配直接内存
        ByteBuffer buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024); // 10MB
        System.out.println("  分配10MB直接内存...");

        long afterAlloc = getDirectMemoryUsed();
        System.out.println("  分配后: " + formatBytes(afterAlloc));
        System.out.println("  增加: " + formatBytes(afterAlloc - initialMemory));

        // 清除引用
        buffer = null;
        System.out.println("\n  清除引用...");

        // 触发GC
        System.out.println("  触发GC...");
        System.gc();

        // 等待回收
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long afterGC = getDirectMemoryUsed();
        System.out.println("  GC后: " + formatBytes(afterGC));
        System.out.println("  释放: " + formatBytes(afterAlloc - afterGC));

        System.out.println("\n注意:");
        System.out.println("  • 直接内存不由GC直接管理");
        System.out.println("  • 但通过Cleaner机制，在DirectByteBuffer被回收时释放");
        System.out.println("  • 可能存在延迟，不会立即释放");
        System.out.println();
    }

    /**
     * 6. 直接内存监控
     */
    private static void monitorDirectMemory() {
        System.out.println("【6. 直接内存监控】\n");

        // 获取BufferPool信息
        List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);

        System.out.println("BufferPool统计:");
        for (BufferPoolMXBean pool : pools) {
            System.out.println("  " + pool.getName() + ":");
            System.out.println("    缓冲区数量: " + pool.getCount());
            System.out.println("    总容量    : " + formatBytes(pool.getTotalCapacity()));
            System.out.println("    已使用    : " + formatBytes(pool.getMemoryUsed()));
        }

        // 非堆内存统计
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();

        System.out.println("\n非堆内存统计 (包括直接内存、Metaspace等):");
        System.out.println("  已使用: " + formatBytes(nonHeapMemory.getUsed()));
        System.out.println("  已提交: " + formatBytes(nonHeapMemory.getCommitted()));
        if (nonHeapMemory.getMax() > 0) {
            System.out.println("  最大值: " + formatBytes(nonHeapMemory.getMax()));
        }

        System.out.println("\n监控直接内存的方法:");
        System.out.println("  1. JMX: BufferPoolMXBean");
        System.out.println("  2. jconsole: 查看非堆内存");
        System.out.println("  3. JProfiler/VisualVM: 直接内存监控");
        System.out.println("  4. -XX:NativeMemoryTracking=detail");
        System.out.println();
    }

    /**
     * 7. 直接内存OOM演示 (注释掉，仅供参考)
     * 运行参数: -XX:MaxDirectMemorySize=10m
     */
    @SuppressWarnings("unused")
    private static void demonstrateDirectMemoryOOM() {
        System.out.println("【7. 直接内存OOM演示】\n");
        System.out.println("运行参数: -XX:MaxDirectMemorySize=10m\n");

        List<ByteBuffer> buffers = new ArrayList<>();
        int count = 0;

        try {
            while (true) {
                // 每次分配1MB
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
                buffers.add(buffer);
                count++;

                if (count % 10 == 0) {
                    System.out.println("已分配 " + count + " MB直接内存");
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("\n捕获 OutOfMemoryError!");
            System.out.println("分配了 " + count + " MB后发生OOM");
            System.out.println("错误信息: " + e.getMessage());
        }
    }

    /**
     * 8. 直接内存参数
     */
    private static void displayDirectMemoryParameters() {
        System.out.println("【8. 直接内存JVM参数】\n");

        System.out.println("直接内存大小设置:");
        System.out.println("  -XX:MaxDirectMemorySize=<size>");
        System.out.println("    • 设置直接内存最大值");
        System.out.println("    • 默认: 等于-Xmx (最大堆内存)");
        System.out.println("    • 超过此值会抛出OOM");

        System.out.println("\n本地内存追踪:");
        System.out.println("  -XX:NativeMemoryTracking=[off|summary|detail]");
        System.out.println("    • off: 不追踪 (默认)");
        System.out.println("    • summary: 摘要信息");
        System.out.println("    • detail: 详细信息");
        System.out.println("  使用 jcmd <pid> VM.native_memory 查看");

        System.out.println("\n推荐配置:");
        System.out.println("  Web服务器 (大量NIO操作):");
        System.out.println("    -XX:MaxDirectMemorySize=512m");
        System.out.println("  大数据处理:");
        System.out.println("    -XX:MaxDirectMemorySize=1g");
        System.out.println("  一般应用:");
        System.out.println("    使用默认值即可");

        System.out.println("\n使用建议:");
        System.out.println("  1. 不要过度使用直接内存");
        System.out.println("  2. 确保正确释放DirectByteBuffer引用");
        System.out.println("  3. 监控直接内存使用情况");
        System.out.println("  4. 设置合适的MaxDirectMemorySize");
        System.out.println("  5. 大对象考虑使用直接内存");
        System.out.println();
    }

    /**
     * 获取直接内存使用量
     */
    private static long getDirectMemoryUsed() {
        List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (BufferPoolMXBean pool : pools) {
            if (pool.getName().equals("direct")) {
                return pool.getMemoryUsed();
            }
        }
        return 0;
    }

    /**
     * 清理测试文件
     */
    private static void cleanupTestFile() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 格式化字节数
     */
    private static String formatBytes(long bytes) {
        if (bytes < 0) return "N/A";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
