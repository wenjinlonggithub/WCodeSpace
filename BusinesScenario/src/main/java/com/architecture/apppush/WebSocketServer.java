package com.architecture.apppush;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务器（示例实现）
 *
 * 实际项目中使用：
 * - Spring WebSocket
 * - Netty WebSocket
 * - Java-WebSocket库
 *
 * 核心功能：
 * 1. 连接建立/断开
 * 2. 消息收发
 * 3. 心跳处理
 * 4. 用户会话管理
 *
 * 本示例展示核心逻辑，实际使用时替换为真实WebSocket实现
 */
public class WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private final String serverId;
    private final OnlineStatusManager statusManager;
    private final Gson gson = new Gson();

    // 会话管理：userId:deviceId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public WebSocketServer(String serverId, OnlineStatusManager statusManager) {
        this.serverId = serverId;
        this.statusManager = statusManager;
    }

    /**
     * WebSocket连接建立
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param platform 平台
     * @param token 认证token
     */
    public void onConnect(String userId, String deviceId, String platform, String token) {

        // 1. 验证token（实际项目中调用认证服务）
        if (!validateToken(token, userId)) {
            logger.warn("Token验证失败: userId={}, deviceId={}", userId, deviceId);
            return;
        }

        // 2. 检查是否需要踢出旧设备（互踢场景）
        String sessionKey = userId + ":" + deviceId;
        if (sessions.containsKey(sessionKey)) {
            // 同一设备重复连接，关闭旧连接
            WebSocketSession oldSession = sessions.get(sessionKey);
            oldSession.close("重复登录");
        }

        // 3. 创建新会话
        WebSocketSession session = new WebSocketSession(userId, deviceId, platform);
        sessions.put(sessionKey, session);

        // 4. 更新在线状态
        statusManager.online(userId, deviceId, serverId, platform);

        // 5. 发送连接成功消息
        sendToSession(session, new ConnectSuccessMessage("连接成功"));

        logger.info("WebSocket连接建立: userId={}, deviceId={}, platform={}, sessionCount={}",
                userId, deviceId, platform, sessions.size());
    }

    /**
     * WebSocket连接断开
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    public void onDisconnect(String userId, String deviceId) {

        String sessionKey = userId + ":" + deviceId;

        // 1. 移除会话
        sessions.remove(sessionKey);

        // 2. 更新在线状态（延迟5秒，给重连机会）
        scheduleOffline(userId, deviceId, 5000);

        logger.info("WebSocket连接断开: userId={}, deviceId={}, sessionCount={}",
                userId, deviceId, sessions.size());
    }

    /**
     * 收到消息
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param message 消息内容
     */
    public void onMessage(String userId, String deviceId, String message) {

        try {
            // 解析消息类型
            MessageWrapper wrapper = gson.fromJson(message, MessageWrapper.class);

            switch (wrapper.getType()) {
                case "heartbeat":
                    // 心跳消息
                    handleHeartbeat(userId, deviceId);
                    break;

                case "chat":
                    // 聊天消息
                    handleChatMessage(userId, wrapper.getData());
                    break;

                default:
                    logger.warn("未知消息类型: type={}", wrapper.getType());
            }

        } catch (Exception e) {
            logger.error("处理消息失败: userId={}, message={}", userId, message, e);
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(String userId, String deviceId) {
        // 更新在线状态的过期时间
        statusManager.heartbeat(userId, deviceId);

        // 回复心跳
        String sessionKey = userId + ":" + deviceId;
        WebSocketSession session = sessions.get(sessionKey);
        if (session != null) {
            sendToSession(session, new HeartbeatAckMessage());
        }

        logger.debug("收到心跳: userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(String userId, String data) {
        logger.info("收到聊天消息: userId={}, data={}", userId, data);
        // 处理业务逻辑...
    }

    /**
     * 向指定用户发送消息（所有在线设备）
     *
     * @param userId 用户ID
     * @param message 消息对象
     */
    public void sendToUser(String userId, Object message) {

        // 获取用户的所有在线设备
        var devices = statusManager.getOnlineDevices(userId);

        int successCount = 0;
        for (String deviceId : devices) {
            // 检查设备是否连接到本服务器
            String sessionKey = userId + ":" + deviceId;
            WebSocketSession session = sessions.get(sessionKey);

            if (session != null) {
                // 连接在本服务器，直接发送
                sendToSession(session, message);
                successCount++;
            } else {
                // 连接在其他服务器，通过消息队列转发
                forwardToOtherServer(userId, deviceId, message);
            }
        }

        logger.info("发送消息给用户: userId={}, deviceCount={}, successCount={}",
                userId, devices.size(), successCount);
    }

    /**
     * 向指定设备发送消息
     */
    public void sendToDevice(String userId, String deviceId, Object message) {
        String sessionKey = userId + ":" + deviceId;
        WebSocketSession session = sessions.get(sessionKey);

        if (session != null) {
            sendToSession(session, message);
        } else {
            logger.warn("会话不存在: userId={}, deviceId={}", userId, deviceId);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(Object message) {
        int count = 0;
        for (WebSocketSession session : sessions.values()) {
            sendToSession(session, message);
            count++;
        }
        logger.info("广播消息: sessionCount={}", count);
    }

    /**
     * 向会话发送消息（实际实现中调用WebSocket API）
     */
    private void sendToSession(WebSocketSession session, Object message) {
        try {
            String json = gson.toJson(message);
            // 实际实现：session.sendText(json);
            logger.debug("发送消息: userId={}, deviceId={}, message={}",
                    session.getUserId(), session.getDeviceId(), json);
        } catch (Exception e) {
            logger.error("发送消息失败: userId={}", session.getUserId(), e);
        }
    }

    /**
     * 转发消息到其他服务器（通过Redis Pub/Sub或消息队列）
     */
    private void forwardToOtherServer(String userId, String deviceId, Object message) {
        // 实际实现：
        // 1. 获取设备连接的服务器ID
        OnlineStatusManager.OnlineStatus status = statusManager.getOnlineStatus(userId, deviceId);
        if (status != null) {
            String targetServerId = status.getServerId();
            // 2. 通过Redis Pub/Sub发送到目标服务器
            // redis.publish("ws:server:" + targetServerId, json);
            logger.debug("转发消息到其他服务器: serverId={}, userId={}, deviceId={}",
                    targetServerId, userId, deviceId);
        }
    }

    /**
     * 延迟下线（给重连机会）
     */
    private void scheduleOffline(String userId, String deviceId, long delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);

                // 检查是否已经重连
                String sessionKey = userId + ":" + deviceId;
                if (!sessions.containsKey(sessionKey)) {
                    // 没有重连，执行下线
                    statusManager.offline(userId, deviceId, serverId);
                    logger.info("延迟下线执行: userId={}, deviceId={}", userId, deviceId);
                } else {
                    logger.info("用户已重连，取消下线: userId={}, deviceId={}", userId, deviceId);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Token验证（示例）
     */
    private boolean validateToken(String token, String userId) {
        // 实际实现：调用认证服务验证JWT token
        return token != null && !token.isEmpty();
    }

    /**
     * 获取当前服务器连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 服务器关闭
     */
    public void shutdown() {
        logger.info("WebSocket服务器关闭: serverId={}, sessionCount={}", serverId, sessions.size());

        // 关闭所有连接
        for (WebSocketSession session : sessions.values()) {
            session.close("服务器关闭");
        }

        // 清理在线状态
        statusManager.serverOffline(serverId);
    }

    // ==================== 消息对象 ====================

    static class MessageWrapper {
        private String type;
        private String data;

        public String getType() {
            return type;
        }

        public String getData() {
            return data;
        }
    }

    static class ConnectSuccessMessage {
        private String type = "connect_success";
        private String message;

        public ConnectSuccessMessage(String message) {
            this.message = message;
        }
    }

    static class HeartbeatAckMessage {
        private String type = "heartbeat_ack";
        private long timestamp = System.currentTimeMillis();
    }

    // ==================== WebSocket会话 ====================

    static class WebSocketSession {
        private final String userId;
        private final String deviceId;
        private final String platform;
        private final long createTime;

        public WebSocketSession(String userId, String deviceId, String platform) {
            this.userId = userId;
            this.deviceId = deviceId;
            this.platform = platform;
            this.createTime = System.currentTimeMillis();
        }

        public void close(String reason) {
            // 实际实现：调用WebSocket close方法
            logger.info("关闭会话: userId={}, deviceId={}, reason={}", userId, deviceId, reason);
        }

        public String getUserId() {
            return userId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getPlatform() {
            return platform;
        }

        public long getCreateTime() {
            return createTime;
        }
    }
}
