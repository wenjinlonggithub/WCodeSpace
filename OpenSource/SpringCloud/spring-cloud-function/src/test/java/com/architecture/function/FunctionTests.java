package com.architecture.function;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Cloud Function 测试示例
 *
 * 展示如何测试函数式接口
 * 函数式编程的一大优势就是易于测试
 */
class FunctionTests {

    // ========== 基础函数测试 ==========

    @Test
    void testUppercaseFunction() {
        // 创建函数实例
        Function<String, String> uppercase = String::toUpperCase;

        // 测试
        String result = uppercase.apply("hello");

        // 断言
        assertEquals("HELLO", result);
    }

    @Test
    void testReverseFunction() {
        Function<String, String> reverse =
            value -> new StringBuilder(value).reverse().toString();

        String result = reverse.apply("hello");

        assertEquals("olleh", result);
    }

    @Test
    void testSupplier() {
        Supplier<Long> timestamp = System::currentTimeMillis;

        Long result = timestamp.get();

        assertNotNull(result);
        assertTrue(result > 0);
    }

    @Test
    void testConsumer() {
        // Consumer 没有返回值，测试其副作用
        StringBuilder log = new StringBuilder();
        Consumer<String> logger = value -> log.append("Log: ").append(value);

        logger.accept("test message");

        assertEquals("Log: test message", log.toString());
    }

    // ========== 函数组合测试 ==========

    @Test
    void testFunctionComposition() {
        Function<String, String> uppercase = String::toUpperCase;
        Function<String, String> reverse =
            value -> new StringBuilder(value).reverse().toString();

        // 组合函数: 先转大写，再反转
        Function<String, String> combined = uppercase.andThen(reverse);

        String result = combined.apply("hello");

        assertEquals("OLLEH", result);
    }

    // ========== 业务函数测试 ==========

    @Test
    void testCalculateOrder() {
        BusinessFunctions businessFunctions = new BusinessFunctions();
        Function<BusinessFunctions.Order, BusinessFunctions.OrderResult> calculateOrder =
            businessFunctions.calculateOrder();

        // 准备测试数据
        BusinessFunctions.Order order = new BusinessFunctions.Order("ORDER001", 100.0, 2);

        // 执行函数
        BusinessFunctions.OrderResult result = calculateOrder.apply(order);

        // 验证结果
        assertEquals("ORDER001", result.getOrderId());
        assertEquals(200.0, result.getSubtotal());
        assertEquals(20.0, result.getTax());
        assertEquals(220.0, result.getTotal());
    }

    @Test
    void testCalculatePoints() {
        BusinessFunctions businessFunctions = new BusinessFunctions();
        Function<Double, Integer> calculatePoints = businessFunctions.calculatePoints();

        // 测试基础积分计算
        Integer points1 = calculatePoints.apply(50.0);
        assertEquals(50, points1); // 50元 = 50积分

        // 测试带奖励积分计算
        Integer points2 = calculatePoints.apply(250.0);
        assertEquals(270, points2); // 250元 = 250积分 + 20奖励积分
    }

    @Test
    void testValidateEmail() {
        BusinessFunctions businessFunctions = new BusinessFunctions();
        Function<String, BusinessFunctions.ValidationResult> validateEmail =
            businessFunctions.validateEmail();

        // 测试有效邮箱
        BusinessFunctions.ValidationResult result1 =
            validateEmail.apply("user@example.com");
        assertTrue(result1.isValid());
        assertEquals("邮箱格式正确", result1.getMessage());

        // 测试无效邮箱
        BusinessFunctions.ValidationResult result2 =
            validateEmail.apply("invalid-email");
        assertFalse(result2.isValid());
        assertEquals("邮箱格式错误", result2.getMessage());
    }

    // ========== 消息处理测试 ==========

    @Test
    void testOrderToNotification() {
        MessageProcessingFunctions msgFunctions = new MessageProcessingFunctions();
        Function<Message<MessageProcessingFunctions.OrderEvent>,
                 Message<MessageProcessingFunctions.NotificationEvent>> orderToNotification =
            msgFunctions.orderToNotification();

        // 准备测试消息
        MessageProcessingFunctions.OrderEvent orderEvent =
            new MessageProcessingFunctions.OrderEvent(
                "ORDER001",
                "USER001",
                1500.0,
                LocalDateTime.now()
            );

        Message<MessageProcessingFunctions.OrderEvent> inputMessage =
            MessageBuilder
                .withPayload(orderEvent)
                .setHeader("eventType", "ORDER_CREATED")
                .build();

        // 执行转换
        Message<MessageProcessingFunctions.NotificationEvent> outputMessage =
            orderToNotification.apply(inputMessage);

        // 验证结果
        assertNotNull(outputMessage);
        MessageProcessingFunctions.NotificationEvent notification =
            outputMessage.getPayload();
        assertEquals("USER001", notification.getUserId());
        assertEquals("订单通知", notification.getTitle());
        assertTrue(notification.getContent().contains("ORDER001"));
        assertTrue(notification.getContent().contains("1500.0"));

        // 验证 Headers
        assertEquals("order-service", outputMessage.getHeaders().get("source"));
        assertNotNull(outputMessage.getHeaders().get("timestamp"));
    }

    @Test
    void testFilterHighValueOrders() {
        MessageProcessingFunctions msgFunctions = new MessageProcessingFunctions();
        Function<MessageProcessingFunctions.OrderEvent,
                 MessageProcessingFunctions.OrderEvent> filter =
            msgFunctions.filterHighValueOrders();

        // 测试高价值订单（应该通过）
        MessageProcessingFunctions.OrderEvent highValueOrder =
            new MessageProcessingFunctions.OrderEvent(
                "ORDER001", "USER001", 1500.0, LocalDateTime.now()
            );
        MessageProcessingFunctions.OrderEvent result1 = filter.apply(highValueOrder);
        assertNotNull(result1);
        assertEquals("ORDER001", result1.getOrderId());

        // 测试低价值订单（应该被过滤）
        MessageProcessingFunctions.OrderEvent lowValueOrder =
            new MessageProcessingFunctions.OrderEvent(
                "ORDER002", "USER002", 500.0, LocalDateTime.now()
            );
        MessageProcessingFunctions.OrderEvent result2 = filter.apply(lowValueOrder);
        assertNull(result2);
    }

    // ========== 用户转换测试 ==========

    @Test
    void testUserConverter() {
        FunctionApplication app = new FunctionApplication();
        Function<FunctionApplication.User, FunctionApplication.UserDTO> converter =
            app.userConverter();

        // 准备测试数据
        FunctionApplication.User user = new FunctionApplication.User(1L, "john");

        // 执行转换
        FunctionApplication.UserDTO dto = converter.apply(user);

        // 验证结果
        assertEquals(1L, dto.getId());
        assertEquals("JOHN", dto.getDisplayName()); // 名称应该被转为大写
    }
}
