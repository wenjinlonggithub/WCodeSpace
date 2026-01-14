# AI代码生成工作流系统

基于 **OpenAI GPT-4** 和 **Claude (Anthropic)** 的多AI提供商智能Java代码生成工具。

## 项目简介

这是一个完整的AI代码生成工作流系统，支持通过自然语言描述生成高质量的Java代码。系统采用工作流引擎架构，支持多种Prompt策略和多个AI提供商，可生成各种设计模式和业务代码。

## 核心特性

- ✅ **多AI提供商**: 支持 OpenAI (GPT-4/3.5) 和 Claude (Sonnet 3.5/Opus/Haiku)，自动选择或手动切换
- ✅ **多种Prompt策略**: Zero-shot, Few-shot, Chain-of-Thought
- ✅ **完整工作流**: 需求解析 → Prompt构建 → API调用 → 代码提取 → 代码验证
- ✅ **智能重试机制**: 指数退避策略处理网络问题和API限流
- ✅ **代码验证**: 自动验证生成代码的语法正确性
- ✅ **Token统计**: 实时跟踪Token使用和成本
- ✅ **灵活配置**: 支持项目上下文、代码风格等自定义配置

## 技术栈

- **Java**: 17+
- **构建工具**: Maven
- **HTTP客户端**: OkHttp 4.12.0
- **JSON处理**: Jackson 2.16.1
- **日志框架**: SLF4J + Logback
- **代码简化**: Lombok 1.18.30
- **测试框架**: JUnit 5.9.3

## 快速开始

### 1. 配置 API Key

系统支持多个AI提供商，可以选择其中一个或同时配置多个（系统会自动选择）：

#### 选项A: 使用 OpenAI

```bash
# Windows
set OPENAI_API_KEY=your-openai-api-key-here

# Linux/Mac
export OPENAI_API_KEY=your-openai-api-key-here
```

#### 选项B: 使用 Claude (推荐)

```bash
# Windows
set CLAUDE_API_KEY=your-claude-api-key-here
# 或者
set ANTHROPIC_API_KEY=your-anthropic-api-key-here

# Linux/Mac
export CLAUDE_API_KEY=your-claude-api-key-here
# 或者
export ANTHROPIC_API_KEY=your-anthropic-api-key-here
```

#### 选项C: 自动选择（推荐）

同时配置多个API Key，系统会自动选择可用的提供商（优先级: Claude > OpenAI）：

```bash
export CLAUDE_API_KEY=your-claude-api-key
export OPENAI_API_KEY=your-openai-api-key
export AI_PROVIDER=auto  # 可选，默认就是auto
```

#### 手动指定提供商

```bash
# 使用 OpenAI
export AI_PROVIDER=openai

# 使用 Claude
export AI_PROVIDER=claude

# 自动选择（默认）
export AI_PROVIDER=auto
```

或在 `src/main/resources/application.properties` 中配置：

```properties
# AI提供商选择
ai.provider=auto

# OpenAI配置
openai.api.key=your-openai-key

# Claude配置
claude.api.key=your-claude-key
```

### 2. 编译项目

```bash
mvn clean compile
```

### 3. 运行示例

```bash
mvn exec:java -Dexec.mainClass="com.architecture.aicoding.AICodingApplication"
```

## 使用示例

### 示例1: 自动选择AI提供商

系统会自动选择可用的AI提供商（优先级: Claude > OpenAI）

```java
CodeGenerationService service = new CodeGenerationServiceImpl();

CodeGenerationRequest request = CodeGenerationRequest.builder()
    .requirement("创建一个线程安全的单例模式类DatabaseConnection")
    .promptStrategy("zero-shot")
    .model("gpt-4")  // 会自动映射到可用的模型
    .validateCode(true)
    .projectContext(ProjectContext.builder()
        .packageName("com.example.db")
        .javaVersion("17")
        .build())
    .build();

CodeGenerationResponse response = service.generateCode(request);

if (response.isSuccess()) {
    System.out.println("AI提供商: " + response.getMetadata("aiProvider"));
    System.out.println("使用模型: " + response.getMetadata("aiModel"));
    System.out.println(response.getGeneratedCode().getCode());
    System.out.println("Token使用: " + response.getMetadata("totalTokens"));
    System.out.println("估算成本: " + response.getMetadata("estimatedCostUSD"));
}
```

### 示例2: 明确使用Claude生成代码

```java
CodeGenerationRequest request = CodeGenerationRequest.builder()
    .requirement("创建一个User类使用Builder模式，包含id, username, email字段")
    .promptStrategy("zero-shot")
    .model("claude-3-5-sonnet-20241022")  // 使用Claude最新模型
    .codeStyle(CodeStyle.builder()
        .useLombok(true)
        .build())
    .build();

CodeGenerationResponse response = service.generateCode(request);
```

### 示例3: 使用OpenAI GPT-4

```java
CodeGenerationRequest request = CodeGenerationRequest.builder()
    .requirement("创建一个OrderService服务类，实现订单创建、查询、取消功能")
    .promptStrategy("chain-of-thought")
    .model("gpt-4")  // 明确使用GPT-4
    .projectContext(ProjectContext.builder()
        .packageName("com.example.service")
        .frameworks("Spring Boot")
        .build())
    .build();

CodeGenerationResponse response = service.generateCode(request);
```

### 示例4: 比较不同AI提供商的效果

```java
String requirement = "创建一个简单的缓存管理器";

// 使用OpenAI
CodeGenerationRequest openaiReq = CodeGenerationRequest.builder()
    .requirement(requirement)
    .model("gpt-4")
    .build();
CodeGenerationResponse openaiResp = service.generateCode(openaiReq);

// 使用Claude
CodeGenerationRequest claudeReq = CodeGenerationRequest.builder()
    .requirement(requirement)
    .model("claude-3-5-sonnet-20241022")
    .build();
CodeGenerationResponse claudeResp = service.generateCode(claudeReq);

// 比较结果
System.out.println("OpenAI生成: " + openaiResp.getGeneratedCode().getLineCount() + " 行");
System.out.println("Claude生成: " + claudeResp.getGeneratedCode().getLineCount() + " 行");
```

## 配置说明

### application.properties

```properties
# AI提供商选择 (openai, claude, auto)
ai.provider=auto

# OpenAI API配置
openai.api.key=${OPENAI_API_KEY:your-api-key-here}
openai.api.base.url=https://api.openai.com
openai.api.model=gpt-4
openai.api.max.tokens=2000
openai.api.temperature=0.7
openai.api.timeout=30
openai.api.max.retries=3

# Claude API配置
claude.api.key=${CLAUDE_API_KEY:your-api-key-here}
claude.api.base.url=https://api.anthropic.com
claude.api.model=claude-3-5-sonnet-20241022
claude.api.max.tokens=2000
claude.api.temperature=0.7

# 代码验证配置
code.validation.enabled=true
code.validation.syntax.check=true
```

### 支持的模型列表

#### OpenAI 模型
- `gpt-4` - 最强大的模型，适合复杂任务
- `gpt-4-turbo` - GPT-4加速版
- `gpt-3.5-turbo` - 性价比最高，适合简单任务
- `gpt-3.5-turbo-16k` - 支持更长上下文

#### Claude 模型
- `claude-3-5-sonnet-20241022` - **最新最强**，推荐使用
- `claude-3-opus-20240229` - 最强推理能力
- `claude-3-sonnet-20240229` - 平衡性能和成本
- `claude-3-haiku-20240307` - 最快最便宜
- `claude-2.1` - 上一代模型
- `claude-2.0` - 上一代模型

## Prompt策略说明

### Zero-shot
直接提供任务描述，不提供示例。适用于简单、明确的代码生成任务。

**推荐模型**: GPT-4

### Few-shot
提供1-3个代码示例来引导AI生成期望的代码风格。适用于需要特定格式或风格的代码生成。

**推荐模型**: GPT-3.5-turbo

### Chain-of-Thought (CoT)
引导AI逐步思考，分步骤完成复杂任务（需求分析 → 架构设计 → 代码实现）。适用于复杂的架构设计和代码生成。

**推荐模型**: GPT-4

## 项目结构

```
src/main/java/com/architecture/aicoding/
├── AICodingApplication.java        # 主应用入口
├── config/                          # 配置管理
│   └── OpenAIConfig.java
├── client/                          # OpenAI API客户端
│   ├── OpenAIClient.java
│   ├── OpenAIClientImpl.java
│   ├── request/                     # 请求模型
│   └── response/                    # 响应模型
├── prompt/                          # Prompt工程策略
│   ├── PromptStrategy.java
│   ├── ZeroShotPromptStrategy.java
│   ├── FewShotPromptStrategy.java
│   └── ChainOfThoughtPromptStrategy.java
├── workflow/                        # 工作流引擎
│   ├── WorkflowStep.java
│   ├── WorkflowContext.java
│   ├── CodeGenerationWorkflow.java
│   ├── DefaultCodeGenerationWorkflow.java
│   └── steps/                       # 工作流步骤
│       ├── RequirementParseStep.java
│       ├── PromptBuildStep.java
│       ├── APICallStep.java
│       ├── CodeExtractionStep.java
│       └── CodeValidationStep.java
├── service/                         # 服务层
│   ├── CodeGenerationService.java
│   ├── CodeGenerationServiceImpl.java
│   └── TokenCounterService.java
├── model/                           # 领域模型
│   ├── CodeGenerationRequest.java
│   ├── CodeGenerationResponse.java
│   ├── GeneratedCode.java
│   ├── ProjectContext.java
│   └── CodeStyle.java
├── retry/                           # 重试机制
│   └── RetryHandler.java
└── exception/                       # 自定义异常
    ├── OpenAIException.java
    ├── CodeGenerationException.java
    └── ValidationException.java
```

## 工作流步骤

1. **需求解析** (RequirementParseStep)
   - 验证需求描述
   - 识别代码类型（class/interface/enum）
   - 识别设计模式（singleton/factory/builder等）

2. **Prompt构建** (PromptBuildStep)
   - 选择Prompt策略
   - 注入项目上下文和代码风格
   - 构建完整的Prompt

3. **API调用** (APICallStep)
   - 调用OpenAI API
   - 带重试的错误处理
   - Token使用统计

4. **代码提取** (CodeExtractionStep)
   - 从响应中提取代码
   - 识别类名、包名
   - 解析代码类型

5. **代码验证** (CodeValidationStep)
   - 检查括号匹配
   - 验证基本语法
   - 标记验证结果

## 成本估算

### OpenAI 价格 (2026年1月参考)

| 模型 | 输入价格 | 输出价格 | 示例成本* |
|------|---------|---------|---------|
| GPT-4 | $0.03 / 1K | $0.06 / 1K | ~$0.036 |
| GPT-3.5-turbo | $0.0005 / 1K | $0.0015 / 1K | ~$0.00085 |

### Claude 价格 (2026年1月参考)

| 模型 | 输入价格 | 输出价格 | 示例成本* |
|------|---------|---------|---------|
| Claude 3.5 Sonnet | $0.003 / 1K | $0.015 / 1K | ~$0.0081 |
| Claude 3 Opus | $0.015 / 1K | $0.075 / 1K | ~$0.0405 |
| Claude 3 Sonnet | $0.003 / 1K | $0.015 / 1K | ~$0.0081 |
| Claude 3 Haiku | $0.00025 / 1K | $0.00125 / 1K | ~$0.000675 |

*示例成本基于生成一个简单类（约200 tokens输入，500 tokens输出）

### 性价比对比

1. **最便宜**: Claude 3 Haiku ($0.000675) 或 GPT-3.5-turbo ($0.00085)
2. **最平衡**: Claude 3.5 Sonnet ($0.0081) - **推荐用于生产**
3. **最强大**: GPT-4 ($0.036) 或 Claude 3 Opus ($0.0405)

### 推荐使用策略

- **开发测试**: Claude 3 Haiku 或 GPT-3.5-turbo
- **生产环境**: Claude 3.5 Sonnet（性能强，成本适中）
- **复杂任务**: GPT-4 或 Claude 3 Opus

## 注意事项

1. **API Key安全**: 不要将API Key提交到版本控制系统
2. **成本控制**:
   - GPT-4价格较高，建议先用GPT-3.5-turbo或Claude Haiku测试
   - Claude 3.5 Sonnet是性价比最优选择（比GPT-4便宜4-5倍）
3. **Token限制**: 注意maxTokens配置，避免超出模型限制
4. **网络超时**: Claude API可能需要更长的响应时间，已自动调整
5. **模型选择**:
   - 简单任务: Claude Haiku 或 GPT-3.5-turbo
   - 复杂任务: Claude 3.5 Sonnet 或 GPT-4
   - 超级复杂任务: Claude 3 Opus 或 GPT-4

## 架构特性

### 多AI提供商支持

系统采用**适配器模式**设计，已实现：

- ✅ **OpenAI适配器** (OpenAIClientAdapter)
- ✅ **Claude适配器** (ClaudeClientAdapter)
- ✅ **AI客户端工厂** (AIClientFactory) - 自动选择或手动指定
- ✅ **统一接口** (AIClient) - 屏蔽底层差异

### 扩展性

系统设计支持以下扩展：

1. **更多AI提供商**: 通过AIClient接口轻松添加新提供商
   - 本地模型（Ollama, LM Studio）
   - 其他云服务（Google PaLM, Cohere等）
2. **插件化Prompt模板**: 支持外部JSON配置模板
3. **代码后处理管道**: 代码格式化、注释增强等
4. **多轮对话优化**: 保持对话历史，增量生成和修正

## 许可证

Copyright © 2026 WCodeSpace

## 联系方式

- 项目: WCodeSpace/AI/AICoding
- 版本: 1.0-SNAPSHOT
