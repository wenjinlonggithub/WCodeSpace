# REST心跳接口客户端集成指南

## 方案说明

**核心思路**：客户端定时调用REST API上报心跳，服务端将在线状态写入Redis（带TTL过期），推送时查询Redis判断在线/离线。

**推送方式**：
- 在线：不推送（或通过极光推送静默消息）
- 离线：极光推送系统通知

## 接口定义

### 1. 心跳上报接口

**接口路径**：`POST /api/heartbeat`

**请求示例**：
```json
{
  "userId": "user123",
  "deviceId": "device456",
  "platform": "android",
  "appVersion": "1.0.0",
  "timestamp": 1704038400000
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "nextHeartbeatTime": 30
  }
}
```

### 2. 下线接口

**接口路径**：`POST /api/offline`

**请求示例**：
```json
{
  "userId": "user123",
  "deviceId": "device456"
}
```

## Android客户端实现

### 1. 定义API接口

```kotlin
import retrofit2.http.Body
import retrofit2.http.POST

interface HeartbeatApi {

    @POST("api/heartbeat")
    suspend fun reportHeartbeat(@Body request: HeartbeatRequest): HeartbeatResponse

    @POST("api/offline")
    suspend fun offline(@Body request: OfflineRequest): BaseResponse
}

data class HeartbeatRequest(
    val userId: String,
    val deviceId: String,
    val platform: String,
    val appVersion: String,
    val timestamp: Long
)

data class HeartbeatResponse(
    val code: Int,
    val message: String,
    val data: HeartbeatData?
)

data class HeartbeatData(
    val nextHeartbeatTime: Int  // 下次心跳间隔（秒）
)

data class OfflineRequest(
    val userId: String,
    val deviceId: String
)

data class BaseResponse(
    val code: Int,
    val message: String
)
```

### 2. 心跳管理器

```kotlin
import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HeartbeatManager(private val context: Context) {

    private val api: HeartbeatApi = createRetrofitApi()
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * 启动心跳
     * 使用WorkManager实现后台定时任务
     */
    fun startHeartbeat() {
        val heartbeatRequest = PeriodicWorkRequestBuilder<HeartbeatWorker>(
            30, TimeUnit.SECONDS,  // 每30秒执行一次
            15, TimeUnit.SECONDS   // 弹性时间窗口
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)  // 需要网络
                .build()
        ).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "heartbeat",
                ExistingPeriodicWorkPolicy.KEEP,  // 已存在则保持
                heartbeatRequest
            )

        Log.i(TAG, "心跳任务已启动")
    }

    /**
     * 停止心跳
     */
    fun stopHeartbeat() {
        WorkManager.getInstance(context).cancelUniqueWork("heartbeat")

        // 调用下线接口
        scope.launch {
            try {
                val request = OfflineRequest(
                    userId = getUserId(),
                    deviceId = getDeviceId()
                )
                api.offline(request)
                Log.i(TAG, "下线成功")
            } catch (e: Exception) {
                Log.e(TAG, "下线失败", e)
            }
        }
    }

    /**
     * 心跳Worker
     */
    class HeartbeatWorker(
        context: Context,
        params: WorkerParameters
    ) : CoroutineWorker(context, params) {

        override suspend fun doWork(): Result {
            return try {
                val api = createRetrofitApi()

                val request = HeartbeatRequest(
                    userId = getUserId(),
                    deviceId = getDeviceId(),
                    platform = "android",
                    appVersion = getAppVersion(),
                    timestamp = System.currentTimeMillis()
                )

                val response = api.reportHeartbeat(request)

                if (response.code == 200) {
                    Log.d(TAG, "心跳上报成功")
                    Result.success()
                } else {
                    Log.w(TAG, "心跳上报失败: ${response.message}")
                    Result.retry()
                }

            } catch (e: Exception) {
                Log.e(TAG, "心跳上报异常", e)
                Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "HeartbeatManager"
    }
}
```

### 3. 生命周期集成

```kotlin
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class Application : android.app.Application() {

    private lateinit var heartbeatManager: HeartbeatManager

    override fun onCreate() {
        super.onCreate()

        heartbeatManager = HeartbeatManager(this)

        // 监听APP生命周期
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                // APP进入前台，启动心跳
                Log.i(TAG, "APP进入前台，启动心跳")
                heartbeatManager.startHeartbeat()
            }

            override fun onStop(owner: LifecycleOwner) {
                // APP进入后台，停止心跳
                Log.i(TAG, "APP进入后台，停止心跳")
                heartbeatManager.stopHeartbeat()
            }
        })
    }

    companion object {
        private const val TAG = "Application"
    }
}
```

### 4. 方案优化：前台使用AlarmManager

**问题**：WorkManager在某些国产ROM上会被系统杀掉

**解决**：前台使用AlarmManager，后台依赖极光推送

```kotlin
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class HeartbeatManagerV2(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 启动心跳（使用AlarmManager）
     */
    fun startHeartbeat() {
        val intent = Intent(context, HeartbeatReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 使用setRepeating确保准时执行
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            30 * 1000L,  // 30秒间隔
            pendingIntent
        )

        Log.i(TAG, "AlarmManager心跳已启动")
    }

    /**
     * 停止心跳
     */
    fun stopHeartbeat() {
        val intent = Intent(context, HeartbeatReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        Log.i(TAG, "AlarmManager心跳已停止")
    }

    /**
     * 心跳广播接收器
     */
    class HeartbeatReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // 在协程中执行网络请求
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = createRetrofitApi()
                    val request = HeartbeatRequest(
                        userId = getUserId(),
                        deviceId = getDeviceId(),
                        platform = "android",
                        appVersion = getAppVersion(),
                        timestamp = System.currentTimeMillis()
                    )

                    val response = api.reportHeartbeat(request)
                    Log.d(TAG, "心跳上报: code=${response.code}")

                } catch (e: Exception) {
                    Log.e(TAG, "心跳上报失败", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "HeartbeatManagerV2"
    }
}
```

## iOS客户端实现

### 1. 定义API接口

```swift
import Foundation

struct HeartbeatRequest: Codable {
    let userId: String
    let deviceId: String
    let platform: String
    let appVersion: String
    let timestamp: Int64
}

struct HeartbeatResponse: Codable {
    let code: Int
    let message: String
    let data: HeartbeatData?
}

struct HeartbeatData: Codable {
    let nextHeartbeatTime: Int
}

class HeartbeatAPI {

    static let shared = HeartbeatAPI()

    private let baseURL = "https://your-server.com"

    /// 上报心跳
    func reportHeartbeat(request: HeartbeatRequest, completion: @escaping (Result<HeartbeatResponse, Error>) -> Void) {

        guard let url = URL(string: "\(baseURL)/api/heartbeat") else {
            completion(.failure(NSError(domain: "Invalid URL", code: -1)))
            return
        }

        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = "POST"
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")

        do {
            let jsonData = try JSONEncoder().encode(request)
            urlRequest.httpBody = jsonData

            URLSession.shared.dataTask(with: urlRequest) { data, response, error in
                if let error = error {
                    completion(.failure(error))
                    return
                }

                guard let data = data else {
                    completion(.failure(NSError(domain: "No data", code: -1)))
                    return
                }

                do {
                    let response = try JSONDecoder().decode(HeartbeatResponse.self, from: data)
                    completion(.success(response))
                } catch {
                    completion(.failure(error))
                }
            }.resume()

        } catch {
            completion(.failure(error))
        }
    }

    /// 下线
    func offline(userId: String, deviceId: String, completion: @escaping (Bool) -> Void) {
        guard let url = URL(string: "\(baseURL)/api/offline") else {
            completion(false)
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let params = ["userId": userId, "deviceId": deviceId]
        request.httpBody = try? JSONSerialization.data(withJSONObject: params)

        URLSession.shared.dataTask(with: request) { _, _, error in
            completion(error == nil)
        }.resume()
    }
}
```

### 2. 心跳管理器

```swift
import UIKit

class HeartbeatManager {

    static let shared = HeartbeatManager()

    private var timer: Timer?

    /// 启动心跳
    func startHeartbeat() {
        // 立即执行一次
        reportHeartbeat()

        // 定时执行（30秒）
        timer = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { [weak self] _ in
            self?.reportHeartbeat()
        }

        // 确保Timer在主线程运行
        RunLoop.main.add(timer!, forMode: .common)

        print("心跳已启动")
    }

    /// 停止心跳
    func stopHeartbeat() {
        timer?.invalidate()
        timer = nil

        // 调用下线接口
        let userId = getUserId()
        let deviceId = getDeviceId()

        HeartbeatAPI.shared.offline(userId: userId, deviceId: deviceId) { success in
            print("下线\(success ? "成功" : "失败")")
        }

        print("心跳已停止")
    }

    /// 上报心跳
    private func reportHeartbeat() {
        let request = HeartbeatRequest(
            userId: getUserId(),
            deviceId: getDeviceId(),
            platform: "ios",
            appVersion: getAppVersion(),
            timestamp: Int64(Date().timeIntervalSince1970 * 1000)
        )

        HeartbeatAPI.shared.reportHeartbeat(request: request) { result in
            switch result {
            case .success(let response):
                print("心跳上报成功: code=\(response.code)")
            case .failure(let error):
                print("心跳上报失败: \(error.localizedDescription)")
            }
        }
    }
}
```

### 3. 生命周期集成

```swift
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(_ application: UIApplication,
                    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        // 监听APP生命周期
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(appDidBecomeActive),
            name: UIApplication.didBecomeActiveNotification,
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(appDidEnterBackground),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil
        )

        return true
    }

    /// APP进入前台
    @objc func appDidBecomeActive() {
        print("APP进入前台，启动心跳")
        HeartbeatManager.shared.startHeartbeat()
    }

    /// APP进入后台
    @objc func appDidEnterBackground() {
        print("APP进入后台，停止心跳")
        HeartbeatManager.shared.stopHeartbeat()
    }
}
```

## Web客户端实现

### 1. 心跳管理

```javascript
class HeartbeatManager {
    constructor(userId, deviceId) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.timer = null;
        this.heartbeatInterval = 30000; // 30秒
    }

    /**
     * 启动心跳
     */
    startHeartbeat() {
        // 立即执行一次
        this.reportHeartbeat();

        // 定时执行
        this.timer = setInterval(() => {
            this.reportHeartbeat();
        }, this.heartbeatInterval);

        console.log('心跳已启动');
    }

    /**
     * 停止心跳
     */
    stopHeartbeat() {
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }

        // 调用下线接口
        this.offline();

        console.log('心跳已停止');
    }

    /**
     * 上报心跳
     */
    async reportHeartbeat() {
        try {
            const request = {
                userId: this.userId,
                deviceId: this.deviceId,
                platform: 'web',
                appVersion: '1.0.0',
                timestamp: Date.now()
            };

            const response = await fetch('/api/heartbeat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(request)
            });

            const data = await response.json();
            console.log('心跳上报成功:', data);

        } catch (error) {
            console.error('心跳上报失败:', error);
        }
    }

    /**
     * 下线
     */
    async offline() {
        try {
            await fetch('/api/offline', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    userId: this.userId,
                    deviceId: this.deviceId
                })
            });
            console.log('下线成功');
        } catch (error) {
            console.error('下线失败:', error);
        }
    }
}

// 使用示例
const heartbeatManager = new HeartbeatManager('user123', 'device456');

// 页面可见性监听
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        console.log('页面隐藏，停止心跳');
        heartbeatManager.stopHeartbeat();
    } else {
        console.log('页面可见，启动心跳');
        heartbeatManager.startHeartbeat();
    }
});

// 页面卸载时下线
window.addEventListener('beforeunload', () => {
    // 使用sendBeacon确保请求发送（即使页面关闭）
    const data = JSON.stringify({
        userId: heartbeatManager.userId,
        deviceId: heartbeatManager.deviceId
    });
    navigator.sendBeacon('/api/offline', data);
});
```

## Spring Boot后端控制器示例

```java
@RestController
@RequestMapping("/api")
public class HeartbeatController {

    @Autowired
    private RestHeartbeatService heartbeatService;

    /**
     * 心跳上报接口
     */
    @PostMapping("/heartbeat")
    public RestHeartbeatService.HeartbeatResponse heartbeat(
            @RequestBody RestHeartbeatService.HeartbeatRequest request) {

        return heartbeatService.reportHeartbeat(request);
    }

    /**
     * 下线接口
     */
    @PostMapping("/offline")
    public Map<String, Object> offline(@RequestBody OfflineRequest request) {
        heartbeatService.offline(request.getUserId(), request.getDeviceId());

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        return response;
    }

    /**
     * 查询在线状态接口（测试用）
     */
    @GetMapping("/online/check")
    public Map<String, Object> checkOnline(@RequestParam String userId) {
        boolean online = heartbeatService.isUserOnline(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", Map.of("online", online));
        return response;
    }

    static class OfflineRequest {
        private String userId;
        private String deviceId;

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    }
}
```

## 方案对比总结

| 特性 | REST心跳 | WebSocket |
|------|---------|-----------|
| 实现复杂度 | 低 | 中 |
| 实时性 | 中（30秒延迟） | 高（秒级） |
| 流量消耗 | 中 | 低 |
| 电量消耗 | 中 | 低 |
| 服务端推送 | 不支持 | 支持 |
| 技术要求 | HTTP即可 | 需要WebSocket |
| 适用场景 | 只需判断在线状态 | 需要实时双向通信 |

## 推荐方案

- **纯推送场景**：REST心跳 + 极光推送（简单高效）
- **IM聊天场景**：WebSocket + 极光推送（实时性高）
- **混合场景**：前台WebSocket，后台REST心跳
