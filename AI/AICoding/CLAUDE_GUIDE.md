# Claude Code 集成指南

本文档说明如何在AI代码生成工作流系统中使用 **Claude (Anthropic)** 作为AI提供商。

## 为什么选择Claude？

### Claude的优势

1. **性价比最优**: Claude 3.5 Sonnet 比 GPT-4 便宜 4-5 倍，性能却相当
2. **更长上下文**: Claude支持200K tokens上下文（GPT-4仅8K-128K）
3. **代码理解能力强**: Claude在代码生成和理解任务上表现出色
4. **更新更快**: Claude 3.5 Sonnet (2024年10月发布) 是最新模型
5. **更安全**: Anthropic专注于AI安全性和对齐

### 性能对比

| 模型 | 代码生成质量 | 成本 | 响应速度 |
|------|------------|-----|---------|
| Claude 3.5 Sonnet | ⭐⭐⭐⭐⭐ | $0.0081 | 快 |
| Claude 3 Opus | ⭐⭐⭐⭐⭐ | $0.0405 | 中 |
| Claude 3 Haiku | ⭐⭐⭐⭐ | $0.000675 | 极快 |
| GPT-4 | ⭐⭐⭐⭐⭐ | $0.036 | 中 |
| GPT-3.5-turbo | ⭐⭐⭐ | $0.00085 | 快 |

**推荐**: 优先使用 **Claude 3.5 Sonnet** 作为生产环境的默认选择。

---

## 快速开始

### 1. 获取 Claude API Key

访问 [Anthropic Console](https://console.anthropic.com/) 创建API Key

### 2. 配置环境变量

```bash
# Linux/Mac
export CLAUDE_API_KEY=sk-ant-api03-xxxxx

# Windows CMD
set CLAUDE_API_KEY=sk-ant-api03-xxxxx

# Windows PowerShell
$env:CLAUDE_API_KEY="sk-ant-api03-xxxxx"
```

### 3. 自动使用Claude

系统会自动检测配置的API Key并选择Claude：

```java
// 自动选择可用的AI提供商（如果配置了Claude，会优先使用）
CodeGenerationService service = new CodeGenerationServiceImpl();

CodeGenerationRequest request = CodeGenerationRequest.builder()
    .requirement("创建一个单例模式类")
    .promptStrategy("zero-shot")
    .model("gpt-4")  // 系统会自动映射到Claude 3.5 Sonnet
    .build();

CodeGenerationResponse response = service.generateCode(request);

// 查看实际使用的提供商
System.out.println("使用提供商: " + response.getMetadata("aiProvider"));
// 输出: Claude (Anthropic)
```

---

## 明确指定使用Claude

### 方法1: 在代码中指定Claude模型

```java
CodeGenerationRequest request = CodeGenerationRequest.builder()
    .requirement("创建一个Builder模式类")
    .model("claude-3-5-sonnet-20241022")  // 明确使用Claude
    .build();
```

### 方法2: 设置环境变量

```bash
export AI_PROVIDER=claude
```

### 方法3: 修改配置文件

编辑 `application.properties`:

```properties
ai.provider=claude
```

---

## 支持的Claude模型

### 推荐模型

| 模型 | 版本号 | 适用场景 | 成本/性能 |
|------|-------|---------|---------|
| **Claude 3.5 Sonnet** | claude-3-5-sonnet-20241022 | **生产环境首选** | ⭐⭐⭐⭐⭐ |
| Claude 3 Opus | claude-3-opus-20240229 | 超复杂任务 | ⭐⭐⭐⭐ |
| Claude 3 Sonnet | claude-3-sonnet-20240229 | 平衡性能 | ⭐⭐⭐⭐ |
| Claude 3 Haiku | claude-3-haiku-20240307 | 快速原型/测试 | ⭐⭐⭐⭐⭐ |

### 模型选择建议

```java
// 开发测试 - 使用最快最便宜的Haiku
.model("claude-3-haiku-20240307")

// 生产环境 - 使用性价比最高的Sonnet 3.5
.model("claude-3-5-sonnet-20241022")

// 超级复杂任务 - 使用最强的Opus
.model("claude-3-opus-20240229")
```

---

## 完整示例

### 示例1: 使用Claude生成单例模式

```java
public class ClaudeExample {
    public static void main(String[] args) {
        CodeGenerationService service = new CodeGenerationServiceImpl();

        CodeGenerationRequest request = CodeGenerationRequest.builder()
            .requirement("创建一个线程安全的单例模式类CacheManager，支持get/put/clear操作")
            .promptStrategy("zero-shot")
            .model("claude-3-5-sonnet-20241022")
            .validateCode(true)
            .projectContext(ProjectContext.builder()
                .packageName("com.example.cache")
                .javaVersion("17")
                .build())
            .build();

        CodeGenerationResponse response = service.generateCode(request);

        if (response.isSuccess()) {
            System.out.println("=== 使用Claude生成的代码 ===");
            System.out.println(response.getGeneratedCode().getCode());

            System.out.println("\n=== 性能统计 ===");
            System.out.println("提供商: " + response.getMetadata("aiProvider"));
            System.out.println("模型: " + response.getMetadata("aiModel"));
            System.out.println("Token使用: " + response.getMetadata("totalTokens"));
            System.out.println("成本: " + response.getMetadata("estimatedCostUSD"));
            System.out.println("耗时: " + response.getDurationMs() + "ms");
        }
    }
}
```

### 示例2: 比较Claude和OpenAI

```java
public class CompareProviders {
    public static void main(String[] args) {
        CodeGenerationService service = new CodeGenerationServiceImpl();
        String requirement = "创建一个简单的日志工具类";

        // 使用Claude
        CodeGenerationRequest claudeReq = CodeGenerationRequest.builder()
            .requirement(requirement)
            .model("claude-3-5-sonnet-20241022")
            .build();

        CodeGenerationResponse claudeResp = service.generateCode(claudeReq);

        // 使用OpenAI
        CodeGenerationRequest openaiReq = CodeGenerationRequest.builder()
            .requirement(requirement)
            .model("gpt-4")
            .build();

        CodeGenerationResponse openaiResp = service.generateCode(openaiReq);

        // 比较结果
        System.out.println("=== Claude 3.5 Sonnet ===");
        System.out.println("代码行数: " + claudeResp.getGeneratedCode().getLineCount());
        System.out.println("成本: " + claudeResp.getMetadata("estimatedCostUSD"));
        System.out.println("耗时: " + claudeResp.getDurationMs() + "ms");

        System.out.println("\n=== OpenAI GPT-4 ===");
        System.out.println("代码行数: " + openaiResp.getGeneratedCode().getLineCount());
        System.out.println("成本: " + openaiResp.getMetadata("estimatedCostUSD"));
        System.out.println("耗时: " + openaiResp.getDurationMs() + "ms");
    }
}
```

---

## 成本对比

基于生成一个200 tokens输入、500 tokens输出的简单类：

| 提供商 | 模型 | 成本 | 相对价格 |
|-------|------|------|---------|
| Claude | 3.5 Sonnet | $0.0081 | **1x (基准)** |
| Claude | Haiku | $0.000675 | 0.08x (最便宜) |
| Claude | Opus | $0.0405 | 5x |
| OpenAI | GPT-3.5-turbo | $0.00085 | 0.1x |
| OpenAI | GPT-4 | $0.036 | **4.4x** |

**结论**: Claude 3.5 Sonnet 比 GPT-4 便宜 4.4 倍，同时性能相当甚至更好！

---

## 常见问题

### Q1: Claude和OpenAI可以同时配置吗？

**答**: 可以！系统会根据配置的模型名称自动选择正确的提供商。

```bash
export CLAUDE_API_KEY=your-claude-key
export OPENAI_API_KEY=your-openai-key
export AI_PROVIDER=auto  # 自动选择（优先Claude）
```

### Q2: 如何切换回OpenAI？

**答**: 三种方式：

1. 在代码中指定GPT模型：`.model("gpt-4")`
2. 设置环境变量：`export AI_PROVIDER=openai`
3. 修改配置文件：`ai.provider=openai`

### Q3: Claude生成的代码质量如何？

**答**: Claude 3.5 Sonnet 在代码生成任务上与 GPT-4 性能相当，某些场景下甚至更好。特别是：
- 代码注释更详细
- 错误处理更完善
- 遵循最佳实践更严格

### Q4: Claude的响应速度如何？

**答**:
- Haiku: 最快（<2秒）
- Sonnet 3.5: 快（2-5秒）
- Opus: 中等（5-10秒）

系统已自动调整Claude的超时时间为60秒。

### Q5: 支持哪些环境变量名？

**答**: Claude支持两种环境变量名：
- `CLAUDE_API_KEY`（推荐）
- `ANTHROPIC_API_KEY`（Anthropic官方名称）

任选其一即可。

---

## 最佳实践

### 1. 开发阶段

使用Claude Haiku进行快速迭代：

```java
.model("claude-3-haiku-20240307")  // 最快最便宜
```

### 2. 生产环境

使用Claude 3.5 Sonnet作为默认选择：

```java
.model("claude-3-5-sonnet-20241022")  // 性价比最优
```

### 3. 复杂任务

使用Claude Opus或GPT-4：

```java
.model("claude-3-opus-20240229")  // 最强推理能力
// 或
.model("gpt-4")
```

### 4. 成本控制

```java
// 设置Token限制
.maxTokens(1000)  // 限制输出长度，降低成本
```

### 5. 自动选择策略

```bash
# 让系统自动选择最优提供商
export AI_PROVIDER=auto
export CLAUDE_API_KEY=your-claude-key
export OPENAI_API_KEY=your-openai-key
```

---

## 故障排查

### 问题: "Claude API Key未配置"

**解决方案**:
```bash
# 检查环境变量是否正确设置
echo $CLAUDE_API_KEY

# 重新设置
export CLAUDE_API_KEY=sk-ant-api03-xxxxx
```

### 问题: API调用超时

**解决方案**:
- 检查网络连接
- 使用更快的模型 (Haiku)
- 减少maxTokens设置

### 问题: 想查看使用了哪个提供商

**解决方案**:
```java
System.out.println("提供商: " + response.getMetadata("aiProvider"));
System.out.println("模型: " + response.getMetadata("aiModel"));
```

---

## 总结

✅ **推荐使用Claude的理由**:
1. 性价比最优（比GPT-4便宜4-5倍）
2. 代码生成质量优秀
3. 更长的上下文窗口
4. 更注重安全性和对齐

✅ **开始使用**:
1. 获取Claude API Key
2. 设置环境变量 `CLAUDE_API_KEY`
3. 在代码中使用 `.model("claude-3-5-sonnet-20241022")`
4. 享受高性价比的AI代码生成！

---

## 相关链接

- [Anthropic官网](https://www.anthropic.com/)
- [Claude API文档](https://docs.anthropic.com/)
- [获取API Key](https://console.anthropic.com/)
- [Claude定价](https://www.anthropic.com/api)

---

**最后更新**: 2026年1月
**版本**: 1.0
