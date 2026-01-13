package com.architecture.apppush;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 智能推送服务
 *
 * 核心逻辑：
 * 1. 查询用户在线状态
 * 2. 在线：通过WebSocket推送
 * 3. 离线：通过第三方推送服务（极光/FCM）推送
 *
 * 优势：
 * - 避免重复推送
 * - 节省推送成本
 * - 提升用户体验
 */
public class SmartPushService {

    private static final Logger logger = LoggerFactory.getLogger(SmartPushService.class);

    private final OnlineStatusManager statusManager;
    private final WebSocketServer webSocketServer;
    private final JPushClient jPushClient;

    public SmartPushService(OnlineStatusManager statusManager,
                           WebSocketServer webSocketServer,
                           JPushClient jPushClient) {
        this.statusManager = statusManager;
        this.webSocketServer = webSocketServer;
        this.jPushClient = jPushClient;
    }

    /**
     * 推送消息给单个用户
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void pushToUser(String userId, PushMessage message) {

        // 1. 查询用户在线状态
        boolean online = statusManager.isUserOnline(userId);

        if (online) {
            // 2. 在线：WebSocket推送
            logger.info("用户在线，WebSocket推送: userId={}", userId);
            webSocketServer.sendToUser(userId, message);

            // 记录推送日志
            logPush(userId, message, "websocket", true);

        } else {
            // 3. 离线：极光推送
            logger.info("用户离线，极光推送: userId={}", userId);
            boolean success = jPushClient.push(userId, message);

            // 记录推送日志
            logPush(userId, message, "jpush", success);
        }
    }

    /**
     * 推送消息给指定设备
     *
     * 适用场景：需要指定推送到某个设备（如只推送到手机，不推送到平板）
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param message 消息内容
     */
    public void pushToDevice(String userId, String deviceId, PushMessage message) {

        // 1. 查询设备在线状态
        boolean online = statusManager.isDeviceOnline(userId, deviceId);

        if (online) {
            // 2. 在线：WebSocket推送
            logger.info("设备在线，WebSocket推送: userId={}, deviceId={}", userId, deviceId);
            webSocketServer.sendToDevice(userId, deviceId, message);

        } else {
            // 3. 离线：极光推送（指定设备）
            logger.info("设备离线，极光推送: userId={}, deviceId={}", userId, deviceId);
            jPushClient.pushToDevice(deviceId, message);
        }
    }

    /**
     * 批量推送
     *
     * 性能优化：批量查询在线状态，分类推送
     *
     * @param userIds 用户ID列表
     * @param message 消息内容
     */
    public void batchPush(List<String> userIds, PushMessage message) {

        logger.info("批量推送开始: userCount={}", userIds.size());

        // 1. 批量查询在线状态
        Map<String, Boolean> onlineStatus = statusManager.batchCheckOnline(userIds);

        // 2. 分类：在线用户 vs 离线用户
        List<String> onlineUsers = new ArrayList<>();
        List<String> offlineUsers = new ArrayList<>();

        for (String userId : userIds) {
            if (Boolean.TRUE.equals(onlineStatus.get(userId))) {
                onlineUsers.add(userId);
            } else {
                offlineUsers.add(userId);
            }
        }

        logger.info("批量推送分类: total={}, online={}, offline={}",
                userIds.size(), onlineUsers.size(), offlineUsers.size());

        // 3. 批量WebSocket推送（在线用户）
        if (!onlineUsers.isEmpty()) {
            for (String userId : onlineUsers) {
                webSocketServer.sendToUser(userId, message);
            }
        }

        // 4. 批量极光推送（离线用户）
        if (!offlineUsers.isEmpty()) {
            jPushClient.batchPush(offlineUsers, message);
        }

        logger.info("批量推送完成");
    }

    /**
     * 智能推送（带重试机制）
     *
     * 流程：
     * 1. 尝试WebSocket推送
     * 2. 如果失败（用户刚好下线），立即尝试极光推送
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void smartPushWithRetry(String userId, PushMessage message) {

        boolean online = statusManager.isUserOnline(userId);

        if (online) {
            // 尝试WebSocket推送
            try {
                webSocketServer.sendToUser(userId, message);
                logger.info("WebSocket推送成功: userId={}", userId);

            } catch (Exception e) {
                // WebSocket推送失败（可能刚好下线），立即尝试极光推送
                logger.warn("WebSocket推送失败，降级为极光推送: userId={}", userId, e);
                jPushClient.push(userId, message);
            }

        } else {
            // 直接极光推送
            jPushClient.push(userId, message);
        }
    }

    /**
     * 定时推送（延迟推送）
     *
     * 适用场景：定时提醒、预约推送
     *
     * @param userId 用户ID
     * @param message 消息内容
     * @param delaySeconds 延迟秒数
     */
    public void schedulePush(String userId, PushMessage message, int delaySeconds) {

        logger.info("定时推送: userId={}, delay={}秒", userId, delaySeconds);

        // 使用延迟队列实现（参考场景三：延迟队列）
        // delayQueue.offer(() -> pushToUser(userId, message), delaySeconds, TimeUnit.SECONDS);

        // 简化版：直接使用线程延迟
        new Thread(() -> {
            try {
                Thread.sleep(delaySeconds * 1000L);
                pushToUser(userId, message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 静默推送（后台推送）
     *
     * 适用场景：
     * - iOS后台推送（APNs静默推送）
     * - 用户在线但APP在后台
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void silentPush(String userId, PushMessage message) {

        // 查询用户的在线设备
        Set<String> devices = statusManager.getOnlineDevices(userId);

        for (String deviceId : devices) {
            OnlineStatusManager.OnlineStatus status = statusManager.getOnlineStatus(userId, deviceId);

            if (status != null) {
                if ("ios".equals(status.getPlatform())) {
                    // iOS：APNs静默推送
                    jPushClient.silentPushIOS(deviceId, message);
                    logger.info("iOS静默推送: userId={}, deviceId={}", userId, deviceId);

                } else {
                    // Android：WebSocket推送或极光推送
                    webSocketServer.sendToDevice(userId, deviceId, message);
                }
            }
        }
    }

    /**
     * 记录推送日志
     */
    private void logPush(String userId, PushMessage message, String channel, boolean success) {
        // 实际实现：写入数据库或日志系统
        logger.info("推送日志: userId={}, messageId={}, channel={}, success={}",
                userId, message.getMessageId(), channel, success);
    }

    /**
     * 推送消息对象
     */
    public static class PushMessage {
        private String messageId;      // 消息ID
        private String type;           // 消息类型（chat/notification/system）
        private String title;          // 标题
        private String content;        // 内容
        private Map<String, String> extras;  // 扩展字段

        public PushMessage(String messageId, String type, String title, String content) {
            this.messageId = messageId;
            this.type = type;
            this.title = title;
            this.content = content;
            this.extras = new HashMap<>();
        }

        public String getMessageId() {
            return messageId;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public Map<String, String> getExtras() {
            return extras;
        }

        public void addExtra(String key, String value) {
            extras.put(key, value);
        }
    }

    /**
     * 极光推送客户端（示例）
     */
    public static class JPushClient {

        private static final Logger logger = LoggerFactory.getLogger(JPushClient.class);

        /**
         * 推送给单个用户
         */
        public boolean push(String userId, PushMessage message) {
            // 实际实现：调用极光SDK
            // JPushClient jpush = new JPushClient(masterSecret, appKey);
            // PushPayload payload = buildPayload(userId, message);
            // PushResult result = jpush.sendPush(payload);

            logger.info("极光推送: userId={}, messageId={}, title={}",
                    userId, message.getMessageId(), message.getTitle());

            // 模拟推送成功
            return true;
        }

        /**
         * 推送给指定设备
         */
        public boolean pushToDevice(String deviceId, PushMessage message) {
            logger.info("极光推送（设备）: deviceId={}, messageId={}",
                    deviceId, message.getMessageId());
            return true;
        }

        /**
         * 批量推送
         */
        public boolean batchPush(List<String> userIds, PushMessage message) {
            logger.info("极光批量推送: userCount={}, messageId={}",
                    userIds.size(), message.getMessageId());

            // 实际实现：调用极光批量推送API
            // PushPayload payload = buildBatchPayload(userIds, message);
            // PushResult result = jpush.sendPush(payload);

            return true;
        }

        /**
         * iOS静默推送
         */
        public boolean silentPushIOS(String deviceId, PushMessage message) {
            logger.info("iOS静默推送: deviceId={}, messageId={}",
                    deviceId, message.getMessageId());

            // 实际实现：设置 content-available=1
            // IosAlert alert = IosAlert.newBuilder()
            //     .setContentAvailable(true)
            //     .build();

            return true;
        }
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        // 模拟场景：用户A给用户B发送聊天消息

        // 初始化（实际项目中通过Spring IoC管理）
        // OnlineStatusManager statusManager = new OnlineStatusManager(jedisPool);
        // WebSocketServer wsServer = new WebSocketServer("server-01", statusManager);
        // JPushClient jpushClient = new JPushClient();
        // SmartPushService pushService = new SmartPushService(statusManager, wsServer, jpushClient);

        // 构建消息
        PushMessage message = new PushMessage(
                "msg_" + System.currentTimeMillis(),
                "chat",
                "用户A",
                "你好，在吗？"
        );
        message.addExtra("fromUserId", "user_A");
        message.addExtra("toUserId", "user_B");

        // 智能推送
        // pushService.pushToUser("user_B", message);

        // 结果：
        // - 如果user_B在线：通过WebSocket推送，APP内直接显示消息
        // - 如果user_B离线：通过极光推送，手机收到系统通知
    }
}
