# 代码注释工具使用说明

## 工具列表

### 1. add_comments.py - 批量添加注释模板

自动为Java代码添加基本的JavaDoc注释模板。

#### 功能特性
- 自动检测未添加注释的类、方法、字段
- 生成标准的JavaDoc注释模板
- 保留已有的注释不变
- 自动创建备份文件

#### 使用方法

```bash
# 为单个文件添加注释
python tools/add_comments.py finance-user/src/main/java/com/okx/finance/user/service/UserService.java

# 为整个目录添加注释
python tools/add_comments.py finance-user/src/main/java

# 为所有模块添加注释
python tools/add_comments.py .
```

#### 输出示例

**原代码：**
```java
public class UserService {
    private UserMapper userMapper;

    public Result<?> register(RegisterRequest request) {
        // ...
    }
}
```

**处理后：**
```java
/**
 * UserService
 * TODO: 添加类的功能说明
 *
 * @author OKX Finance Team
 * @version 1.0
 */
public class UserService {
    /**
     * userMapper
     * TODO: 添加字段说明
     */
    private UserMapper userMapper;

    /**
     * register
     * TODO: 添加方法说明
     *
     * @param request TODO: 添加参数说明
     * @return TODO: 添加返回值说明
     */
    public Result<?> register(RegisterRequest request) {
        // ...
    }
}
```

#### 注意事项

1. **备份文件**：工具会自动创建`.bak`备份文件
2. **手动完善**：生成的是模板，需要手动替换TODO部分
3. **编码格式**：确保文件使用UTF-8编码
4. **版本控制**：建议在git干净状态下运行

## 批量处理流程

### 步骤1：运行工具生成模板

```bash
# 为所有服务模块生成注释模板
python tools/add_comments.py finance-user/src
python tools/add_comments.py finance-account/src
python tools/add_comments.py finance-trading/src
python tools/add_comments.py finance-market/src
python tools/add_comments.py finance-wallet/src
python tools/add_comments.py finance-gateway/src
python tools/add_comments.py finance-risk/src
python tools/add_comments.py finance-notification/src
```

### 步骤2：手动完善注释内容

使用IDE的搜索功能查找所有"TODO"，逐个完善：

1. **类注释**：
   - 说明类的主要功能
   - 说明核心概念
   - 添加使用示例（如有必要）

2. **方法注释**：
   - 说明方法的功能
   - 详细解释参数的含义和约束
   - 说明返回值的含义
   - 说明可能抛出的异常

3. **字段注释**：
   - 说明字段的含义
   - 说明字段的约束（范围、格式等）
   - 说明默认值（如有）

### 步骤3：检查注释质量

使用以下命令检查是否还有未完成的TODO：

```bash
# Linux/Mac
grep -r "TODO:" --include="*.java" .

# Windows PowerShell
Get-ChildItem -Recurse -Filter *.java | Select-String "TODO:"
```

### 步骤4：生成JavaDoc文档

```bash
# 生成JavaDoc文档
mvn javadoc:javadoc

# 查看生成的文档
open target/site/apidocs/index.html  # Mac
start target/site/apidocs/index.html # Windows
```

## 注释质量标准

### 优秀注释示例

```java
/**
 * 用户服务类
 * 处理用户相关的核心业务逻辑
 *
 * <p>主要功能：
 * 1. 用户注册：验证用户名唯一性，密码加密存储
 * 2. 用户登录：验证密码，生成JWT Token
 * 3. KYC认证：实名认证，提升用户等级
 * 4. API密钥管理：生成和管理用户的API密钥
 *
 * <p>安全措施：
 * - 密码使用MD5加密存储
 * - Token存储在Redis中，24小时有效期
 * - API密钥使用UUID生成，确保唯一性
 *
 * @author OKX Finance Team
 * @version 1.0
 * @see UserMapper
 * @see JwtUtil
 */
@Service
public class UserService {
    /**
     * 用户数据访问对象
     * 用于执行数据库操作（增删改查）
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Redis模板
     * 用于缓存Token和其他临时数据
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 雪花算法ID生成器
     * 机器ID为1，用于生成用户ID
     */
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);

    /**
     * 用户注册
     * 创建新用户账户，验证用户名唯一性，密码加密存储
     *
     * <p>注册流程：
     * 1. 检查用户名是否已存在
     * 2. 生成唯一用户ID（雪花算法）
     * 3. 密码MD5加密
     * 4. 初始化用户状态（KYC等级0，状态正常）
     * 5. 保存到数据库
     *
     * <p>安全措施：
     * - 用户名唯一性校验
     * - 密码不明文存储
     * - 新用户默认未认证状态
     *
     * @param request 注册请求，包含用户名、密码、邮箱、手机号
     * @return Result对象，成功返回"Register success"，失败返回错误信息
     * @throws IllegalArgumentException 如果请求参数为空
     */
    public Result<?> register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existUser = userMapper.findByUsername(request.getUsername());
        if (existUser != null) {
            return Result.error("Username already exists");
        }

        // 创建新用户对象
        User user = new User();
        user.setId(idGenerator.nextId());
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setKycLevel(0);  // 初始KYC等级为0（未认证）
        user.setStatus(1);    // 状态为1（正常）

        // 保存到数据库
        userMapper.insert(user);

        return Result.success("Register success");
    }
}
```

## 常见问题

### Q1: 工具会覆盖已有的注释吗？
A: 不会。工具只会为没有注释的类、方法、字段添加注释模板，已有注释会被保留。

### Q2: 生成的注释可以直接使用吗？
A: 不能。生成的是模板，包含"TODO"标记，需要手动填充具体内容。

### Q3: 如何恢复原文件？
A: 每次运行工具都会创建`.bak`备份文件，可以从备份文件恢复。

### Q4: 工具支持哪些Java文件？
A: 支持所有标准Java源文件（.java），包括类、接口、枚举等。

### Q5: 可以自定义注释模板吗？
A: 可以。修改`add_comments.py`中的模板变量即可。

## 快速开始

```bash
# 1. 确保Python 3.x已安装
python --version

# 2. 进入项目根目录
cd DemoProject/Finance

# 3. 为单个模块添加注释（测试）
python tools/add_comments.py finance-common/src/main/java

# 4. 检查生成结果
# 使用IDE打开任意文件，查看是否生成了注释模板

# 5. 如果满意，为所有模块添加注释
for dir in finance-*/src/main/java; do
    python tools/add_comments.py "$dir"
done

# 6. 手动完善所有TODO标记的内容

# 7. 生成JavaDoc文档验证
mvn javadoc:javadoc
```

## 后续维护

1. **新增类时**：运行工具自动生成注释模板
2. **修改代码时**：同步更新注释
3. **定期检查**：搜索TODO标记，确保都已完善
4. **文档生成**：定期生成JavaDoc文档

## 参考资料

- [Java Doc规范](https://www.oracle.com/java/technologies/javase/javadoc.html)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 项目文档：`docs/CODE_COMMENTS_GUIDE.md`
