package com.io.nio;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Java I/O和NIO深度分析
 * 
 * 详细内容：
 * 1. 传统I/O (BIO) 详解
 * 2. NIO核心组件分析
 * 3. NIO.2 (AIO) 异步I/O
 * 4. 文件操作对比分析
 * 5. 网络编程模型对比
 * 6. 性能测试和最佳实践
 */

public class IOAndNIOAnalysis {
    
    /**
     * 传统I/O (BIO) 详解
     * 
     * 特点：
     * 1. 阻塞式I/O
     * 2. 面向流的操作
     * 3. 一个线程处理一个连接
     * 4. 简单易用但性能有限
     */
    
    static class TraditionalIOAnalysis {
        
        /**
         * 文件I/O操作演示
         */
        public static void demonstrateFileIO() {
            System.out.println("=== Traditional File I/O Analysis ===");
            
            String fileName = "test_bio.txt";
            String content = "Hello, Traditional I/O!\nThis is a test file.\n中文测试内容。";
            
            try {
                // 1. 字节流写入
                System.out.println("\n--- Byte Stream Operations ---");
                
                long startTime = System.nanoTime();
                try (FileOutputStream fos = new FileOutputStream(fileName);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    
                    bos.write(content.getBytes(StandardCharsets.UTF_8));
                    bos.flush();
                }
                long writeTime = System.nanoTime() - startTime;
                
                // 字节流读取
                startTime = System.nanoTime();
                try (FileInputStream fis = new FileInputStream(fileName);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    StringBuilder result = new StringBuilder();
                    
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        result.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                    }
                    
                    System.out.println("Read content: " + result.toString().trim());
                }
                long readTime = System.nanoTime() - startTime;
                
                System.out.println("Byte stream write time: " + writeTime / 1_000_000.0 + " ms");
                System.out.println("Byte stream read time: " + readTime / 1_000_000.0 + " ms");
                
                // 2. 字符流操作
                System.out.println("\n--- Character Stream Operations ---");
                
                String charFileName = "test_char.txt";
                
                startTime = System.nanoTime();
                try (FileWriter fw = new FileWriter(charFileName, StandardCharsets.UTF_8);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    
                    bw.write(content);
                    bw.flush();
                }
                long charWriteTime = System.nanoTime() - startTime;
                
                startTime = System.nanoTime();
                try (FileReader fr = new FileReader(charFileName, StandardCharsets.UTF_8);
                     BufferedReader br = new BufferedReader(fr)) {
                    
                    String line;
                    StringBuilder result = new StringBuilder();
                    
                    while ((line = br.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    
                    System.out.println("Read content: " + result.toString().trim());
                }
                long charReadTime = System.nanoTime() - startTime;
                
                System.out.println("Character stream write time: " + charWriteTime / 1_000_000.0 + " ms");
                System.out.println("Character stream read time: " + charReadTime / 1_000_000.0 + " ms");
                
                // 3. 随机访问文件
                System.out.println("\n--- Random Access File ---");
                
                String rafFileName = "test_raf.txt";
                
                try (RandomAccessFile raf = new RandomAccessFile(rafFileName, "rw")) {
                    // 写入数据
                    raf.writeUTF("Hello");
                    raf.writeInt(42);
                    raf.writeDouble(3.14159);
                    raf.writeUTF("World");
                    
                    // 随机读取
                    raf.seek(0); // 回到文件开头
                    System.out.println("First string: " + raf.readUTF());
                    System.out.println("Integer: " + raf.readInt());
                    System.out.println("Double: " + raf.readDouble());
                    System.out.println("Last string: " + raf.readUTF());
                    
                    // 修改中间的整数
                    raf.seek(raf.readUTF().length() + 2); // 跳过第一个字符串
                    raf.writeInt(100);
                    
                    // 验证修改
                    raf.seek(raf.readUTF().length() + 2);
                    System.out.println("Modified integer: " + raf.readInt());
                }
                
                // 清理文件
                new File(fileName).delete();
                new File(charFileName).delete();
                new File(rafFileName).delete();
                
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
            }
        }
        
        /**
         * 网络I/O演示
         */
        public static void demonstrateNetworkIO() {
            System.out.println("\n--- Traditional Network I/O ---");
            
            // 简单的Echo服务器和客户端
            int port = 8080;
            
            // 启动服务器线程
            Thread serverThread = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    System.out.println("Echo server started on port " + port);
                    
                    // 只处理一个连接作为演示
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(
                             new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter out = new PrintWriter(
                             clientSocket.getOutputStream(), true)) {
                        
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            System.out.println("Server received: " + inputLine);
                            out.println("Echo: " + inputLine);
                            
                            if ("bye".equalsIgnoreCase(inputLine)) {
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Server error: " + e.getMessage());
                }
            });
            
            serverThread.start();
            
            // 等待服务器启动
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 客户端连接
            try (Socket socket = new Socket("localhost", port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {
                
                // 发送测试消息
                String[] messages = {"Hello", "World", "Java I/O", "bye"};
                
                for (String message : messages) {
                    out.println(message);
                    String response = in.readLine();
                    System.out.println("Client received: " + response);
                }
                
            } catch (IOException e) {
                System.err.println("Client error: " + e.getMessage());
            }
            
            try {
                serverThread.join(5000); // 等待服务器线程结束
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * NIO核心组件分析
     * 
     * 核心概念：
     * 1. Channel - 数据传输通道
     * 2. Buffer - 数据缓冲区
     * 3. Selector - 多路复用选择器
     * 4. 非阻塞I/O模型
     */
    
    static class NIOAnalysis {
        
        /**
         * Buffer详解
         */
        public static void analyzeBuffer() {
            System.out.println("\n=== NIO Buffer Analysis ===");
            
            // 1. ByteBuffer基本操作
            System.out.println("\n--- ByteBuffer Operations ---");
            
            ByteBuffer buffer = ByteBuffer.allocate(10);
            System.out.println("Initial state - Position: " + buffer.position() + 
                ", Limit: " + buffer.limit() + ", Capacity: " + buffer.capacity());
            
            // 写入数据
            buffer.put((byte) 'H');
            buffer.put((byte) 'e');
            buffer.put((byte) 'l');
            buffer.put((byte) 'l');
            buffer.put((byte) 'o');
            
            System.out.println("After writing - Position: " + buffer.position() + 
                ", Limit: " + buffer.limit());
            
            // 切换到读模式
            buffer.flip();
            System.out.println("After flip - Position: " + buffer.position() + 
                ", Limit: " + buffer.limit());
            
            // 读取数据
            StringBuilder result = new StringBuilder();
            while (buffer.hasRemaining()) {
                result.append((char) buffer.get());
            }
            System.out.println("Read data: " + result.toString());
            
            System.out.println("After reading - Position: " + buffer.position() + 
                ", Limit: " + buffer.limit());
            
            // 重置和清空
            buffer.rewind(); // 重置position到0
            System.out.println("After rewind - Position: " + buffer.position());
            
            buffer.clear(); // 清空缓冲区
            System.out.println("After clear - Position: " + buffer.position() + 
                ", Limit: " + buffer.limit());
            
            // 2. 不同类型的Buffer
            System.out.println("\n--- Different Buffer Types ---");
            
            // IntBuffer
            IntBuffer intBuffer = IntBuffer.allocate(5);
            intBuffer.put(1).put(2).put(3).put(4).put(5);
            intBuffer.flip();
            
            System.out.print("IntBuffer contents: ");
            while (intBuffer.hasRemaining()) {
                System.out.print(intBuffer.get() + " ");
            }
            System.out.println();
            
            // CharBuffer
            CharBuffer charBuffer = CharBuffer.allocate(10);
            charBuffer.put("Hello");
            charBuffer.flip();
            
            System.out.println("CharBuffer contents: " + charBuffer.toString());
            
            // 3. 直接内存 vs 堆内存
            System.out.println("\n--- Direct vs Heap Buffer ---");
            
            ByteBuffer heapBuffer = ByteBuffer.allocate(1024);
            ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
            
            System.out.println("Heap buffer isDirect: " + heapBuffer.isDirect());
            System.out.println("Direct buffer isDirect: " + directBuffer.isDirect());
            
            // 性能测试
            int iterations = 1000000;
            byte[] data = "Test data for buffer performance".getBytes();
            
            // 堆缓冲区性能
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                heapBuffer.clear();
                heapBuffer.put(data);
                heapBuffer.flip();
            }
            long heapTime = System.nanoTime() - startTime;
            
            // 直接缓冲区性能
            startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                directBuffer.clear();
                directBuffer.put(data);
                directBuffer.flip();
            }
            long directTime = System.nanoTime() - startTime;
            
            System.out.println("Heap buffer time: " + heapTime / 1_000_000.0 + " ms");
            System.out.println("Direct buffer time: " + directTime / 1_000_000.0 + " ms");
        }
        
        /**
         * Channel详解
         */
        public static void analyzeChannel() {
            System.out.println("\n=== NIO Channel Analysis ===");
            
            String fileName = "test_nio.txt";
            String content = "Hello NIO Channel!\nThis is a test for NIO operations.";
            
            try {
                // 1. FileChannel操作
                System.out.println("\n--- FileChannel Operations ---");
                
                // 写入文件
                try (FileChannel writeChannel = FileChannel.open(
                        Paths.get(fileName), 
                        StandardOpenOption.CREATE, 
                        StandardOpenOption.WRITE, 
                        StandardOpenOption.TRUNCATE_EXISTING)) {
                    
                    ByteBuffer buffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
                    int bytesWritten = writeChannel.write(buffer);
                    System.out.println("Bytes written: " + bytesWritten);
                }
                
                // 读取文件
                try (FileChannel readChannel = FileChannel.open(
                        Paths.get(fileName), StandardOpenOption.READ)) {
                    
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = readChannel.read(buffer);
                    
                    buffer.flip();
                    String readContent = StandardCharsets.UTF_8.decode(buffer).toString();
                    
                    System.out.println("Bytes read: " + bytesRead);
                    System.out.println("Content: " + readContent);
                }
                
                // 2. 文件映射
                System.out.println("\n--- Memory Mapped File ---");
                
                try (RandomAccessFile file = new RandomAccessFile(fileName, "rw");
                     FileChannel channel = file.getChannel()) {
                    
                    long fileSize = channel.size();
                    MappedByteBuffer mappedBuffer = channel.map(
                        FileChannel.MapMode.READ_WRITE, 0, fileSize);
                    
                    System.out.println("File mapped to memory, size: " + fileSize);
                    
                    // 读取映射内容
                    byte[] data = new byte[(int) fileSize];
                    mappedBuffer.get(data);
                    System.out.println("Mapped content: " + new String(data, StandardCharsets.UTF_8));
                    
                    // 修改映射内容
                    mappedBuffer.position(0);
                    mappedBuffer.put("Modified via memory mapping!".getBytes(StandardCharsets.UTF_8));
                    mappedBuffer.force(); // 强制写入磁盘
                }
                
                // 验证修改
                String modifiedContent = Files.readString(Paths.get(fileName), StandardCharsets.UTF_8);
                System.out.println("Modified file content: " + modifiedContent.substring(0, 30) + "...");
                
                // 3. 文件传输
                System.out.println("\n--- File Transfer ---");
                
                String sourceFile = fileName;
                String targetFile = "test_nio_copy.txt";
                
                try (FileChannel sourceChannel = FileChannel.open(Paths.get(sourceFile), StandardOpenOption.READ);
                     FileChannel targetChannel = FileChannel.open(
                         Paths.get(targetFile), 
                         StandardOpenOption.CREATE, 
                         StandardOpenOption.WRITE, 
                         StandardOpenOption.TRUNCATE_EXISTING)) {
                    
                    long transferred = sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
                    System.out.println("Transferred bytes: " + transferred);
                }
                
                // 清理文件
                Files.deleteIfExists(Paths.get(fileName));
                Files.deleteIfExists(Paths.get(targetFile));
                
            } catch (IOException e) {
                System.err.println("Channel error: " + e.getMessage());
            }
        }
        
        /**
         * Selector多路复用演示
         */
        public static void analyzeSelector() {
            System.out.println("\n=== NIO Selector Analysis ===");
            
            int port = 8081;
            
            // NIO服务器
            Thread serverThread = new Thread(() -> {
                try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
                     Selector selector = Selector.open()) {
                    
                    serverChannel.bind(new InetSocketAddress(port));
                    serverChannel.configureBlocking(false);
                    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                    
                    System.out.println("NIO server started on port " + port);
                    
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    
                    for (int i = 0; i < 10; i++) { // 限制循环次数
                        int readyChannels = selector.select(1000); // 1秒超时
                        
                        if (readyChannels == 0) {
                            continue;
                        }
                        
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                        
                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();
                            
                            if (key.isAcceptable()) {
                                // 接受新连接
                                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                                SocketChannel client = server.accept();
                                client.configureBlocking(false);
                                client.register(selector, SelectionKey.OP_READ);
                                
                                System.out.println("New client connected: " + client.getRemoteAddress());
                                
                            } else if (key.isReadable()) {
                                // 读取数据
                                SocketChannel client = (SocketChannel) key.channel();
                                buffer.clear();
                                
                                int bytesRead = client.read(buffer);
                                if (bytesRead > 0) {
                                    buffer.flip();
                                    String message = StandardCharsets.UTF_8.decode(buffer).toString();
                                    System.out.println("Received: " + message.trim());
                                    
                                    // 回写数据
                                    String response = "Echo: " + message;
                                    ByteBuffer responseBuffer = ByteBuffer.wrap(
                                        response.getBytes(StandardCharsets.UTF_8));
                                    client.write(responseBuffer);
                                    
                                    if (message.trim().equalsIgnoreCase("bye")) {
                                        client.close();
                                        key.cancel();
                                    }
                                } else if (bytesRead == -1) {
                                    // 客户端断开连接
                                    client.close();
                                    key.cancel();
                                }
                            }
                            
                            keyIterator.remove();
                        }
                    }
                    
                } catch (IOException e) {
                    System.err.println("NIO server error: " + e.getMessage());
                }
            });
            
            serverThread.start();
            
            // 等待服务器启动
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // NIO客户端
            try (SocketChannel clientChannel = SocketChannel.open()) {
                clientChannel.connect(new InetSocketAddress("localhost", port));
                
                String[] messages = {"Hello NIO", "Selector test", "bye"};
                
                for (String message : messages) {
                    ByteBuffer buffer = ByteBuffer.wrap(
                        (message + "\n").getBytes(StandardCharsets.UTF_8));
                    clientChannel.write(buffer);
                    
                    // 读取响应
                    ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                    int bytesRead = clientChannel.read(responseBuffer);
                    
                    if (bytesRead > 0) {
                        responseBuffer.flip();
                        String response = StandardCharsets.UTF_8.decode(responseBuffer).toString();
                        System.out.println("Client received: " + response.trim());
                    }
                    
                    Thread.sleep(500); // 短暂延迟
                }
                
            } catch (IOException | InterruptedException e) {
                System.err.println("NIO client error: " + e.getMessage());
            }
            
            try {
                serverThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void main(String[] args) {
        TraditionalIOAnalysis.demonstrateFileIO();
        TraditionalIOAnalysis.demonstrateNetworkIO();
        
        NIOAnalysis.analyzeBuffer();
        NIOAnalysis.analyzeChannel();
        NIOAnalysis.analyzeSelector();
    }
}