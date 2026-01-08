package com.architecture.designpattern.strategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 企业级策略模式示例
 * 包含：消息推送策略、用户认证策略、缓存策略、限流策略等企业应用场景
 */
public class EnterpriseExamples {

    // ================ 消息推送策略 ================
    
    /**
     * 消息内容
     */
    static class Message {
        private final String id;
        private final String title;
        private final String content;
        private final String recipient;
        private final MessagePriority priority;
        private final Map<String, Object> metadata;
        private final LocalDateTime createTime;
        
        public Message(String id, String title, String content, String recipient, MessagePriority priority) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.recipient = recipient;
            this.priority = priority;
            this.metadata = new ConcurrentHashMap<>();
            this.createTime = LocalDateTime.now();
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getRecipient() { return recipient; }
        public MessagePriority getPriority() { return priority; }
        public Map<String, Object> getMetadata() { return metadata; }
        public LocalDateTime getCreateTime() { return createTime; }
        
        public Message addMetadata(String key, Object value) {
            metadata.put(key, value);
            return this;
        }
        
        @Override
        public String toString() {
            return String.format("Message{id='%s', title='%s', recipient='%s', priority=%s}", 
                id, title, recipient, priority);
        }
    }
    
    /**
     * 消息优先级
     */
    enum MessagePriority {
        LOW(1, "低优先级"),
        NORMAL(2, "普通优先级"), 
        HIGH(3, "高优先级"),
        URGENT(4, "紧急优先级");
        
        private final int level;
        private final String description;
        
        MessagePriority(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }
    
    /**
     * 推送结果
     */
    static class PushResult {
        private final boolean success;
        private final String messageId;
        private final String channel;
        private final String targetId;
        private final String errorMessage;
        private final LocalDateTime sentTime;
        
        private PushResult(boolean success, String messageId, String channel, String targetId, String errorMessage) {
            this.success = success;
            this.messageId = messageId;
            this.channel = channel;
            this.targetId = targetId;
            this.errorMessage = errorMessage;
            this.sentTime = LocalDateTime.now();
        }
        
        public static PushResult success(String messageId, String channel, String targetId) {
            return new PushResult(true, messageId, channel, targetId, null);
        }
        
        public static PushResult failure(String messageId, String channel, String errorMessage) {
            return new PushResult(false, messageId, channel, null, errorMessage);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessageId() { return messageId; }
        public String getChannel() { return channel; }
        public String getTargetId() { return targetId; }
        public String getErrorMessage() { return errorMessage; }
        public LocalDateTime getSentTime() { return sentTime; }
        
        @Override
        public String toString() {
            return String.format("PushResult{success=%s, messageId='%s', channel='%s', targetId='%s', error='%s'}", 
                success, messageId, channel, targetId, errorMessage);
        }
    }
    
    /**
     * 消息推送策略接口
     */
    interface MessagePushStrategy extends Strategy<Message, PushResult> {
        String getChannelName();
        MessagePriority getSupportedMinPriority();
        boolean isAsync();
        int getMaxRetryCount();
    }
    
    /**
     * 抽象消息推送策略
     */
    abstract static class AbstractMessagePushStrategy extends AbstractStrategy<Message, PushResult> 
            implements MessagePushStrategy {
        
        protected final String channelName;
        protected final MessagePriority supportedMinPriority;
        protected final boolean async;
        protected final int maxRetryCount;
        
        protected AbstractMessagePushStrategy(String name, String description, String channelName,
                MessagePriority supportedMinPriority, boolean async, int maxRetryCount) {
            super(name, description);
            this.channelName = channelName;
            this.supportedMinPriority = supportedMinPriority;
            this.async = async;
            this.maxRetryCount = maxRetryCount;
        }
        
        @Override
        protected void beforeExecute(Message input) {
            System.out.println(String.format("[%s] 开始推送消息: %s -> %s", 
                channelName, input.getTitle(), input.getRecipient()));
            validateMessage(input);
        }
        
        @Override
        protected void afterExecute(Message input, PushResult result) {
            System.out.println(String.format("[%s] 推送完成: %s", channelName, result));
        }
        
        protected void validateMessage(Message message) {
            if (message.getPriority().getLevel() < supportedMinPriority.getLevel()) {
                throw new IllegalArgumentException(
                    String.format("消息优先级%s低于渠道%s最低要求%s", 
                        message.getPriority(), channelName, supportedMinPriority));
            }
        }
        
        @Override
        public String getChannelName() { return channelName; }
        @Override
        public MessagePriority getSupportedMinPriority() { return supportedMinPriority; }
        @Override
        public boolean isAsync() { return async; }
        @Override
        public int getMaxRetryCount() { return maxRetryCount; }
    }
    
    /**
     * 邮件推送策略
     */
    static class EmailPushStrategy extends AbstractMessagePushStrategy {
        
        public EmailPushStrategy() {
            super("EMAIL", "邮件推送策略", "邮件", 
                MessagePriority.LOW, false, 3);
        }
        
        @Override
        protected PushResult doExecute(Message message) {
            try {
                // 模拟邮件发送
                Thread.sleep(200);
                
                // 模拟邮件地址验证
                if (!message.getRecipient().contains("@")) {
                    return PushResult.failure(message.getId(), channelName, "无效的邮件地址");
                }
                
                // 模拟95%成功率
                if (Math.random() < 0.95) {
                    String emailId = "EMAIL_" + System.currentTimeMillis();
                    return PushResult.success(message.getId(), channelName, emailId);
                } else {
                    return PushResult.failure(message.getId(), channelName, "SMTP服务器暂不可用");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return PushResult.failure(message.getId(), channelName, "发送被中断");
            }
        }
    }
    
    /**
     * 短信推送策略
     */
    static class SmsStrategy extends AbstractMessagePushStrategy {
        
        public SmsStrategy() {
            super("SMS", "短信推送策略", "短信", 
                MessagePriority.NORMAL, true, 2);
        }
        
        @Override
        protected PushResult doExecute(Message message) {
            try {
                // 模拟短信发送
                Thread.sleep(100);
                
                // 模拟手机号验证
                if (!message.getRecipient().matches("^1[3-9]\\d{9}$")) {
                    return PushResult.failure(message.getId(), channelName, "无效的手机号码");
                }
                
                // 模拟90%成功率
                if (Math.random() < 0.90) {
                    String smsId = "SMS_" + System.currentTimeMillis();
                    return PushResult.success(message.getId(), channelName, smsId);
                } else {
                    return PushResult.failure(message.getId(), channelName, "短信网关异常");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return PushResult.failure(message.getId(), channelName, "发送被中断");
            }
        }
    }
    
    /**
     * APP推送策略
     */
    static class AppPushStrategy extends AbstractMessagePushStrategy {
        
        public AppPushStrategy() {
            super("APP_PUSH", "APP推送策略", "APP推送", 
                MessagePriority.HIGH, true, 1);
        }
        
        @Override
        protected PushResult doExecute(Message message) {
            try {
                // 模拟APP推送
                Thread.sleep(50);
                
                // 模拟设备ID验证
                String deviceId = message.getRecipient();
                if (deviceId.length() < 10) {
                    return PushResult.failure(message.getId(), channelName, "无效的设备ID");
                }
                
                // 模拟85%成功率
                if (Math.random() < 0.85) {
                    String pushId = "PUSH_" + System.currentTimeMillis();
                    return PushResult.success(message.getId(), channelName, pushId);
                } else {
                    return PushResult.failure(message.getId(), channelName, "设备离线或推送服务异常");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return PushResult.failure(message.getId(), channelName, "发送被中断");
            }
        }
    }

    // ================ 用户认证策略 ================
    
    /**
     * 认证请求
     */
    static class AuthRequest {
        private final String username;
        private final String credential;
        private final String clientIp;
        private final Map<String, Object> attributes;
        
        public AuthRequest(String username, String credential, String clientIp) {
            this.username = username;
            this.credential = credential;
            this.clientIp = clientIp;
            this.attributes = new ConcurrentHashMap<>();
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getCredential() { return credential; }
        public String getClientIp() { return clientIp; }
        public Map<String, Object> getAttributes() { return attributes; }
        
        public AuthRequest addAttribute(String key, Object value) {
            attributes.put(key, value);
            return this;
        }
        
        @Override
        public String toString() {
            return String.format("AuthRequest{username='%s', clientIp='%s'}", username, clientIp);
        }
    }
    
    /**
     * 认证结果
     */
    static class AuthResult {
        private final boolean success;
        private final String userId;
        private final String token;
        private final String message;
        private final LocalDateTime authTime;
        private final long validDuration; // 有效期(秒)
        
        private AuthResult(boolean success, String userId, String token, String message, long validDuration) {
            this.success = success;
            this.userId = userId;
            this.token = token;
            this.message = message;
            this.authTime = LocalDateTime.now();
            this.validDuration = validDuration;
        }
        
        public static AuthResult success(String userId, String token, long validDuration) {
            return new AuthResult(true, userId, token, "认证成功", validDuration);
        }
        
        public static AuthResult failure(String message) {
            return new AuthResult(false, null, null, message, 0);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getUserId() { return userId; }
        public String getToken() { return token; }
        public String getMessage() { return message; }
        public LocalDateTime getAuthTime() { return authTime; }
        public long getValidDuration() { return validDuration; }
        
        public LocalDateTime getExpireTime() {
            return authTime.plusSeconds(validDuration);
        }
        
        @Override
        public String toString() {
            return String.format("AuthResult{success=%s, userId='%s', token='%s', message='%s'}", 
                success, userId, token, message);
        }
    }
    
    /**
     * 用户认证策略接口
     */
    interface UserAuthStrategy extends Strategy<AuthRequest, AuthResult> {
        long getTokenValidDuration();
        boolean supportsTwoFactor();
        int getMaxFailAttempts();
    }
    
    /**
     * 用户名密码认证策略
     */
    static class UsernamePasswordAuthStrategy extends AbstractStrategy<AuthRequest, AuthResult> 
            implements UserAuthStrategy {
        
        private static final Map<String, String> userDatabase = new ConcurrentHashMap<>();
        private static final Map<String, Integer> failAttempts = new ConcurrentHashMap<>();
        
        static {
            // 初始化用户数据
            userDatabase.put("admin", "admin123");
            userDatabase.put("user1", "password1");
            userDatabase.put("user2", "password2");
        }
        
        public UsernamePasswordAuthStrategy() {
            super("USERNAME_PASSWORD", "用户名密码认证策略");
        }
        
        @Override
        protected void beforeExecute(AuthRequest input) {
            System.out.println(String.format("开始用户名密码认证: %s", input.getUsername()));
        }
        
        @Override
        protected AuthResult doExecute(AuthRequest request) {
            String username = request.getUsername();
            String password = request.getCredential();
            
            // 检查失败次数
            int attempts = failAttempts.getOrDefault(username, 0);
            if (attempts >= getMaxFailAttempts()) {
                return AuthResult.failure("账户被锁定，请稍后再试");
            }
            
            // 验证用户名密码
            String correctPassword = userDatabase.get(username);
            if (correctPassword == null || !correctPassword.equals(password)) {
                failAttempts.put(username, attempts + 1);
                return AuthResult.failure("用户名或密码错误");
            }
            
            // 认证成功，清除失败记录
            failAttempts.remove(username);
            String token = generateToken(username);
            return AuthResult.success(username, token, getTokenValidDuration());
        }
        
        private String generateToken(String username) {
            return "TOKEN_" + username + "_" + System.currentTimeMillis();
        }
        
        @Override
        public long getTokenValidDuration() {
            return TimeUnit.HOURS.toSeconds(24); // 24小时
        }
        
        @Override
        public boolean supportsTwoFactor() {
            return false;
        }
        
        @Override
        public int getMaxFailAttempts() {
            return 5;
        }
    }
    
    /**
     * OAuth认证策略
     */
    static class OAuthAuthStrategy extends AbstractStrategy<AuthRequest, AuthResult> 
            implements UserAuthStrategy {
        
        public OAuthAuthStrategy() {
            super("OAUTH", "OAuth第三方认证策略");
        }
        
        @Override
        protected AuthResult doExecute(AuthRequest request) {
            try {
                String oauthToken = request.getCredential();
                
                // 模拟OAuth验证
                Thread.sleep(300);
                
                if (oauthToken == null || !oauthToken.startsWith("oauth_")) {
                    return AuthResult.failure("无效的OAuth令牌");
                }
                
                // 模拟OAuth验证95%成功率
                if (Math.random() < 0.95) {
                    String userId = extractUserIdFromOAuth(oauthToken);
                    String token = generateOAuthToken(userId);
                    return AuthResult.success(userId, token, getTokenValidDuration());
                } else {
                    return AuthResult.failure("OAuth服务器验证失败");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return AuthResult.failure("认证过程被中断");
            }
        }
        
        private String extractUserIdFromOAuth(String oauthToken) {
            // 模拟从OAuth令牌中提取用户ID
            return "oauth_user_" + Math.abs(oauthToken.hashCode()) % 10000;
        }
        
        private String generateOAuthToken(String userId) {
            return "OAUTH_TOKEN_" + userId + "_" + System.currentTimeMillis();
        }
        
        @Override
        public long getTokenValidDuration() {
            return TimeUnit.HOURS.toSeconds(2); // 2小时
        }
        
        @Override
        public boolean supportsTwoFactor() {
            return true;
        }
        
        @Override
        public int getMaxFailAttempts() {
            return 3;
        }
    }

    // ================ 缓存策略 ================
    
    /**
     * 缓存请求
     */
    static class CacheRequest {
        private final String key;
        private final Object value;
        private final long ttlSeconds;
        private final Map<String, Object> options;
        
        public CacheRequest(String key, Object value, long ttlSeconds) {
            this.key = key;
            this.value = value;
            this.ttlSeconds = ttlSeconds;
            this.options = new ConcurrentHashMap<>();
        }
        
        // Getters
        public String getKey() { return key; }
        public Object getValue() { return value; }
        public long getTtlSeconds() { return ttlSeconds; }
        public Map<String, Object> getOptions() { return options; }
        
        public CacheRequest addOption(String key, Object value) {
            options.put(key, value);
            return this;
        }
    }
    
    /**
     * 缓存结果
     */
    static class CacheResult {
        private final boolean success;
        private final String key;
        private final Object value;
        private final String operation;
        private final String message;
        private final LocalDateTime timestamp;
        
        private CacheResult(boolean success, String key, Object value, String operation, String message) {
            this.success = success;
            this.key = key;
            this.value = value;
            this.operation = operation;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
        
        public static CacheResult success(String key, Object value, String operation) {
            return new CacheResult(true, key, value, operation, "操作成功");
        }
        
        public static CacheResult failure(String key, String operation, String message) {
            return new CacheResult(false, key, null, operation, message);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getKey() { return key; }
        public Object getValue() { return value; }
        public String getOperation() { return operation; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("CacheResult{success=%s, key='%s', operation='%s', message='%s'}", 
                success, key, operation, message);
        }
    }
    
    /**
     * 缓存策略接口
     */
    interface CacheStrategy {
        CacheResult put(CacheRequest request);
        CacheResult get(String key);
        CacheResult remove(String key);
        String getCacheType();
        long getMaxSize();
        long getDefaultTtlSeconds();
    }
    
    /**
     * 内存缓存策略
     */
    static class MemoryCacheStrategy implements CacheStrategy {
        
        private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();
        private final long maxSize = 1000;
        private final long defaultTtl = TimeUnit.MINUTES.toSeconds(30);
        
        private static class CacheItem {
            final Object value;
            final LocalDateTime expireTime;
            
            CacheItem(Object value, long ttlSeconds) {
                this.value = value;
                this.expireTime = LocalDateTime.now().plusSeconds(ttlSeconds);
            }
            
            boolean isExpired() {
                return LocalDateTime.now().isAfter(expireTime);
            }
        }
        
        @Override
        public CacheResult put(CacheRequest request) {
            try {
                if (cache.size() >= maxSize) {
                    cleanExpiredItems();
                    if (cache.size() >= maxSize) {
                        return CacheResult.failure(request.getKey(), "PUT", "缓存已满");
                    }
                }
                
                long ttl = request.getTtlSeconds() > 0 ? request.getTtlSeconds() : defaultTtl;
                CacheItem item = new CacheItem(request.getValue(), ttl);
                cache.put(request.getKey(), item);
                
                System.out.println(String.format("[内存缓存] 存储: %s (TTL: %ds)", request.getKey(), ttl));
                return CacheResult.success(request.getKey(), request.getValue(), "PUT");
                
            } catch (Exception e) {
                return CacheResult.failure(request.getKey(), "PUT", "存储失败: " + e.getMessage());
            }
        }
        
        @Override
        public CacheResult get(String key) {
            try {
                CacheItem item = cache.get(key);
                if (item == null) {
                    return CacheResult.failure(key, "GET", "键不存在");
                }
                
                if (item.isExpired()) {
                    cache.remove(key);
                    return CacheResult.failure(key, "GET", "缓存已过期");
                }
                
                System.out.println(String.format("[内存缓存] 命中: %s", key));
                return CacheResult.success(key, item.value, "GET");
                
            } catch (Exception e) {
                return CacheResult.failure(key, "GET", "获取失败: " + e.getMessage());
            }
        }
        
        @Override
        public CacheResult remove(String key) {
            try {
                Object removed = cache.remove(key);
                if (removed == null) {
                    return CacheResult.failure(key, "REMOVE", "键不存在");
                }
                
                System.out.println(String.format("[内存缓存] 删除: %s", key));
                return CacheResult.success(key, null, "REMOVE");
                
            } catch (Exception e) {
                return CacheResult.failure(key, "REMOVE", "删除失败: " + e.getMessage());
            }
        }
        
        private void cleanExpiredItems() {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            System.out.println("[内存缓存] 清理过期项目");
        }
        
        @Override
        public String getCacheType() {
            return "MEMORY";
        }
        
        @Override
        public long getMaxSize() {
            return maxSize;
        }
        
        @Override
        public long getDefaultTtlSeconds() {
            return defaultTtl;
        }
    }
    
    /**
     * Redis模拟缓存策略
     */
    static class RedisCacheStrategy implements CacheStrategy {
        
        // 模拟Redis存储
        private final Map<String, String> redisStore = new ConcurrentHashMap<>();
        private final Map<String, LocalDateTime> expireMap = new ConcurrentHashMap<>();
        private final long maxSize = 10000;
        private final long defaultTtl = TimeUnit.HOURS.toSeconds(1);
        
        @Override
        public CacheResult put(CacheRequest request) {
            try {
                // 模拟Redis延迟
                Thread.sleep(10);
                
                if (redisStore.size() >= maxSize) {
                    return CacheResult.failure(request.getKey(), "PUT", "Redis存储空间不足");
                }
                
                String serializedValue = serialize(request.getValue());
                long ttl = request.getTtlSeconds() > 0 ? request.getTtlSeconds() : defaultTtl;
                
                redisStore.put(request.getKey(), serializedValue);
                expireMap.put(request.getKey(), LocalDateTime.now().plusSeconds(ttl));
                
                System.out.println(String.format("[Redis缓存] 存储: %s (TTL: %ds)", request.getKey(), ttl));
                return CacheResult.success(request.getKey(), request.getValue(), "PUT");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CacheResult.failure(request.getKey(), "PUT", "操作被中断");
            } catch (Exception e) {
                return CacheResult.failure(request.getKey(), "PUT", "存储失败: " + e.getMessage());
            }
        }
        
        @Override
        public CacheResult get(String key) {
            try {
                // 模拟Redis延迟
                Thread.sleep(5);
                
                LocalDateTime expireTime = expireMap.get(key);
                if (expireTime == null || LocalDateTime.now().isAfter(expireTime)) {
                    redisStore.remove(key);
                    expireMap.remove(key);
                    return CacheResult.failure(key, "GET", "缓存已过期或不存在");
                }
                
                String serializedValue = redisStore.get(key);
                if (serializedValue == null) {
                    return CacheResult.failure(key, "GET", "键不存在");
                }
                
                Object value = deserialize(serializedValue);
                System.out.println(String.format("[Redis缓存] 命中: %s", key));
                return CacheResult.success(key, value, "GET");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CacheResult.failure(key, "GET", "操作被中断");
            } catch (Exception e) {
                return CacheResult.failure(key, "GET", "获取失败: " + e.getMessage());
            }
        }
        
        @Override
        public CacheResult remove(String key) {
            try {
                Thread.sleep(5);
                
                String removed = redisStore.remove(key);
                expireMap.remove(key);
                
                if (removed == null) {
                    return CacheResult.failure(key, "REMOVE", "键不存在");
                }
                
                System.out.println(String.format("[Redis缓存] 删除: %s", key));
                return CacheResult.success(key, null, "REMOVE");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CacheResult.failure(key, "REMOVE", "操作被中断");
            }
        }
        
        private String serialize(Object value) {
            return "SERIALIZED:" + value.toString();
        }
        
        private Object deserialize(String serialized) {
            return serialized.replace("SERIALIZED:", "");
        }
        
        @Override
        public String getCacheType() {
            return "REDIS";
        }
        
        @Override
        public long getMaxSize() {
            return maxSize;
        }
        
        @Override
        public long getDefaultTtlSeconds() {
            return defaultTtl;
        }
    }

    // ================ 管理器类 ================
    
    /**
     * 消息推送管理器
     */
    static class MessagePushManager {
        private final StrategyRegistry<Message, PushResult> strategies;
        private final ConditionalStrategySelector<Message, PushResult> autoSelector;
        
        public MessagePushManager() {
            this.strategies = new StrategyRegistry<>();
            this.autoSelector = new ConditionalStrategySelector<>();
            initializeStrategies();
        }
        
        private void initializeStrategies() {
            strategies.register(new EmailPushStrategy());
            strategies.register(new SmsStrategy());
            strategies.register(new AppPushStrategy());
            
            // 设置自动选择规则
            autoSelector
                .when(msg -> msg.getPriority() == MessagePriority.URGENT, strategies.get("APP_PUSH"))
                .when(msg -> msg.getPriority().getLevel() >= MessagePriority.HIGH.getLevel(), strategies.get("SMS"))
                .otherwise(strategies.get("EMAIL"));
        }
        
        public PushResult push(String strategyName, Message message) {
            MessagePushStrategy strategy = (MessagePushStrategy) strategies.get(strategyName);
            if (strategy == null) {
                return PushResult.failure(message.getId(), "UNKNOWN", "不支持的推送策略: " + strategyName);
            }
            return strategy.execute(message);
        }
        
        public PushResult autoPush(Message message) {
            return autoSelector.execute(message);
        }
        
        public void printSupportedChannels() {
            System.out.println("支持的推送渠道:");
            for (String name : strategies.getAllNames()) {
                MessagePushStrategy strategy = (MessagePushStrategy) strategies.get(name);
                System.out.println(String.format("- %s: %s (最低优先级: %s, 异步: %s, 最大重试: %d)", 
                    name, strategy.getDescription(), 
                    strategy.getSupportedMinPriority().getDescription(),
                    strategy.isAsync() ? "是" : "否",
                    strategy.getMaxRetryCount()));
            }
        }
    }
    
    /**
     * 认证管理器
     */
    static class AuthenticationManager {
        private final StrategyRegistry<AuthRequest, AuthResult> strategies;
        
        public AuthenticationManager() {
            this.strategies = new StrategyRegistry<>();
            strategies.register(new UsernamePasswordAuthStrategy());
            strategies.register(new OAuthAuthStrategy());
        }
        
        public AuthResult authenticate(String strategyName, AuthRequest request) {
            UserAuthStrategy strategy = (UserAuthStrategy) strategies.get(strategyName);
            if (strategy == null) {
                return AuthResult.failure("不支持的认证策略: " + strategyName);
            }
            return strategy.execute(request);
        }
        
        public void printSupportedMethods() {
            System.out.println("支持的认证方式:");
            for (String name : strategies.getAllNames()) {
                UserAuthStrategy strategy = (UserAuthStrategy) strategies.get(name);
                System.out.println(String.format("- %s: %s (令牌有效期: %ds, 双因子: %s, 最大失败: %d)", 
                    name, strategy.getDescription(),
                    strategy.getTokenValidDuration(),
                    strategy.supportsTwoFactor() ? "支持" : "不支持",
                    strategy.getMaxFailAttempts()));
            }
        }
    }
    
    /**
     * 缓存管理器
     */
    static class CacheManager {
        private final Map<String, CacheStrategy> strategies;
        
        public CacheManager() {
            this.strategies = new ConcurrentHashMap<>();
            strategies.put("MEMORY", new MemoryCacheStrategy());
            strategies.put("REDIS", new RedisCacheStrategy());
        }
        
        public CacheResult put(String cacheType, CacheRequest request) {
            CacheStrategy strategy = strategies.get(cacheType);
            if (strategy == null) {
                return CacheResult.failure(request.getKey(), "PUT", "不支持的缓存类型: " + cacheType);
            }
            return strategy.put(request);
        }
        
        public CacheResult get(String cacheType, String key) {
            CacheStrategy strategy = strategies.get(cacheType);
            if (strategy == null) {
                return CacheResult.failure(key, "GET", "不支持的缓存类型: " + cacheType);
            }
            return strategy.get(key);
        }
        
        public CacheResult remove(String cacheType, String key) {
            CacheStrategy strategy = strategies.get(cacheType);
            if (strategy == null) {
                return CacheResult.failure(key, "REMOVE", "不支持的缓存类型: " + cacheType);
            }
            return strategy.remove(key);
        }
        
        public void printSupportedTypes() {
            System.out.println("支持的缓存类型:");
            for (Map.Entry<String, CacheStrategy> entry : strategies.entrySet()) {
                CacheStrategy strategy = entry.getValue();
                System.out.println(String.format("- %s: 最大容量=%d, 默认TTL=%ds", 
                    entry.getKey(), strategy.getMaxSize(), strategy.getDefaultTtlSeconds()));
            }
        }
    }

    // ================ 演示方法 ================
    
    /**
     * 演示消息推送策略
     */
    public static void demonstrateMessagePush() {
        System.out.println("========== 消息推送策略演示 ==========");
        
        MessagePushManager manager = new MessagePushManager();
        manager.printSupportedChannels();
        System.out.println();
        
        // 创建不同优先级的消息
        Message lowMsg = new Message("MSG001", "系统通知", "您有一条新的系统消息", "user@example.com", MessagePriority.LOW);
        Message normalMsg = new Message("MSG002", "订单更新", "您的订单已发货", "13812345678", MessagePriority.NORMAL);
        Message urgentMsg = new Message("MSG003", "安全警告", "检测到异常登录", "device_12345", MessagePriority.URGENT);
        
        // 指定策略推送
        System.out.println("=== 指定策略推送 ===");
        manager.push("EMAIL", lowMsg);
        manager.push("SMS", normalMsg);
        manager.push("APP_PUSH", urgentMsg);
        System.out.println();
        
        // 自动选择策略推送
        System.out.println("=== 自动策略选择推送 ===");
        manager.autoPush(lowMsg);
        manager.autoPush(normalMsg);
        manager.autoPush(urgentMsg);
        System.out.println();
    }
    
    /**
     * 演示用户认证策略
     */
    public static void demonstrateAuthentication() {
        System.out.println("========== 用户认证策略演示 ==========");
        
        AuthenticationManager manager = new AuthenticationManager();
        manager.printSupportedMethods();
        System.out.println();
        
        // 用户名密码认证
        System.out.println("=== 用户名密码认证 ===");
        AuthRequest request1 = new AuthRequest("admin", "admin123", "192.168.1.100");
        AuthResult result1 = manager.authenticate("USERNAME_PASSWORD", request1);
        System.out.println("认证结果: " + result1);
        
        AuthRequest request2 = new AuthRequest("admin", "wrongpass", "192.168.1.100");
        AuthResult result2 = manager.authenticate("USERNAME_PASSWORD", request2);
        System.out.println("认证结果: " + result2);
        System.out.println();
        
        // OAuth认证
        System.out.println("=== OAuth认证 ===");
        AuthRequest request3 = new AuthRequest("oauth_user", "oauth_token_12345", "192.168.1.101");
        AuthResult result3 = manager.authenticate("OAUTH", request3);
        System.out.println("认证结果: " + result3);
        System.out.println();
    }
    
    /**
     * 演示缓存策略
     */
    public static void demonstrateCache() {
        System.out.println("========== 缓存策略演示 ==========");
        
        CacheManager manager = new CacheManager();
        manager.printSupportedTypes();
        System.out.println();
        
        // 内存缓存测试
        System.out.println("=== 内存缓存测试 ===");
        CacheRequest memRequest = new CacheRequest("user:1001", "用户数据1001", 60);
        manager.put("MEMORY", memRequest);
        CacheResult memGet = manager.get("MEMORY", "user:1001");
        System.out.println("获取结果: " + memGet);
        System.out.println();
        
        // Redis缓存测试
        System.out.println("=== Redis缓存测试 ===");
        CacheRequest redisRequest = new CacheRequest("session:abc123", "会话数据", 300);
        manager.put("REDIS", redisRequest);
        CacheResult redisGet = manager.get("REDIS", "session:abc123");
        System.out.println("获取结果: " + redisGet);
        manager.remove("REDIS", "session:abc123");
        System.out.println();
    }
    
    /**
     * 主演示方法
     */
    public static void main(String[] args) {
        demonstrateMessagePush();
        demonstrateAuthentication();
        demonstrateCache();
    }
}