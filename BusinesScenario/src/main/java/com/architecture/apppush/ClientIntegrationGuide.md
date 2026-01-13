# 客户端集成指南

## Android客户端实现

### 1. 建立WebSocket连接

```kotlin
class WebSocketManager(private val context: Context) {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * 连接WebSocket
     */
    fun connect(userId: String, deviceId: String, token: String) {
        val request = Request.Builder()
            .url("wss://your-server.com/ws?userId=$userId&deviceId=$deviceId&token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "WebSocket连接成功")
                // 连接成功，启动心跳
                startHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.i(TAG, "收到消息: $text")
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket连接失败", t)
                // 自动重连
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket连接关闭: $reason")
                stopHeartbeat()
            }
        })
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        stopHeartbeat()
        webSocket?.close(1000, "主动断开")
        webSocket = null
    }

    /**
     * 发送消息
     */
    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    companion object {
        private const val TAG = "WebSocketManager"
    }
}
```

### 2. 心跳机制

```kotlin
class HeartbeatManager {

    private var heartbeatJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * 启动心跳（每30秒一次）
     */
    fun start(webSocket: WebSocket) {
        heartbeatJob = scope.launch {
            while (isActive) {
                // 发送心跳消息
                val heartbeat = JSONObject().apply {
                    put("type", "heartbeat")
                    put("timestamp", System.currentTimeMillis())
                }.toString()

                webSocket.send(heartbeat)
                Log.d(TAG, "发送心跳")

                // 等待30秒
                delay(30_000)
            }
        }
    }

    /**
     * 停止心跳
     */
    fun stop() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    companion object {
        private const val TAG = "HeartbeatManager"
    }
}
```

### 3. 生命周期管理

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webSocketManager = WebSocketManager(this)

        // 监听APP生命周期
        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onResume(owner: LifecycleOwner) {
                // APP进入前台，建立WebSocket连接
                connectWebSocket()
            }

            override fun onPause(owner: LifecycleOwner) {
                // APP进入后台
                // 方案1：保持连接（消耗电量，部分Android系统会杀进程）
                // 方案2：断开连接（推荐，依赖极光推送）
                disconnectWebSocket()
            }
        })
    }

    private fun connectWebSocket() {
        val userId = getUserId()
        val deviceId = getDeviceId()
        val token = getToken()

        webSocketManager.connect(userId, deviceId, token)
    }

    private fun disconnectWebSocket() {
        webSocketManager.disconnect()
    }

    private fun getDeviceId(): String {
        // 获取设备唯一ID
        return Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}
```

### 4. 自动重连

```kotlin
class ReconnectManager {

    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    /**
     * 指数退避重连
     */
    fun scheduleReconnect(connectAction: () -> Unit) {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG, "达到最大重连次数，停止重连")
            return
        }

        // 计算延迟时间：1s, 2s, 4s, 8s, 16s
        val delayMs = (1000L * (1 shl reconnectAttempts)).coerceAtMost(30_000)

        Log.i(TAG, "计划重连: 第${reconnectAttempts + 1}次, 延迟${delayMs}ms")

        Handler(Looper.getMainLooper()).postDelayed({
            reconnectAttempts++
            connectAction()
        }, delayMs)
    }

    /**
     * 重连成功，重置计数
     */
    fun resetReconnectAttempts() {
        reconnectAttempts = 0
    }

    companion object {
        private const val TAG = "ReconnectManager"
    }
}
```

### 5. 集成极光推送

```kotlin
class JPushManager(private val context: Context) {

    /**
     * 初始化极光推送
     */
    fun init() {
        JPushInterface.setDebugMode(BuildConfig.DEBUG)
        JPushInterface.init(context)
    }

    /**
     * 设置别名（用户ID）
     */
    fun setAlias(userId: String) {
        JPushInterface.setAlias(context, 0, userId)
    }

    /**
     * 接收推送消息
     */
    class JPushReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                JPushInterface.ACTION_NOTIFICATION_RECEIVED -> {
                    // 收到通知
                    val title = intent.getStringExtra(JPushInterface.EXTRA_NOTIFICATION_TITLE)
                    val content = intent.getStringExtra(JPushInterface.EXTRA_ALERT)
                    Log.i(TAG, "收到推送: title=$title, content=$content")
                }

                JPushInterface.ACTION_NOTIFICATION_OPENED -> {
                    // 用户点击通知
                    val extras = intent.getStringExtra(JPushInterface.EXTRA_EXTRA)
                    handleNotificationClick(extras)
                }
            }
        }

        private fun handleNotificationClick(extras: String?) {
            // 处理通知点击事件，如跳转到聊天页面
        }

        companion object {
            private const val TAG = "JPushReceiver"
        }
    }
}
```

### 6. 完整流程示例

```kotlin
class PushManager(private val context: Context) {

    private val webSocketManager = WebSocketManager(context)
    private val jpushManager = JPushManager(context)

    /**
     * 初始化
     */
    fun init(userId: String) {
        // 1. 初始化极光推送
        jpushManager.init()
        jpushManager.setAlias(userId)

        // 2. 监听网络状态
        observeNetworkState()

        // 3. 监听APP生命周期
        observeAppLifecycle()
    }

    /**
     * 监听APP生命周期
     */
    private fun observeAppLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                // APP进入前台
                Log.i(TAG, "APP进入前台，建立WebSocket连接")
                connectWebSocket()
            }

            override fun onStop(owner: LifecycleOwner) {
                // APP进入后台
                Log.i(TAG, "APP进入后台，断开WebSocket连接")
                disconnectWebSocket()
            }
        })
    }

    /**
     * 监听网络状态
     */
    private fun observeNetworkState() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // 网络可用，重连WebSocket
                Log.i(TAG, "网络恢复，重连WebSocket")
                connectWebSocket()
            }

            override fun onLost(network: Network) {
                // 网络断开
                Log.i(TAG, "网络断开")
            }
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun connectWebSocket() {
        // 建立WebSocket连接
    }

    private fun disconnectWebSocket() {
        // 断开WebSocket连接
    }

    companion object {
        private const val TAG = "PushManager"
    }
}
```

## iOS客户端实现

### 1. 建立WebSocket连接

```swift
import Starscream

class WebSocketManager: WebSocketDelegate {

    private var webSocket: WebSocket?
    private var heartbeatTimer: Timer?

    /// 连接WebSocket
    func connect(userId: String, deviceId: String, token: String) {
        let url = URL(string: "wss://your-server.com/ws?userId=\(userId)&deviceId=\(deviceId)&token=\(token)")!

        var request = URLRequest(url: url)
        request.timeoutInterval = 10

        webSocket = WebSocket(request: request)
        webSocket?.delegate = self
        webSocket?.connect()
    }

    /// 连接成功
    func didReceive(event: WebSocketEvent, client: WebSocket) {
        switch event {
        case .connected(_):
            print("WebSocket连接成功")
            startHeartbeat()

        case .disconnected(let reason, let code):
            print("WebSocket断开: \(reason), code: \(code)")
            stopHeartbeat()
            scheduleReconnect()

        case .text(let text):
            print("收到消息: \(text)")
            handleMessage(text)

        case .error(let error):
            print("WebSocket错误: \(error?.localizedDescription ?? "")")

        default:
            break
        }
    }

    /// 发送消息
    func sendMessage(_ message: String) {
        webSocket?.write(string: message)
    }

    /// 断开连接
    func disconnect() {
        stopHeartbeat()
        webSocket?.disconnect()
    }
}
```

### 2. 心跳机制

```swift
extension WebSocketManager {

    /// 启动心跳（每30秒）
    func startHeartbeat() {
        heartbeatTimer = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { [weak self] _ in
            let heartbeat = """
            {
                "type": "heartbeat",
                "timestamp": \(Date().timeIntervalSince1970 * 1000)
            }
            """
            self?.sendMessage(heartbeat)
            print("发送心跳")
        }
    }

    /// 停止心跳
    func stopHeartbeat() {
        heartbeatTimer?.invalidate()
        heartbeatTimer = nil
    }
}
```

### 3. APP生命周期管理

```swift
class AppDelegate: UIResponder, UIApplicationDelegate {

    var webSocketManager: WebSocketManager?

    func application(_ application: UIApplication,
                    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        webSocketManager = WebSocketManager()

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
        print("APP进入前台，建立WebSocket连接")
        connectWebSocket()
    }

    /// APP进入后台
    @objc func appDidEnterBackground() {
        print("APP进入后台，断开WebSocket连接")
        webSocketManager?.disconnect()
    }

    func connectWebSocket() {
        let userId = getUserId()
        let deviceId = getDeviceId()
        let token = getToken()

        webSocketManager?.connect(userId: userId, deviceId: deviceId, token: token)
    }
}
```

### 4. APNs推送集成

```swift
import UserNotifications

class APNsManager: NSObject, UNUserNotificationCenterDelegate {

    /// 注册APNs
    func registerAPNs() {
        UNUserNotificationCenter.current().delegate = self

        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { granted, error in
            if granted {
                print("推送权限已授予")
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
        }
    }

    /// 收到DeviceToken
    func application(_ application: UIApplication,
                    didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        print("DeviceToken: \(token)")

        // 上传到服务器
        uploadDeviceToken(token)
    }

    /// 前台收到推送
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                               willPresent notification: UNNotification,
                               withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {

        let userInfo = notification.request.content.userInfo
        print("前台收到推送: \(userInfo)")

        // 前台也显示通知
        completionHandler([.alert, .sound, .badge])
    }

    /// 点击推送
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                               didReceive response: UNNotificationResponse,
                               withCompletionHandler completionHandler: @escaping () -> Void) {

        let userInfo = response.notification.request.content.userInfo
        print("点击推送: \(userInfo)")

        // 处理跳转逻辑
        handleNotificationClick(userInfo)

        completionHandler()
    }

    func handleNotificationClick(_ userInfo: [AnyHashable: Any]) {
        // 跳转到聊天页面等
    }
}
```

## 前端（Web）客户端实现

### 1. WebSocket连接

```javascript
class WebSocketManager {
    constructor(userId, deviceId, token) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.token = token;
        this.ws = null;
        this.heartbeatTimer = null;
        this.reconnectAttempts = 0;
    }

    /**
     * 连接WebSocket
     */
    connect() {
        const url = `wss://your-server.com/ws?userId=${this.userId}&deviceId=${this.deviceId}&token=${this.token}`;

        this.ws = new WebSocket(url);

        this.ws.onopen = () => {
            console.log('WebSocket连接成功');
            this.reconnectAttempts = 0;
            this.startHeartbeat();
        };

        this.ws.onmessage = (event) => {
            console.log('收到消息:', event.data);
            this.handleMessage(JSON.parse(event.data));
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket错误:', error);
        };

        this.ws.onclose = () => {
            console.log('WebSocket连接关闭');
            this.stopHeartbeat();
            this.scheduleReconnect();
        };
    }

    /**
     * 发送消息
     */
    sendMessage(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        }
    }

    /**
     * 启动心跳
     */
    startHeartbeat() {
        this.heartbeatTimer = setInterval(() => {
            this.sendMessage({
                type: 'heartbeat',
                timestamp: Date.now()
            });
            console.log('发送心跳');
        }, 30000); // 30秒
    }

    /**
     * 停止心跳
     */
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }

    /**
     * 自动重连
     */
    scheduleReconnect() {
        if (this.reconnectAttempts >= 5) {
            console.warn('达到最大重连次数');
            return;
        }

        const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
        console.log(`计划重连: 第${this.reconnectAttempts + 1}次, 延迟${delay}ms`);

        setTimeout(() => {
            this.reconnectAttempts++;
            this.connect();
        }, delay);
    }

    /**
     * 断开连接
     */
    disconnect() {
        this.stopHeartbeat();
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }

    /**
     * 处理消息
     */
    handleMessage(message) {
        switch (message.type) {
            case 'heartbeat_ack':
                // 心跳响应
                break;
            case 'chat':
                // 聊天消息
                this.onChatMessage(message);
                break;
            default:
                console.log('未知消息类型:', message.type);
        }
    }

    onChatMessage(message) {
        // 显示聊天消息
        console.log('收到聊天消息:', message);
    }
}

// 使用示例
const wsManager = new WebSocketManager('user123', 'device456', 'your-token');
wsManager.connect();
```

### 2. 页面可见性监听

```javascript
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        console.log('页面隐藏，断开WebSocket');
        wsManager.disconnect();
    } else {
        console.log('页面可见，重连WebSocket');
        wsManager.connect();
    }
});
```

## 总结

### 客户端核心要点

1. **生命周期管理**：APP前台建立连接，后台断开连接
2. **心跳保活**：每30秒发送一次心跳，服务端60秒超时
3. **自动重连**：网络恢复或APP恢复时自动重连，使用指数退避策略
4. **双通道推送**：在线时WebSocket，离线时第三方推送
5. **设备ID**：确保设备ID全局唯一且稳定

### 最佳实践

- Android：前台保持WebSocket，后台依赖极光推送
- iOS：前台保持WebSocket，后台使用APNs推送
- Web：页面可见时保持WebSocket，隐藏时断开

这样可以在保证实时性的同时，最大程度节省电量和流量。
