package com.architecture.jdk;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.management.*;
import java.net.http.*;
import java.net.URI;

/**
 * JDK高级特性和性能优化
 * 
 * 涵盖内容：
 * 1. NIO.2文件操作 (JDK 7+)
 * 2. HTTP Client API (JDK 11+)
 * 3. JVM监控和调优
 * 4. 垃圾回收器选择
 * 5. 内存分析和优化
 * 6. 性能测试和基准测试
 */

public class JDKAdvancedFeatures {
    
    /**
     * NIO.2文件操作详解
     * 
     * 核心类：
     * 1. Path - 文件路径抽象
     * 2. Files - 文件操作工具类
     * 3. FileSystem - 文件系统抽象
     * 4. WatchService - 文件监控服务
     */
    public static void demonstrateNIOFileOperations() {
        System.out.println("=== NIO.2 File Operations Demo ===");
        
        try {
            // 1. Path操作
            System.out.println("\n--- Path Operations ---");
            Path currentDir = Paths.get(".");
            Path absolutePath = currentDir.toAbsolutePath();
            Path normalizedPath = absolutePath.normalize();
            
            System.out.println("Current directory: " + currentDir);
            System.out.println("Absolute path: " + absolutePath);
            System.out.println("Normalized path: " + normalizedPath);
            
            // 2. 文件创建和写入
            System.out.println("\n--- File Creation and Writing ---");
            Path tempFile = Files.createTempFile("demo", ".txt");
            System.out.println("Created temp file: " + tempFile);
            
            List<String> lines = Arrays.asList(
                "Line 1: Hello NIO.2",
                "Line 2: File operations",
                "Line 3: Java advanced features"
            );
            
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            System.out.println("Written " + lines.size() + " lines to file");
            
            // 3. 文件读取
            System.out.println("\n--- File Reading ---");
            List<String> readLines = Files.readAllLines(tempFile, StandardCharsets.UTF_8);
            System.out.println("Read lines:");
            readLines.forEach(line -> System.out.println("  " + line));
            
            // 4. 文件属性
            System.out.println("\n--- File Attributes ---");
            System.out.println("File size: " + Files.size(tempFile) + " bytes");
            System.out.println("Is regular file: " + Files.isRegularFile(tempFile));
            System.out.println("Is readable: " + Files.isReadable(tempFile));
            System.out.println("Is writable: " + Files.isWritable(tempFile));
            System.out.println("Last modified: " + Files.getLastModifiedTime(tempFile));
            
            // 5. 目录遍历
            System.out.println("\n--- Directory Walking ---");
            Path currentPath = Paths.get(".");
            try (Stream<Path> paths = Files.walk(currentPath, 2)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.toString().endsWith(".java"))
                     .limit(5)
                     .forEach(path -> System.out.println("Java file: " + path));
            }
            
            // 6. 文件复制和移动
            System.out.println("\n--- File Copy and Move ---");
            Path copyTarget = Files.createTempFile("copy", ".txt");
            Files.copy(tempFile, copyTarget, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied file to: " + copyTarget);
            
            // 清理临时文件
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(copyTarget);
            System.out.println("Cleaned up temporary files");
            
        } catch (IOException e) {
            System.err.println("File operation error: " + e.getMessage());
        }
    }
    
    /**
     * HTTP Client API详解 (JDK 11+)
     * 
     * 特性：
     * 1. 同步和异步请求
     * 2. HTTP/2支持
     * 3. WebSocket支持
     * 4. 响应处理
     */
    public static void demonstrateHTTPClient() {
        System.out.println("\n=== HTTP Client Demo (JDK 11+) ===");
        
        try {
            // 检查JDK版本
            String javaVersion = System.getProperty("java.version");
            System.out.println("Java version: " + javaVersion);
            
            // 由于HTTP Client是JDK 11+的特性，这里提供示例代码
            System.out.println("\n--- HTTP Client Example Code ---");
            System.out.println("""
                // 1. 创建HTTP客户端
                HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();
                
                // 2. 创建请求
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/octocat"))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
                
                // 3. 同步发送请求
                HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                System.out.println("Status code: " + response.statusCode());
                System.out.println("Response body: " + response.body());
                
                // 4. 异步发送请求
                CompletableFuture<HttpResponse<String>> asyncResponse = 
                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                
                asyncResponse.thenApply(HttpResponse::body)
                           .thenAccept(System.out::println)
                           .join();
                """);
            
        } catch (Exception e) {
            System.err.println("HTTP Client demo error: " + e.getMessage());
        }
    }
    
    /**
     * JVM监控和性能分析
     * 
     * 工具：
     * 1. ManagementFactory - JVM管理接口
     * 2. MemoryMXBean - 内存监控
     * 3. GarbageCollectorMXBean - GC监控
     * 4. ThreadMXBean - 线程监控
     */
    public static void demonstrateJVMMonitoring() {
        System.out.println("\n=== JVM Monitoring Demo ===");
        
        // 1. 运行时信息
        System.out.println("\n--- Runtime Information ---");
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        System.out.println("JVM Name: " + runtimeBean.getVmName());
        System.out.println("JVM Version: " + runtimeBean.getVmVersion());
        System.out.println("JVM Vendor: " + runtimeBean.getVmVendor());
        System.out.println("Uptime: " + runtimeBean.getUptime() + " ms");
        System.out.println("Start Time: " + new Date(runtimeBean.getStartTime()));
        
        // 2. 内存信息
        System.out.println("\n--- Memory Information ---");
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        System.out.println("Heap Memory:");
        printMemoryUsage(heapUsage);
        System.out.println("Non-Heap Memory:");
        printMemoryUsage(nonHeapUsage);
        
        // 3. 垃圾回收信息
        System.out.println("\n--- Garbage Collection Information ---");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("GC Name: " + gcBean.getName());
            System.out.println("  Collection Count: " + gcBean.getCollectionCount());
            System.out.println("  Collection Time: " + gcBean.getCollectionTime() + " ms");
            System.out.println("  Memory Pool Names: " + Arrays.toString(gcBean.getMemoryPoolNames()));
        }
        
        // 4. 线程信息
        System.out.println("\n--- Thread Information ---");
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        System.out.println("Current Thread Count: " + threadBean.getThreadCount());
        System.out.println("Peak Thread Count: " + threadBean.getPeakThreadCount());
        System.out.println("Total Started Thread Count: " + threadBean.getTotalStartedThreadCount());
        System.out.println("Daemon Thread Count: " + threadBean.getDaemonThreadCount());
        
        // 5. 内存池信息
        System.out.println("\n--- Memory Pool Information ---");
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : memoryPools) {
            System.out.println("Pool Name: " + pool.getName());
            System.out.println("  Type: " + pool.getType());
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                System.out.println("  Usage: " + formatBytes(usage.getUsed()) + 
                    " / " + formatBytes(usage.getMax()));
            }
        }
        
        // 6. 系统属性
        System.out.println("\n--- System Properties ---");
        Properties props = System.getProperties();
        String[] importantProps = {
            "java.version", "java.vendor", "java.home",
            "os.name", "os.version", "os.arch",
            "user.name", "user.home", "user.dir"
        };
        
        for (String prop : importantProps) {
            System.out.println(prop + ": " + props.getProperty(prop));
        }
    }
    
    private static void printMemoryUsage(MemoryUsage usage) {
        System.out.println("  Init: " + formatBytes(usage.getInit()));
        System.out.println("  Used: " + formatBytes(usage.getUsed()));
        System.out.println("  Committed: " + formatBytes(usage.getCommitted()));
        System.out.println("  Max: " + formatBytes(usage.getMax()));
    }
    
    private static String formatBytes(long bytes) {
        if (bytes < 0) return "N/A";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    /**
     * 性能测试和基准测试
     * 
     * 内容：
     * 1. 微基准测试
     * 2. 内存分配测试
     * 3. 并发性能测试
     * 4. 算法性能对比
     */
    public static void demonstratePerformanceTesting() {
        System.out.println("\n=== Performance Testing Demo ===");
        
        // 1. 字符串拼接性能对比
        System.out.println("\n--- String Concatenation Performance ---");
        int iterations = 10000;
        
        // String + 操作
        long startTime = System.nanoTime();
        String result1 = "";
        for (int i = 0; i < iterations; i++) {
            result1 += "a";
        }
        long stringConcatTime = System.nanoTime() - startTime;
        
        // StringBuilder
        startTime = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sb.append("a");
        }
        String result2 = sb.toString();
        long stringBuilderTime = System.nanoTime() - startTime;
        
        System.out.println("String concatenation: " + stringConcatTime / 1_000_000.0 + " ms");
        System.out.println("StringBuilder: " + stringBuilderTime / 1_000_000.0 + " ms");
        System.out.println("StringBuilder is " + (stringConcatTime / (double) stringBuilderTime) + "x faster");
        
        // 2. 集合性能对比
        System.out.println("\n--- Collection Performance ---");
        testCollectionPerformance();
        
        // 3. 并发性能测试
        System.out.println("\n--- Concurrent Performance ---");
        testConcurrentPerformance();
        
        // 4. 内存分配测试
        System.out.println("\n--- Memory Allocation Test ---");
        testMemoryAllocation();
    }
    
    private static void testCollectionPerformance() {
        int size = 100000;
        
        // ArrayList vs LinkedList 随机访问
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        
        // 填充数据
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
        
        // 随机访问测试
        Random random = new Random();
        int accessCount = 10000;
        
        long startTime = System.nanoTime();
        for (int i = 0; i < accessCount; i++) {
            int index = random.nextInt(size);
            arrayList.get(index);
        }
        long arrayListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < accessCount; i++) {
            int index = random.nextInt(size);
            linkedList.get(index);
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        System.out.println("ArrayList random access: " + arrayListTime / 1_000_000.0 + " ms");
        System.out.println("LinkedList random access: " + linkedListTime / 1_000_000.0 + " ms");
        System.out.println("ArrayList is " + (linkedListTime / (double) arrayListTime) + "x faster for random access");
    }
    
    private static void testConcurrentPerformance() {
        int threadCount = 4;
        int operationsPerThread = 100000;
        
        // 测试ConcurrentHashMap vs synchronized HashMap
        Map<Integer, String> concurrentMap = new ConcurrentHashMap<>();
        Map<Integer, String> syncMap = Collections.synchronizedMap(new HashMap<>());
        
        // ConcurrentHashMap测试
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                for (int i = 0; i < operationsPerThread; i++) {
                    concurrentMap.put(threadId * operationsPerThread + i, "value" + i);
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long concurrentMapTime = System.currentTimeMillis() - startTime;
        
        // Synchronized HashMap测试
        startTime = System.currentTimeMillis();
        executor = Executors.newFixedThreadPool(threadCount);
        
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                for (int i = 0; i < operationsPerThread; i++) {
                    syncMap.put(threadId * operationsPerThread + i, "value" + i);
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long syncMapTime = System.currentTimeMillis() - startTime;
        
        System.out.println("ConcurrentHashMap: " + concurrentMapTime + " ms");
        System.out.println("Synchronized HashMap: " + syncMapTime + " ms");
        System.out.println("ConcurrentHashMap is " + (syncMapTime / (double) concurrentMapTime) + "x faster");
    }
    
    private static void testMemoryAllocation() {
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存状态
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Initial memory usage: " + formatBytes(initialMemory));
        
        // 分配大量对象
        List<String> objects = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            objects.add("Object " + i);
        }
        
        long afterAllocation = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("After allocation: " + formatBytes(afterAllocation));
        System.out.println("Memory increase: " + formatBytes(afterAllocation - initialMemory));
        
        // 清理对象
        objects.clear();
        System.gc(); // 建议垃圾回收
        
        try {
            Thread.sleep(1000); // 等待GC完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long afterGC = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("After GC: " + formatBytes(afterGC));
        System.out.println("Memory freed: " + formatBytes(afterAllocation - afterGC));
    }
    
    public static void main(String[] args) {
        demonstrateNIOFileOperations();
        demonstrateHTTPClient();
        demonstrateJVMMonitoring();
        demonstratePerformanceTesting();
    }
}