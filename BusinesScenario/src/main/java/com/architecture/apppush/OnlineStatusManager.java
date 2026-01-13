package com.architecture.apppush;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * 在线状态管理服务
 *
 * 核心功能：
 * 1. 用户上线/下线状态管理
 * 2. 心跳更新
 * 3. 在线状态查询
 * 4. 多端登录管理
 */
public class OnlineStatusManager {

    private static final Logger logger = LoggerFactory.getLogger(OnlineStatusManager.class);

    private static final String ONLINE_USER_PREFIX = "online:user:";
    private static final String ONLINE_SERVER_PREFIX = "online:server:";
    private static final String ONLINE_DEVICES_PREFIX = "online:user:devices:";

    // 心跳超时时间（秒）
    private static final int HEARTBEAT_TIMEOUT = 60;

    private final JedisPool jedisPool;
    private final Gson gson = new Gson();

    public OnlineStatusManager(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 用户上线
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param serverId 连接的服务器ID
     * @param platform 平台（android/ios/web）
     */
    public void online(String userId, String deviceId, String serverId, String platform) {
        try (Jedis jedis = jedisPool.getResource()) {

            // 构建在线状态对象
            OnlineStatus status = new OnlineStatus(
                    serverId,
                    System.currentTimeMillis(),
                    deviceId,
                    platform,
                    "1.0.0"
            );

            String key = ONLINE_USER_PREFIX + userId + ":" + deviceId;
            String value = gson.toJson(status);

            // 1. 写入用户在线状态（带过期时间）
            jedis.setex(key, HEARTBEAT_TIMEOUT, value);

            // 2. 添加到服务器维度的在线用户集合
            jedis.sadd(ONLINE_SERVER_PREFIX + serverId, userId + ":" + deviceId);

            // 3. 添加到用户的多端设备集合
            jedis.sadd(ONLINE_DEVICES_PREFIX + userId, deviceId);

            logger.info("用户上线: userId={}, deviceId={}, serverId={}, platform={}",
                    userId, deviceId, serverId, platform);
        } catch (Exception e) {
            logger.error("用户上线失败: userId={}, deviceId={}", userId, deviceId, e);
            throw new RuntimeException("用户上线失败", e);
        }
    }

    /**
     * 用户下线
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param serverId 服务器ID
     */
    public void offline(String userId, String deviceId, String serverId) {
        try (Jedis jedis = jedisPool.getResource()) {

            String key = ONLINE_USER_PREFIX + userId + ":" + deviceId;

            // 1. 删除在线状态
            jedis.del(key);

            // 2. 从服务器集合中移除
            jedis.srem(ONLINE_SERVER_PREFIX + serverId, userId + ":" + deviceId);

            // 3. 从用户设备集合中移除
            jedis.srem(ONLINE_DEVICES_PREFIX + userId, deviceId);

            // 4. 如果用户没有任何在线设备，删除设备集合
            if (jedis.scard(ONLINE_DEVICES_PREFIX + userId) == 0) {
                jedis.del(ONLINE_DEVICES_PREFIX + userId);
            }

            logger.info("用户下线: userId={}, deviceId={}", userId, deviceId);
        } catch (Exception e) {
            logger.error("用户下线失败: userId={}, deviceId={}", userId, deviceId, e);
        }
    }

    /**
     * 心跳更新
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    public void heartbeat(String userId, String deviceId) {
        try (Jedis jedis = jedisPool.getResource()) {

            String key = ONLINE_USER_PREFIX + userId + ":" + deviceId;

            // 检查key是否存在
            if (jedis.exists(key)) {
                // 重置过期时间
                jedis.expire(key, HEARTBEAT_TIMEOUT);
                logger.debug("心跳更新: userId={}, deviceId={}", userId, deviceId);
            } else {
                logger.warn("心跳更新失败，用户不在线: userId={}, deviceId={}", userId, deviceId);
            }

        } catch (Exception e) {
            logger.error("心跳更新失败: userId={}, deviceId={}", userId, deviceId, e);
        }
    }

    /**
     * 判断用户是否在线（任意设备）
     *
     * @param userId 用户ID
     * @return true-在线，false-离线
     */
    public boolean isUserOnline(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 查询用户的设备集合
            Set<String> devices = jedis.smembers(ONLINE_DEVICES_PREFIX + userId);
            return devices != null && !devices.isEmpty();
        } catch (Exception e) {
            logger.error("查询用户在线状态失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 判断指定设备是否在线
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return true-在线，false-离线
     */
    public boolean isDeviceOnline(String userId, String deviceId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = ONLINE_USER_PREFIX + userId + ":" + deviceId;
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error("查询设备在线状态失败: userId={}, deviceId={}", userId, deviceId, e);
            return false;
        }
    }

    /**
     * 获取用户的所有在线设备
     *
     * @param userId 用户ID
     * @return 设备ID列表
     */
    public Set<String> getOnlineDevices(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(ONLINE_DEVICES_PREFIX + userId);
        } catch (Exception e) {
            logger.error("获取在线设备失败: userId={}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 获取用户在指定设备上的在线状态信息
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 在线状态对象，离线返回null
     */
    public OnlineStatus getOnlineStatus(String userId, String deviceId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = ONLINE_USER_PREFIX + userId + ":" + deviceId;
            String value = jedis.get(key);

            if (value != null) {
                return gson.fromJson(value, OnlineStatus.class);
            }
            return null;

        } catch (Exception e) {
            logger.error("获取在线状态失败: userId={}, deviceId={}", userId, deviceId, e);
            return null;
        }
    }

    /**
     * 批量查询用户在线状态
     *
     * @param userIds 用户ID列表
     * @return userId -> 是否在线
     */
    public Map<String, Boolean> batchCheckOnline(List<String> userIds) {
        Map<String, Boolean> result = new HashMap<>();

        try (Jedis jedis = jedisPool.getResource()) {
            // 使用Pipeline批量查询
            for (String userId : userIds) {
                Set<String> devices = jedis.smembers(ONLINE_DEVICES_PREFIX + userId);
                result.put(userId, devices != null && !devices.isEmpty());
            }
        } catch (Exception e) {
            logger.error("批量查询在线状态失败", e);
        }

        return result;
    }

    /**
     * 获取服务器上的所有在线用户
     *
     * @param serverId 服务器ID
     * @return 用户ID:设备ID集合
     */
    public Set<String> getServerOnlineUsers(String serverId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(ONLINE_SERVER_PREFIX + serverId);
        } catch (Exception e) {
            logger.error("获取服务器在线用户失败: serverId={}", serverId, e);
            return new HashSet<>();
        }
    }

    /**
     * 服务器下线，批量处理在线用户
     *
     * @param serverId 服务器ID
     */
    public void serverOffline(String serverId) {
        try (Jedis jedis = jedisPool.getResource()) {

            // 获取该服务器上的所有在线用户
            Set<String> users = jedis.smembers(ONLINE_SERVER_PREFIX + serverId);

            logger.info("服务器下线，处理在线用户: serverId={}, userCount={}", serverId, users.size());

            // 批量下线
            for (String userDevice : users) {
                String[] parts = userDevice.split(":");
                if (parts.length == 2) {
                    offline(parts[0], parts[1], serverId);
                }
            }

            // 删除服务器集合
            jedis.del(ONLINE_SERVER_PREFIX + serverId);

        } catch (Exception e) {
            logger.error("服务器下线处理失败: serverId={}", serverId, e);
        }
    }

    /**
     * 踢出指定设备（互踢场景）
     *
     * @param userId 用户ID
     * @param deviceId 要踢出的设备ID
     * @return 踢出成功返回该设备连接的服务器ID，否则返回null
     */
    public String kickoutDevice(String userId, String deviceId) {
        OnlineStatus status = getOnlineStatus(userId, deviceId);

        if (status != null) {
            offline(userId, deviceId, status.getServerId());
            logger.info("踢出设备: userId={}, deviceId={}", userId, deviceId);
            return status.getServerId();
        }

        return null;
    }

    /**
     * 在线状态对象
     */
    public static class OnlineStatus {
        private String serverId;        // 连接的服务器ID
        private long connectTime;       // 连接时间戳
        private String deviceId;        // 设备ID
        private String platform;        // 平台
        private String appVersion;      // APP版本

        public OnlineStatus(String serverId, long connectTime, String deviceId,
                           String platform, String appVersion) {
            this.serverId = serverId;
            this.connectTime = connectTime;
            this.deviceId = deviceId;
            this.platform = platform;
            this.appVersion = appVersion;
        }

        public String getServerId() {
            return serverId;
        }

        public long getConnectTime() {
            return connectTime;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getPlatform() {
            return platform;
        }

        public String getAppVersion() {
            return appVersion;
        }
    }
}
