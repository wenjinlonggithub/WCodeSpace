package com.architecture.function.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Web 适配器（HTTP 端点控制器）
 *
 * 模拟 Spring Cloud Function 的 Web 适配器
 * 将 HTTP 请求映射到函数调用
 *
 * 核心职责：
 * 1. 接收 HTTP 请求
 * 2. 解析请求参数和 Body
 * 3. 调用 FunctionInvoker 执行函数
 * 4. 将函数结果转换为 HTTP 响应
 *
 * 请求处理流程：
 * HTTP Request → FunctionController → FunctionInvoker → FunctionCatalog
 * → 执行函数 → 返回结果 → HTTP Response
 */
@RestController
@RequestMapping
public class FunctionController {

    private final FunctionInvoker functionInvoker;
    private final ObjectMapper objectMapper;

    public FunctionController(FunctionInvoker functionInvoker) {
        this.functionInvoker = functionInvoker;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 通用函数调用端点
     *
     * 自动为所有注册的函数生成 HTTP 端点
     * 路径格式: POST /{functionName}
     *
     * 例如：
     * - POST /uppercase
     * - POST /calculateOrder
     * - POST /uppercase|reverse (函数组合)
     *
     * @param functionName 函数名称（从路径中提取）
     * @param requestBody 请求体（JSON 或纯文本）
     * @return HTTP 响应
     */
    @PostMapping(value = "/{functionName}",
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> invokeFunction(
            @PathVariable String functionName,
            @RequestBody(required = false) String requestBody) {

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║          HTTP 请求处理开始                              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("[FunctionController] 接收到请求");
        System.out.println("[FunctionController] 函数名称: " + functionName);
        System.out.println("[FunctionController] 请求体: " + requestBody);

        try {
            // 步骤1: 解析请求体
            System.out.println("\n--- 步骤1: 解析请求体 ---");
            Object input = parseRequestBody(requestBody);
            System.out.println("[FunctionController] 解析后的输入: " + input);

            // 步骤2: 调用函数执行引擎
            System.out.println("\n--- 步骤2: 调用函数执行引擎 ---");
            FunctionInvoker.InvocationResult<Object> result =
                functionInvoker.invoke(functionName, input, Object.class, Object.class);

            // 步骤3: 构建 HTTP 响应
            System.out.println("\n--- 步骤3: 构建 HTTP 响应 ---");
            if (result.isSuccess()) {
                System.out.println("[FunctionController] ✓ 执行成功");
                System.out.println("[FunctionController] 响应数据: " + result.getResult());
                System.out.println("╔════════════════════════════════════════════════════════╗");
                System.out.println("║          HTTP 请求处理完成                              ║");
                System.out.println("╚════════════════════════════════════════════════════════╝\n");

                return ResponseEntity.ok()
                    .header("X-Execution-Time", String.valueOf(result.getExecutionTime()))
                    .body(result.getResult());
            } else {
                System.err.println("[FunctionController] ✗ 执行失败");
                System.err.println("[FunctionController] 错误信息: " + result.getException().getMessage());
                System.out.println("╔════════════════════════════════════════════════════════╗");
                System.out.println("║          HTTP 请求处理失败                              ║");
                System.out.println("╚════════════════════════════════════════════════════════╝\n");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(result.getException()));
            }

        } catch (Exception e) {
            System.err.println("[FunctionController] ✗ 请求处理异常: " + e.getMessage());
            e.printStackTrace();
            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║          HTTP 请求处理异常                              ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e));
        }
    }

    /**
     * 函数组合调用端点
     *
     * 支持通过逗号分隔的函数名称进行组合调用
     * 路径格式: POST /{function1},{function2}
     *
     * 例如：POST /uppercase,reverse
     *
     * 注意：这个端点会将逗号转换为管道符 |
     */
    @PostMapping(value = "/{function1},{function2}",
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE},
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> invokeComposedFunction(
            @PathVariable String function1,
            @PathVariable String function2,
            @RequestBody(required = false) String requestBody) {

        // 将逗号分隔转换为管道符分隔
        String functionName = function1 + "|" + function2;
        System.out.println("[FunctionController] 函数组合调用: " + functionName);

        return invokeFunction(functionName, requestBody);
    }

    /**
     * 获取所有已注册的函数列表
     *
     * GET /functions
     */
    @GetMapping("/functions")
    public ResponseEntity<?> listFunctions() {
        System.out.println("[FunctionController] 查询函数列表");

        // 这里简化实现，实际应该从 FunctionCatalog 获取
        Map<String, String> response = new HashMap<>();
        response.put("message", "请查看 FunctionCatalog 获取完整函数列表");
        response.put("endpoint", "POST /{functionName}");
        response.put("example", "POST /uppercase");

        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Spring Cloud Function");
        return ResponseEntity.ok(response);
    }

    // ========== 辅助方法 ==========

    /**
     * 解析请求体
     *
     * 支持多种格式：
     * 1. JSON 对象
     * 2. JSON 数组
     * 3. 纯文本
     * 4. 数字
     */
    private Object parseRequestBody(String requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            return null;
        }

        // 尝试解析为 JSON
        try {
            if (requestBody.trim().startsWith("{")) {
                return objectMapper.readValue(requestBody, Map.class);
            } else if (requestBody.trim().startsWith("[")) {
                return objectMapper.readValue(requestBody, Object[].class);
            }
        } catch (Exception e) {
            // 不是 JSON，继续尝试其他格式
        }

        // 尝试解析为数字
        try {
            if (requestBody.matches("-?\\d+")) {
                return Integer.parseInt(requestBody);
            } else if (requestBody.matches("-?\\d+\\.\\d+")) {
                return Double.parseDouble(requestBody);
            }
        } catch (Exception e) {
            // 不是数字，继续
        }

        // 默认作为字符串处理
        return requestBody;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", e.getMessage());
        error.put("type", e.getClass().getSimpleName());
        return error;
    }
}
