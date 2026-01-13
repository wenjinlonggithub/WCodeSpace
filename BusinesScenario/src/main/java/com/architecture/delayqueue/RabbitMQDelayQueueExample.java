package com.architecture.delayqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于RabbitMQ死信队列的延迟队列示例
 *
 * 实现原理：
 * 1. 创建延迟交换机和延迟队列（设置TTL）
 * 2. 创建死信交换机和死信队列
 * 3. 延迟队列的消息过期后，进入死信队列
 * 4. 消费者监听死信队列
 *
 * 配置示例：
 * [生产者] -> [延迟交换机] -> [延迟队列(TTL=30s)] -> [消息过期] -> [死信交换机] -> [死信队列] -> [消费者]
 *
 * Maven依赖：
 * <dependency>
 *     <groupId>com.rabbitmq</groupId>
 *     <artifactId>amqp-client</artifactId>
 *     <version>5.16.0</version>
 * </dependency>
 */
public class RabbitMQDelayQueueExample {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQDelayQueueExample.class);

    /**
     * 配置延迟队列
     */
    public static void setupDelayQueue() {
        /*
        // 1. 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 2. 声明死信交换机和死信队列
        String dlxExchange = "dlx.exchange";
        String dlxQueue = "dlx.queue";
        String dlxRoutingKey = "dlx.routing.key";

        channel.exchangeDeclare(dlxExchange, "direct", true);
        channel.queueDeclare(dlxQueue, true, false, false, null);
        channel.queueBind(dlxQueue, dlxExchange, dlxRoutingKey);

        // 3. 声明延迟交换机和延迟队列
        String delayExchange = "delay.exchange";
        String delayQueue = "delay.queue";
        String delayRoutingKey = "delay.routing.key";

        channel.exchangeDeclare(delayExchange, "direct", true);

        // 配置延迟队列参数
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 30000);              // 消息TTL：30秒
        args.put("x-dead-letter-exchange", dlxExchange); // 死信交换机
        args.put("x-dead-letter-routing-key", dlxRoutingKey); // 死信路由键

        channel.queueDeclare(delayQueue, true, false, false, args);
        channel.queueBind(delayQueue, delayExchange, delayRoutingKey);

        logger.info("延迟队列配置成功");

        channel.close();
        connection.close();
        */
    }

    /**
     * 发送延迟消息
     */
    public static void sendDelayMessage() {
        /*
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            String exchange = "delay.exchange";
            String routingKey = "delay.routing.key";
            String message = "订单超时取消：ORDER_12345";

            // 发送消息到延迟队列
            channel.basicPublish(exchange, routingKey, null, message.getBytes());

            logger.info("发送延迟消息成功: {}", message);

        } catch (Exception e) {
            logger.error("发送延迟消息失败", e);
        }
        */
    }

    /**
     * 消费延迟消息（监听死信队列）
     */
    public static void consumeDelayMessage() {
        /*
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            String dlxQueue = "dlx.queue";

            // 消费死信队列
            channel.basicConsume(dlxQueue, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                         AMQP.BasicProperties properties, byte[] body) {
                    try {
                        String message = new String(body, "UTF-8");
                        logger.info("收到延迟消息: {}", message);

                        // 处理业务逻辑（如取消订单）
                        // ...

                        // 手动确认
                        channel.basicAck(envelope.getDeliveryTag(), false);

                    } catch (Exception e) {
                        logger.error("处理延迟消息失败", e);
                        try {
                            // 拒绝消息，重新入队
                            channel.basicNack(envelope.getDeliveryTag(), false, true);
                        } catch (IOException ex) {
                            logger.error("拒绝消息失败", ex);
                        }
                    }
                }
            });

            logger.info("延迟消息消费者启动成功");

        } catch (Exception e) {
            logger.error("启动消费者失败", e);
        }
        */
    }

    /**
     * 支持多种延迟时间的方案
     *
     * 问题：上述方案中，延迟时间是固定的（队列TTL）
     * 解决：为每种延迟时间创建一个队列
     *
     * 例如：
     * - delay.queue.5s：延迟5秒
     * - delay.queue.30s：延迟30秒
     * - delay.queue.30m：延迟30分钟
     *
     * 发送消息时，根据延迟时间选择对应的队列
     */
    public static void sendDelayMessageWithMultipleQueues(int delaySeconds) {
        /*
        String exchange = "delay.exchange";
        String routingKey;

        // 根据延迟时间选择队列
        if (delaySeconds <= 5) {
            routingKey = "delay.5s";
        } else if (delaySeconds <= 30) {
            routingKey = "delay.30s";
        } else if (delaySeconds <= 1800) {
            routingKey = "delay.30m";
        } else {
            routingKey = "delay.1h";
        }

        // 发送消息
        channel.basicPublish(exchange, routingKey, null, message.getBytes());
        */
    }

    /**
     * 使用消息级别的TTL（更灵活）
     *
     * 优势：每条消息可以设置不同的延迟时间
     * 劣势：可能导致消息阻塞（短延迟消息排在长延迟消息后面）
     */
    public static void sendDelayMessageWithMessageTTL() {
        /*
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            String exchange = "delay.exchange";
            String routingKey = "delay.routing.key";
            String message = "订单超时取消：ORDER_12345";

            // 设置消息属性：TTL
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .expiration("30000")  // 30秒（字符串类型）
                    .build();

            // 发送消息
            channel.basicPublish(exchange, routingKey, properties, message.getBytes());

            logger.info("发送延迟消息成功（消息TTL）: {}", message);

        } catch (Exception e) {
            logger.error("发送延迟消息失败", e);
        }
        */
    }

    /**
     * RabbitMQ 3.6.0+版本的延迟插件（推荐）
     *
     * 安装插件：rabbitmq-plugins enable rabbitmq_delayed_message_exchange
     *
     * 优势：
     * - 支持任意延迟时间
     * - 不会阻塞消息
     * - 使用简单
     */
    public static void sendDelayMessageWithPlugin() {
        /*
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 声明延迟交换机（类型：x-delayed-message）
            String exchange = "delayed.exchange";
            Map<String, Object> args = new HashMap<>();
            args.put("x-delayed-type", "direct");

            channel.exchangeDeclare(exchange, "x-delayed-message", true, false, args);

            // 声明队列并绑定
            String queue = "delayed.queue";
            String routingKey = "delayed.routing.key";
            channel.queueDeclare(queue, true, false, false, null);
            channel.queueBind(queue, exchange, routingKey);

            // 发送延迟消息
            String message = "订单超时取消：ORDER_12345";
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-delay", 30000);  // 延迟30秒

            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .headers(headers)
                    .build();

            channel.basicPublish(exchange, routingKey, properties, message.getBytes());

            logger.info("发送延迟消息成功（延迟插件）: {}, delay=30s", message);

        } catch (Exception e) {
            logger.error("发送延迟消息失败", e);
        }
        */
    }
}
