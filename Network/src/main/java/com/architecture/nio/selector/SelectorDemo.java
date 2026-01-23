package com.architecture.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Java NIO Selector 核心组件示例
 *
 * Selector（选择器）是NIO的核心组件之一，用于实现单线程管理多个Channel
 * 这是NIO实现高性能、高并发的关键技术
 *
 * 核心概念：
 * 1. 一个Selector可以监听多个Channel的事件（连接、接收、读、写）
 * 2. 只有在Channel真正有事件发生时，才会进行读写操作
 * 3. 避免了传统I/O中为每个连接创建线程的开销
 * 4. 实现了I/O多路复用
 *
 * 主要步骤：
 * 1. 创建Selector
 * 2. 将Channel注册到Selector上，并指定感兴趣的事件
 * 3. 循环调用select()方法，获取就绪的事件
 * 4. 处理就绪的事件
 */
public class SelectorDemo {

    /**
     * 演示Selector的基本使用
     * 这是一个简单的TCP服务器示例
     */
    public static void demonstrateSelector() throws IOException {
        System.out.println("=== Selector 基本操作 ===");

        // 1. 创建Selector
        Selector selector = Selector.open();
        System.out.println("Selector已创建");

        // 2. 创建ServerSocketChannel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));

        // 3. 设置为非阻塞模式（使用Selector必须是非阻塞模式）
        serverChannel.configureBlocking(false);
        System.out.println("ServerSocketChannel已设置为非阻塞模式");

        // 4. 将Channel注册到Selector上，并指定感兴趣的事件
        // SelectionKey.OP_ACCEPT - 接收连接事件
        SelectionKey serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("ServerSocketChannel已注册到Selector");
        System.out.println("监听端口: 8080");

        printSelectionKeyInfo(serverKey);

        // 清理资源
        serverChannel.close();
        selector.close();
    }

    /**
     * 演示完整的NIO服务器
     * 使用Selector实现单线程处理多个客户端连接
     */
    public static class NioServer implements Runnable {
        private final int port;
        private Selector selector;
        private volatile boolean running = true;

        public NioServer(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void startServer() throws IOException {
            System.out.println("\n=== 启动NIO服务器 ===");

            // 1. 创建Selector
            selector = Selector.open();

            // 2. 创建ServerSocketChannel
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);

            // 3. 注册到Selector
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("服务器启动成功，监听端口: " + port);
            System.out.println("等待客户端连接...");

            // 4. 事件循环
            while (running) {
                // select()会阻塞，直到至少有一个通道就绪
                // 返回值是就绪通道的数量
                int readyChannels = selector.select(1000); // 超时1秒

                if (readyChannels == 0) {
                    continue;
                }

                // 5. 获取就绪的SelectionKey集合
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                // 6. 遍历处理每个就绪的事件
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    try {
                        // 处理事件
                        handleKey(key);
                    } catch (IOException e) {
                        System.err.println("处理客户端请求时出错: " + e.getMessage());
                        key.cancel();
                        if (key.channel() != null) {
                            key.channel().close();
                        }
                    }

                    // 7. 处理完后必须手动移除，否则会一直存在
                    keyIterator.remove();
                }
            }

            // 清理资源
            serverChannel.close();
            selector.close();
            System.out.println("服务器已关闭");
        }

        private void handleKey(SelectionKey key) throws IOException {
            // 测试此键的通道是否已准备好接受新的套接字连接
            if (key.isAcceptable()) {
                handleAccept(key);
            }
            // 测试此键的通道是否已准备好进行读取
            else if (key.isReadable()) {
                handleRead(key);
            }
            // 测试此键的通道是否已准备好进行写入
            else if (key.isWritable()) {
                handleWrite(key);
            }
        }

        /**
         * 处理接受连接事件
         */
        private void handleAccept(SelectionKey key) throws IOException {
            System.out.println("\n[ACCEPT] 新客户端连接");

            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

            // 接受连接
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);

            // 将客户端通道注册到Selector，关注读事件
            clientChannel.register(selector, SelectionKey.OP_READ);

            System.out.println("客户端已连接: " + clientChannel.getRemoteAddress());
        }

        /**
         * 处理读事件
         */
        private void handleRead(SelectionKey key) throws IOException {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                // 客户端关闭连接
                System.out.println("[READ] 客户端断开连接: " + clientChannel.getRemoteAddress());
                clientChannel.close();
                key.cancel();
                return;
            }

            if (bytesRead > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.limit()];
                buffer.get(data);
                String message = new String(data, "UTF-8").trim();

                System.out.println("[READ] 收到消息: " + message);
                System.out.println("       来自: " + clientChannel.getRemoteAddress());

                // 将响应数据附加到key上，准备写回
                String response = "服务器收到: " + message;
                key.attach(response);

                // 修改关注的事件为写事件
                key.interestOps(SelectionKey.OP_WRITE);
            }
        }

        /**
         * 处理写事件
         */
        private void handleWrite(SelectionKey key) throws IOException {
            SocketChannel clientChannel = (SocketChannel) key.channel();

            // 获取附加的响应数据
            String response = (String) key.attachment();
            if (response != null) {
                ByteBuffer buffer = ByteBuffer.wrap(response.getBytes("UTF-8"));
                clientChannel.write(buffer);

                System.out.println("[WRITE] 发送响应: " + response);
                System.out.println("        发送到: " + clientChannel.getRemoteAddress());

                // 清除附加数据
                key.attach(null);
            }

            // 修改关注的事件为读事件，继续接收数据
            key.interestOps(SelectionKey.OP_READ);
        }

        public void stop() {
            running = false;
        }
    }

    /**
     * 演示NIO客户端
     */
    public static class NioClient {
        private final String host;
        private final int port;

        public NioClient(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public void sendMessage(String message) throws IOException {
            System.out.println("\n=== NIO客户端 ===");

            // 1. 创建SocketChannel
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);

            // 2. 创建Selector
            Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_CONNECT);

            // 3. 发起连接
            channel.connect(new InetSocketAddress(host, port));
            System.out.println("正在连接到服务器 " + host + ":" + port);

            // 4. 事件循环
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isConnectable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        if (sc.finishConnect()) {
                            System.out.println("连接成功");

                            // 发送消息
                            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
                            sc.write(buffer);
                            System.out.println("发送消息: " + message);

                            // 关注读事件
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } else if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int bytesRead = sc.read(buffer);

                        if (bytesRead > 0) {
                            buffer.flip();
                            byte[] data = new byte[buffer.limit()];
                            buffer.get(data);
                            System.out.println("收到响应: " + new String(data, "UTF-8"));

                            // 关闭连接
                            sc.close();
                            selector.close();
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * 打印SelectionKey的信息
     */
    private static void printSelectionKeyInfo(SelectionKey key) {
        System.out.println("\nSelectionKey信息:");
        System.out.println("  Channel: " + key.channel());
        System.out.println("  Selector: " + key.selector());
        System.out.println("  Interest ops: " + getInterestOpsString(key.interestOps()));
        System.out.println("  Ready ops: " + getInterestOpsString(key.readyOps()));
        System.out.println("  Is valid: " + key.isValid());
    }

    /**
     * 将操作位转换为可读字符串
     */
    private static String getInterestOpsString(int ops) {
        StringBuilder sb = new StringBuilder();
        if ((ops & SelectionKey.OP_ACCEPT) != 0) sb.append("ACCEPT ");
        if ((ops & SelectionKey.OP_CONNECT) != 0) sb.append("CONNECT ");
        if ((ops & SelectionKey.OP_READ) != 0) sb.append("READ ");
        if ((ops & SelectionKey.OP_WRITE) != 0) sb.append("WRITE ");
        return sb.toString().trim();
    }

    /**
     * 演示Selector的四种事件类型
     */
    public static void demonstrateSelectionKeyOps() {
        System.out.println("\n=== SelectionKey 事件类型 ===");
        System.out.println("1. OP_ACCEPT  = " + SelectionKey.OP_ACCEPT + "  (接受连接)");
        System.out.println("2. OP_CONNECT = " + SelectionKey.OP_CONNECT + " (连接就绪)");
        System.out.println("3. OP_READ    = " + SelectionKey.OP_READ + "  (读就绪)");
        System.out.println("4. OP_WRITE   = " + SelectionKey.OP_WRITE + " (写就绪)");

        System.out.println("\n可以通过位运算组合多个事件:");
        int combined = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        System.out.println("READ | WRITE = " + combined + " -> " + getInterestOpsString(combined));
    }

    public static void main(String[] args) throws IOException {
        demonstrateSelector();
        demonstrateSelectionKeyOps();

        System.out.println("\n=== Selector工作原理总结 ===");
        System.out.println("1. Selector使用单线程监听多个Channel");
        System.out.println("2. Channel必须处于非阻塞模式");
        System.out.println("3. Channel注册到Selector时指定感兴趣的事件");
        System.out.println("4. select()方法会阻塞，直到至少有一个Channel就绪");
        System.out.println("5. 通过SelectionKey访问就绪的Channel和事件类型");
        System.out.println("6. 处理完事件后必须手动移除SelectionKey");
        System.out.println("7. 这种模式称为I/O多路复用或事件驱动模型");
    }
}
