# 场景九：微信小程序订阅消息推送异常分析

## 业务背景

在小程序开发中，订阅消息是向用户推送服务通知的重要功能。然而，经常遇到一个困扰开发者的问题：

**现象**：调用订阅消息发送接口返回错误码 43101（用户拒绝接受消息），但用户在小程序端明明显示已经订阅了消息。

这种前后端状态不一致的问题会导致：
1. 用户无法收到重要的服务通知
2. 用户体验下降，产生投诉
3. 开发者难以定位问题根因
4. 浪费大量排查时间

## 问题描述

### 典型场景

某电商小程序的订单通知场景：
- 用户下单时，点击弹窗授权订阅"订单状态更新通知"
- 小程序调用 `wx.requestSubscribeMessage()` 成功，返回 `{ errMsg: "requestSubscribeMessage:ok" }`
- 用户在小程序"消息订阅管理"页面看到该模板处于"接受"状态
- 但后端调用发送接口时，返回：`{ errcode: 43101, errmsg: "user refuse to accept the msg" }`

### 用户困惑

"明明我开启了订阅，为什么还收不到消息？"

## 核心问题分析

### 错误码 43101 的官方定义

根据微信官方文档，错误码 43101 表示：

> **用户拒绝接受消息**。如果用户之前曾经订阅过，则表示用户取消了订阅关系。

来源：[发送订阅消息接口文档](https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/subscribe-message/subscribeMessage.send.html)

### 腾讯订阅消息底层逻辑分析

#### 1. 订阅消息的分类

微信订阅消息分为两种类型：

**一次性订阅消息**（most common）：
- 用户每次授权只能发送**一条**消息
- 订阅后立即生成一个"消息凭证"，发送后即销毁
- 如果要再次发送，必须用户重新授权

**长期订阅消息**（仅限特殊行业）：
- 仅限政务、医疗、交通、金融、教育等特定行业
- 需要单独申请权限
- 用户一次授权可以多次发送

#### 2. 订阅状态的存储机制

```
用户订阅状态存储结构（推测）：

微信服务器端：
┌─────────────────────────────────────────┐
│ 用户订阅关系表                             │
│ ┌──────────┬────────┬──────┬─────────┐  │
│ │ 用户ID    │模板ID   │剩余次数│ 状态   │  │
│ ├──────────┼────────┼──────┼─────────┤  │
│ │ user_123 │ tpl_01 │  0   │ CONSUMED│  │  ← 43101 来源
│ │ user_123 │ tpl_02 │  1   │ ACTIVE  │  │
│ └──────────┴────────┴──────┴─────────┘  │
└─────────────────────────────────────────┘

小程序客户端（缓存）：
┌─────────────────────────────────────────┐
│ 本地订阅状态（仅用于UI展示）                │
│ ┌──────────┬────────┬─────────────┐     │
│ │ 模板ID    │ 状态   │ 更新时间      │     │
│ ├──────────┼────────┼─────────────┤     │
│ │ tpl_01   │ 接受   │ 2026-01-10  │     │  ← 用户看到的
│ │ tpl_02   │ 接受   │ 2026-01-11  │     │
│ └──────────┴────────┴─────────────┘     │
└─────────────────────────────────────────┘
```

**关键发现**：小程序端的"接受/拒绝"状态**只是UI展示状态**，不代表实际可发送次数！

#### 3. 状态不同步的根本原因

```
用户操作流程：
1. 用户点击授权  →  剩余次数 +1
2. 后端发送消息  →  剩余次数 -1 (变为0)
3. 用户查看设置  →  仍显示"接受"（因为用户没有主动拒绝）
4. 后端再次发送  →  43101 (剩余次数为0)
```

**核心矛盾**：
- 服务端判断依据：**剩余可发送次数**（精确计数）
- 客户端展示依据：**用户最后一次操作**（接受/拒绝开关）

这两个状态由不同的维度控制，导致不一致！

### 4. 腾讯为什么这样设计？

#### 设计目的（推测）
1. **防骚扰**：严格限制推送频率，防止商家滥用
2. **用户体验**：让用户明确知道每次授权只能换一条通知
3. **降低误操作**：用户不小心关闭后，可以重新打开

#### 设计缺陷
- **状态展示不透明**：用户无法看到"剩余次数"
- **开发者困惑**：错误码提示与实际情况不符
- **运营困难**：无法精确预测消息发送成功率

## 触发 43101 错误的所有情况

### 情况1：一次性订阅已消费

```java
// 场景：用户授权1次，后端发送2次

// 第1次授权
wx.requestSubscribeMessage({ tmplIds: ['tmpl_001'] })
// 后端发送第1条 ✅ 成功

// 后端发送第2条 ❌ 43101
// 原因：一次性订阅次数已用完
```

**判断方法**：
- 查看后端日志，是否对同一个模板、同一个用户连续发送了多次
- 检查业务逻辑，是否有重复触发推送的BUG

### 情况2：用户取消订阅

```java
// 场景：用户主动在设置中关闭

用户进入：设置 → 通知管理 → 订阅消息 → 关闭某个模板
// 后端发送 ❌ 43101
```

**判断方法**：
- 让用户截图小程序"订阅消息"设置页面
- 确认对应模板是否为"接受"状态

### 情况3："总是保持以上选择"的陷阱

```java
// 最坑的情况：
1. 用户首次授权时，勾选了"总是保持以上选择，不再询问"，并选择"拒绝"
2. 后来用户在设置中手动改为"接受"
3. 但由于之前勾选了"总是保持"，系统记录的是"永久拒绝"
4. 即使UI显示"接受"，实际发送仍然 43101

// 这是腾讯的已知BUG！
```

**官方社区讨论**：
- [订阅消息后，为什么直接提示user refuse to accept](https://developers.weixin.qq.com/community/develop/doc/000666487a0928a452b03929266400)
- [subscribeMessage开始能发送消息，但后来手动不接收消息，再次开启后，怎就一直接收不了](https://developers.weixin.qq.com/community/develop/doc/000004239a0138c769696d4f256400)

### 情况4：模板被封禁

```java
// 场景：模板因违规被微信封禁

// 可能原因：
1. 模板内容包含敏感词
2. 投诉率过高
3. 未按规范使用（如订单类模板发送营销内容）

// 表现：所有用户都返回 43101
```

**判断方法**：
- 登录小程序后台，查看模板状态
- 检查是否有"已停用"或"审核中"标识

### 情况5：用户从未订阅

```java
// 场景：后端直接调用发送接口，但用户从未授权

// 常见于：
1. 测试环境直接调用
2. 从其他渠道获取的openid，但用户从未在小程序中操作
3. 业务逻辑错误，未判断用户是否已授权
```

### 情况6：订阅后超过有效期

```java
// 一次性订阅的有效期：

// 官方规则（可能）：
用户授权后，如果长时间不发送，凭证可能过期
// 推测有效期：30天（未官方明确）

// 建议：授权后尽快发送，不要长期囤积
```

## 完整排查流程

### 1. 前端排查清单

```javascript
// 检查订阅授权代码
wx.requestSubscribeMessage({
  tmplIds: ['tmpl_001'],
  success(res) {
    console.log('订阅结果：', res)
    // ✅ 必须检查每个模板的订阅结果
    if (res['tmpl_001'] === 'accept') {
      // 订阅成功，通知后端
      api.notifySubscribed('tmpl_001')
    } else {
      // 订阅失败，记录日志
      console.error('订阅失败：', res['tmpl_001'])
    }
  },
  fail(err) {
    console.error('订阅失败：', err)
  }
})

// 检查订阅设置
wx.getSetting({
  withSubscriptions: true,
  success(res) {
    console.log('订阅状态：', res.subscriptionsSetting)
    // 输出示例：
    // {
    //   mainSwitch: true,  // 订阅消息总开关
    //   itemSettings: {
    //     'tmpl_001': 'accept'  // 具体模板状态
    //   }
    // }
  }
})
```

### 2. 后端排查清单

```java
// 检查1：是否重复发送
SELECT COUNT(*) FROM message_log
WHERE user_id = 'xxx' AND template_id = 'tmpl_001' AND DATE(created_at) = CURDATE();
// 如果 > 1，说明重复发送

// 检查2：用户是否有订阅记录
SELECT * FROM user_subscription
WHERE user_id = 'xxx' AND template_id = 'tmpl_001' AND status = 'ACTIVE';
// 如果没有记录，说明用户从未订阅

// 检查3：检查发送日志
{
  "userId": "xxx",
  "templateId": "tmpl_001",
  "sendTime": "2026-01-14 10:00:00",
  "response": {
    "errcode": 43101,
    "errmsg": "user refuse to accept the msg",
    "msgid": ""
  }
}
```

### 3. 用户侧排查清单

让用户提供以下信息：
1. **截图**：小程序"设置-订阅消息"页面
2. **操作记录**：是否勾选过"总是保持以上选择"
3. **重现步骤**：
   - 删除小程序
   - 重新进入
   - 重新授权订阅
   - 测试是否能收到消息

## 解决方案

### 方案1：限制发送频率（治标）

```java
@Service
public class WechatSubscribeMessageService {

    @Autowired
    private RedisTemplate<String, String> redis;

    /**
     * 发送订阅消息（带防重校验）
     */
    public SendResult sendMessage(String userId, String templateId, Map<String, String> data) {
        String key = "wx:subscribe:sent:" + userId + ":" + templateId;

        // 检查今日是否已发送
        if (redis.hasKey(key)) {
            return SendResult.fail("今日已发送过该消息");
        }

        // 调用微信接口
        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(userId);
        message.setTemplateId(templateId);
        message.setData(data);

        try {
            wxMaService.getMsgService().sendSubscribeMsg(message);

            // 发送成功，记录已发送标记（24小时过期）
            redis.opsForValue().set(key, "1", 24, TimeUnit.HOURS);

            return SendResult.success();
        } catch (WxErrorException e) {
            if (e.getError().getErrorCode() == 43101) {
                // 43101错误，记录到数据库，提醒用户重新订阅
                saveFailedRecord(userId, templateId, "用户拒绝接收或订阅已消费");
            }
            return SendResult.fail(e.getMessage());
        }
    }
}
```

### 方案2：引导用户重新订阅（治本）

```java
// 前端：检测到43101后，引导用户重新订阅

async function sendOrderNotification(orderId) {
  // 1. 后端尝试发送
  const result = await api.sendSubscribeMessage(orderId)

  // 2. 如果返回43101，引导用户重新订阅
  if (result.errcode === 43101) {
    wx.showModal({
      title: '订阅消息已失效',
      content: '您的订阅次数已用完，需要重新订阅才能接收订单通知',
      confirmText: '重新订阅',
      success(res) {
        if (res.confirm) {
          // 引导用户重新授权
          wx.requestSubscribeMessage({
            tmplIds: ['tmpl_order_update'],
            success(subRes) {
              if (subRes['tmpl_order_update'] === 'accept') {
                // 重新订阅成功，再次发送
                api.sendSubscribeMessage(orderId)
                wx.showToast({ title: '订阅成功' })
              }
            }
          })
        }
      }
    })
  }
}
```

### 方案3：订阅次数管理（推荐）

```java
/**
 * 订阅记录表
 */
@Entity
@Table(name = "wechat_subscription")
public class WechatSubscription {
    @Id
    private Long id;

    private String userId;
    private String openid;
    private String templateId;

    /**
     * 剩余可发送次数
     */
    private Integer remainingCount;

    /**
     * 订阅时间
     */
    private LocalDateTime subscribeTime;

    /**
     * 过期时间（授权后30天）
     */
    private LocalDateTime expireTime;

    /**
     * 状态：ACTIVE-有效, CONSUMED-已消费, EXPIRED-已过期
     */
    private String status;
}

@Service
public class SubscriptionManager {

    /**
     * 用户订阅时调用（前端授权成功后调用）
     */
    public void onUserSubscribe(String userId, String templateId) {
        WechatSubscription subscription = new WechatSubscription();
        subscription.setUserId(userId);
        subscription.setTemplateId(templateId);
        subscription.setRemainingCount(1);  // 一次性订阅
        subscription.setSubscribeTime(LocalDateTime.now());
        subscription.setExpireTime(LocalDateTime.now().plusDays(30));
        subscription.setStatus("ACTIVE");

        subscriptionRepo.save(subscription);
    }

    /**
     * 发送前检查
     */
    public boolean canSend(String userId, String templateId) {
        WechatSubscription subscription = subscriptionRepo
            .findByUserIdAndTemplateIdAndStatus(userId, templateId, "ACTIVE");

        if (subscription == null) {
            return false;
        }

        if (subscription.getRemainingCount() <= 0) {
            return false;
        }

        if (subscription.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    /**
     * 发送后扣减次数
     */
    public void onMessageSent(String userId, String templateId) {
        WechatSubscription subscription = subscriptionRepo
            .findByUserIdAndTemplateIdAndStatus(userId, templateId, "ACTIVE");

        if (subscription != null) {
            subscription.setRemainingCount(subscription.getRemainingCount() - 1);
            if (subscription.getRemainingCount() <= 0) {
                subscription.setStatus("CONSUMED");
            }
            subscriptionRepo.save(subscription);
        }
    }

    /**
     * 处理43101错误
     */
    public void handle43101Error(String userId, String templateId) {
        WechatSubscription subscription = subscriptionRepo
            .findByUserIdAndTemplateId(userId, templateId);

        if (subscription != null) {
            subscription.setStatus("CONSUMED");
            subscriptionRepo.save(subscription);
        }

        // 记录到通知表，提醒运营人员
        notifyOps("用户 " + userId + " 的订阅已失效，需引导重新订阅");
    }
}
```

### 方案4："总是保持选择"BUG的终极解决方案

```java
// 前端：提醒用户不要勾选"总是保持"

// 方法1：在授权弹窗前显示提示
wx.showModal({
  title: '温馨提示',
  content: '即将打开订阅授权，请不要勾选"总是保持以上选择"，以免影响后续接收消息',
  confirmText: '我知道了',
  success(res) {
    if (res.confirm) {
      wx.requestSubscribeMessage({
        tmplIds: ['tmpl_001']
      })
    }
  }
})

// 方法2：提供"重置订阅"功能
async function resetSubscription() {
  wx.showModal({
    title: '重置订阅',
    content: '如果无法接收消息，请删除小程序后重新进入，即可重置订阅状态',
    confirmText: '知道了'
  })
}

// 方法3：后端定期清理异常状态
@Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点
public void cleanExpiredSubscriptions() {
    // 清理30天前的订阅记录
    subscriptionRepo.deleteByExpireTimeBefore(LocalDateTime.now().minusDays(30));

    // 清理已消费的订阅记录（保留7天用于统计）
    subscriptionRepo.deleteByStatusAndCreatedAtBefore("CONSUMED", LocalDateTime.now().minusDays(7));
}
```

### 方案5：多模板轮换策略

```java
/**
 * 适用于需要频繁推送的场景（如电商订单）
 * 原理：准备3个内容相似的模板，轮换发送
 */
@Service
public class MultiTemplateStrategy {

    private static final List<String> ORDER_TEMPLATES = Arrays.asList(
        "tmpl_order_01",  // 订单状态更新通知
        "tmpl_order_02",  // 订单进度提醒
        "tmpl_order_03"   // 物流信息通知
    );

    /**
     * 轮换发送
     */
    public void sendOrderNotification(String userId, OrderInfo order) {
        for (String templateId : ORDER_TEMPLATES) {
            // 检查该模板是否可用
            if (canSend(userId, templateId)) {
                // 发送成功，退出
                if (send(userId, templateId, order)) {
                    return;
                }
            }
        }

        // 所有模板都失效，引导用户重新订阅
        notifyUserToResubscribe(userId);
    }
}
```

## 最佳实践建议

### 1. 产品设计层面

```
✅ 在关键节点引导订阅（下单、支付成功）
✅ 明确告知用户：每次订阅可收到1条通知
✅ 提供"消息中心"功能，作为兜底方案
✅ 对于高频通知场景，引导用户关注公众号
```

### 2. 技术实现层面

```java
// ✅ 订阅状态本地化管理
// ❌ 完全依赖微信接口判断

// ✅ 发送前检查本地订阅记录
// ❌ 直接调用发送接口

// ✅ 捕获43101错误，记录到数据库
// ❌ 忽略错误，导致消息丢失

// ✅ 提供用户自助订阅入口
// ❌ 只在授权弹窗时订阅
```

### 3. 运营层面

```
✅ 监控43101错误率，超过10%需优化
✅ 定期提醒用户重新订阅
✅ 准备应急方案（短信、公众号推送）
✅ 收集用户反馈，优化订阅流程
```

### 4. 用户教育层面

```
✅ 在授权页面添加说明文案
✅ 在FAQ中解释订阅机制
✅ 提供客服渠道，解答订阅问题
❌ 让用户自己摸索
```

## 监控与报警

### 1. 关键指标

```java
// 订阅转化率
订阅成功率 = 订阅成功次数 / 订阅请求次数

// 消息送达率
消息送达率 = 发送成功次数 / 发送请求次数

// 43101错误率
43101错误率 = 43101错误次数 / 发送请求次数

// 订阅有效利用率
订阅利用率 = 实际发送次数 / 订阅次数
```

### 2. 报警规则

```java
@Component
public class SubscriptionMonitor {

    @Scheduled(fixedRate = 60000)  // 每分钟检查
    public void checkErrorRate() {
        // 计算最近1小时的43101错误率
        double errorRate = calculate43101ErrorRate(1);

        if (errorRate > 0.2) {  // 超过20%
            alert("43101错误率过高：" + errorRate);
        }
    }

    private void alert(String message) {
        // 发送钉钉/企业微信报警
        dingTalkService.sendAlert("订阅消息异常", message);

        // 记录到监控系统
        prometheusService.recordAlert("subscribe_error_high", message);
    }
}
```

## 官方文档参考

### 核心文档

1. **订阅消息开发指南**（必读）
   - https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/subscribe-message.html
   - 介绍订阅消息的基本机制和使用方法

2. **发送订阅消息接口**
   - https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/subscribe-message/subscribeMessage.send.html
   - API接口文档，包含错误码说明

3. **wx.requestSubscribeMessage**
   - https://developers.weixin.qq.com/miniprogram/dev/api/open-api/subscribe-message/wx.requestSubscribeMessage.html
   - 前端授权接口文档

4. **wx.getSetting**
   - https://developers.weixin.qq.com/miniprogram/dev/api/open-api/setting/wx.getSetting.html
   - 获取用户订阅设置

### 社区讨论（重要）

这些社区帖子记录了大量开发者的实际遇到的问题和解决方案：

1. **"总是保持选择"BUG**
   - https://developers.weixin.qq.com/community/develop/doc/000666487a0928a452b03929266400
   - https://developers.weixin.qq.com/community/develop/doc/000004239a0138c769696d4f256400

2. **43101错误讨论**
   - https://developers.weixin.qq.com/community/develop/doc/000c4cce9c84a0e94e7921a2756c00
   - https://developers.weixin.qq.com/community/develop/doc/00042a99010e00c21cb9e3e5351400

3. **订阅消息汇总**
   - https://developers.weixin.qq.com/community/develop/article/doc/0006821235c6108cf7a98001f51813

## 总结

### 问题本质

**43101错误的根本原因**：一次性订阅消息的"一次性"特性导致的状态不一致。

```
微信的设计逻辑：
用户授权 → 获得1次发送机会 → 发送后机会清零 → 再次发送返回43101

用户的理解：
订阅开关打开 → 应该一直能收到消息

这种认知差异导致了大量的困惑和投诉。
```

### 关键发现

1. **小程序端的"接受/拒绝"状态不等于可发送次数**
2. **"总是保持以上选择"存在BUG，可能导致永久无法接收**
3. **微信没有提供查询剩余次数的接口，需自行管理**
4. **一次性订阅的有效期未明确，建议授权后尽快发送**

### 终极解决方案

```java
// 三步走策略：

// 1. 本地化管理订阅状态（不依赖微信）
WechatSubscription subscription = subscriptionRepo.find(userId, templateId);

// 2. 发送前严格校验
if (!subscription.canSend()) {
    // 引导用户重新订阅
    return;
}

// 3. 发送后立即更新状态
subscription.decreaseCount();

// 4. 捕获43101错误，同步状态
try {
    wxMaService.sendSubscribeMsg(message);
} catch (WxErrorException e) {
    if (e.getError().getErrorCode() == 43101) {
        subscription.markAsConsumed();  // 同步本地状态
    }
}
```

### 给腾讯的建议

如果微信官方能改进以下几点，将大大提升开发者体验：

1. **在小程序端显示剩余次数**："订阅消息（剩余1次）"
2. **提供查询剩余次数的API**：`wx.getSubscriptionCount()`
3. **修复"总是保持选择"的BUG**
4. **统一前后端状态展示逻辑**
5. **提供更详细的错误码说明**：区分"用户拒绝"和"次数用完"

## 相关文件

- `WechatSubscriptionManager.java` - 订阅状态管理服务
- `WechatMessageSender.java` - 消息发送服务（含43101处理）
- `SubscriptionMonitor.java` - 监控报警服务
- `前端集成示例.md` - 小程序前端最佳实践

---

**最后更新时间**：2026-01-14
**文档维护者**：开发团队
**问题反馈**：[GitHub Issues](https://github.com/your-repo/issues)
