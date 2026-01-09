package com.io.nio;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * NIO.2 (AIO) 异步I/O深度分析
 * 
 * 详细内容：
 * 1. AsynchronousFileChannel异步文件操作
 * 2. AsynchronousSocketChannel异步网络操作
 * 3. CompletionHandler回调机制
 * 4. Future模式异步处理
 * 5. 文件系统监控 (WatchService)
 * 6. I/O模型性能对比
 */

public class AIOAndAdvancedNIO {
    
    /**
     * 异步文件操作分析
     */
    
    static class AsynchronousFileOperations {
        
        /**
         * AsynchronousFileChannel详解
         */
        public static void demonstrateAsyncFileChannel() {
            System.out.println("=== Asynchronous File Channel Analysis ===");
            
            String fileName = "test_async.txt";
            String content = "Hello Asynchronous I/O!\nThis demonstrates AIO file operations.\n异步文件操作测试。";
            
            try {
                // 1. 异步写入 - Future模式
                System.out.println("\n--- Async Write with Future ---");
                
                Path filePath = Paths.get(fileName);
                AsynchronousFileChannel writeChannel = AsynchronousFileChannel.open(
                    filePath, 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.WRITE, 
                    StandardOpenOption.TRUNCATE_EXISTING
                );
                
                ByteBuffer writeBuffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
                Future<Integer> writeFuture = writeChannel.write(writeBuffer, 0);
                
                // 可以在这里做其他工作
                System.out.println("Write operation submitted, doing other work...");
                Thread.sleep(100); // 模拟其他工作
                
                // 获取写入结果
                Integer bytesWritten = writeFuture.get(5, TimeUnit.SECONDS);
                System.out.println("Bytes written: " + bytesWritten);
                writeChannel.close();
                
                // 2. 异步读取 - CompletionHandler模式
                System.out.println("\n--- Async Read with CompletionHandler ---");
                
                AsynchronousFileChannel readChannel = AsynchronousFileChannel.open(
                    filePath, StandardOpenOption.READ
                );
                
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                CountDownLatch latch = new CountDownLatch(1);
                
                readChannel.read(readBuffer, 0, null, new CompletionHandler<Integer, Object>() {
                    @Override
                    public void completed(Integer result, Object attachment) {
                        System.out.println("Read completed, bytes read: " + result);
                        
                        readBuffer.flip();
                        String readContent = StandardCharsets.UTF_8.decode(readBuffer).toString();
                        System.out.println("Content: " + readContent);
                        
                        try {
                            readChannel.close();
                        } catch (IOException e) {
                            System.err.println("Error closing channel: " + e.getMessage());
                        }
                        
                        latch.countDown();
                    }
                    
                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        System.err.println("Read failed: " + exc.getMessage());
                        latch.countDown();
                    }
                });
                
                System.out.println("Read operation submitted, waiting for completion...");
                latch.await(5, TimeUnit.SECONDS);
                
                // 3. 多个异步操作并发执行
                System.out.println("\n--- Concurrent Async Operations ---");
                
                ExecutorService executor = Executors.newFixedThreadPool(4);
                List<Future<String>> futures = new ArrayList<>();
                
                for (int i = 0; i < 4; i++) {
                    final int fileIndex = i;
                    Future<String> future = executor.submit(() -> {
                        String asyncFileName = "async_test_" + fileIndex + ".txt";
                        String asyncContent = "Async file " + fileIndex + " content";
                        
                        try {
                            Path asyncPath = Paths.get(asyncFileName);
                            
                            // 异步写入
                            AsynchronousFileChannel channel = AsynchronousFileChannel.open(
                                asyncPath,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE,
                                StandardOpenOption.TRUNCATE_EXISTING
                            );
                            
                            ByteBuffer buffer = ByteBuffer.wrap(asyncContent.getBytes(StandardCharsets.UTF_8));
                            Future<Integer> writeResult = channel.write(buffer, 0);
                            writeResult.get(); // 等待写入完成
                            channel.close();
                            
                            // 异步读取验证
                            channel = AsynchronousFileChannel.open(asyncPath, StandardOpenOption.READ);
                            ByteBuffer readBuf = ByteBuffer.allocate(1024);
                            Future<Integer> readResult = channel.read(readBuf, 0);
                            readResult.get(); // 等待读取完成
                            
                            readBuf.flip();
                            String result = StandardCharsets.UTF_8.decode(readBuf).toString();
                            channel.close();
                            
                            // 清理文件
                            Files.deleteIfExists(asyncPath);
                            
                            return "File " + fileIndex + ": " + result;
                            
                        } catch (Exception e) {
                            return "File " + fileIndex + " error: " + e.getMessage();
                        }
                    });
                    
                    futures.add(future);
                }
                
                // 收集所有结果
                for (Future<String> future : futures) {
                    try {
                        String result = future.get(10, TimeUnit.SECONDS);
                        System.out.println(result);
                    } catch (Exception e) {
                        System.err.println("Future error: " + e.getMessage());
                    }
                }
                
                executor.shutdown();
                
                // 清理主测试文件
                Files.deleteIfExists(filePath);
                
            } catch (Exception e) {
                System.err.println("Async file operation error: " + e.getMessage());
            }
        }
    }
    
    /**
     * 异步网络操作分析
     */
    
    static class AsynchronousNetworkOperations {
        
        /**
         * AsynchronousServerSocketChannel和AsynchronousSocketChannel演示
         */
        public static void demonstrateAsyncNetwork() {
            System.out.println("\n=== Asynchronous Network Operations ===");
            
            int port = 8082;
            CountDownLatch serverLatch = new CountDownLatch(1);
            CountDownLatch clientLatch = new CountDownLatch(1);
            
            // 异步服务器
            Thread serverThread = new Thread(() -> {
                try {
                    AsynchronousServerSocketChannel serverChannel = 
                        AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
                    
                    System.out.println("Async server started on port " + port);
                    serverLatch.countDown();
                    
                    // 接受连接
                    serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                        @Override
                        public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
                            System.out.println("Client connected");
                            
                            // 继续接受其他连接
                            serverChannel.accept(null, this);
                            
                            // 处理当前连接
                            handleClient(clientChannel);
                        }
                        
                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            System.err.println("Accept failed: " + exc.getMessage());
                        }
                    });
                    
                    // 保持服务器运行
                    Thread.sleep(10000);
                    serverChannel.close();
                    
                } catch (Exception e) {
                    System.err.println("Async server error: " + e.getMessage());
                }
            });
            
            serverThread.start();
            
            // 等待服务器启动
            try {
                serverLatch.await(5, TimeUnit.SECONDS);
                Thread.sleep(1000); // 额外等待时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 异步客户端
            Thread clientThread = new Thread(() -> {
                try {
                    AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();
                    
                    // 异步连接
                    Future<Void> connectFuture = clientChannel.connect(new InetSocketAddress("localhost", port));
                    connectFuture.get(5, TimeUnit.SECONDS);
                    
                    System.out.println("Client connected to server");
                    
                    // 发送消息
                    String[] messages = {"Hello Async Server", "How are you?", "Goodbye"};
                    
                    for (String message : messages) {
                        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes(StandardCharsets.UTF_8));
                        
                        // 异步写入
                        Future<Integer> writeFuture = clientChannel.write(buffer);
                        writeFuture.get(); // 等待写入完成
                        
                        // 异步读取响应
                        ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                        Future<Integer> readFuture = clientChannel.read(responseBuffer);
                        Integer bytesRead = readFuture.get(5, TimeUnit.SECONDS);
                        
                        if (bytesRead > 0) {
                            responseBuffer.flip();
                            String response = StandardCharsets.UTF_8.decode(responseBuffer).toString();
                            System.out.println("Client received: " + response.trim());
                        }
                        
                        Thread.sleep(500); // 短暂延迟
                    }
                    
                    clientChannel.close();
                    clientLatch.countDown();
                    
                } catch (Exception e) {
                    System.err.println("Async client error: " + e.getMessage());
                    clientLatch.countDown();
                }
            });
            
            clientThread.start();
            
            // 等待客户端完成
            try {
                clientLatch.await(15, TimeUnit.SECONDS);
                serverThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private static void handleClient(AsynchronousSocketChannel clientChannel) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            // 异步读取客户端数据
            clientChannel.read(buffer, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    if (result > 0) {
                        buffer.flip();
                        String message = StandardCharsets.UTF_8.decode(buffer).toString();
                        System.out.println("Server received: " + message.trim());
                        
                        // 发送响应
                        String response = "Echo: " + message;
                        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
                        
                        clientChannel.write(responseBuffer, null, new CompletionHandler<Integer, Object>() {
                            @Override
                            public void completed(Integer result, Object attachment) {
                                // 继续读取下一条消息
                                buffer.clear();
                                clientChannel.read(buffer, null, 
                                    CompletionHandler.class.cast(handleClient(clientChannel)));
                            }
                            
                            @Override
                            public void failed(Throwable exc, Object attachment) {
                                System.err.println("Write failed: " + exc.getMessage());
                                closeChannel(clientChannel);
                            }
                        });
                        
                    } else {
                        // 客户端断开连接
                        closeChannel(clientChannel);
                    }
                }
                
                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.err.println("Read failed: " + exc.getMessage());
                    closeChannel(clientChannel);
                }
            });
        }
        
        private static void closeChannel(AsynchronousSocketChannel channel) {
            try {
                if (channel.isOpen()) {
                    channel.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing channel: " + e.getMessage());
            }
        }
    }
    
    /**
     * 文件系统监控服务
     */
    
    static class FileSystemWatchService {
        
        /**
         * WatchService文件监控演示
         */
        public static void demonstrateWatchService() {
            System.out.println("\n=== File System Watch Service ===");
            
            try {
                Path watchDir = Paths.get("watch_test_dir");
                Files.createDirectories(watchDir);
                
                WatchService watchService = FileSystems.getDefault().newWatchService();
                
                // 注册监控事件
                watchDir.register(watchService, 
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
                
                System.out.println("Watching directory: " + watchDir.toAbsolutePath());
                
                // 启动监控线程
                Thread watchThread = new Thread(() -> {
                    try {
                        for (int i = 0; i < 10; i++) { // 限制监控次数
                            WatchKey key = watchService.poll(2, TimeUnit.SECONDS);
                            
                            if (key == null) {
                                continue; // 超时，继续监控
                            }
                            
                            for (WatchEvent<?> event : key.pollEvents()) {
                                WatchEvent.Kind<?> kind = event.kind();
                                
                                if (kind == StandardWatchEventKinds.OVERFLOW) {
                                    continue;
                                }
                                
                                @SuppressWarnings("unchecked")
                                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                                Path fileName = pathEvent.context();
                                
                                System.out.println("Event: " + kind.name() + " - File: " + fileName);
                            }
                            
                            boolean valid = key.reset();
                            if (!valid) {
                                break; // 监控目录不再有效
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Watch service interrupted");
                    }
                });
                
                watchThread.start();
                
                // 模拟文件操作
                Thread.sleep(1000); // 等待监控启动
                
                System.out.println("Performing file operations...");
                
                // 创建文件
                Path testFile1 = watchDir.resolve("test1.txt");
                Files.write(testFile1, "Hello Watch Service".getBytes(StandardCharsets.UTF_8));
                Thread.sleep(500);
                
                // 修改文件
                Files.write(testFile1, "Modified content".getBytes(StandardCharsets.UTF_8));
                Thread.sleep(500);
                
                // 创建另一个文件
                Path testFile2 = watchDir.resolve("test2.txt");
                Files.write(testFile2, "Another test file".getBytes(StandardCharsets.UTF_8));
                Thread.sleep(500);
                
                // 删除文件
                Files.delete(testFile1);
                Thread.sleep(500);
                
                Files.delete(testFile2);
                Thread.sleep(500);
                
                // 等待监控线程完成
                watchThread.join(5000);
                
                // 清理
                watchService.close();
                Files.deleteIfExists(watchDir);
                
            } catch (Exception e) {
                System.err.println("Watch service error: " + e.getMessage());
            }
        }
    }
    
    /**
     * I/O模型性能对比
     */
    
    static class IOPerformanceComparison {
        
        /**
         * 不同I/O模型的性能对比
         */
        public static void compareIOModels() {
            System.out.println("\n=== I/O Models Performance Comparison ===");
            
            String testContent = "Performance test content for I/O comparison.\n".repeat(1000);
            int fileCount = 100;
            
            // 1. 传统I/O性能测试
            System.out.println("\n--- Traditional I/O Performance ---");
            long bioTime = testTraditionalIO(testContent, fileCount);
            
            // 2. NIO性能测试
            System.out.println("\n--- NIO Performance ---");
            long nioTime = testNIO(testContent, fileCount);
            
            // 3. AIO性能测试
            System.out.println("\n--- AIO Performance ---");
            long aioTime = testAIO(testContent, fileCount);
            
            // 性能对比总结
            System.out.println("\n--- Performance Summary ---");
            System.out.println("Traditional I/O: " + bioTime + " ms");
            System.out.println("NIO: " + nioTime + " ms");
            System.out.println("AIO: " + aioTime + " ms");
            
            System.out.println("\nRelative Performance:");
            System.out.println("NIO vs BIO: " + (bioTime / (double) nioTime) + "x");
            System.out.println("AIO vs BIO: " + (bioTime / (double) aioTime) + "x");
            System.out.println("AIO vs NIO: " + (nioTime / (double) aioTime) + "x");
        }
        
        private static long testTraditionalIO(String content, int fileCount) {
            long startTime = System.currentTimeMillis();
            
            try {
                for (int i = 0; i < fileCount; i++) {
                    String fileName = "bio_test_" + i + ".txt";
                    
                    try (FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8);
                         BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                        
                        bufferedWriter.write(content);
                    }
                    
                    // 立即删除文件
                    Files.deleteIfExists(Paths.get(fileName));
                }
            } catch (IOException e) {
                System.err.println("Traditional I/O error: " + e.getMessage());
            }
            
            return System.currentTimeMillis() - startTime;
        }
        
        private static long testNIO(String content, int fileCount) {
            long startTime = System.currentTimeMillis();
            
            try {
                for (int i = 0; i < fileCount; i++) {
                    String fileName = "nio_test_" + i + ".txt";
                    Path filePath = Paths.get(fileName);
                    
                    try (FileChannel channel = FileChannel.open(filePath,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING)) {
                        
                        ByteBuffer buffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
                        channel.write(buffer);
                    }
                    
                    // 立即删除文件
                    Files.deleteIfExists(filePath);
                }
            } catch (IOException e) {
                System.err.println("NIO error: " + e.getMessage());
            }
            
            return System.currentTimeMillis() - startTime;
        }
        
        private static long testAIO(String content, int fileCount) {
            long startTime = System.currentTimeMillis();
            
            try {
                CountDownLatch latch = new CountDownLatch(fileCount);
                
                for (int i = 0; i < fileCount; i++) {
                    String fileName = "aio_test_" + i + ".txt";
                    Path filePath = Paths.get(fileName);
                    
                    AsynchronousFileChannel channel = AsynchronousFileChannel.open(filePath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                    
                    ByteBuffer buffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
                    
                    channel.write(buffer, 0, null, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            try {
                                channel.close();
                                Files.deleteIfExists(filePath);
                            } catch (IOException e) {
                                System.err.println("AIO cleanup error: " + e.getMessage());
                            }
                            latch.countDown();
                        }
                        
                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            try {
                                channel.close();
                            } catch (IOException e) {
                                System.err.println("AIO channel close error: " + e.getMessage());
                            }
                            latch.countDown();
                        }
                    });
                }
                
                // 等待所有操作完成
                latch.await(30, TimeUnit.SECONDS);
                
            } catch (Exception e) {
                System.err.println("AIO error: " + e.getMessage());
            }
            
            return System.currentTimeMillis() - startTime;
        }
    }
    
    public static void main(String[] args) {
        AsynchronousFileOperations.demonstrateAsyncFileChannel();
        AsynchronousNetworkOperations.demonstrateAsyncNetwork();
        FileSystemWatchService.demonstrateWatchService();
        IOPerformanceComparison.compareIOModels();
    }
}