# 好的命名让代码自解释：我的命名原则

## 推文内容

读代码最痛苦的事：
看不懂变量名 😭

好的命名 = 活文档

**坏命名 vs 好命名：**

```java
// Bad
int d; // days
String s;
List<User> list;
boolean flag;

// Good
int daysUntilExpiry;
String customerEmail;
List<User> activeUsers;
boolean isEmailVerified;
```

差别一目了然

**我的命名原则：**

**1. 用完整单词，不缩写**

❌ usr, msg, btn
✅ user, message, button

除非是公认缩写：
✓ id, url, html, max, min

**2. 名字要说明"是什么"**

```java
// Bad
String data;
int value;
Object obj;

// Good
String userEmail;
int totalPrice;
Order pendingOrder;
```

**3. Boolean用is/has/can开头**

```java
isActive
hasPermission
canDelete
shouldRetry
```

看名字就知道是布尔值

**4. 函数名用动词开头**

```java
getUserById()
createOrder()
sendEmail()
validateInput()
```

名字 = 做什么

**5. 避免无意义的词**

❌ Manager, Handler, Processor, Data, Info

这些词不传达信息：

```java
// Bad
UserManager
DataProcessor
InfoHandler

// Better
UserRepository
PaymentValidator
EmailSender
```

**6. 统一术语**

同一概念用同一个词：

❌ get/fetch/retrieve混用
✅ 统一用get

❌ user/customer/client混用
✅ 根据领域选一个

**7. 上下文决定长度**

类内部：
```java
class Order {
    Money total;  // 够了
}
```

全局：
```java
Money orderTotal;  // 需要完整
```

**8. 集合用复数**

```java
List<User> users;
Set<String> emails;
Map<Long, Order> ordersById;
```

清晰表达是集合

**9. 常量全大写**

```java
static final int MAX_RETRY_COUNT = 3;
static final String DEFAULT_CURRENCY = "USD";
```

**10. 私有方法用_前缀**

```java
private void _calculateDiscount() {
    // helper method
}
```

一看就知道是内部方法

**真实案例：**

重构前：
```java
public void process(List<Object> data) {
    for (Object o : data) {
        // 200行代码
        // 根本看不懂在干嘛
    }
}
```

重构后：
```java
public void validateAndSaveOrders(List<OrderDto> pendingOrders) {
    for (OrderDto orderDto : pendingOrders) {
        if (_isValidOrder(orderDto)) {
            _saveToDatabase(orderDto);
            _sendConfirmationEmail(orderDto);
        }
    }
}
```

不用看实现就知道干什么

**命名的ROI：**

花5秒想个好名字
省下5分钟看代码时间
×100次阅读
= 省500分钟

投资回报：6000倍！

**检查清单：**

命名时问自己：
□ 6个月后的我能懂吗？
□ 新同事能懂吗？
□ 不看实现能猜出功能吗？

3个Yes = 好命名

**记住：**

Code is read 10 times more than it's written

为读者优化
不是为作者

你有什么命名技巧？

---

## 标签
#代码规范 #最佳实践 #Clean Code

## 发布建议
- 对比代码截图
- 引发命名讨论
