package com.architecture.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * 完整的NIO聊天室示例
 * 演示了Buffer、Channel、Selector三大核心组件的综合应用
 *
 * 功能：
 * 1. 支持多客户端连接
 * 2. 消息广播
 * 3. 在线用户管理
 * 4. 优雅关闭
 */
public class NioChatRoom {

    /**
     * NIO聊天室服务器
     */
    public static class ChatServer implements Runnable {
        private static final int PORT = 8888;
        private static final int BUFFER_SIZE = 1024;

        private Selector selector;
        private ServerSocketChannel serverChannel;
        private volatile boolean running = true;

        @Override
        public void run() {
            try {
                startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void startServer() throws IOException {
            System.out.println("========================================");
            System.out.println("   NIO 聊天室服务器");
            System.out.println("========================================");

            // 1. 创建Selector
            selector = Selector.open();
            System.out.println("[启动] Selector已创建");

            // 2. 创建ServerSocketChannel
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);
            System.out.println("[启动] ServerSocketChannel已创建并绑定到端口: " + PORT);

            // 3. 注册到Selector
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("[启动] ServerSocketChannel已注册到Selector");
            System.out.println("[就绪] 服务器启动成功，等待客户端连接...\n");

            // 4. 事件循环
            while (running) {
                try {
                    // 等待事件，超时时间1秒
                    int readyChannels = selector.select(1000);

                    if (readyChannels == 0) {
                        continue;
                    }

                    // 获取就绪的SelectionKey
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();

                    // 处理每个就绪的事件
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        try {
                            handleKey(key);
                        } catch (IOException e) {
                            System.err.println("[错误] 处理客户端时出错: " + e.getMessage());
                            closeChannel(key);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("[错误] Selector出错: " + e.getMessage());
                }
            }

            // 关闭服务器
            shutdown();
        }

        /**
         * 处理就绪的事件
         */
        private void handleKey(SelectionKey key) throws IOException {
            if (!key.isValid()) {
                return;
            }

            if (key.isAcceptable()) {
                handleAccept(key);
            } else if (key.isReadable()) {
                handleRead(key);
            }
        }

        /**
         * 处理客户端连接
         */
        private void handleAccept(SelectionKey key) throws IOException {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();

            if (client == null) {
                return;
            }

            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);

            String clientAddress = client.getRemoteAddress().toString();
            System.out.println("[连接] 新客户端加入: " + clientAddress);

            // 欢迎消息
            String welcome = "欢迎加入聊天室！当前在线人数: " + getOnlineCount();
            sendToClient(client, welcome);

            // 广播新用户加入消息
            broadcast(clientAddress + " 加入了聊天室", client);
        }

        /**
         * 处理客户端消息
         */
        private void handleRead(SelectionKey key) throws IOException {
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            int bytesRead;
            try {
                bytesRead = client.read(buffer);
            } catch (IOException e) {
                closeChannel(key);
                return;
            }

            if (bytesRead == -1) {
                // 客户端正常关闭
                String clientAddress = client.getRemoteAddress().toString();
                System.out.println("[断开] 客户端断开: " + clientAddress);
                broadcast(clientAddress + " 离开了聊天室", client);
                closeChannel(key);
                return;
            }

            if (bytesRead > 0) {
                buffer.flip();
                String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                String clientAddress = client.getRemoteAddress().toString();

                System.out.println("[消息] " + clientAddress + ": " + message);

                // 广播消息给所有其他客户端
                broadcast(clientAddress + ": " + message, client);
            }
        }

        /**
         * 广播消息给所有客户端（除了发送者）
         */
        private void broadcast(String message, SocketChannel sender) {
            for (SelectionKey key : selector.keys()) {
                Channel channel = key.channel();

                // 只发送给SocketChannel，且不是发送者本身
                if (channel instanceof SocketChannel && channel != sender) {
                    SocketChannel client = (SocketChannel) channel;
                    try {
                        sendToClient(client, message);
                    } catch (IOException e) {
                        System.err.println("[错误] 发送消息失败: " + e.getMessage());
                    }
                }
            }
        }

        /**
         * 发送消息给指定客户端
         */
        private void sendToClient(SocketChannel client, String message) throws IOException {
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(message + "\n");
            client.write(buffer);
        }

        /**
         * 获取在线人数
         */
        private int getOnlineCount() {
            int count = 0;
            for (SelectionKey key : selector.keys()) {
                if (key.channel() instanceof SocketChannel) {
                    count++;
                }
            }
            return count;
        }

        /**
         * 关闭客户端连接
         */
        private void closeChannel(SelectionKey key) {
            try {
                key.cancel();
                key.channel().close();
            } catch (IOException e) {
                System.err.println("[错误] 关闭连接失败: " + e.getMessage());
            }
        }

        /**
         * 关闭服务器
         */
        private void shutdown() {
            try {
                running = false;
                if (serverChannel != null) {
                    serverChannel.close();
                }
                if (selector != null) {
                    selector.close();
                }
                System.out.println("\n[关闭] 服务器已关闭");
            } catch (IOException e) {
                System.err.println("[错误] 关闭服务器失败: " + e.getMessage());
            }
        }

        public void stop() {
            running = false;
        }
    }

    /**
     * NIO聊天室客户端
     */
    public static class ChatClient implements Runnable {
        private static final String HOST = "localhost";
        private static final int PORT = 8888;
        private static final int BUFFER_SIZE = 1024;

        private Selector selector;
        private SocketChannel socketChannel;
        private volatile boolean running = true;

        @Override
        public void run() {
            try {
                connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void connect() throws IOException {
            System.out.println("========================================");
            System.out.println("   NIO 聊天室客户端");
            System.out.println("========================================");

            // 1. 创建Selector
            selector = Selector.open();

            // 2. 创建SocketChannel
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            // 3. 注册连接事件
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            // 4. 发起连接
            socketChannel.connect(new InetSocketAddress(HOST, PORT));
            System.out.println("[连接] 正在连接到服务器 " + HOST + ":" + PORT);

            // 5. 事件循环
            while (running) {
                try {
                    selector.select(1000);
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        try {
                            handleKey(key);
                        } catch (IOException e) {
                            System.err.println("[错误] " + e.getMessage());
                            disconnect();
                        }
                    }
                } catch (IOException e) {
                    System.err.println("[错误] Selector出错: " + e.getMessage());
                    break;
                }
            }
        }

        private void handleKey(SelectionKey key) throws IOException {
            if (!key.isValid()) {
                return;
            }

            if (key.isConnectable()) {
                handleConnect(key);
            } else if (key.isReadable()) {
                handleRead(key);
            }
        }

        private void handleConnect(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();

            if (channel.finishConnect()) {
                System.out.println("[成功] 已连接到服务器");
                System.out.println("[提示] 输入消息并回车发送，输入 'quit' 退出\n");

                // 修改关注的事件为读
                channel.register(selector, SelectionKey.OP_READ);

                // 启动发送消息的线程
                startSendThread();
            }
        }

        private void handleRead(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            int bytesRead = channel.read(buffer);

            if (bytesRead == -1) {
                System.out.println("\n[断开] 与服务器的连接已断开");
                disconnect();
                return;
            }

            if (bytesRead > 0) {
                buffer.flip();
                String message = StandardCharsets.UTF_8.decode(buffer).toString();
                System.out.println(message);
            }
        }

        /**
         * 启动发送消息的线程
         */
        private void startSendThread() {
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (running) {
                    try {
                        String message = scanner.nextLine();

                        if ("quit".equalsIgnoreCase(message.trim())) {
                            disconnect();
                            break;
                        }

                        if (!message.trim().isEmpty()) {
                            sendMessage(message);
                        }
                    } catch (Exception e) {
                        System.err.println("[错误] 发送消息失败: " + e.getMessage());
                    }
                }
                scanner.close();
            }, "SendThread").start();
        }

        /**
         * 发送消息到服务器
         */
        private void sendMessage(String message) throws IOException {
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(message);
            socketChannel.write(buffer);
        }

        /**
         * 断开连接
         */
        private void disconnect() {
            try {
                running = false;
                if (socketChannel != null) {
                    socketChannel.close();
                }
                if (selector != null) {
                    selector.close();
                }
                System.out.println("[退出] 已退出聊天室");
                System.exit(0);
            } catch (IOException e) {
                System.err.println("[错误] 关闭连接失败: " + e.getMessage());
            }
        }
    }

    /**
     * 主方法 - 演示如何启动服务器和客户端
     */
    public static void main(String[] args) {
        if (args.length > 0 && "server".equals(args[0])) {
            // 启动服务器
            ChatServer server = new ChatServer();
            server.run();
        } else if (args.length > 0 && "client".equals(args[0])) {
            // 启动客户端
            ChatClient client = new ChatClient();
            client.run();
        } else {
            System.out.println("使用方法:");
            System.out.println("  启动服务器: java NioChatRoom server");
            System.out.println("  启动客户端: java NioChatRoom client");
            System.out.println("\n或者在IDE中分别运行 ChatServer 和 ChatClient 的 main 方法");
        }
    }
}
