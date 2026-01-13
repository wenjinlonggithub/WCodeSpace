package com.architecture.apppush;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

/**
 * 基于REST API的心跳上报服务
 *
 * 方案说明：
 * 1. 客户端定时调用REST接口上报心跳（如每30秒）
 * 2. 服务端将在线状态写入Redis，设置过期时间（如60秒）
 * 3. 推送时查询Redis判断是否在线
 *
 * 优势：
 * - 实现简单，无需维护WebSocket长连接
 * - 兼容性好，任何HTTP客户端都可以调用
 * - 降低技术复杂度，适合快速开发
 *
 * 劣势：
 * - 实时性稍差（取决于心跳间隔）
 * - 无法服务端主动推送消息（需配合极光等推送）
 * - 流量和电量消耗相对较高
 *
 * 适用场景：
 * - 只需要判断在线状态，不需要实时双向通信
 * - 推送消息统一使用第三方推送服务
 * - 技术栈不支持WebSocket或想降低复杂度
 */
public class RestHeartbeatService {

    private static final Logger logger = LoggerFactory.getLogger(RestHeartbeatService.class);

    private static final String ONLINE_KEY_PREFIX = "online:rest:user:";
    private static final String ONLINE_DEVICES_PREFIX = "online:rest:devices:";

    // 心跳有效期（秒）
    // 建议设置为心跳间隔的2倍，如心跳30秒，过期时间60秒
    private static final int HEARTBEAT_EXPIRE_SECONDS = 60;

    private final JedisPool jedisPool;
    private final Gson gson = new Gson();

    public RestHeartbeatService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * REST接口：心跳上报
     *
     * 接口路径：POST /api/heartbeat
     *
     * 请求参数：
     * {
     *   "userId": "user123",
     *   "deviceId": "device456",
     *   "platform": "android",  // android/ios/web
     *   "appVersion": "1.0.0",
     *   "timestamp": 1704038400000
     * }
     *
     * 响应：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "nextHeartbeatTime": 30  // 下次心跳间隔（秒）
     *   }
     * }
     *
     * @param request 心跳请求
     * @return 心跳响应
     */
    public HeartbeatResponse reportHeartbeat(HeartbeatRequest request) {

        // 1. 参数校验
        if (request == null || request.getUserId() == null || request.getDeviceId() == null) {
            logger.warn("心跳参数错误");
            return HeartbeatResponse.error("参数错误");
        }

        try (Jedis jedis = jedisPool.getResource()) {

            String userId = request.getUserId();
            String deviceId = request.getDeviceId();

            // 2. 构建在线状态数据
            OnlineInfo info = new OnlineInfo(
                    request.getPlatform(),
                    request.getAppVersion(),
                    System.currentTimeMillis()
            );

            String key = ONLINE_KEY_PREFIX + userId + ":" + deviceId;
            String value = gson.toJson(info);

            // 3. 写入Redis，设置过期时间
            jedis.setex(key, HEARTBEAT_EXPIRE_SECONDS, value);

            // 4. 维护用户的设备列表
            String devicesKey = ONLINE_DEVICES_PREFIX + userId;
            jedis.sadd(devicesKey, deviceId);
            jedis.expire(devicesKey, HEARTBEAT_EXPIRE_SECONDS);

            logger.debug("心跳上报成功: userId={}, deviceId={}, platform={}",
                    userId, deviceId, request.getPlatform());

            // 5. 返回成功响应
            return HeartbeatResponse.success(30); // 建议30秒后再次上报

        } catch (Exception e) {
            logger.error("心跳上报失败: userId={}, deviceId={}",
                    request.getUserId(), request.getDeviceId(), e);
            return HeartbeatResponse.error("服务器错误");
        }
    }

    /**
     * 查询用户是否在线（任意设备）
     *
     * @param userId 用户ID
     * @return true-在线，false-离线
     */
    public boolean isUserOnline(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String devicesKey = ONLINE_DEVICES_PREFIX + userId;

            // 检查设备集合是否存在且不为空
            if (!jedis.exists(devicesKey)) {
                return false;
            }

            // 获取所有设备，逐一检查是否在线
            Set<String> devices = jedis.smembers(devicesKey);
            if (devices == null || devices.isEmpty()) {
                return false;
            }

            // 只要有一个设备在线，就认为用户在线
            for (String deviceId : devices) {
                String key = ONLINE_KEY_PREFIX + userId + ":" + deviceId;
                if (jedis.exists(key)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            logger.error("查询在线状态失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 查询指定设备是否在线
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return true-在线，false-离线
     */
    public boolean isDeviceOnline(String userId, String deviceId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = ONLINE_KEY_PREFIX + userId + ":" + deviceId;
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error("查询设备在线状态失败: userId={}, deviceId={}", userId, deviceId, e);
            return false;
        }
    }

    /**
     * 获取用户的在线信息
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 在线信息，离线返回null
     */
    public OnlineInfo getOnlineInfo(String userId, String deviceId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = ONLINE_KEY_PREFIX + userId + ":" + deviceId;
            String value = jedis.get(key);

            if (value != null) {
                return gson.fromJson(value, OnlineInfo.class);
            }
            return null;

        } catch (Exception e) {
            logger.error("获取在线信息失败: userId={}, deviceId={}", userId, deviceId, e);
            return null;
        }
    }

    /**
     * 用户主动下线
     *
     * 场景：用户点击"退出登录"时调用
     *
     * 接口路径：POST /api/offline
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    public void offline(String userId, String deviceId) {
        try (Jedis jedis = jedisPool.getResource()) {

            // 1. 删除在线状态
            String key = ONLINE_KEY_PREFIX + userId + ":" + deviceId;
            jedis.del(key);

            // 2. 从设备列表中移除
            String devicesKey = ONLINE_DEVICES_PREFIX + userId;
            jedis.srem(devicesKey, deviceId);

            // 3. 如果没有设备在线，删除设备列表
            if (jedis.scard(devicesKey) == 0) {
                jedis.del(devicesKey);
            }

            logger.info("用户下线: userId={}, deviceId={}", userId, deviceId);

        } catch (Exception e) {
            logger.error("用户下线失败: userId={}, deviceId={}", userId, deviceId, e);
        }
    }

    /**
     * 获取在线用户数量（用于监控）
     *
     * @return 在线用户数
     */
    public long getOnlineUserCount() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 统计所有在线设备前缀的key数量
            Set<String> keys = jedis.keys(ONLINE_DEVICES_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.error("获取在线用户数失败", e);
            return 0;
        }
    }

    // ==================== 数据对象 ====================

    /**
     * 心跳请求
     */
    public static class HeartbeatRequest {
        private String userId;
        private String deviceId;
        private String platform;      // android/ios/web
        private String appVersion;
        private Long timestamp;

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * 心跳响应
     */
    public static class HeartbeatResponse {
        private int code;
        private String message;
        private HeartbeatData data;

        public static HeartbeatResponse success(int nextHeartbeatSeconds) {
            HeartbeatResponse response = new HeartbeatResponse();
            response.code = 200;
            response.message = "success";
            response.data = new HeartbeatData(nextHeartbeatSeconds);
            return response;
        }

        public static HeartbeatResponse error(String message) {
            HeartbeatResponse response = new HeartbeatResponse();
            response.code = 500;
            response.message = message;
            return response;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public HeartbeatData getData() {
            return data;
        }

        static class HeartbeatData {
            private int nextHeartbeatTime;

            public HeartbeatData(int nextHeartbeatTime) {
                this.nextHeartbeatTime = nextHeartbeatTime;
            }

            public int getNextHeartbeatTime() {
                return nextHeartbeatTime;
            }
        }
    }

    /**
     * 在线信息
     */
    public static class OnlineInfo {
        private String platform;
        private String appVersion;
        private long lastHeartbeatTime;

        public OnlineInfo(String platform, String appVersion, long lastHeartbeatTime) {
            this.platform = platform;
            this.appVersion = appVersion;
            this.lastHeartbeatTime = lastHeartbeatTime;
        }

        public String getPlatform() {
            return platform;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public long getLastHeartbeatTime() {
            return lastHeartbeatTime;
        }
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) throws InterruptedException {
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        RestHeartbeatService heartbeatService = new RestHeartbeatService(jedisPool);

        // 模拟客户端心跳上报
        HeartbeatRequest request = new HeartbeatRequest();
        request.setUserId("user123");
        request.setDeviceId("device456");
        request.setPlatform("android");
        request.setAppVersion("1.0.0");
        request.setTimestamp(System.currentTimeMillis());

        // 第一次心跳
        HeartbeatResponse response = heartbeatService.reportHeartbeat(request);
        logger.info("心跳响应: code={}, message={}", response.getCode(), response.getMessage());

        // 查询在线状态
        boolean online = heartbeatService.isUserOnline("user123");
        logger.info("用户在线状态: {}", online); // true

        // 等待65秒（超过60秒过期时间）
        Thread.sleep(65000);

        // 再次查询
        online = heartbeatService.isUserOnline("user123");
        logger.info("用户在线状态（65秒后）: {}", online); // false

        jedisPool.close();
    }
}
