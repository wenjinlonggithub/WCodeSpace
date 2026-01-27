# 互联网医院后端技术栈文档

## 文档版本信息
- **版本号**：v1.0
- **更新时间**：2026-01-27
- **维护团队**：后端架构组
- **适用范围**：互联网医院全栈业务系统

---

## 一、技术栈概览

### 1.1 架构风格
- **微服务架构**：基于Spring Cloud Alibaba生态构建分布式微服务体系
- **前后端分离**：RESTful API设计，支持多端接入
- **中台化设计**：支付、消息等通用能力中台化

### 1.2 核心技术组件

| 技术分类 | 技术选型 | 版本要求 | 用途说明 |
|---------|---------|---------|---------|
| 编程语言 | Java | JDK 11 | 核心开发语言 |
| 开发框架 | Spring Boot | 2.6.x+ | 微服务应用框架 |
| 服务调用 | OpenFeign | 3.1.x+ | 声明式HTTP客户端 |
| 注册中心 | Nacos | 2.1.x+ | 服务注册与发现 |
| 配置中心 | Nacos | 2.1.x+ | 动态配置管理 |
| 关系数据库 | MySQL | 8.0+ | 核心业务数据存储 |
| 对象存储 | OSS | - | 文件、图片、视频存储 |
| 短信服务 | 腾讯云短信 | SDK 3.x | 验证码、通知短信 |
| 支付中台 | 自研支付中台 | - | 统一支付接入 |

---

## 二、基础设施层

### 2.1 Java运行环境

#### 2.1.1 JDK版本选择
```bash
# 推荐使用OpenJDK 11 LTS版本
java version "11.0.x"
OpenJDK Runtime Environment (build 11.0.x)
OpenJDK 64-Bit Server VM (build 11.0.x)
```

#### 2.1.2 JVM参数配置
```bash
# 生产环境推荐JVM参数
-Xms4g 
-Xmx4g 
-XX:+UseG1GC 
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError 
-XX:HeapDumpPath=/logs/heapdump.hprof
-XX:+PrintGCDetails 
-XX:+PrintGCDateStamps
-Xloggc:/logs/gc.log
```

#### 2.1.3 为什么选择JDK 11
- **长期支持版本**：Oracle提供至2026年的长期支持
- **性能提升**：相比JDK 8，G1 GC优化，启动速度提升
- **新特性支持**：HTTP Client API、字符串增强、局部变量类型推断
- **稳定性**：已在业界大规模应用，生态成熟

---

## 三、应用框架层

### 3.1 Spring Boot

#### 3.1.1 版本与依赖
```xml
<!-- 父POM依赖 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.6.13</version>
</parent>

<!-- 核心依赖 -->
<dependencies>
    <!-- Web开发 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- 数据访问 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- 参数校验 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- 监控健康检查 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

#### 3.1.2 应用配置结构
```yaml
# application.yml 核心配置
spring:
  application:
    name: medical-consultation-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
server:
  port: 8080
  servlet:
    context-path: /api/v1

# application-dev.yml 开发环境
# application-test.yml 测试环境
# application-prod.yml 生产环境
```

#### 3.1.3 统一异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("SYSTEM_ERROR", "系统繁忙，请稍后重试"));
    }
}
```

---

### 3.2 OpenFeign 服务调用

#### 3.2.1 依赖配置
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>3.1.5</version>
</dependency>

<!-- 负载均衡 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

#### 3.2.2 启用Feign
```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.hospital.*.feign")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### 3.2.3 Feign接口定义
```java
/**
 * 用户服务Feign客户端
 */
@FeignClient(
    name = "medical-user-service",
    path = "/api/v1/users",
    fallbackFactory = UserServiceFallbackFactory.class
)
public interface UserServiceFeign {
    
    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/{userId}")
    Result<UserDTO> getUserById(@PathVariable("userId") Long userId);
    
    /**
     * 批量查询用户信息
     */
    @PostMapping("/batch")
    Result<List<UserDTO>> batchGetUsers(@RequestBody List<Long> userIds);
}
```

#### 3.2.4 降级处理
```java
@Component
public class UserServiceFallbackFactory implements FallbackFactory<UserServiceFeign> {
    
    @Override
    public UserServiceFeign create(Throwable cause) {
        return new UserServiceFeign() {
            @Override
            public Result<UserDTO> getUserById(Long userId) {
                log.error("调用用户服务失败, userId={}", userId, cause);
                return Result.fail("用户服务暂时不可用");
            }
            
            @Override
            public Result<List<UserDTO>> batchGetUsers(List<Long> userIds) {
                log.error("批量查询用户失败", cause);
                return Result.fail("用户服务暂时不可用");
            }
        };
    }
}
```

#### 3.2.5 Feign配置
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000      # 连接超时时间(ms)
        readTimeout: 10000        # 读取超时时间(ms)
        loggerLevel: basic        # 日志级别: none/basic/headers/full
  compression:
    request:
      enabled: true               # 请求压缩
      mime-types: text/xml,application/xml,application/json
      min-request-size: 2048
    response:
      enabled: true               # 响应压缩
  httpclient:
    enabled: true
    max-connections: 200          # 最大连接数
    max-connections-per-route: 50 # 每个路由最大连接数
```

---

### 3.3 Nacos 服务治理

#### 3.3.1 依赖配置
```xml
<!-- Nacos服务发现 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <version>2021.0.5.0</version>
</dependency>

<!-- Nacos配置中心 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    <version>2021.0.5.0</version>
</dependency>
```

#### 3.3.2 Nacos注册中心配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848      # Nacos服务器地址
        namespace: medical-prod              # 命名空间(环境隔离)
        group: DEFAULT_GROUP                 # 分组
        service: ${spring.application.name}  # 服务名
        weight: 1                            # 权重
        metadata:
          version: 1.0.0                     # 服务版本
          region: cn-hangzhou                # 所属区域
        heart-beat-interval: 5000            # 心跳间隔(ms)
        heart-beat-timeout: 15000            # 心跳超时(ms)
        ip: ${SPRING_CLOUD_CLIENT_IP:}       # 服务IP(可指定)
        port: ${server.port}                 # 服务端口
```

#### 3.3.3 Nacos配置中心配置
```yaml
# bootstrap.yml (优先级高于application.yml)
spring:
  application:
    name: medical-consultation-service
  cloud:
    nacos:
      config:
        server-addr: nacos-server:8848
        namespace: medical-prod
        group: DEFAULT_GROUP
        file-extension: yaml                 # 配置文件格式
        refresh-enabled: true                # 支持动态刷新
        
        # 共享配置(多服务共享)
        shared-configs:
          - data-id: common-mysql.yaml
            group: DEFAULT_GROUP
            refresh: true
          - data-id: common-redis.yaml
            group: DEFAULT_GROUP
            refresh: true
            
        # 扩展配置
        extension-configs:
          - data-id: mq-config.yaml
            group: DEFAULT_GROUP
            refresh: true
```

#### 3.3.4 Nacos配置示例

**服务专属配置 (medical-consultation-service.yaml)**
```yaml
# Data ID: medical-consultation-service.yaml
consultation:
  max-duration: 30              # 单次问诊最大时长(分钟)
  timeout-reminder: 5           # 超时提醒(分钟)
  doctor:
    max-concurrent: 5           # 医生最大并发问诊数
```

**共享MySQL配置 (common-mysql.yaml)**
```yaml
# Data ID: common-mysql.yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://mysql-master:3306/medical_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
    
    # Druid连接池配置
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
```

#### 3.3.5 动态配置刷新
```java
@RestController
@RefreshScope  // 支持配置动态刷新
public class ConsultationController {
    
    @Value("${consultation.max-duration}")
    private Integer maxDuration;
    
    @Value("${consultation.doctor.max-concurrent}")
    private Integer doctorMaxConcurrent;
    
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxDuration", maxDuration);
        config.put("doctorMaxConcurrent", doctorMaxConcurrent);
        return config;
    }
}
```

#### 3.3.6 配置监听器
```java
@Component
public class ConsultationConfigListener {
    
    @NacosValue(value = "${consultation.max-duration}", autoRefreshed = true)
    private Integer maxDuration;
    
    @NacosConfigListener(dataId = "medical-consultation-service.yaml", groupId = "DEFAULT_GROUP")
    public void onConfigChange(String newConfig) {
        log.info("配置发生变更: {}", newConfig);
        // 执行配置变更后的业务逻辑
    }
}
```

---

## 四、数据持久层

### 4.1 MySQL数据库

#### 4.1.1 版本与特性
- **版本要求**：MySQL 8.0.28+
- **核心特性**：
  - InnoDB存储引擎，支持事务ACID
  - 行级锁，支持高并发
  - MVCC多版本并发控制
  - 主从复制，读写分离

#### 4.1.2 连接池配置
```xml
<!-- Druid连接池 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.16</version>
</dependency>
```

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 连接池配置
      initial-size: 5                    # 初始连接数
      min-idle: 5                        # 最小空闲连接数
      max-active: 20                     # 最大活跃连接数
      max-wait: 60000                    # 获取连接最大等待时间(ms)
      
      # 检测配置
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      
      # 连接保活
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      
      # SQL监控
      filters: stat,wall,slf4j
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: ${DRUID_PASSWORD}
      
      # SQL防火墙
      wall:
        enabled: true
        config:
          multi-statement-allow: true
```

#### 4.1.3 MyBatis-Plus配置
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.3</version>
</dependency>
```

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true    # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto                       # 主键策略
      logic-delete-field: deleted         # 逻辑删除字段
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:/mapper/**/*.xml
```

#### 4.1.4 数据库设计规范

**表设计示例**
```sql
-- 问诊订单表
CREATE TABLE `consultation_order` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `patient_id` BIGINT(20) NOT NULL COMMENT '患者ID',
  `doctor_id` BIGINT(20) NOT NULL COMMENT '医生ID',
  `consultation_type` TINYINT(2) NOT NULL COMMENT '问诊类型:1-图文,2-视频',
  `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '状态:0-待支付,1-待接诊,2-进行中,3-已完成,4-已取消',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊订单表';
```

**分库分表策略**
```yaml
# ShardingSphere配置(如需分表)
spring:
  shardingsphere:
    datasource:
      names: ds0,ds1
    rules:
      sharding:
        tables:
          consultation_order:
            actual-data-nodes: ds$->{0..1}.consultation_order_$->{0..7}
            table-strategy:
              standard:
                sharding-column: patient_id
                sharding-algorithm-name: order-inline
        sharding-algorithms:
          order-inline:
            type: INLINE
            props:
              algorithm-expression: consultation_order_$->{patient_id % 8}
```

#### 4.1.5 读写分离配置
```yaml
spring:
  datasource:
    # 主库(写)
    master:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://mysql-master:3306/medical_db
      username: ${MYSQL_MASTER_USERNAME}
      password: ${MYSQL_MASTER_PASSWORD}
    
    # 从库(读)
    slave:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://mysql-slave:3306/medical_db
      username: ${MYSQL_SLAVE_USERNAME}
      password: ${MYSQL_SLAVE_PASSWORD}
```

---

## 五、中台服务层

### 5.1 支付中台

#### 5.1.1 架构设计
```
┌─────────────────────────────────────────┐
│         业务系统(问诊/购药/挂号)          │
└──────────────┬──────────────────────────┘
               │ 统一支付接口
┌──────────────┴──────────────────────────┐
│            支付中台服务                   │
├─────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │ 支付路由  │  │ 订单管理  │  │ 对账   ││
│  └──────────┘  └──────────┘  └────────┘│
└──────┬─────────┬──────────┬─────────────┘
       │         │          │
   ┌───┴───┐ ┌──┴───┐  ┌───┴────┐
   │微信支付│ │支付宝 │  │医保支付 │
   └───────┘ └──────┘  └────────┘
```

#### 5.1.2 Feign接口定义
```java
/**
 * 支付中台Feign客户端
 */
@FeignClient(
    name = "payment-platform-service",
    path = "/api/v1/payment"
)
public interface PaymentPlatformFeign {
    
    /**
     * 创建支付订单
     */
    @PostMapping("/orders/create")
    Result<PaymentOrderDTO> createPaymentOrder(@RequestBody CreatePaymentRequest request);
    
    /**
     * 查询支付状态
     */
    @GetMapping("/orders/{orderNo}/status")
    Result<PaymentStatusDTO> queryPaymentStatus(@PathVariable("orderNo") String orderNo);
    
    /**
     * 申请退款
     */
    @PostMapping("/refund/apply")
    Result<RefundDTO> applyRefund(@RequestBody RefundRequest request);
}
```

#### 5.1.3 支付请求模型
```java
@Data
public class CreatePaymentRequest {
    /**
     * 业务订单号
     */
    @NotBlank(message = "业务订单号不能为空")
    private String bizOrderNo;
    
    /**
     * 业务类型: CONSULTATION-问诊, MEDICINE-购药
     */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    
    /**
     * 支付金额(单位:分)
     */
    @NotNull(message = "支付金额不能为空")
    @Min(value = 1, message = "支付金额必须大于0")
    private Long amount;
    
    /**
     * 支付渠道: WECHAT-微信, ALIPAY-支付宝, MEDICAL_INSURANCE-医保
     */
    @NotBlank(message = "支付渠道不能为空")
    private String payChannel;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 订单标题
     */
    private String subject;
    
    /**
     * 回调通知URL
     */
    private String notifyUrl;
}
```

#### 5.1.4 支付中台配置
```yaml
# 支付中台配置
payment:
  platform:
    timeout: 180000          # 支付超时时间(ms) 3分钟
    retry:
      max-attempts: 3        # 最大重试次数
      backoff: 1000          # 重试间隔(ms)
    
  channels:
    # 微信支付
    wechat:
      app-id: ${WECHAT_APP_ID}
      mch-id: ${WECHAT_MCH_ID}
      api-key: ${WECHAT_API_KEY}
      cert-path: /certs/wechat/apiclient_cert.p12
      notify-url: https://api.hospital.com/payment/notify/wechat
      
    # 支付宝
    alipay:
      app-id: ${ALIPAY_APP_ID}
      private-key: ${ALIPAY_PRIVATE_KEY}
      public-key: ${ALIPAY_PUBLIC_KEY}
      notify-url: https://api.hospital.com/payment/notify/alipay
```

#### 5.1.5 支付回调处理
```java
@RestController
@RequestMapping("/payment/notify")
public class PaymentNotifyController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 微信支付回调
     */
    @PostMapping("/wechat")
    public String wechatNotify(@RequestBody String xmlData) {
        try {
            // 验签
            boolean isValid = WechatPayUtil.verifySignature(xmlData);
            if (!isValid) {
                return WechatPayUtil.buildFailResponse("签名验证失败");
            }
            
            // 解析回调数据
            WechatPayNotify notify = WechatPayUtil.parseNotify(xmlData);
            
            // 处理支付结果
            paymentService.handlePaymentSuccess(notify.getOutTradeNo(), notify);
            
            return WechatPayUtil.buildSuccessResponse();
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return WechatPayUtil.buildFailResponse("处理失败");
        }
    }
}
```

---

### 5.2 腾讯云短信服务

#### 5.2.1 SDK依赖
```xml
<dependency>
    <groupId>com.tencentcloudapi</groupId>
    <artifactId>tencentcloud-sdk-java</artifactId>
    <version>3.1.720</version>
</dependency>
```

#### 5.2.2 短信配置
```yaml
# 腾讯云短信配置
tencent:
  sms:
    secret-id: ${TENCENT_SECRET_ID}
    secret-key: ${TENCENT_SECRET_KEY}
    sdk-app-id: ${TENCENT_SMS_SDK_APP_ID}
    sign-name: 互联网医院                  # 短信签名
    
    # 模板配置
    templates:
      verification-code: 1234567          # 验证码模板ID
      appointment-reminder: 1234568       # 预约提醒模板ID
      consultation-notice: 1234569        # 问诊通知模板ID
    
    # 限流配置
    rate-limit:
      per-phone-daily: 10                 # 单手机号每日上限
      per-phone-minute: 1                 # 单手机号每分钟上限
```

#### 5.2.3 短信服务封装
```java
/**
 * 短信服务
 */
@Service
@Slf4j
public class SmsService {
    
    @Value("${tencent.sms.secret-id}")
    private String secretId;
    
    @Value("${tencent.sms.secret-key}")
    private String secretKey;
    
    @Value("${tencent.sms.sdk-app-id}")
    private String sdkAppId;
    
    @Value("${tencent.sms.sign-name}")
    private String signName;
    
    /**
     * 发送验证码短信
     */
    public boolean sendVerificationCode(String phoneNumber, String code) {
        try {
            // 创建客户端
            Credential cred = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            
            SmsClient client = new SmsClient(cred, "ap-guangzhou", clientProfile);
            
            // 构建请求
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(sdkAppId);
            req.setSignName(signName);
            req.setTemplateId("1234567");  // 验证码模板ID
            
            // 设置手机号
            String[] phoneNumbers = {"+86" + phoneNumber};
            req.setPhoneNumberSet(phoneNumbers);
            
            // 设置模板参数: {1}为验证码, {2}为有效期
            String[] templateParams = {code, "5"};
            req.setTemplateParamSet(templateParams);
            
            // 发送短信
            SendSmsResponse resp = client.SendSms(req);
            
            // 检查结果
            if (resp.getSendStatusSet().length > 0) {
                SendStatus status = resp.getSendStatusSet()[0];
                if ("Ok".equals(status.getCode())) {
                    log.info("短信发送成功, phone={}, code={}", phoneNumber, code);
                    return true;
                } else {
                    log.error("短信发送失败, phone={}, error={}", phoneNumber, status.getMessage());
                    return false;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("发送短信异常, phone={}", phoneNumber, e);
            return false;
        }
    }
    
    /**
     * 发送问诊通知短信
     */
    public boolean sendConsultationNotice(String phoneNumber, String doctorName, String time) {
        // 类似实现...
        return true;
    }
}
```

#### 5.2.4 短信限流控制
```java
/**
 * 短信限流器
 */
@Component
public class SmsRateLimiter {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String DAILY_LIMIT_KEY = "sms:limit:daily:";
    private static final String MINUTE_LIMIT_KEY = "sms:limit:minute:";
    
    /**
     * 检查是否允许发送
     */
    public boolean allowSend(String phoneNumber) {
        String dailyKey = DAILY_LIMIT_KEY + phoneNumber;
        String minuteKey = MINUTE_LIMIT_KEY + phoneNumber;
        
        // 检查每日限制
        Long dailyCount = redisTemplate.opsForValue().increment(dailyKey);
        if (dailyCount == 1) {
            redisTemplate.expire(dailyKey, 24, TimeUnit.HOURS);
        }
        if (dailyCount > 10) {
            log.warn("手机号{}超过每日发送上限", phoneNumber);
            return false;
        }
        
        // 检查每分钟限制
        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
        if (minuteCount == 1) {
            redisTemplate.expire(minuteKey, 1, TimeUnit.MINUTES);
        }
        if (minuteCount > 1) {
            log.warn("手机号{}超过每分钟发送上限", phoneNumber);
            return false;
        }
        
        return true;
    }
}
```

---

### 5.3 阿里云OSS对象存储

#### 5.3.1 SDK依赖
```xml
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.16.1</version>
</dependency>
```

#### 5.3.2 OSS配置
```yaml
# 阿里云OSS配置
aliyun:
  oss:
    endpoint: https://oss-cn-hangzhou.aliyuncs.com
    access-key-id: ${ALIYUN_ACCESS_KEY_ID}
    access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
    bucket-name: medical-hospital-prod
    
    # 存储路径配置
    paths:
      prescription: prescription/            # 处方图片
      medical-record: medical-record/        # 病历文件
      user-avatar: avatar/                   # 用户头像
      consultation-image: consultation/      # 问诊图片
      
    # 访问控制
    public-read: true                        # 是否公共读
    cdn-domain: https://cdn.hospital.com     # CDN加速域名
    
    # 文件限制
    max-file-size: 10485760                  # 最大文件10MB
    allowed-extensions: jpg,jpeg,png,pdf,doc,docx
```

#### 5.3.3 OSS服务封装
```java
/**
 * OSS文件服务
 */
@Service
@Slf4j
public class OssFileService {
    
    @Autowired
    private OssProperties ossProperties;
    
    private OSS ossClient;
    
    @PostConstruct
    public void init() {
        this.ossClient = new OSSClientBuilder()
            .build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
            );
    }
    
    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file, String pathPrefix) {
        try {
            // 验证文件
            validateFile(file);
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = pathPrefix + UUID.randomUUID().toString() + extension;
            
            // 上传文件
            PutObjectRequest putRequest = new PutObjectRequest(
                ossProperties.getBucketName(),
                fileName,
                file.getInputStream()
            );
            
            // 设置文件元信息
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            putRequest.setMetadata(metadata);
            
            ossClient.putObject(putRequest);
            
            // 返回文件URL
            String fileUrl = ossProperties.getCdnDomain() + "/" + fileName;
            log.info("文件上传成功: {}", fileUrl);
            
            return fileUrl;
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }
    
    /**
     * 上传处方图片
     */
    public String uploadPrescription(MultipartFile file) {
        return uploadFile(file, ossProperties.getPaths().get("prescription"));
    }
    
    /**
     * 下载文件
     */
    public InputStream downloadFile(String fileUrl) {
        try {
            String objectName = fileUrl.replace(ossProperties.getCdnDomain() + "/", "");
            OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), objectName);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("文件下载失败, url={}", fileUrl, e);
            throw new BusinessException("文件下载失败");
        }
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        try {
            String objectName = fileUrl.replace(ossProperties.getCdnDomain() + "/", "");
            ossClient.deleteObject(ossProperties.getBucketName(), objectName);
            log.info("文件删除成功: {}", fileUrl);
        } catch (Exception e) {
            log.error("文件删除失败, url={}", fileUrl, e);
        }
    }
    
    /**
     * 生成临时访问URL(用于私有文件)
     */
    public String generatePresignedUrl(String fileUrl, int expireMinutes) {
        try {
            String objectName = fileUrl.replace(ossProperties.getCdnDomain() + "/", "");
            Date expiration = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000);
            
            URL url = ossClient.generatePresignedUrl(
                ossProperties.getBucketName(),
                objectName,
                expiration
            );
            
            return url.toString();
        } catch (Exception e) {
            log.error("生成临时URL失败", e);
            throw new BusinessException("生成临时URL失败");
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > ossProperties.getMaxFileSize()) {
            throw new BusinessException("文件大小超过限制");
        }
        
        // 检查文件扩展名
        String filename = file.getOriginalFilename();
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        List<String> allowedExtensions = Arrays.asList(
            ossProperties.getAllowedExtensions().split(",")
        );
        
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException("不支持的文件类型");
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
```

#### 5.3.4 文件上传接口
```java
@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {
    
    @Autowired
    private OssFileService ossFileService;
    
    /**
     * 上传处方图片
     */
    @PostMapping("/prescription/upload")
    public Result<String> uploadPrescription(@RequestParam("file") MultipartFile file) {
        String fileUrl = ossFileService.uploadPrescription(file);
        return Result.success(fileUrl);
    }
    
    /**
     * 上传问诊图片
     */
    @PostMapping("/consultation/upload")
    public Result<String> uploadConsultationImage(@RequestParam("file") MultipartFile file) {
        String fileUrl = ossFileService.uploadFile(
            file, 
            "consultation/"
        );
        return Result.success(fileUrl);
    }
    
    /**
     * 批量上传
     */
    @PostMapping("/batch/upload")
    public Result<List<String>> batchUpload(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = ossFileService.uploadFile(file, "batch/");
            urls.add(url);
        }
        return Result.success(urls);
    }
}
```

---

## 六、微服务体系架构

### 6.1 整体架构分层

根据业务架构图，互联网医院系统采用"外部接入层 + 业务应用层 + 中台层 + 互联网服务层"的四层架构：

```
┌──────────────────────────────────────────────────────────────┐
│                      外部接入体系                              │
│  北京卫健委 | 老保农 | 药械中心 | 药械分级监管 | 处方流转监管    │
│  物流送药 | 检验实验室 | 医技检查 | 快速TOC扫码 | 药房 | 邮寄  │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│                      业务应用层                                │
│  问诊 | 处方审核 | 药品流转 | 物流 | 医院TOC | 慢病100        │
│  (9个核心业务服务模块)                                          │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│                      中台服务层                                │
│  大数据与机器学习 | SaaS平台 | 代理工作台 | 中台              │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│                    互联网服务体系                              │
│  基础组件 | 监控中心 | 统一网关 | 配置中心 | 五联网融合        │
└──────────────────────────────────────────────────────────────┘
```

---

### 6.2 业务应用层服务清单

#### 6.2.1 核心业务服务（黄色标注）

| 服务名称 | 服务标识 | 端口 | 核心功能 | 依赖服务 |
|---------|---------|------|---------|---------|
| **北京卫健委对接** | beijing-health-service | 8081 | 1. 医疗机构远程会诊<br>2. 远程心电诊断 | 问诊、患者档案 |
| **老保农对接** | insurance-service | 8082 | 1. 医保实名制认证<br>2. 医保支付对接 | 用户、订单、支付中台 |
| **药械分级监管对接** | drug-regulation-service | 8083 | 1. 管控处方管理<br>2. 医保药品监管<br>3. 处方审核 | 处方、药品目录 |
| **药械分级监管系统** | drug-classification-service | 8084 | 1. 药品分级管理<br>2. 医保药品监管<br>3. 用药安全监控 | 药品服务、医保 |
| **处方流转监管平台** | prescription-flow-service | 8085 | 1. 医院处方上传<br>2. 互联网医院诊疗<br>3. 处方审核追溯 | 处方、药品、物流 |
| **物流送药** | logistics-delivery-service | 8086 | 1. 医保处方下载<br>2. 医保处方配送<br>3. 发货配送管理<br>4. 用户签收 | 处方、订单、第三方物流 |
| **检验实验室** | lab-test-service | 8087 | 1. 医技检查预约<br>2. 医技检查报告 | 医院、医生、患者 |
| **医院TOC扫码** | hospital-toc-service | 8088 | 1. 检验报告扫码 | 患者、检验 |
| **慢病100项目** | chronic-disease-service | 8089 | 1. 慢病TOC项目 | 患者、问诊、处方 |

#### 6.2.2 业务服务职责详述

**1. 北京卫健委对接服务（beijing-health-service）**
```yaml
职责范围:
  - 对接北京市卫健委监管平台
  - 上报医疗机构执业信息
  - 远程会诊数据上传
  - 医疗质量监控数据同步
  
技术实现:
  - WebService/SOAP协议对接
  - XML报文格式
  - 定时任务批量上报
  - 数据加密传输
```

**2. 老保农对接服务（insurance-service）**
```yaml
职责范围:
  - 医保实名认证
  - 医保资格验证
  - 医保费用结算
  - 医保目录对照
  
核心接口:
  - 身份验证接口
  - 费用预结算接口
  - 费用确认上传接口
  - 结算单查询接口
```

**3. 药械监管服务（drug-regulation-service）**
```yaml
职责范围:
  - 处方合规性校验
  - 药品使用监控
  - 抗菌药物分级管理
  - 处方点评统计
  
监管规则:
  - 麻精药品管控
  - 抗菌药物分级
  - 用药频次限制
  - 配伍禁忌检查
```

**4. 处方流转平台（prescription-flow-service）**
```yaml
职责范围:
  - 电子处方签名
  - 处方流转记录
  - 处方审核管理
  - 处方追溯查询
  
流转流程:
  1. 医生开具处方
  2. 药师审核处方
  3. 处方签名存证
  4. 推送至药房/物流
  5. 用药指导反馈
```

**5. 物流配送服务（logistics-delivery-service）**
```yaml
职责范围:
  - 订单配送管理
  - 物流轨迹追踪
  - 配送时效监控
  - 签收确认管理
  
对接物流商:
  - 顺丰医药
  - 京东物流
  - 自建配送
  
配送模式:
  - 快递配送
  - 上门自取
  - 门店配送
```

---

### 6.3 中台服务层

#### 6.3.1 中台架构设计（绿色标注）

| 中台名称 | 服务标识 | 核心能力 |
|---------|---------|---------|
| **大数据与机器学习中台** | big-data-ml-platform | 1. 数据建模分析<br>2. 处方合理性审核<br>3. 代理工作台小程序 |
| **SaaS服务中台** | saas-platform-service | 1. 电商运营管理<br>2. 医保天使审核<br>3. 代理工作台审核<br>4. 组织架构管理<br>5. 线上营销管理<br>6. 知识管理培训 |
| **代理工作台** | agent-workspace-service | 1. 代理工作台审核<br>2. 代理工作台组织<br>3. 代理工作台小程序 |
| **业务中台** | business-middle-platform | 1. 即时通讯<br>2. 短信推送<br>3. 短信服务<br>4. 支付中心<br>5. 商品中心<br>6. 订单中心<br>7. 客服服务<br>8. 对账管理 |

#### 6.3.2 大数据与机器学习中台

```java
/**
 * 处方合理性审核服务
 */
@Service
public class PrescriptionAuditService {
    
    /**
     * AI智能审核处方
     * - 用药适应症判断
     * - 配伍禁忌检查
     * - 用药剂量校验
     * - 疾病用药匹配
     */
    public AuditResult auditPrescription(PrescriptionDTO prescription) {
        // 1. 基础规则审核
        List<String> ruleErrors = basicRuleCheck(prescription);
        
        // 2. AI模型审核
        MLAuditResult mlResult = mlModelService.audit(prescription);
        
        // 3. 专家知识库匹配
        List<String> expertSuggestions = expertKnowledgeMatch(prescription);
        
        // 4. 综合评估
        return buildAuditResult(ruleErrors, mlResult, expertSuggestions);
    }
}
```

#### 6.3.3 SaaS服务中台

**组织架构管理**
```yaml
功能模块:
  - 多租户管理: 支持多医疗机构接入
  - 科室管理: 科室层级、权限配置
  - 医生管理: 医生资质、执业范围
  - 角色权限: 基于RBAC的权限体系
  
技术实现:
  - 多租户数据隔离
  - 动态权限加载
  - 组织架构树形存储
```

**电商运营管理**
```yaml
功能模块:
  - 商品管理: SKU、SPU、库存
  - 营销活动: 优惠券、满减、秒杀
  - 订单中心: 订单全流程管理
  - 数据分析: 销售报表、用户画像
```

#### 6.3.4 业务中台详细设计

**即时通讯中台**
```java
@Service
public class ImService {
    /**
     * 支持多种通讯场景
     */
    public enum ImSceneType {
        DOCTOR_PATIENT,      // 医患沟通
        PHARMACIST_PATIENT,  // 药患沟通
        CUSTOMER_SERVICE,    // 客服咨询
        GROUP_CHAT          // 群组讨论
    }
    
    /**
     * 消息类型
     */
    public enum MessageType {
        TEXT,               // 文本消息
        IMAGE,              // 图片消息
        VOICE,              // 语音消息
        VIDEO,              // 视频消息
        PRESCRIPTION,       // 处方消息
        MEDICAL_RECORD     // 病历消息
    }
}
```

**支付中心（已详细描述，见第五章）**

**订单中心**
```yaml
订单类型:
  - 问诊订单: consultation_order
  - 药品订单: medicine_order
  - 检验订单: lab_test_order
  - 套餐订单: package_order

订单状态流转:
  待支付 → 待接诊/待配送 → 进行中 → 已完成 → 已评价
           ↓
          已取消 → 退款中 → 已退款

订单聚合根:
  - 订单主信息
  - 订单明细
  - 支付信息
  - 物流信息
  - 售后信息
```

---

### 6.4 服务依赖关系图

```
外部接入层
    ↓
┌─────────────────────────────────────────────────────────┐
│  北京卫健委  老保农  药械中心  处方监管  物流  检验  慢病  │
└────┬────────┬────────┬────────┬────────┬────────┬───────┘
     │        │        │        │        │        │
     ↓        ↓        ↓        ↓        ↓        ↓
┌────────────────────────────────────────────────────────┐
│              统一API网关 (gateway-service)              │
│    路由转发 | 鉴权认证 | 限流熔断 | 日志追踪             │
└────────────────────────────────────────────────────────┘
                         ↓
     ┌──────────────────┼──────────────────┐
     ↓                  ↓                  ↓
┌─────────┐      ┌──────────┐      ┌──────────┐
│问诊服务  │      │处方服务   │      │订单服务   │
│(问诊)   │←────→│(处方审核) │←────→│(订单)    │
└────┬────┘      └─────┬────┘      └────┬─────┘
     │                 │                 │
     │                 ↓                 │
     │          ┌──────────┐            │
     │          │药品服务   │            │
     │          │(物流送药) │            │
     │          └─────┬────┘            │
     │                │                 │
     ↓                ↓                 ↓
┌──────────────────────────────────────────┐
│              业务中台层                    │
├──────────────────────────────────────────┤
│  支付中心 | 订单中心 | 商品中心 | IM中台   │
│  短信服务 | 客服中心 | 对账管理            │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│              SaaS中台                     │
│  组织架构 | 权限管理 | 电商运营 | 营销管理 │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│          大数据与机器学习中台              │
│  数据分析 | AI审核 | 知识图谱 | 推荐算法  │
└──────────────────────────────────────────┘
```

---

### 6.5 核心业务流程服务编排

#### 6.5.1 在线问诊完整流程

```
用户端 → API网关 → 问诊服务
                    ↓
              【创建问诊订单】
                    ↓
            调用: 订单服务.createOrder()
            调用: 支付中台.createPayment()
                    ↓
              【支付成功回调】
                    ↓
            调用: 问诊服务.confirmOrder()
            调用: IM中台.createChatRoom()
            调用: 短信服务.notifyDoctor()
                    ↓
              【医生接诊问诊】
                    ↓
            IM中台: 实时通讯
            调用: 用户服务.getPatientInfo()
            调用: 病历服务.getHistory()
                    ↓
              【医生开具处方】
                    ↓
            调用: 处方服务.createPrescription()
            调用: 药品服务.checkInventory()
            调用: 药师审核.auditPrescription()
                    ↓
            【处方审核通过】
                    ↓
            调用: 处方流转.uploadPrescription()
            调用: 物流服务.createDeliveryOrder()
            调用: 短信服务.notifyPatient()
                    ↓
              【药品配送】
                    ↓
            物流服务: 轨迹追踪
            调用: 订单服务.updateStatus()
                    ↓
              【用户签收】
                    ↓
            调用: 订单服务.complete()
            调用: 评价服务.createEvaluation()
```

#### 6.5.2 医保支付流程

```
用户 → 支付中心
         ↓
    【选择医保支付】
         ↓
    调用: 老保农服务.verifyIdentity()  // 实名认证
         ↓
    【身份验证通过】
         ↓
    调用: 老保农服务.preSettle()       // 费用预结算
         ↓
    【获取医保支付金额】
         ↓
    个人支付 = 总额 - 医保支付
         ↓
    调用: 支付中台.mixedPay()          // 组合支付
         ├─ 医保账户支付
         └─ 个人微信/支付宝支付
         ↓
    【支付成功】
         ↓
    调用: 老保农服务.confirmSettle()   // 确认结算
    调用: 订单服务.paymentSuccess()
```

---

### 6.6 服务治理配置

#### 6.6.1 服务注册配置（Nacos）

```yaml
# 问诊服务注册配置
spring:
  cloud:
    nacos:
      discovery:
        service: medical-consultation-service
        group: BUSINESS_SERVICE_GROUP
        namespace: medical-prod
        metadata:
          version: 1.0.0
          layer: business              # 业务层服务
          category: core               # 核心服务
```

#### 6.6.2 服务路由配置（Gateway）

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 问诊服务路由
        - id: consultation-route
          uri: lb://medical-consultation-service
          predicates:
            - Path=/api/v1/consultation/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
        
        # 处方流转路由
        - id: prescription-flow-route
          uri: lb://prescription-flow-service
          predicates:
            - Path=/api/v1/prescription/**
          filters:
            - StripPrefix=1
        
        # 支付中台路由
        - id: payment-route
          uri: lb://payment-platform-service
          predicates:
            - Path=/api/v1/payment/**
          filters:
            - StripPrefix=1
```

---

### 6.7 服务依赖矩阵（扩展版）

| 调用方 ↓ \ 被调用方 → | 用户 | 医生 | 问诊 | 处方 | 订单 | 药品 | 物流 | 支付 | IM | 短信 | 老保农 |
|---------------------|-----|-----|-----|-----|-----|-----|-----|-----|----|----|-------|
| 问诊服务              | ✓   | ✓   | -   | ✓   | ✓   |     |     | ✓   | ✓  | ✓  |       |
| 处方服务              | ✓   | ✓   | ✓   | -   | ✓   | ✓   | ✓   |     |    | ✓  |       |
| 订单服务              | ✓   | ✓   | ✓   | ✓   | -   | ✓   | ✓   | ✓   |    | ✓  | ✓     |
| 物流服务              | ✓   |     |     | ✓   | ✓   | ✓   | -   |     |    | ✓  |       |
| 老保农服务            | ✓   |     | ✓   | ✓   | ✓   |     |     | ✓   |    |    | -     |
| 药械监管服务          |     | ✓   |     | ✓   |     | ✓   |     |     |    |    | ✓     |
| 北京卫健委服务        |     | ✓   | ✓   |     |     |     |     |     |    |    |       |

---

## 七、开发规范

### 7.1 项目结构规范

```
medical-consultation-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hospital/consultation/
│   │   │       ├── controller/          # 控制器层
│   │   │       │   ├── ConsultationController.java
│   │   │       │   └── PrescriptionController.java
│   │   │       ├── service/             # 服务层
│   │   │       │   ├── ConsultationService.java
│   │   │       │   └── impl/
│   │   │       │       └── ConsultationServiceImpl.java
│   │   │       ├── repository/          # 数据访问层
│   │   │       │   ├── ConsultationMapper.java
│   │   │       │   └── entity/
│   │   │       │       └── ConsultationOrder.java
│   │   │       ├── feign/               # Feign客户端
│   │   │       │   ├── UserServiceFeign.java
│   │   │       │   └── fallback/
│   │   │       ├── model/               # 数据模型
│   │   │       │   ├── dto/             # 数据传输对象
│   │   │       │   ├── vo/              # 视图对象
│   │   │       │   └── request/         # 请求对象
│   │   │       ├── common/              # 通用组件
│   │   │       │   ├── constant/        # 常量定义
│   │   │       │   ├── enums/           # 枚举类型
│   │   │       │   ├── exception/       # 异常定义
│   │   │       │   └── util/            # 工具类
│   │   │       └── config/              # 配置类
│   │   │           ├── FeignConfig.java
│   │   │           └── MybatisPlusConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── bootstrap.yml
│   │       └── mapper/                  # MyBatis XML
│   └── test/                            # 测试代码
├── pom.xml
└── README.md
```

### 7.2 代码规范

#### 7.2.1 命名规范
```java
// 类名: 大驼峰
public class ConsultationService {}

// 方法名: 小驼峰
public void createConsultation() {}

// 常量: 全大写下划线
public static final int MAX_RETRY_COUNT = 3;

// 包名: 全小写
package com.hospital.consultation.service;
```

#### 7.2.2 注释规范
```java
/**
 * 创建问诊订单
 * 
 * @param request 问诊请求
 * @return 订单信息
 * @throws BusinessException 业务异常
 */
@Transactional(rollbackFor = Exception.class)
public ConsultationOrderDTO createOrder(CreateConsultationRequest request) {
    // 1. 参数校验
    validateRequest(request);
    
    // 2. 检查医生状态
    DoctorDTO doctor = checkDoctorAvailable(request.getDoctorId());
    
    // 3. 创建订单
    ConsultationOrder order = buildOrder(request, doctor);
    consultationMapper.insert(order);
    
    // 4. 发送通知
    notifyDoctor(order);
    
    return convertToDTO(order);
}
```

### 7.3 日志规范

```java
@Slf4j
public class ConsultationServiceImpl {
    
    public void processConsultation(Long orderId) {
        // INFO: 关键业务流程
        log.info("开始处理问诊订单, orderId={}", orderId);
        
        try {
            // 业务逻辑
            
            // INFO: 业务成功
            log.info("问诊订单处理成功, orderId={}", orderId);
            
        } catch (BusinessException e) {
            // WARN: 业务异常(可预期)
            log.warn("问诊订单处理失败, orderId={}, error={}", orderId, e.getMessage());
            throw e;
            
        } catch (Exception e) {
            // ERROR: 系统异常(不可预期)
            log.error("问诊订单处理异常, orderId={}", orderId, e);
            throw new SystemException("系统异常");
        }
    }
}
```

---

## 八、运维监控

### 8.1 健康检查

```yaml
# Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
    redis:
      enabled: true
```

### 8.2 链路追踪

```xml
<!-- SkyWalking Agent -->
-javaagent:/opt/skywalking/agent/skywalking-agent.jar
-DSW_AGENT_NAME=medical-consultation-service
-DSW_AGENT_COLLECTOR_BACKEND_SERVICES=skywalking-oap:11800
```

### 8.3 日志收集

```yaml
# Logback配置
logging:
  level:
    root: INFO
    com.hospital: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: /logs/consultation-service.log
    max-size: 100MB
    max-history: 30
```

---

## 九、部署架构

### 9.1 容器化部署

```dockerfile
# Dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/consultation-service.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 9.2 Kubernetes配置

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: consultation-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: consultation-service
  template:
    metadata:
      labels:
        app: consultation-service
    spec:
      containers:
      - name: consultation-service
        image: hospital/consultation-service:1.0.0
        ports:
        - containerPort: 8082
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
```

---

## 十、附录

### 10.1 技术选型对比

| 技术选型 | 优势 | 劣势 | 选择理由 |
|---------|------|------|---------|
| JDK 11 | LTS支持、性能优化 | 部分库兼容性 | 长期支持稳定 |
| Spring Boot | 快速开发、生态完善 | 学习曲线 | 行业标准 |
| Nacos | 配置+注册一体 | 相对年轻 | 阿里生态整合 |
| MySQL 8.0 | 稳定可靠、生态成熟 | 水平扩展复杂 | 事务保障需求 |

### 10.2 常见问题FAQ

**Q: 为什么选择Nacos而不是Eureka?**
A: Nacos支持配置管理和服务发现一体化，且性能更优，社区活跃度高。

**Q: OpenFeign调用超时如何处理?**
A: 配置合理的超时时间，实现FallbackFactory降级逻辑，记录日志便于排查。

**Q: 如何保证分布式事务一致性?**
A: 支付等强一致性场景使用Seata分布式事务框架，其他场景使用最终一致性方案。

---

**文档维护**: 后端架构组  
**最后更新**: 2026-01-27  
**文档状态**: 正式发布
