# OKX Finance - 数字货币交易所微服务系统

## 项目简介

OKX Finance 是一个基于微服务架构的完整数字货币交易所系统，参考 OKX 交易所设计，实现了交易所的核心功能模块。

## 系统架构

### 技术栈
- Java 17
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- MyBatis 3.0.3
- MySQL 8.0
- Redis
- JWT 认证

### 微服务模块

```
Finance (父模块)
├── finance-common          # 公共模块
├── finance-gateway         # API 网关 (端口: 8080)
├── finance-user            # 用户服务 (端口: 8081)
├── finance-account         # 账户服务 (端口: 8082)
├── finance-trading         # 交易服务 (端口: 8083)
├── finance-market          # 行情服务 (端口: 8084)
├── finance-wallet          # 钱包服务 (端口: 8085)
├── finance-risk            # 风控服务 (端口: 8086)
└── finance-notification    # 通知服务 (端口: 8087)
```

## 核心功能

### 1. 用户服务 (finance-user)
- 用户注册与登录
- KYC 实名认证（初级、中级、高级）
- API Key 管理
- 用户信息管理

**主要接口：**
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `POST /api/user/kyc` - KYC 认证
- `GET /api/user/info` - 获取用户信息
- `POST /api/user/apikey/generate` - 生成 API Key

### 2. 账户服务 (finance-account)
- 多币种账户管理
- 资产余额查询
- 资金划转（币币转换）
- 资金冻结/解冻

**主要接口：**
- `GET /api/account/balance` - 查询余额
- `GET /api/account/balances` - 查询所有余额
- `POST /api/account/transfer` - 资金划转
- `POST /api/account/freeze` - 冻结资金
- `POST /api/account/unfreeze` - 解冻资金

### 3. 交易服务 (finance-trading)
- 限价单/市价单下单
- 订单撤销
- 订单查询
- 实时撮合引擎
- 订单历史记录

**主要接口：**
- `POST /api/trading/order` - 下单
- `DELETE /api/trading/order/{orderId}` - 撤单
- `GET /api/trading/order/{orderId}` - 查询订单
- `GET /api/trading/orders` - 查询订单列表
- `GET /api/trading/openOrders` - 查询当前委托
- `GET /api/trading/orderHistory` - 订单历史

**撮合引擎特性：**
- 价格优先、时间优先原则
- 支持市价单和限价单
- 异步撮合，高性能处理
- 部分成交支持

### 4. 行情服务 (finance-market)
- 实时行情数据
- K线数据（多周期）
- 市场深度数据
- 最新成交记录
- WebSocket 推送（规划中）

**主要接口：**
- `GET /api/market/ticker/{symbol}` - 获取行情
- `GET /api/market/depth/{symbol}` - 获取深度
- `GET /api/market/klines/{symbol}` - 获取K线
- `GET /api/market/trades/{symbol}` - 获取成交记录

### 5. 钱包服务 (finance-wallet)
- 充值地址管理
- 充值处理
- 提现申请与审核
- 充提历史记录

**主要接口：**
- `POST /api/wallet/deposit/address` - 获取充值地址
- `POST /api/wallet/deposit` - 充值
- `POST /api/wallet/withdraw` - 提现
- `GET /api/wallet/deposits` - 充值历史
- `GET /api/wallet/withdrawals` - 提现历史

### 6. 风控服务 (finance-risk)
- 交易风险控制
- 提现风险控制
- 反洗钱（AML）检测
- 异常行为监控

### 7. 通知服务 (finance-notification)
- 邮件通知
- 短信通知
- 站内消息推送

### 8. API 网关 (finance-gateway)
- 统一入口
- 路由转发
- JWT 认证
- CORS 跨域处理
- 请求限流（规划中）

## 数据库设计

系统采用分库设计，每个服务使用独立的数据库：

- `okx_user` - 用户服务数据库
- `okx_account` - 账户服务数据库
- `okx_trading` - 交易服务数据库
- `okx_wallet` - 钱包服务数据库

详细表结构请参考：`docs/database/init.sql`

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 1. 初始化数据库

```bash
mysql -u root -p < docs/database/init.sql
```

### 2. 修改配置

修改各服务的 `application.yml` 文件，配置数据库和 Redis 连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/okx_user
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 3. 编译项目

```bash
cd Finance
mvn clean install
```

### 4. 启动服务

按以下顺序启动各服务：

```bash
# 1. 启动网关
cd finance-gateway
mvn spring-boot:run

# 2. 启动用户服务
cd finance-user
mvn spring-boot:run

# 3. 启动账户服务
cd finance-account
mvn spring-boot:run

# 4. 启动交易服务
cd finance-trading
mvn spring-boot:run

# 5. 启动行情服务
cd finance-market
mvn spring-boot:run

# 6. 启动钱包服务
cd finance-wallet
mvn spring-boot:run

# 7. 启动风控服务
cd finance-risk
mvn spring-boot:run

# 8. 启动通知服务
cd finance-notification
mvn spring-boot:run
```

### 5. 测试接口

所有接口统一通过网关访问：`http://localhost:8080`

**示例：用户注册**
```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138000"
  }'
```

**示例：用户登录**
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

**示例：下单交易（需要先登录获取 token）**
```bash
curl -X POST http://localhost:8080/api/trading/order \
  -H "Content-Type: application/json" \
  -H "Authorization: YOUR_JWT_TOKEN" \
  -d '{
    "symbol": "BTC-USDT",
    "orderType": "LIMIT",
    "side": "BUY",
    "price": "50000",
    "quantity": "0.1"
  }'
```

## API 文档

### 认证方式

除了以下白名单接口外，所有接口都需要在请求头中携带 JWT Token：

```
Authorization: YOUR_JWT_TOKEN
```

**白名单接口（无需认证）：**
- `/api/user/register` - 用户注册
- `/api/user/login` - 用户登录
- `/api/market/**` - 所有行情接口

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

- `code`: 状态码（200-成功，500-失败，401-未授权）
- `message`: 提示信息
- `data`: 返回数据

## 系统特性

### 1. 微服务架构
- 服务独立部署
- 易于扩展和维护
- 故障隔离

### 2. 高性能撮合引擎
- 内存撮合
- 异步处理
- 价格时间优先

### 3. 安全性
- JWT 认证
- 密码 MD5 加密
- API Key/Secret 机制
- 风控系统

### 4. 高可用
- 服务注册发现（Eureka）
- 负载均衡
- 熔断降级（规划中）

### 5. 数据一致性
- 事务管理
- 乐观锁
- 分布式事务（规划中）

## 技术亮点

1. **撮合引擎**：自研高性能撮合引擎，支持限价单、市价单
2. **分布式ID**：雪花算法生成全局唯一 ID
3. **JWT 认证**：无状态认证，易于扩展
4. **微服务治理**：Spring Cloud 完整生态
5. **数据库分库**：按业务模块分库，降低耦合

## 后续规划

- [ ] WebSocket 实时推送
- [ ] 合约交易功能
- [ ] 杠杆交易功能
- [ ] 配置中心（Nacos）
- [ ] 服务监控（Prometheus + Grafana）
- [ ] 链路追踪（Zipkin）
- [ ] 分布式事务（Seata）
- [ ] 消息队列（RocketMQ/Kafka）
- [ ] 前端管理系统

## 项目结构

```
Finance/
├── finance-common/                 # 公共模块
│   └── src/main/java/com/okx/finance/common/
│       ├── entity/                 # 实体类
│       ├── dto/                    # 数据传输对象
│       ├── constant/               # 常量定义
│       └── util/                   # 工具类
├── finance-gateway/                # API网关
├── finance-user/                   # 用户服务
├── finance-account/                # 账户服务
├── finance-trading/                # 交易服务
│   └── src/main/java/com/okx/finance/trading/
│       ├── controller/             # 控制器
│       ├── service/                # 业务逻辑
│       ├── mapper/                 # 数据访问
│       ├── engine/                 # 撮合引擎
│       └── dto/                    # 数据传输对象
├── finance-market/                 # 行情服务
├── finance-wallet/                 # 钱包服务
├── finance-risk/                   # 风控服务
├── finance-notification/           # 通知服务
├── docs/                           # 文档
│   └── database/                   # 数据库脚本
└── pom.xml                         # 父POM配置
```

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License
