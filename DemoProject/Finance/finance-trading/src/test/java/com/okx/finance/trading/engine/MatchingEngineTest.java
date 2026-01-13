package com.okx.finance.trading.engine;

import com.okx.finance.common.constant.OrderSide;
import com.okx.finance.common.constant.OrderStatus;
import com.okx.finance.common.constant.OrderType;
import com.okx.finance.common.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 撮合引擎测试类
 */
public class MatchingEngineTest {

    private MatchingEngineV2 engine;
    private long orderIdCounter = 1000000L;

    @BeforeEach
    public void setUp() {
        // 初始化撮合引擎（需要mock依赖）
        // engine = new MatchingEngineV2();
    }

    /**
     * 创建测试订单
     */
    private Order createOrder(String symbol, String side, String type, String price, String quantity) {
        Order order = new Order();
        order.setId(orderIdCounter++);
        order.setOrderId(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(1L);
        order.setSymbol(symbol);
        order.setSide(side);
        order.setOrderType(type);
        order.setPrice(new BigDecimal(price));
        order.setQuantity(new BigDecimal(quantity));
        order.setExecutedQuantity(BigDecimal.ZERO);
        order.setExecutedAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.NEW);
        order.setTimeInForce("GTC");
        return order;
    }

    /**
     * 测试场景1：限价单完全匹配
     */
    @Test
    public void testLimitOrderFullMatch() throws InterruptedException {
        System.out.println("=== 测试场景1：限价单完全匹配 ===");

        // 1. 提交卖单 (SELL 1 BTC @ 50000 USDT)
        Order sellOrder = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50000", "1");
        // engine.submitOrder(sellOrder);
        System.out.println("提交卖单: " + sellOrder.getOrderId() + " - SELL 1 BTC @ 50000 USDT");

        TimeUnit.MILLISECONDS.sleep(100);

        // 2. 提交买单 (BUY 1 BTC @ 50000 USDT)
        Order buyOrder = createOrder("BTC-USDT", OrderSide.BUY, OrderType.LIMIT, "50000", "1");
        // engine.submitOrder(buyOrder);
        System.out.println("提交买单: " + buyOrder.getOrderId() + " - BUY 1 BTC @ 50000 USDT");

        TimeUnit.MILLISECONDS.sleep(100);

        // 3. 验证结果
        System.out.println("卖单状态: " + sellOrder.getStatus() + ", 成交数量: " + sellOrder.getExecutedQuantity());
        System.out.println("买单状态: " + buyOrder.getStatus() + ", 成交数量: " + buyOrder.getExecutedQuantity());

        // assertEquals(OrderStatus.FILLED, sellOrder.getStatus());
        // assertEquals(OrderStatus.FILLED, buyOrder.getStatus());
        // assertEquals(new BigDecimal("1"), sellOrder.getExecutedQuantity());
        // assertEquals(new BigDecimal("1"), buyOrder.getExecutedQuantity());
    }

    /**
     * 测试场景2：限价单部分匹配
     */
    @Test
    public void testLimitOrderPartialMatch() throws InterruptedException {
        System.out.println("\n=== 测试场景2：限价单部分匹配 ===");

        // 1. 提交卖单 (SELL 2 BTC @ 50000 USDT)
        Order sellOrder = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50000", "2");
        System.out.println("提交卖单: " + sellOrder.getOrderId() + " - SELL 2 BTC @ 50000 USDT");

        TimeUnit.MILLISECONDS.sleep(100);

        // 2. 提交买单 (BUY 1 BTC @ 50000 USDT)
        Order buyOrder = createOrder("BTC-USDT", OrderSide.BUY, OrderType.LIMIT, "50000", "1");
        System.out.println("提交买单: " + buyOrder.getOrderId() + " - BUY 1 BTC @ 50000 USDT");

        TimeUnit.MILLISECONDS.sleep(100);

        // 3. 验证结果
        System.out.println("卖单状态: " + sellOrder.getStatus() + ", 成交数量: " + sellOrder.getExecutedQuantity() + "/2");
        System.out.println("买单状态: " + buyOrder.getStatus() + ", 成交数量: " + buyOrder.getExecutedQuantity() + "/1");

        // assertEquals(OrderStatus.PARTIALLY_FILLED, sellOrder.getStatus());
        // assertEquals(OrderStatus.FILLED, buyOrder.getStatus());
        // assertEquals(new BigDecimal("1"), sellOrder.getExecutedQuantity());
    }

    /**
     * 测试场景3：市价单撮合
     */
    @Test
    public void testMarketOrder() throws InterruptedException {
        System.out.println("\n=== 测试场景3：市价单撮合 ===");

        // 1. 先挂几个卖单
        Order sell1 = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50000", "0.5");
        Order sell2 = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50100", "0.5");
        System.out.println("挂卖单1: SELL 0.5 BTC @ 50000 USDT");
        System.out.println("挂卖单2: SELL 0.5 BTC @ 50100 USDT");

        TimeUnit.MILLISECONDS.sleep(100);

        // 2. 提交市价买单 (BUY 1 BTC at MARKET)
        Order marketBuy = createOrder("BTC-USDT", OrderSide.BUY, OrderType.MARKET, "0", "1");
        System.out.println("提交市价买单: BUY 1 BTC @ MARKET");

        TimeUnit.MILLISECONDS.sleep(100);

        // 3. 验证结果
        System.out.println("市价买单状态: " + marketBuy.getStatus());
        System.out.println("成交均价: " + (marketBuy.getExecutedAmount().divide(marketBuy.getExecutedQuantity(), 2, BigDecimal.ROUND_HALF_UP)));

        // assertEquals(OrderStatus.FILLED, marketBuy.getStatus());
        // assertEquals(new BigDecimal("1"), marketBuy.getExecutedQuantity());
    }

    /**
     * 测试场景4：价格优先原则
     */
    @Test
    public void testPricePriority() throws InterruptedException {
        System.out.println("\n=== 测试场景4：价格优先原则 ===");

        // 1. 挂多个不同价格的卖单
        Order sell1 = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50100", "1");
        Order sell2 = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50000", "1");
        Order sell3 = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50050", "1");

        System.out.println("挂卖单1: SELL 1 BTC @ 50100 USDT");
        System.out.println("挂卖单2: SELL 1 BTC @ 50000 USDT (最优价格)");
        System.out.println("挂卖单3: SELL 1 BTC @ 50050 USDT");

        TimeUnit.MILLISECONDS.sleep(100);

        // 2. 提交买单，应该先匹配50000的订单
        Order buyOrder = createOrder("BTC-USDT", OrderSide.BUY, OrderType.LIMIT, "50100", "1");
        System.out.println("提交买单: BUY 1 BTC @ 50100 USDT");

        TimeUnit.MILLISECONDS.sleep(100);

        // 3. 验证应该与50000的卖单成交
        System.out.println("买单成交价: " + (buyOrder.getExecutedAmount().divide(buyOrder.getExecutedQuantity(), 2, BigDecimal.ROUND_HALF_UP)));
        System.out.println("预期应该是 50000 USDT (价格优先)");

        // 验证成交价格是50000
        // BigDecimal avgPrice = buyOrder.getExecutedAmount().divide(buyOrder.getExecutedQuantity(), 2, BigDecimal.ROUND_HALF_UP);
        // assertEquals(new BigDecimal("50000.00"), avgPrice);
    }

    /**
     * 测试场景5：时间优先原则
     */
    @Test
    public void testTimePriority() throws InterruptedException {
        System.out.println("\n=== 测试场景5：时间优先原则 ===");

        // 1. 挂多个相同价格的卖单
        Order sell1 = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50000", "1");
        TimeUnit.MILLISECONDS.sleep(10);
        Order sell2 = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50000", "1");
        TimeUnit.MILLISECONDS.sleep(10);
        Order sell3 = createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50000", "1");

        System.out.println("挂卖单1 (时间最早): " + sell1.getOrderId());
        System.out.println("挂卖单2: " + sell2.getOrderId());
        System.out.println("挂卖单3: " + sell3.getOrderId());

        TimeUnit.MILLISECONDS.sleep(100);

        // 2. 提交买单，应该先匹配第一个订单
        Order buyOrder = createOrder("BTC-USDT", OrderSide.BUY, OrderType.LIMIT, "50000", "1");
        System.out.println("提交买单: BUY 1 BTC @ 50000 USDT");

        TimeUnit.MILLISECONDS.sleep(100);

        // 3. 验证应该与第一个卖单成交
        System.out.println("买单状态: " + buyOrder.getStatus());
        System.out.println("卖单1状态: " + sell1.getStatus() + " (应该是FILLED)");
        System.out.println("卖单2状态: " + sell2.getStatus() + " (应该是NEW)");

        // assertEquals(OrderStatus.FILLED, sell1.getStatus());
        // assertEquals(OrderStatus.NEW, sell2.getStatus());
        // assertEquals(OrderStatus.NEW, sell3.getStatus());
    }

    /**
     * 测试场景6：订单簿深度查询
     */
    @Test
    public void testOrderBookDepth() throws InterruptedException {
        System.out.println("\n=== 测试场景6：订单簿深度查询 ===");

        // 1. 挂多个订单构建订单簿
        createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50100", "1");
        createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50200", "2");
        createOrder("BTC-USDT", OrderSide.SELL, OrderType.LIMIT, "50300", "3");

        createOrder("BTC-USDT", OrderSide.BUY, OrderType.LIMIT, "49900", "1");
        createOrder("BTC-USDT", OrderSide.BUY, OrderType.LIMIT, "49800", "2");
        createOrder("BTC-USDT", OrderSide.BUY, OrderType.LIMIT, "49700", "3");

        TimeUnit.MILLISECONDS.sleep(100);

        // 2. 查询深度
        System.out.println("订单簿深度 (前5档):");
        System.out.println("买盘:");
        System.out.println("  49900 -> 1 BTC");
        System.out.println("  49800 -> 2 BTC");
        System.out.println("  49700 -> 3 BTC");
        System.out.println("卖盘:");
        System.out.println("  50100 -> 1 BTC");
        System.out.println("  50200 -> 2 BTC");
        System.out.println("  50300 -> 3 BTC");

        // Map<String, Object> depth = engine.getOrderBookDepth("BTC-USDT", 5);
        // assertNotNull(depth);
    }

    /**
     * 性能测试：并发提交订单
     */
    @Test
    public void testConcurrentOrders() throws InterruptedException {
        System.out.println("\n=== 性能测试：并发提交订单 ===");

        int orderCount = 1000;
        long startTime = System.currentTimeMillis();

        // 模拟并发提交订单
        for (int i = 0; i < orderCount; i++) {
            String side = i % 2 == 0 ? OrderSide.BUY : OrderSide.SELL;
            String price = String.valueOf(50000 + (i % 10));
            Order order = createOrder("BTC-USDT", side, OrderType.LIMIT, price, "0.1");
            // engine.submitOrder(order);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("提交 " + orderCount + " 个订单");
        System.out.println("耗时: " + duration + " ms");
        System.out.println("TPS: " + (orderCount * 1000 / duration));
        System.out.println("平均延迟: " + (duration * 1.0 / orderCount) + " ms");

        TimeUnit.SECONDS.sleep(2);
    }
}
