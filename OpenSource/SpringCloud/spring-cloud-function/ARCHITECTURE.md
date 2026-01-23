# Spring Cloud Function 架构图

## 目录

1. [整体架构图](#1-整体架构图)
2. [分层架构](#2-分层架构)
3. [核心组件架构](#3-核心组件架构)
4. [请求处理流程架构](#4-请求处理流程架构)
5. [函数生命周期架构](#5-函数生命周期架构)
6. [部署架构](#6-部署架构)

---

## 1. 整体架构图

### 1.1 宏观架构视图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Spring Cloud Function                            │
│                         整体架构视图                                      │
└─────────────────────────────────────────────────────────────────────────┘

                              ┌─────────────┐
                              │   客户端     │
                              │  (HTTP/MQ)  │
                              └──────┬──────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          适配器层 (Adapter Layer)                       │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │  Web Adapter │  │   Messaging  │  │     AWS      │  │   Azure   │ │
│  │   (HTTP)     │  │   Adapter    │  │   Lambda     │  │ Functions │ │
│  │              │  │ (Kafka/RMQ)  │  │   Adapter    │  │  Adapter  │ │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘ │
│         │                 │                 │                │        │
└─────────┼─────────────────┼─────────────────┼────────────────┼────────┘
          │                 │                 │                │
          └─────────────────┴─────────────────┴────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        执行引擎层 (Execution Layer)                     │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │                    FunctionInvoker (执行引擎)                     │ │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌───────────┐  │ │
│  │  │ 函数查找   │  │ 类型转换   │  │ 函数执行   │  │ 异常处理  │  │ │
│  │  └────────────┘  └────────────┘  └────────────┘  └───────────┘  │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        函数目录层 (Catalog Layer)                       │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │              FunctionCatalog (函数目录)                           │ │
│  │                                                                   │ │
│  │  ┌─────────────────────────────────────────────────────────┐    │ │
│  │  │  Map<String, Object> functions                          │    │ │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │    │ │
│  │  │  │uppercase │  │ reverse  │  │calculate │  │  log    │ │    │ │
│  │  │  │Function  │  │ Function │  │ Function │  │Consumer │ │    │ │
│  │  │  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │    │ │
│  │  └─────────────────────────────────────────────────────────┘    │ │
│  │                                                                   │ │
│  │  功能：                                                           │ │
│  │  • 存储所有注册的函数                                             │ │
│  │  • 提供函数查找能力                                               │ │
│  │  • 支持函数组合                                                   │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        业务函数层 (Function Layer)                      │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                │
│  │   Function   │  │   Supplier   │  │   Consumer   │                │
│  │   <I, O>     │  │     <O>      │  │     <I>      │                │
│  ├──────────────┤  ├──────────────┤  ├──────────────┤                │
│  │ 接收输入     │  │ 无输入       │  │ 接收输入     │                │
│  │ 返回输出     │  │ 产生输出     │  │ 无输出       │                │
│  └──────────────┘  └──────────────┘  └──────────────┘                │
│                                                                         │
│  示例：                                                                 │
│  • uppercase: String → String                                          │
│  • calculateOrder: Order → OrderResult                                 │
│  • timestamp: () → Long                                                │
│  • log: String → void                                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 分层架构

### 2.1 垂直分层视图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Layer 1: 接入层                               │
│                        (Access Layer)                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  职责：接收外部请求，适配不同的调用方式                              │
│                                                                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │
│  │    HTTP    │  │  Message   │  │   Event    │  │    RPC     │   │
│  │  Endpoint  │  │   Queue    │  │   Stream   │  │   Call     │   │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘   │
│                                                                      │
└──────────────────────────────┬───────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Layer 2: 适配层                               │
│                        (Adapter Layer)                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  职责：协议转换、参数解析、响应构建                                  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  FunctionController (Web Adapter)                            │  │
│  │  • parseRequestBody()     - 解析请求体                       │  │
│  │  • invokeFunction()       - 调用函数                         │  │
│  │  • buildHttpResponse()    - 构建响应                         │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
└──────────────────────────────┬───────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Layer 3: 编排层                               │
│                        (Orchestration Layer)                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  职责：函数查找、类型转换、执行协调、结果封装                        │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  FunctionInvoker (Execution Engine)                          │  │
│  │  • lookup()         - 查找函数                               │  │
│  │  • convertInput()   - 输入类型转换                           │  │
│  │  • execute()        - 执行函数                               │  │
│  │  • handleError()    - 异常处理                               │  │
│  │  • wrapResult()     - 结果封装                               │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
└──────────────────────────────┬───────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Layer 4: 目录层                               │
│                        (Registry Layer)                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  职责：函数注册、存储、管理、组合                                    │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  SimpleFunctionCatalog (Function Registry)                   │  │
│  │  • registerFunction()   - 注册函数                           │  │
│  │  • lookup()             - 查找函数                           │  │
│  │  • composeFunctions()   - 函数组合                           │  │
│  │  • getFunctionNames()   - 获取函数列表                       │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  FunctionRegistrar (Auto Registration)                       │  │
│  │  • postProcessAfterInitialization() - 自动注册                │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                      │
└──────────────────────────────┬───────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Layer 5: 业务层                               │
│                        (Business Layer)                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  职责：实现具体的业务逻辑                                            │
│                                                                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │
│  │ uppercase  │  │ calculate  │  │  validate  │  │    log     │   │
│  │  Function  │  │  Function  │  │  Function  │  │  Consumer  │   │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 数据流视图

```
┌──────────┐
│  Client  │  发送请求: POST /uppercase {"input": "hello"}
└────┬─────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  Layer 1: Access Layer                                      │
│  HTTP Request: POST /uppercase                              │
│  Body: "hello"                                              │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  Layer 2: Adapter Layer (FunctionController)                │
│  • 提取 functionName = "uppercase"                          │
│  • 提取 requestBody = "hello"                               │
│  • 解析为 Object input = "hello"                            │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  Layer 3: Orchestration Layer (FunctionInvoker)             │
│  invoke("uppercase", "hello", String.class, String.class)   │
│                                                              │
│  步骤1: lookup("uppercase")                                 │
│         ↓                                                    │
│  步骤2: convertInput("hello", String.class)                 │
│         ↓                                                    │
│  步骤3: function.apply("hello")                             │
│         ↓                                                    │
│  步骤4: wrapResult("HELLO", executionTime)                  │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  Layer 4: Registry Layer (FunctionCatalog)                  │
│  • 从 Map 中获取函数实例                                     │
│  • functions.get("uppercase") → Function<String, String>    │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  Layer 5: Business Layer                                    │
│  • 执行业务逻辑: value.toUpperCase()                        │
│  • 返回结果: "HELLO"                                        │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│  返回路径 (向上传递)                                         │
│                                                              │
│  Layer 5 → Layer 4: "HELLO"                                 │
│  Layer 4 → Layer 3: "HELLO"                                 │
│  Layer 3 → Layer 2: InvocationResult("HELLO", 2ms)          │
│  Layer 2 → Layer 1: ResponseEntity.ok().body("HELLO")       │
│  Layer 1 → Client:  HTTP 200 OK, Body: "HELLO"              │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 核心组件架构

### 3.1 组件关系图

```
┌───────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application Context                 │
├───────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │           FunctionCoreConfiguration                         │ │
│  │           (核心配置类)                                       │ │
│  │                                                              │ │
│  │  @Bean SimpleFunctionCatalog functionCatalog()              │ │
│  │  @Bean FunctionRegistrar functionRegistrar(...)             │ │
│  │  @Bean FunctionInvoker functionInvoker(...)                 │ │
│  │  @Bean FunctionController functionController(...)           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │ @Component   │    │ @Component   │    │ @Component   │       │
│  │              │    │              │    │              │       │
│  │  Function    │───>│ Function     │───>│  Function    │       │
│  │  Catalog     │    │  Registrar   │    │  Invoker     │       │
│  │              │    │              │    │              │       │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘       │
│         │                   │                   │                │
│         │ 存储              │ 注册              │ 执行           │
│         │                   │                   │                │
│         ▼                   ▼                   ▼                │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    Function Instances                      │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │ │
│  │  │uppercase │  │ reverse  │  │calculate │  │   log    │  │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │           FunctionController                                │ │
│  │           (REST API 端点)                                   │ │
│  │                                                              │ │
│  │  POST /{functionName}                                       │ │
│  │  GET  /functions                                            │ │
│  │  GET  /health                                               │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

### 3.2 组件依赖关系

```
                    ┌─────────────────────┐
                    │  Spring Container   │
                    └──────────┬──────────┘
                               │ 创建并管理
                               ▼
        ┌──────────────────────────────────────────┐
        │                                          │
        ▼                                          ▼
┌──────────────────┐                    ┌──────────────────┐
│ Function         │                    │  Function        │
│ Catalog          │<───────────────────│  Registrar       │
│                  │   注册函数          │ (BeanPostProc)   │
└────────┬─────────┘                    └──────────────────┘
         │                                         ▲
         │ 依赖                                    │ 实现接口
         ▼                                         │
┌──────────────────┐                    ┌──────────────────┐
│ Function         │                    │ Spring           │
│ Invoker          │                    │ BeanPostProc     │
└────────┬─────────┘                    └──────────────────┘
         │
         │ 依赖
         ▼
┌──────────────────┐
│ Function         │
│ Controller       │
└──────────────────┘

依赖方向：
FunctionController → FunctionInvoker → FunctionCatalog
FunctionRegistrar → FunctionCatalog
Spring Container → All Components
```

### 3.3 核心接口设计

```
┌─────────────────────────────────────────────────────────────────┐
│                        核心接口                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  SimpleFunctionCatalog                                          │
├─────────────────────────────────────────────────────────────────┤
│  - functions: Map<String, Object>                               │
│  - functionTypes: Map<String, FunctionType>                     │
├─────────────────────────────────────────────────────────────────┤
│  + registerFunction(name: String, function: Function): void     │
│  + registerSupplier(name: String, supplier: Supplier): void     │
│  + registerConsumer(name: String, consumer: Consumer): void     │
│  + lookup(functionDefinition: String): Function                 │
│  + composeFunctions(definition: String): Function               │
│  + getFunctionNames(): Set<String>                              │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  FunctionRegistrar implements BeanPostProcessor                 │
├─────────────────────────────────────────────────────────────────┤
│  - functionCatalog: SimpleFunctionCatalog                       │
├─────────────────────────────────────────────────────────────────┤
│  + postProcessAfterInitialization(bean: Object,                 │
│                                    beanName: String): Object    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  FunctionInvoker                                                │
├─────────────────────────────────────────────────────────────────┤
│  - functionCatalog: SimpleFunctionCatalog                       │
│  - objectMapper: ObjectMapper                                   │
├─────────────────────────────────────────────────────────────────┤
│  + invoke(functionName: String, input: Object,                  │
│           inputType: Class, outputType: Class):                 │
│           InvocationResult                                      │
│  - convertInput(input: Object, targetType: Class): Object       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  FunctionController                                             │
├─────────────────────────────────────────────────────────────────┤
│  - functionInvoker: FunctionInvoker                             │
│  - objectMapper: ObjectMapper                                   │
├─────────────────────────────────────────────────────────────────┤
│  + invokeFunction(functionName: String,                         │
│                   requestBody: String): ResponseEntity          │
│  + listFunctions(): ResponseEntity                              │
│  - parseRequestBody(requestBody: String): Object                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. 请求处理流程架构

### 4.1 HTTP 请求处理流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    HTTP 请求处理完整流程                         │
└─────────────────────────────────────────────────────────────────┘

客户端
  │
  │ POST /uppercase
  │ Content-Type: text/plain
  │ Body: "hello"
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ 步骤1: Spring MVC DispatcherServlet                             │
│ • 接收 HTTP 请求                                                 │
│ • 查找对应的 Controller                                          │
│ • 匹配到 FunctionController.invokeFunction()                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 步骤2: FunctionController.invokeFunction()                      │
│                                                                  │
│ 2.1 提取路径参数                                                 │
│     @PathVariable String functionName = "uppercase"             │
│                                                                  │
│ 2.2 提取请求体                                                   │
│     @RequestBody String requestBody = "hello"                   │
│                                                                  │
│ 2.3 解析请求体                                                   │
│     Object input = parseRequestBody(requestBody)                │
│     • 检查是否是 JSON 对象 (以 { 开头)                          │
│     • 检查是否是 JSON 数组 (以 [ 开头)                          │
│     • 检查是否是数字                                             │
│     • 默认作为字符串处理                                         │
│     结果: input = "hello" (String)                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 步骤3: FunctionInvoker.invoke()                                 │
│                                                                  │
│ 调用参数:                                                        │
│ • functionName = "uppercase"                                    │
│ • input = "hello"                                               │
│ • inputType = String.class                                     │
│ • outputType = Object.class                                    │
│                                                                  │
│ 3.1 查找函数                                                     │
│     Function<String, String> function =                         │
│         functionCatalog.lookup("uppercase")                     │
│                                                                  │
│ 3.2 类型转换（输入）                                             │
│     String convertedInput =                                     │
│         convertInput("hello", String.class)                     │
│     • 检查类型是否匹配: String.class.isInstance("hello") ✓      │
│     • 无需转换，直接返回                                         │
│                                                                  │
│ 3.3 执行函数                                                     │
│     long startTime = System.currentTimeMillis()                 │
│     String result = function.apply("hello")                     │
│     long endTime = System.currentTimeMillis()                   │
│     • 调用业务逻辑: "hello".toUpperCase()                        │
│     • 返回结果: "HELLO"                                          │
│     • 执行时间: 2ms                                              │
│                                                                  │
│ 3.4 封装结果                                                     │
│     return InvocationResult.success("HELLO", 2)                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 步骤4: FunctionController 构建响应                              │
│                                                                  │
│ 4.1 检查执行结果                                                 │
│     if (result.isSuccess()) { ... }                             │
│                                                                  │
│ 4.2 构建 HTTP 响应                                               │
│     return ResponseEntity.ok()                                  │
│         .header("X-Execution-Time", "2")                        │
│         .body("HELLO")                                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 步骤5: Spring MVC 响应处理                                       │
│ • 序列化响应体                                                   │
│ • 设置响应头                                                     │
│ • 返回给客户端                                                   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
                        客户端接收响应
                        HTTP 200 OK
                        X-Execution-Time: 2
                        Content-Type: text/plain
                        Body: "HELLO"
```

### 4.2 函数组合请求处理流程

```
┌─────────────────────────────────────────────────────────────────┐
│              函数组合请求处理流程                                │
│              POST /uppercase|reverse                             │
│              Body: "hello"                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ FunctionController 接收请求                                      │
│ functionName = "uppercase|reverse"                              │
│ requestBody = "hello"                                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ FunctionInvoker.invoke("uppercase|reverse", "hello", ...)       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ FunctionCatalog.lookup("uppercase|reverse")                     │
│                                                                  │
│ 检测到 | 符号，调用 composeFunctions()                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ SimpleFunctionCatalog.composeFunctions()                        │
│                                                                  │
│ 步骤1: 分割函数名称                                              │
│     String[] names = "uppercase|reverse".split("\\|")           │
│     names = ["uppercase", "reverse"]                            │
│                                                                  │
│ 步骤2: 查找第一个函数                                            │
│     Function f1 = lookup("uppercase")                           │
│     f1 = value -> value.toUpperCase()                           │
│                                                                  │
│ 步骤3: 遍历并组合后续函数                                        │
│     for (i = 1; i < names.length; i++) {                        │
│         Function f2 = lookup("reverse")                         │
│         f2 = value -> new StringBuilder(value).reverse()        │
│                                                                  │
│         // 使用 andThen 组合                                     │
│         f1 = f1.andThen(f2)                                     │
│     }                                                            │
│                                                                  │
│ 步骤4: 返回组合后的函数                                          │
│     return f1  // 现在是 uppercase.andThen(reverse)             │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 执行组合函数                                                     │
│                                                                  │
│ composed.apply("hello")                                         │
│   ↓                                                              │
│ temp = f1.apply("hello")      // uppercase                      │
│      = "hello".toUpperCase()                                    │
│      = "HELLO"                                                  │
│   ↓                                                              │
│ result = f2.apply(temp)       // reverse                        │
│        = new StringBuilder("HELLO").reverse().toString()        │
│        = "OLLEH"                                                │
│   ↓                                                              │
│ return "OLLEH"                                                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
                    返回给客户端: "OLLEH"
```

### 4.3 错误处理流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    错误处理流程                                  │
└─────────────────────────────────────────────────────────────────┘

场景1: 函数不存在
  │
  │ POST /nonexistent
  │
  ▼
FunctionCatalog.lookup("nonexistent")
  │
  │ functions.get("nonexistent") = null
  │
  ▼
抛出 IllegalArgumentException("函数不存在: nonexistent")
  │
  ▼
FunctionInvoker 捕获异常
  │
  ▼
return InvocationResult.failure(exception)
  │
  ▼
FunctionController 构建错误响应
  │
  ▼
ResponseEntity.status(500).body({
    "error": true,
    "message": "函数不存在: nonexistent",
    "type": "IllegalArgumentException"
})

─────────────────────────────────────────────────────────────────

场景2: 类型转换失败
  │
  │ POST /calculatePoints
  │ Body: "invalid"  (期望数字)
  │
  ▼
FunctionInvoker.convertInput("invalid", Double.class)
  │
  │ 尝试解析为 Double
  │ Double.valueOf("invalid") → NumberFormatException
  │
  ▼
FunctionInvoker 捕获异常
  │
  ▼
return InvocationResult.failure(exception)
  │
  ▼
ResponseEntity.status(500).body({
    "error": true,
    "message": "For input string: \"invalid\"",
    "type": "NumberFormatException"
})

─────────────────────────────────────────────────────────────────

场景3: 函数执行异常
  │
  │ POST /divide
  │ Body: {"a": 10, "b": 0}
  │
  ▼
function.apply(input)
  │
  │ 业务逻辑: a / b
  │ 10 / 0 → ArithmeticException
  │
  ▼
FunctionInvoker 捕获异常
  │
  ▼
return InvocationResult.failure(exception)
  │
  ▼
ResponseEntity.status(500).body({
    "error": true,
    "message": "/ by zero",
    "type": "ArithmeticException"
})
```

---

## 5. 函数生命周期架构

### 5.1 函数注册生命周期

```
┌─────────────────────────────────────────────────────────────────┐
│                    函数注册生命周期                              │
└─────────────────────────────────────────────────────────────────┘

阶段1: 定义阶段
  │
  │ 开发者编写代码
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ @Configuration                                                   │
│ public class FunctionApplication {                              │
│                                                                  │
│     @Bean                                                        │
│     public Function<String, String> uppercase() {               │
│         return String::toUpperCase;                             │
│     }                                                            │
│ }                                                                │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段2: Spring 容器启动
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ Spring Boot 启动流程                                             │
│                                                                  │
│ 1. 加载配置类                                                    │
│    • 扫描 @Configuration 类                                      │
│    • 发现 FunctionApplication                                   │
│                                                                  │
│ 2. 创建核心 Bean                                                 │
│    • SimpleFunctionCatalog                                      │
│    • FunctionRegistrar (BeanPostProcessor)                      │
│    • FunctionInvoker                                            │
│    • FunctionController                                         │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段3: 函数 Bean 创建
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ Spring 容器创建函数 Bean                                         │
│                                                                  │
│ 1. 调用 @Bean 方法                                               │
│    Function<String, String> bean = uppercase()                  │
│                                                                  │
│ 2. 创建函数实例                                                  │
│    bean = String::toUpperCase                                   │
│                                                                  │
│ 3. Bean 名称                                                     │
│    beanName = "uppercase" (方法名)                               │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段4: BeanPostProcessor 拦截
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ FunctionRegistrar.postProcessAfterInitialization()              │
│                                                                  │
│ 参数:                                                            │
│ • bean = Function<String, String> 实例                          │
│ • beanName = "uppercase"                                        │
│                                                                  │
│ 处理逻辑:                                                        │
│ 1. 检查类型                                                      │
│    if (bean instanceof Function) { ... }                        │
│                                                                  │
│ 2. 调用注册方法                                                  │
│    functionCatalog.registerFunction("uppercase", bean)          │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段5: 注册到 Catalog
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ SimpleFunctionCatalog.registerFunction()                        │
│                                                                  │
│ 1. 存储函数实例                                                  │
│    functions.put("uppercase", bean)                             │
│                                                                  │
│ 2. 记录函数类型                                                  │
│    functionTypes.put("uppercase", FunctionType.FUNCTION)        │
│                                                                  │
│ 3. 日志输出                                                      │
│    System.out.println("[FunctionCatalog] 注册 Function: ...")   │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段6: 注册完成
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ 函数已就绪，可以被调用                                           │
│                                                                  │
│ • 存储位置: Map<String, Object> functions                       │
│ • 访问方式: functionCatalog.lookup("uppercase")                 │
│ • HTTP 端点: POST /uppercase                                    │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 函数执行生命周期

```
┌─────────────────────────────────────────────────────────────────┐
│                    函数执行生命周期                              │
└─────────────────────────────────────────────────────────────────┘

阶段1: 请求到达
  │
  │ HTTP Request: POST /uppercase
  │ Body: "hello"
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ FunctionController 接收请求                                      │
│ • 提取函数名称: "uppercase"                                      │
│ • 提取请求体: "hello"                                            │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段2: 函数查找
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ FunctionCatalog.lookup("uppercase")                             │
│                                                                  │
│ 1. 从 Map 中查找                                                 │
│    Object function = functions.get("uppercase")                 │
│                                                                  │
│ 2. 类型转换                                                      │
│    return (Function<String, String>) function                   │
│                                                                  │
│ 3. 如果不存在                                                    │
│    throw new IllegalArgumentException("函数不存在")             │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段3: 输入准备
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ FunctionInvoker.convertInput()                                  │
│                                                                  │
│ 1. 检查类型匹配                                                  │
│    if (targetType.isInstance(input)) return input               │
│                                                                  │
│ 2. 类型转换                                                      │
│    • String → String: 直接返回                                   │
│    • String → Integer: Integer.valueOf()                        │
│    • String → Object: JSON 反序列化                              │
│                                                                  │
│ 3. 返回转换后的输入                                              │
│    return convertedInput                                        │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段4: 函数执行
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ function.apply(input)                                           │
│                                                                  │
│ 1. 记录开始时间                                                  │
│    long startTime = System.currentTimeMillis()                  │
│                                                                  │
│ 2. 调用函数                                                      │
│    String result = function.apply("hello")                      │
│    • 执行业务逻辑: "hello".toUpperCase()                         │
│    • 返回结果: "HELLO"                                           │
│                                                                  │
│ 3. 记录结束时间                                                  │
│    long endTime = System.currentTimeMillis()                    │
│    long executionTime = endTime - startTime                     │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段5: 结果封装
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ InvocationResult.success()                                      │
│                                                                  │
│ 创建结果对象:                                                    │
│ • success = true                                                │
│ • result = "HELLO"                                              │
│ • executionTime = 2ms                                           │
│ • exception = null                                              │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段6: 响应构建
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ FunctionController 构建 HTTP 响应                               │
│                                                                  │
│ ResponseEntity.ok()                                             │
│     .header("X-Execution-Time", "2")                            │
│     .body("HELLO")                                              │
└─────────────────────────────────────────────────────────────────┘
  │
  ▼
阶段7: 响应返回
  │
  ▼
┌─────────────────────────────────────────────────────────────────┐
│ 客户端接收响应                                                   │
│                                                                  │
│ HTTP 200 OK                                                     │
│ X-Execution-Time: 2                                             │
│ Content-Type: text/plain                                        │
│ Body: "HELLO"                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 5.3 函数状态管理

```
┌─────────────────────────────────────────────────────────────────┐
│                    函数状态管理                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ 状态1: 未注册 (Unregistered)                                    │
│                                                                  │
│ • 函数已定义但未被 Spring 容器加载                               │
│ • 不在 FunctionCatalog 中                                       │
│ • 无法被调用                                                     │
└─────────────────────────────────────────────────────────────────┘
                             │
                             │ Spring 容器启动
                             │ BeanPostProcessor 处理
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 状态2: 已注册 (Registered)                                      │
│                                                                  │
│ • 函数已存储在 FunctionCatalog 中                               │
│ • 可以通过 lookup() 查找                                        │
│ • HTTP 端点已就绪                                                │
│ • 等待被调用                                                     │
└─────────────────────────────────────────────────────────────────┘
                             │
                             │ HTTP 请求到达
                             │ FunctionInvoker.invoke()
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 状态3: 执行中 (Executing)                                       │
│                                                                  │
│ • 函数正在处理请求                                               │
│ • function.apply() 正在执行                                     │
│ • 占用线程资源                                                   │
│ • 可能被多个请求并发调用                                         │
└─────────────────────────────────────────────────────────────────┘
                             │
                             │ 执行完成
                             │ 返回结果
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 状态4: 空闲 (Idle)                                              │
│                                                                  │
│ • 函数执行完成                                                   │
│ • 返回到已注册状态                                               │
│ • 等待下一次调用                                                 │
│ • 函数实例保持在内存中                                           │
└─────────────────────────────────────────────────────────────────┘

注意事项:
• 函数实例是单例的（Spring Bean 默认单例）
• 多个请求会并发调用同一个函数实例
• 函数应该是无状态的，避免线程安全问题
• 函数实例在应用运行期间一直存在
```

