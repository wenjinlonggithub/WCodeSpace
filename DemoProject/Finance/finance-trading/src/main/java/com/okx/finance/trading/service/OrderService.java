package com.okx.finance.trading.service;

import com.okx.finance.common.constant.OrderSide;
import com.okx.finance.common.constant.OrderStatus;
import com.okx.finance.common.dto.Result;
import com.okx.finance.common.entity.Order;
import com.okx.finance.common.util.JwtUtil;
import com.okx.finance.common.util.SnowflakeIdGenerator;
import com.okx.finance.trading.dto.PlaceOrderRequest;
import com.okx.finance.trading.engine.MatchingEngine;
import com.okx.finance.trading.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单服务类
 * 处理订单相关的核心业务逻辑
 *
 * <p>主要功能：
 * 1. 下单：创建新订单并提交到撮合引擎
 * 2. 撤单：取消未完成的订单
 * 3. 订单查询：查询单个订单、批量查询订单
 * 4. 当前订单：查询未完成的订单
 * 5. 历史订单：分页查询已完成的订单
 *
 * <p>订单流程：
 * <pre>
 * 下单流程：
 *   1. 用户提交订单请求
 *   2. 创建订单记录（状态：NEW）
 *   3. 保存到数据库
 *   4. 提交到撮合引擎
 *   5. 撮合引擎异步处理订单
 *
 * 订单状态流转：
 *   NEW（新建）
 *     ↓
 *   PARTIALLY_FILLED（部分成交）
 *     ↓
 *   FILLED（完全成交）
 *
 *   或者：NEW → CANCELED（已取消）
 *   或者：NEW → REJECTED（被拒绝）
 * </pre>
 *
 * <p>订单类型：
 * - LIMIT：限价单，指定价格，价格达到时才成交
 * - MARKET：市价单，按当前市场最优价格立即成交
 * - STOP_LOSS：止损单，触发价格时以市价执行
 * - TAKE_PROFIT：止盈单，触发价格时以市价执行
 *
 * <p>订单方向：
 * - BUY：买入（做多）
 * - SELL：卖出（做空）
 *
 * <p>时效类型（Time In Force）：
 * - GTC（Good Till Cancel）：一直有效直到成交或取消
 * - IOC（Immediate Or Cancel）：立即成交否则取消
 * - FOK（Fill Or Kill）：完全成交否则取消
 * - GTX（Good Till Crossing）：只做Maker，不主动成交
 *
 * <p>事务处理：
 * - placeOrder和cancelOrder方法使用@Transactional保证数据一致性
 * - 订单创建失败时自动回滚
 * - 撤单操作需要验证订单状态
 *
 * <p>安全措施：
 * - 所有操作都需要Token验证
 * - 撤单时验证订单所有权（只能撤销自己的订单）
 * - 订单ID使用UUID生成，确保唯一性
 * - 数据库主键使用雪花算法，确保分布式环境下的唯一性
 *
 * @author OKX Finance Team
 * @version 1.0
 * @see OrderMapper
 * @see MatchingEngine
 * @see Order
 * @see OrderStatus
 */
@Service
public class OrderService {

    /**
     * 订单数据访问对象
     * 用于执行数据库操作（增删改查）
     */
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 撮合引擎
     * 负责订单的撮合匹配
     */
    @Autowired
    private MatchingEngine matchingEngine;

    /**
     * 雪花算法ID生成器
     * 机器ID为3，用于生成订单的数据库主键ID
     * 保证订单ID全局唯一且趋势递增
     */
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(3);

    /**
     * 下单
     * 创建新订单并提交到撮合引擎进行匹配
     *
     * <p>下单流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 创建订单对象
     *   - 生成数据库主键ID（雪花算法）
     *   - 生成订单ID（UUID，32位）
     *   - 设置订单信息（交易对、类型、方向、价格、数量等）
     *   - 初始化执行数量为0
     *   - 设置状态为NEW
     * 4. 保存订单到数据库
     * 5. 提交订单到撮合引擎
     * 6. 返回订单ID和状态
     *
     * <p>订单字段说明：
     * - orderId：订单ID（32位UUID，业务主键）
     * - symbol：交易对（如BTC-USDT）
     * - orderType：订单类型（LIMIT、MARKET等）
     * - side：订单方向（BUY、SELL）
     * - price：价格（限价单必填，市价单可为空）
     * - quantity：数量
     * - executedQuantity：已成交数量（初始为0）
     * - executedAmount：已成交金额（初始为0）
     * - status：订单状态（初始为NEW）
     * - timeInForce：时效类型（GTC、IOC、FOK、GTX）
     *
     * <p>撮合引擎：
     * - 订单提交后，撮合引擎会异步处理
     * - 限价单：添加到订单簿，等待匹配
     * - 市价单：立即尝试匹配，匹配不完全则取消
     *
     * <p>事务处理：
     * - 使用@Transactional注解保证原子性
     * - 数据库插入失败会回滚
     * - 撮合引擎提交失败不影响数据库记录
     *
     * <p>返回数据：
     * - orderId：订单ID
     * - status：订单状态
     *
     * @param token JWT Token，用于身份验证
     * @param request 下单请求，包含交易对、订单类型、方向、价格、数量、时效类型
     * @return Result对象，成功返回包含orderId和status的Map，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     */
    @Transactional
    public Result<?> placeOrder(String token, PlaceOrderRequest request) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);

        Order order = new Order();
        order.setId(idGenerator.nextId());
        order.setUserId(userId);
        order.setOrderId(UUID.randomUUID().toString().replace("-", ""));
        order.setSymbol(request.getSymbol());
        order.setOrderType(request.getOrderType());
        order.setSide(request.getSide());
        order.setPrice(new BigDecimal(request.getPrice()));
        order.setQuantity(new BigDecimal(request.getQuantity()));
        order.setExecutedQuantity(BigDecimal.ZERO);
        order.setExecutedAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.NEW);
        order.setTimeInForce(request.getTimeInForce());

        orderMapper.insert(order);

        matchingEngine.submitOrder(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("status", order.getStatus());

        return Result.success(result);
    }

    /**
     * 撤单
     * 取消未完成的订单
     *
     * <p>撤单流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 根据订单ID查询订单
     * 4. 验证订单存在性
     * 5. 验证订单所有权（只能撤销自己的订单）
     * 6. 验证订单状态（只有NEW和PARTIALLY_FILLED状态才能撤销）
     * 7. 更新订单状态为CANCELED
     * 8. 通知撮合引擎取消订单
     * 9. 返回成功信息
     *
     * <p>可撤销的订单状态：
     * - NEW：新建订单，尚未成交
     * - PARTIALLY_FILLED：部分成交，还有未成交部分
     *
     * <p>不可撤销的订单状态：
     * - FILLED：完全成交
     * - CANCELED：已经取消
     * - REJECTED：已经被拒绝
     *
     * <p>撤单后的操作：
     * - 订单状态更新为CANCELED
     * - 从撮合引擎的订单簿中移除
     * - 解冻订单冻结的资金（由撮合引擎回调处理）
     *
     * <p>部分成交的订单：
     * - 如果订单已经部分成交，撤单时：
     *   - 已成交部分保持不变
     *   - 未成交部分被取消
     *   - 未成交部分的冻结资金被解冻
     *
     * <p>事务处理：
     * - 使用@Transactional注解保证原子性
     * - 状态更新失败会回滚
     *
     * <p>安全措施：
     * - 验证订单所有权，防止恶意撤销他人订单
     * - 验证订单状态，防止重复撤单
     *
     * @param token JWT Token，用于身份验证
     * @param orderId 订单ID（32位UUID）
     * @return Result对象，成功返回"Order canceled successfully"，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     *         - "Order not found"：订单不存在
     *         - "Unauthorized"：订单不属于当前用户
     *         - "Order cannot be canceled"：订单状态不允许撤销
     */
    @Transactional
    public Result<?> cancelOrder(String token, String orderId) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Order order = orderMapper.findByOrderId(orderId);

        if (order == null) {
            return Result.error("Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            return Result.error("Unauthorized");
        }

        if (!OrderStatus.NEW.equals(order.getStatus()) &&
            !OrderStatus.PARTIALLY_FILLED.equals(order.getStatus())) {
            return Result.error("Order cannot be canceled");
        }

        order.setStatus(OrderStatus.CANCELED);
        orderMapper.update(order);

        matchingEngine.cancelOrder(order);

        return Result.success("Order canceled successfully");
    }

    /**
     * 查询单个订单
     * 根据订单ID查询订单详情
     *
     * <p>查询流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 根据订单ID查询订单
     * 4. 验证订单存在性
     * 5. 验证订单所有权（只能查询自己的订单）
     * 6. 返回订单详情
     *
     * <p>返回字段：
     * - id：数据库主键ID
     * - userId：用户ID
     * - orderId：订单ID（业务主键）
     * - symbol：交易对
     * - orderType：订单类型
     * - side：订单方向
     * - price：价格
     * - quantity：数量
     * - executedQuantity：已成交数量
     * - executedAmount：已成交金额
     * - status：订单状态
     * - timeInForce：时效类型
     * - createTime：创建时间
     * - updateTime：更新时间
     *
     * <p>使用场景：
     * - 订单详情页面
     * - 订单状态追踪
     * - 成交记录查询
     *
     * @param token JWT Token，用于身份验证
     * @param orderId 订单ID（32位UUID）
     * @return Result对象，成功返回订单详情，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     *         - "Order not found"：订单不存在
     *         - "Unauthorized"：订单不属于当前用户
     */
    public Result<?> getOrder(String token, String orderId) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Order order = orderMapper.findByOrderId(orderId);

        if (order == null) {
            return Result.error("Order not found");
        }

        if (!order.getUserId().equals(userId)) {
            return Result.error("Unauthorized");
        }

        return Result.success(order);
    }

    /**
     * 批量查询订单
     * 根据条件查询订单列表（支持多条件组合查询）
     *
     * <p>查询流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 根据查询条件组合查询订单
     *   - 如果同时指定交易对和状态：查询该交易对的指定状态订单
     *   - 如果只指定交易对：查询该交易对的所有订单
     *   - 如果只指定状态：查询所有交易对的指定状态订单
     *   - 如果都不指定：查询用户的所有订单
     * 4. 返回订单列表
     *
     * <p>查询条件：
     * - symbol：交易对（可选）
     *   - 例如：BTC-USDT、ETH-USDT
     *   - 为null时不限制交易对
     * - status：订单状态（可选）
     *   - 可选值：NEW、PARTIALLY_FILLED、FILLED、CANCELED、REJECTED
     *   - 为null时不限制状态
     *
     * <p>查询组合：
     * 1. symbol=null, status=null：查询所有订单
     * 2. symbol=BTC-USDT, status=null：查询BTC-USDT的所有订单
     * 3. symbol=null, status=NEW：查询所有NEW状态的订单
     * 4. symbol=BTC-USDT, status=FILLED：查询BTC-USDT的已完成订单
     *
     * <p>使用场景：
     * - 订单列表页面
     * - 按交易对筛选
     * - 按状态筛选
     * - 多条件组合筛选
     *
     * <p>性能优化：
     * - 建议添加分页参数（待实现）
     * - 数据量大时可能需要限制返回条数
     *
     * @param token JWT Token，用于身份验证
     * @param symbol 交易对（可选，为null表示不限制）
     * @param status 订单状态（可选，为null表示不限制）
     * @return Result对象，成功返回订单列表，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     */
    public Result<?> getOrders(String token, String symbol, String status) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        List<Order> orders;

        if (symbol != null && status != null) {
            orders = orderMapper.findByUserIdAndSymbolAndStatus(userId, symbol, status);
        } else if (symbol != null) {
            orders = orderMapper.findByUserIdAndSymbol(userId, symbol);
        } else if (status != null) {
            orders = orderMapper.findByUserIdAndStatus(userId, status);
        } else {
            orders = orderMapper.findByUserId(userId);
        }

        return Result.success(orders);
    }

    /**
     * 查询当前订单（未完成订单）
     * 查询所有未完成的订单（NEW和PARTIALLY_FILLED状态）
     *
     * <p>查询流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 根据交易对参数查询未完成订单
     *   - 如果指定交易对：查询该交易对的未完成订单
     *   - 如果不指定交易对：查询所有交易对的未完成订单
     * 4. 返回订单列表
     *
     * <p>未完成订单定义：
     * - 状态为NEW：新建订单，尚未成交
     * - 状态为PARTIALLY_FILLED：部分成交，还有未成交部分
     *
     * <p>已完成订单（不包含）：
     * - FILLED：完全成交
     * - CANCELED：已取消
     * - REJECTED：已拒绝
     *
     * <p>使用场景：
     * - 当前委托页面
     * - 挂单管理
     * - 批量撤单
     * - 计算未成交金额
     *
     * <p>与getOrders的区别：
     * - getOrders：可以查询所有状态的订单，需要手动指定状态
     * - getOpenOrders：只查询未完成订单，不需要指定状态
     *
     * @param token JWT Token，用于身份验证
     * @param symbol 交易对（可选，为null表示查询所有交易对）
     * @return Result对象，成功返回未完成订单列表，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     */
    public Result<?> getOpenOrders(String token, String symbol) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        List<Order> orders;

        if (symbol != null) {
            orders = orderMapper.findOpenOrdersByUserIdAndSymbol(userId, symbol);
        } else {
            orders = orderMapper.findOpenOrdersByUserId(userId);
        }

        return Result.success(orders);
    }

    /**
     * 查询历史订单（分页）
     * 分页查询已完成的订单（FILLED、CANCELED、REJECTED状态）
     *
     * <p>查询流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 计算分页偏移量（offset = (page - 1) × size）
     * 4. 根据交易对参数查询历史订单
     *   - 如果指定交易对：查询该交易对的历史订单
     *   - 如果不指定交易对：查询所有交易对的历史订单
     * 5. 返回分页结果
     *
     * <p>历史订单定义：
     * - FILLED：完全成交
     * - CANCELED：已取消
     * - REJECTED：已拒绝
     *
     * <p>当前订单（不包含）：
     * - NEW：新建订单
     * - PARTIALLY_FILLED：部分成交
     *
     * <p>分页参数：
     * - page：页码（从1开始）
     * - size：每页条数
     * - offset：数据库偏移量（内部计算）
     *
     * <p>分页计算示例：
     * - 第1页，每页10条：offset=0, limit=10（查询第1-10条）
     * - 第2页，每页10条：offset=10, limit=10（查询第11-20条）
     * - 第3页，每页20条：offset=40, limit=20（查询第41-60条）
     *
     * <p>使用场景：
     * - 历史订单页面
     * - 成交记录查询
     * - 导出交易报表
     * - 统计分析
     *
     * <p>性能优化：
     * - 使用索引优化查询（userId + symbol + status + createTime）
     * - 建议分页大小不超过100条
     * - 历史数据可以考虑归档到冷存储
     *
     * <p>排序规则：
     * - 按创建时间倒序排列（最新的在前）
     *
     * @param token JWT Token，用于身份验证
     * @param symbol 交易对（可选，为null表示查询所有交易对）
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return Result对象，成功返回历史订单列表，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     */
    public Result<?> getOrderHistory(String token, String symbol, int page, int size) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        int offset = (page - 1) * size;

        List<Order> orders;
        if (symbol != null) {
            orders = orderMapper.findHistoryByUserIdAndSymbol(userId, symbol, offset, size);
        } else {
            orders = orderMapper.findHistoryByUserId(userId, offset, size);
        }

        return Result.success(orders);
    }
}
