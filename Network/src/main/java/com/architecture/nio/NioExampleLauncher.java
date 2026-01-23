package com.architecture.nio;

/**
 * NIO示例程序启动类
 * 提供简单的菜单来运行各个示例
 */
public class NioExampleLauncher {

    public static void main(String[] args) {
        if (args.length > 0) {
            runExample(args[0]);
        } else {
            printMenu();
        }
    }

    private static void printMenu() {
        System.out.println("========================================");
        System.out.println("   Java NIO 核心组件示例程序");
        System.out.println("========================================");
        System.out.println("\n可用的示例程序：");
        System.out.println("\n1. Buffer示例");
        System.out.println("   运行: mvn exec:java -Dexec.mainClass=\"com.architecture.nio.buffer.BufferDemo\"");
        System.out.println("   说明: 演示Buffer的各种操作（allocate, put, get, flip, clear等）");

        System.out.println("\n2. Channel示例");
        System.out.println("   运行: mvn exec:java -Dexec.mainClass=\"com.architecture.nio.channel.ChannelDemo\"");
        System.out.println("   说明: 演示FileChannel的读写、零拷贝、Scatter/Gather等功能");

        System.out.println("\n3. Selector示例");
        System.out.println("   运行: mvn exec:java -Dexec.mainClass=\"com.architecture.nio.selector.SelectorDemo\"");
        System.out.println("   说明: 演示Selector的基本使用和事件类型");

        System.out.println("\n4. NIO聊天室 - 服务器");
        System.out.println("   运行: mvn exec:java -Dexec.mainClass=\"com.architecture.nio.NioChatRoom\" -Dexec.args=\"server\"");
        System.out.println("   说明: 启动聊天室服务器（端口8888）");

        System.out.println("\n5. NIO聊天室 - 客户端");
        System.out.println("   运行: mvn exec:java -Dexec.mainClass=\"com.architecture.nio.NioChatRoom\" -Dexec.args=\"client\"");
        System.out.println("   说明: 启动聊天室客户端（需先启动服务器）");

        System.out.println("\n========================================");
        System.out.println("学习路径建议：");
        System.out.println("1. 先运行 BufferDemo 理解Buffer的工作原理");
        System.out.println("2. 再运行 ChannelDemo 理解Channel的各种操作");
        System.out.println("3. 然后运行 SelectorDemo 理解Selector的事件机制");
        System.out.println("4. 最后运行 NioChatRoom 查看完整应用");
        System.out.println("========================================");

        System.out.println("\n文档：");
        System.out.println("查看 NIO-README.md 获取详细的原理说明");
        System.out.println("========================================\n");
    }

    private static void runExample(String example) {
        try {
            switch (example) {
                case "buffer":
                    com.architecture.nio.buffer.BufferDemo.main(new String[]{});
                    break;
                case "channel":
                    com.architecture.nio.channel.ChannelDemo.main(new String[]{});
                    break;
                case "selector":
                    com.architecture.nio.selector.SelectorDemo.main(new String[]{});
                    break;
                case "chat-server":
                    NioChatRoom.main(new String[]{"server"});
                    break;
                case "chat-client":
                    NioChatRoom.main(new String[]{"client"});
                    break;
                default:
                    System.out.println("未知的示例: " + example);
                    printMenu();
            }
        } catch (Exception e) {
            System.err.println("运行示例时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
