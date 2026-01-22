# Seata 分布式事务示例

## 核心原理

### 1. Seata架构
```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ Order    │  │ Storage  │  │ Account  │              │
│  │ Service  │  │ Service  │  │ Service  │              │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘              │
│       │TM          │RM          │RM                     │
└───────┼────────────┼────────────┼─────────────────────────┘
        │            │            │
        │   ┌────────▼────────────▼────────┐
        │   │   Transaction Coordinator    │ TC
        └──▶│      (Seata Server)          │
            └──────────────────────────────┘
                     │
            ┌────────┴────────┐
            │                 │
        ┌───▼───┐      ┌─────▼─────┐
        │ MySQL │      │   Nacos   │
        │  DB   │      │  Config   │
        └───────┘      └───────────┘
```

### 2. AT模式原理

AT(Automatic Transaction)模式是Seata最常用的分布式事务模式，核心原理：

#### 第一阶段（执行阶段）
1. 解析SQL语义，生成对应的UNDO日志
2. 执行业务SQL
3. 生成行锁
4. 提交本地事务（释放本地锁，业务数据和UNDO日志同时提交）
5. 上报执行结果给TC

#### 第二阶段（完成阶段）
- **提交：** 异步删除UNDO日志即可，秒级完成
- **回滚：** 通过UNDO日志自动生成补偿SQL，完成数据回滚

#### 数据示例
执行 `UPDATE product SET count = count - 1 WHERE id = 1`

**前镜像（Before Image）：**
```json
{
  "id": 1,
  "count": 10
}
```

**后镜像（After Image）：**
```json
{
  "id": 1,
  "count": 9
}
```

**回滚时：** 根据前镜像生成补偿SQL
```sql
UPDATE product SET count = 10 WHERE id = 1
```

### 3. TCC模式原理

TCC(Try-Confirm-Cancel)是两阶段提交的业务层实现：

- **Try：** 尝试执行，完成所有业务检查，预留必需的业务资源
- **Confirm：** 确认执行，真正执行业务，不做任何业务检查，只使用Try阶段预留的资源
- **Cancel：** 取消执行，释放Try阶段预留的业务资源

```java
@LocalTCC
public interface AccountTccService {

    @TwoPhaseBusinessAction(name = "deduct", commitMethod = "confirm", rollbackMethod = "cancel")
    boolean deduct(@BusinessActionContextParameter(paramName = "userId") Long userId,
                   @BusinessActionContextParameter(paramName = "money") BigDecimal money);

    boolean confirm(BusinessActionContext context);

    boolean cancel(BusinessActionContext context);
}
```

### 4. SAGA模式原理

SAGA模式适用于长事务场景，核心是通过状态机编排服务调用：

```json
{
  "Name": "ReduceInventoryAndBalance",
  "Comment": "reduce inventory then reduce balance in a transaction",
  "StartState": "ReduceInventory",
  "States": {
    "ReduceInventory": {
      "Type": "ServiceTask",
      "ServiceName": "storageService",
      "ServiceMethod": "reduce",
      "CompensateState": "CompensateReduceInventory",
      "Next": "ReduceBalance"
    },
    "ReduceBalance": {
      "Type": "ServiceTask",
      "ServiceName": "accountService",
      "ServiceMethod": "reduce",
      "CompensateState": "CompensateReduceBalance"
    }
  }
}
```

### 5. XA模式原理

XA模式是传统的两阶段提交协议，强一致性：

- **优点：** 强一致性，无需编写补偿逻辑
- **缺点：** 性能较差，资源锁定时间长

## 业务应用场景

### 1. 电商订单场景
用户下单涉及多个服务的数据一致性：
- 订单服务：创建订单记录
- 库存服务：扣减商品库存
- 账户服务：扣减用户余额
- 积分服务：增加用户积分

**场景痛点：** 任何一个服务失败，其他服务的操作需要回滚

**Seata解决方案：** 使用AT模式，自动实现分布式事务
```java
@GlobalTransactional
public Order createOrder(OrderDTO order) {
    orderService.create(order);      // 创建订单
    storageService.deduct(order);    // 扣减库存
    accountService.deduct(order);    // 扣减余额
    pointService.add(order);         // 增加积分
    return order;
}
```

### 2. 金融转账场景
跨账户转账需要保证：
- A账户扣款成功
- B账户入账成功
- 交易记录创建成功

**场景痛点：** 资金安全至关重要，不能出现扣款成功但入账失败的情况

**Seata解决方案：** 使用TCC模式，精确控制资金流转
```java
// Try阶段：冻结金额
accountService.freeze(fromAccount, amount);
accountService.freeze(toAccount, -amount);

// Confirm阶段：真实扣款和入账
accountService.transfer(fromAccount, toAccount, amount);

// Cancel阶段：解冻金额
accountService.unfreeze(fromAccount, amount);
```

### 3. 物流系统场景
订单发货涉及：
- 订单服务：更新订单状态为已发货
- 库存服务：更新实物库存
- 物流服务：创建物流记录
- 通知服务：发送发货通知

**场景痛点：** 服务调用链长，任何环节失败都需要回滚前序操作

**Seata解决方案：** 使用SAGA模式，编排长流程事务

### 4. 秒杀场景
高并发秒杀需要：
- 扣减库存
- 创建订单
- 创建支付单

**场景痛点：** 高并发下的数据一致性和性能平衡

**Seata解决方案：** 结合AT模式和全局锁机制
```java
@GlobalTransactional
@GlobalLock  // 全局锁，防止库存超卖
public Order seckill(Long productId, Long userId) {
    storageService.deduct(productId, 1);
    return orderService.create(productId, userId);
}
```

## 数据库准备

### 1. 创建业务数据库
```sql
-- 订单数据库
CREATE DATABASE seata_order;

-- 库存数据库
CREATE DATABASE seata_storage;

-- 账户数据库
CREATE DATABASE seata_account;
```

### 2. 创建UNDO_LOG表（每个数据库都需要）
```sql
CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

### 3. 创建业务表
```sql
-- 订单表
CREATE TABLE `t_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `money` decimal(10,2) DEFAULT NULL,
  `status` int(11) DEFAULT NULL COMMENT '订单状态：0-创建中；1-已完结；2-已取消',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 库存表
CREATE TABLE `t_storage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) DEFAULT NULL,
  `total` int(11) DEFAULT NULL COMMENT '总库存',
  `used` int(11) DEFAULT NULL COMMENT '已用库存',
  `residue` int(11) DEFAULT NULL COMMENT '剩余库存',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 账户表
CREATE TABLE `t_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `total` decimal(10,2) DEFAULT NULL COMMENT '总额度',
  `used` decimal(10,2) DEFAULT NULL COMMENT '已用额度',
  `residue` decimal(10,2) DEFAULT NULL COMMENT '剩余额度',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

## Seata Server配置

### 1. 下载Seata Server
```bash
# 下载地址
https://github.com/seata/seata/releases

# 推荐版本：1.7.0+
```

### 2. 配置文件（conf/application.yml）
```yaml
seata:
  config:
    type: nacos
    nacos:
      server-addr: localhost:8848
      namespace: public
      group: SEATA_GROUP
      data-id: seataServer.properties

  registry:
    type: nacos
    nacos:
      application: seata-server
      server-addr: localhost:8848
      namespace: public
      group: SEATA_GROUP

  store:
    mode: db
    db:
      datasource: druid
      db-type: mysql
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/seata?useSSL=false&serverTimezone=Asia/Shanghai
      user: root
      password: root
```

### 3. 在Nacos中配置
Data ID: `seataServer.properties`
Group: `SEATA_GROUP`

```properties
# 存储模式
store.mode=db
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://localhost:3306/seata?useSSL=false
store.db.user=root
store.db.password=root

# 事务分组配置
service.vgroupMapping.default_tx_group=default
```

### 4. 启动Seata Server
```bash
# Windows
seata-server.bat -p 8091 -m db

# Linux
sh seata-server.sh -p 8091 -m db
```

## 测试步骤

### 1. 启动服务
```bash
# 1. 启动Nacos
# 2. 启动Seata Server
# 3. 启动订单服务
# 4. 启动库存服务
# 5. 启动账户服务
```

### 2. 测试正常场景
```bash
curl -X POST "http://localhost:8091/order/create?userId=1&productId=1&count=1&money=100"
```

预期结果：订单创建成功，库存扣减，余额扣减

### 3. 测试异常回滚场景
```bash
curl -X POST "http://localhost:8091/order/create-with-exception?userId=1&productId=1&count=1&money=100"
```

预期结果：抛出异常，所有操作回滚，数据保持一致

## 监控和排查

### 1. 查看全局事务
Seata Server控制台可以看到全局事务的执行情况

### 2. 查看UNDO_LOG
```sql
SELECT * FROM undo_log ORDER BY log_created DESC LIMIT 10;
```

### 3. 常见问题

**问题1：XID未传播**
- 检查Feign配置
- 确认请求头中包含TX_XID

**问题2：事务无法回滚**
- 检查UNDO_LOG是否正常记录
- 确认数据源代理配置正确

**问题3：性能问题**
- AT模式会记录前后镜像，有一定性能损耗
- 考虑使用TCC模式优化性能

## 最佳实践

1. **选择合适的模式**
   - AT模式：适合简单业务场景，自动化程度高
   - TCC模式：适合对数据一致性要求高的场景
   - SAGA模式：适合长事务场景
   - XA模式：适合对一致性要求极高的场景

2. **超时配置**
   - 合理设置全局事务超时时间
   - 考虑各服务的处理时间

3. **幂等性设计**
   - Confirm和Cancel操作必须支持幂等
   - 防止重复提交和回滚

4. **监控告警**
   - 监控全局事务失败率
   - 监控UNDO_LOG堆积情况
   - 设置合理的告警阈值
