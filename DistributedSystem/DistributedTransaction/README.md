# 分布式事务完整指南

本项目提供了分布式事务的完整学习资料，包括理论文档、代码演示和实战案例。

## 📚 文档目录

### 1. [面试题](docs/面试题.md)
包含分布式事务相关的常见面试题和答案。

### 2. [原理详解](docs/原理.md)
深入讲解分布式事务的核心原理：
- 什么是分布式事务
- CAP 理论和 BASE 理论
- 2PC、3PC、TCC、Saga 等解决方案
- 本地消息表、MQ 事务消息、最大努力通知
- 分布式事务的选择和最佳实践

### 3. [核心概念](docs/核心概念.md)
详细介绍分布式事务的核心概念：
- 事务的 ACID 特性
- 分布式事务的核心问题
- 关键技术：分布式锁、幂等性、补偿机制、超时机制、重试机制
- 一致性模型：强一致性、弱一致性、最终一致性
- 性能优化和监控运维

### 4. [拓展知识](docs/拓展.md)
分布式事务的高级话题：
- 分布式事务框架对比（Seata、ShardingSphere、ByteTCC、Hmily、LCN）
- 分布式事务与微服务、消息队列、缓存、数据库的结合
- 实战经验：电商订单、金融转账、物流配送、秒杀系统
- 未来趋势：Serverless、区块链、AI、云原生

### 5. [新闻动态](docs/新闻动态.md)
分布式事务的最新发展：
- Seata 2.0 重大更新
- CNCF 分布式事务标准化
- 新兴技术：CRDT、区块链、AI
- 行业应用案例：阿里巴巴、京东、滴滴、美团
- 开源社区动态和学术研究进展

### 6. [开源方案](docs/开源方案.md)
主流开源分布式事务框架详解：
- **Seata**：支持 AT/TCC/Saga/XA 模式，功能最完善
- **Apache ShardingSphere**：与分库分表集成，支持 XA/Saga
- **ByteTCC**：专注 TCC 模式，性能好
- **Hmily**：支持 TCC/TAC 模式，支持多种 RPC 框架
- **Eventuate Tram**：基于事件驱动的 Saga 模式
- 框架对比和选择建议

## 💻 代码演示

### 1. 2PC (Two-Phase Commit) 演示

**目录**：`../src/main/java/com/distributed/transaction/twopc/`

**核心类**：
- `Participant.java` - 参与者接口
- `TwoPhaseCommitCoordinator.java` - 2PC 协调器
- `OrderServiceParticipant.java` - 订单服务参与者
- `StockServiceParticipant.java` - 库存服务参与者
- `AccountServiceParticipant.java` - 账户服务参与者
- `TwoPhaseCommitDemo.java` - 演示程序

**运行方式**：
```bash
# 编译
javac -d target/classes src/main/java/com/distributed/transaction/twopc/*.java

# 运行
java -cp target/classes com.distributed.transaction.twopc.TwoPhaseCommitDemo
```

**演示场景**：
1. 正常流程：所有参与者都成功，事务提交
2. 余额不足：账户服务准备失败，事务回滚
3. 库存不足：库存服务准备失败，事务回滚

**特点**：
- 强一致性
- 同步阻塞
- 单点故障风险
- 性能较低

### 2. TCC (Try-Confirm-Cancel) 演示

**目录**：`../src/main/java/com/distributed/transaction/tcc/`

**核心类**：
- `TCCParticipant.java` - TCC 参与者接口
- `TCCTransactionContext.java` - TCC 事务上下文
- `TCCTransactionCoordinator.java` - TCC 协调器
- `OrderServiceTCCParticipant.java` - 订单服务 TCC 参与者
- `StockServiceTCCParticipant.java` - 库存服务 TCC 参与者
- `AccountServiceTCCParticipant.java` - 账户服务 TCC 参与者
- `TCCDemo.java` - 演示程序

**运行方式**：
```bash
# 编译
javac -d target/classes src/main/java/com/distributed/transaction/tcc/*.java

# 运行
java -cp target/classes com.distributed.transaction.tcc.TCCDemo
```

**演示场景**：
1. 正常流程：Try → Confirm，事务成功
2. 幂等性测试：重复执行同一事务，不会重复扣减资源
3. 余额不足：Try 失败 → Cancel，事务回滚
4. 库存不足：Try 失败 → Cancel，事务回滚

**特点**：
- 最终一致性
- 高性能，不需要长时间锁定资源
- 业务侵入大，需要实现三个接口
- Confirm 和 Cancel 必须支持幂等性

### 3. Saga 模式演示

**目录**：`../src/main/java/com/distributed/transaction/saga/`

**即将添加**：Saga 模式的代码演示，包括协调式和编排式两种实现方式。

### 4. Seata 集成示例

**目录**：`../src/main/java/com/distributed/transaction/seata/`

**即将添加**：Seata 框架的集成示例，演示 AT、TCC、Saga 模式的实际应用。

## 🎯 学习路径

### 初级（1-2 周）
1. 阅读[原理详解](docs/原理.md)，理解分布式事务的基本概念
2. 学习 CAP 理论和 BASE 理论
3. 了解 2PC、TCC、Saga 等基本解决方案
4. 运行 2PC 代码演示，理解两阶段提交的流程

### 中级（2-4 周）
1. 阅读[核心概念](docs/核心概念.md)，深入理解关键技术
2. 学习幂等性、补偿机制、超时处理等
3. 运行 TCC 代码演示，理解 TCC 模式的实现
4. 阅读[开源方案](docs/开源方案.md)，了解主流框架

### 高级（4-8 周）
1. 阅读[拓展知识](docs/拓展.md)，学习高级话题
2. 研究分布式事务与微服务、消息队列、缓存的结合
3. 学习实战案例：电商订单、金融转账等
4. 实践 Seata 框架，完成实际项目

### 专家级（持续学习）
1. 关注[新闻动态](docs/新闻动态.md)，了解最新发展
2. 参与开源社区，贡献代码
3. 研究学术论文，探索新技术
4. 分享经验，帮助他人

## 📖 推荐阅读

### 书籍
- 《分布式事务实战》
- 《微服务架构下的分布式事务》
- 《云原生分布式事务》

### 在线资源
- [Seata 官方文档](https://seata.io/zh-cn/)
- [Apache ShardingSphere 官方文档](https://shardingsphere.apache.org/)
- [阿里云开发者社区](https://developer.aliyun.com/)
- [InfoQ 中文站](https://www.infoq.cn/)

### 视频教程
- B 站：Seata 实战教程
- 极客时间：分布式事务实战课
- 慕课网：微服务分布式事务

## 🤝 贡献指南

欢迎贡献代码、文档或提出建议！

### 贡献方式
1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 贡献内容
- 完善文档
- 添加代码示例
- 修复错误
- 提出改进建议
- 分享实战经验

## 📝 更新日志

### 2026-01-26
- ✅ 创建原理详解文档
- ✅ 创建核心概念文档
- ✅ 创建拓展知识文档
- ✅ 创建新闻动态文档
- ✅ 创建开源方案文档
- ✅ 实现 2PC 代码演示
- ✅ 实现 TCC 代码演示
- ✅ 代码迁移到 src/main/java 标准目录结构
- 🚧 Saga 模式演示（计划中）
- 🚧 Seata 集成示例（计划中）

## 📧 联系方式

如有问题或建议，欢迎通过以下方式联系：
- 提交 Issue
- 发送 Pull Request
- 邮件联系

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 🙏 致谢

感谢以下开源项目和社区：
- [Seata](https://github.com/seata/seata)
- [Apache ShardingSphere](https://github.com/apache/shardingsphere)
- [ByteTCC](https://github.com/liuyangming/ByteTCC)
- [Hmily](https://github.com/dromara/hmily)
- [Eventuate Tram](https://github.com/eventuate-tram/eventuate-tram-core)

---

**注意**：本项目仅供学习和参考，生产环境使用请充分测试并根据实际情况调整。
