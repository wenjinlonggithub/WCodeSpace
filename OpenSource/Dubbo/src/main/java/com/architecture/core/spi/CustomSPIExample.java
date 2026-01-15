package com.architecture.core.spi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.extension.SPI;

/**
 * 自定义 Dubbo SPI 扩展点示例
 *
 * 演示如何创建和使用自定义的 SPI 扩展点
 */
public class CustomSPIExample {

    /**
     * 定义扩展点接口
     * @SPI 注解标识这是一个扩展点，参数 "simple" 表示默认实现的名称
     */
    @SPI("simple")
    public interface MessageSender {
        void send(URL url, String message);
    }

    /**
     * 简单消息发送器实现
     */
    public static class SimpleMessageSender implements MessageSender {
        @Override
        public void send(URL url, String message) {
            System.out.println("[Simple Sender] 发送消息: " + message);
        }
    }

    /**
     * 邮件消息发送器实现
     */
    public static class EmailMessageSender implements MessageSender {
        @Override
        public void send(URL url, String message) {
            System.out.println("[Email Sender] 通过邮件发送: " + message);
        }
    }

    /**
     * 短信消息发送器实现
     */
    public static class SmsMessageSender implements MessageSender {
        @Override
        public void send(URL url, String message) {
            System.out.println("[SMS Sender] 通过短信发送: " + message);
        }
    }

    /**
     * 自适应扩展包装类
     * 使用 @Adaptive 注解，在运行时根据 URL 参数动态选择实现
     */
    public static class AdaptiveMessageSender implements MessageSender {
        @Adaptive({"message.sender", "sender"})
        @Override
        public void send(URL url, String message) {
            // 这个方法实际上会被 Dubbo 在运行时自动生成代理代码
            ExtensionLoader<MessageSender> loader =
                ExtensionLoader.getExtensionLoader(MessageSender.class);

            // 从 URL 中获取参数，决定使用哪个实现
            String senderName = url.getParameter("sender", "simple");
            MessageSender sender = loader.getExtension(senderName);
            sender.send(url, message);
        }
    }

    /**
     * 包装类示例
     * 构造函数参数为扩展点接口类型，可以对所有实现进行增强
     */
    public static class MessageSenderWrapper implements MessageSender {
        private final MessageSender sender;

        public MessageSenderWrapper(MessageSender sender) {
            this.sender = sender;
        }

        @Override
        public void send(URL url, String message) {
            System.out.println(">>> 消息发送前置处理");
            long startTime = System.currentTimeMillis();

            sender.send(url, message);

            long endTime = System.currentTimeMillis();
            System.out.println(">>> 消息发送后置处理，耗时: " + (endTime - startTime) + "ms");
        }
    }

    public static void main(String[] args) {
        // 注意：实际使用时需要在 META-INF/dubbo/ 目录下创建配置文件
        // 文件名：com.architecture.core.spi.CustomSPIExample$MessageSender
        // 内容示例：
        // simple=com.architecture.core.spi.CustomSPIExample$SimpleMessageSender
        // email=com.architecture.core.spi.CustomSPIExample$EmailMessageSender
        // sms=com.architecture.core.spi.CustomSPIExample$SmsMessageSender

        System.out.println("自定义 SPI 扩展点示例");
        System.out.println("实际使用时需要配置 META-INF/dubbo/ 目录下的配置文件");
    }
}

/**
 * 配置文件位置和格式：
 *
 * 1. 文件位置（按优先级）：
 *    - META-INF/dubbo/internal/接口全限定名
 *    - META-INF/dubbo/接口全限定名
 *    - META-INF/services/接口全限定名
 *
 * 2. 文件格式：
 *    key=value
 *    其中 key 是扩展名，value 是实现类的全限定名
 *
 * 3. 示例内容：
 *    simple=com.architecture.core.spi.CustomSPIExample$SimpleMessageSender
 *    email=com.architecture.core.spi.CustomSPIExample$EmailMessageSender
 *    sms=com.architecture.core.spi.CustomSPIExample$SmsMessageSender
 *
 * 4. 包装类配置：
 *    wrapper=com.architecture.core.spi.CustomSPIExample$MessageSenderWrapper
 *    包装类会自动应用到所有实现上
 */
