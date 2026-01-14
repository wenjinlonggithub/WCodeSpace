package com.architecture.wechatmessage;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订阅消息管理接口
 *
 * 提供给前端调用的API：
 * 1. 通知订阅/取消订阅
 * 2. 同步订阅状态
 * 3. 检查是否可发送
 * 4. 获取订阅状态详情
 *
 * @author 架构师
 * @date 2026-01-14
 */
@RestController
@RequestMapping("/api/wechat/subscription")
public class SubscriptionController {

    @Autowired
    private WechatSubscriptionManager oneTimeManager;

    @Autowired
    private LongTermSubscriptionManager longTermManager;

    /**
     * 用户授权订阅（前端调用）
     *
     * 前端在 wx.requestSubscribeMessage 成功后调用此接口
     *
     * @param request 订阅请求
     * @return 响应
     */
    @PostMapping("/subscribe")
    public ApiResponse subscribe(@RequestBody SubscribeRequest request) {
        try {
            if ("LONG_TERM".equals(request.getTemplateType())) {
                // 长期订阅
                longTermManager.onUserSubscribe(
                    request.getUserId(),
                    request.getOpenid(),
                    request.getTemplateId()
                );
            } else {
                // 一次性订阅
                oneTimeManager.onUserSubscribe(
                    request.getUserId(),
                    request.getOpenid(),
                    request.getTemplateId()
                );
            }

            return ApiResponse.success("订阅成功");

        } catch (Exception e) {
            return ApiResponse.error("订阅失败：" + e.getMessage());
        }
    }

    /**
     * 用户取消订阅（前端调用）
     *
     * 前端检测到用户在设置中关闭订阅后调用
     *
     * @param request 取消订阅请求
     * @return 响应
     */
    @PostMapping("/unsubscribe")
    public ApiResponse unsubscribe(@RequestBody UnsubscribeRequest request) {
        try {
            if ("LONG_TERM".equals(request.getTemplateType())) {
                longTermManager.onUserUnsubscribe(
                    request.getUserId(),
                    request.getTemplateId()
                );
            } else {
                // 一次性订阅无需处理取消（会自动消费）
            }

            return ApiResponse.success("已更新订阅状态");

        } catch (Exception e) {
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 同步订阅状态（前端定期调用）
     *
     * 前端通过 wx.getSetting 获取状态后调用此接口同步
     *
     * @param request 同步请求
     * @return 响应
     */
    @PostMapping("/sync")
    public ApiResponse syncStatus(@RequestBody SyncStatusRequest request) {
        try {
            if ("LONG_TERM".equals(request.getTemplateType())) {
                longTermManager.syncSubscriptionStatus(
                    request.getUserId(),
                    request.getTemplateId(),
                    request.getFrontendStatus()
                );
            }

            return ApiResponse.success("状态已同步");

        } catch (Exception e) {
            return ApiResponse.error("同步失败：" + e.getMessage());
        }
    }

    /**
     * 检查是否可发送
     *
     * 前端在需要发送消息前调用，检查是否可以发送
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @param templateType 模板类型
     * @return 检查结果
     */
    @GetMapping("/canSend")
    public ApiResponse canSend(
        @RequestParam String userId,
        @RequestParam String templateId,
        @RequestParam(required = false, defaultValue = "ONE_TIME") String templateType
    ) {
        try {
            if ("LONG_TERM".equals(templateType)) {
                LongTermSubscriptionManager.CanSendResult result =
                    longTermManager.canSend(userId, templateId);
                return ApiResponse.success(result);
            } else {
                WechatSubscriptionManager.CanSendResult result =
                    oneTimeManager.canSend(userId, templateId);
                return ApiResponse.success(result);
            }

        } catch (Exception e) {
            return ApiResponse.error("检查失败：" + e.getMessage());
        }
    }

    /**
     * 获取订阅状态详情
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @param templateType 模板类型
     * @return 状态详情
     */
    @GetMapping("/status")
    public ApiResponse getStatus(
        @RequestParam String userId,
        @RequestParam String templateId,
        @RequestParam(required = false, defaultValue = "ONE_TIME") String templateType
    ) {
        try {
            if ("LONG_TERM".equals(templateType)) {
                LongTermSubscriptionManager.SubscriptionStatusDetail detail =
                    longTermManager.getStatusDetail(userId, templateId);
                return ApiResponse.success(detail);
            } else {
                WechatSubscriptionManager.SubscriptionStatus status =
                    oneTimeManager.getStatus(userId, templateId);
                return ApiResponse.success(status);
            }

        } catch (Exception e) {
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 检测"总是保持选择"BUG
     *
     * 用于排查用户反馈的问题
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @param frontendStatus 前端状态
     * @return 检测结果
     */
    @GetMapping("/detectBug")
    public ApiResponse detectAlwaysKeepBug(
        @RequestParam String userId,
        @RequestParam String templateId,
        @RequestParam String frontendStatus
    ) {
        try {
            boolean isBug = longTermManager.detectAlwaysKeepBug(
                userId, templateId, frontendStatus
            );

            if (isBug) {
                return ApiResponse.success()
                    .message("检测到'总是保持选择'BUG，建议引导用户删除小程序后重新授权")
                    .data("isBug", true)
                    .data("solution", "删除小程序 → 重新进入 → 重新授权");
            } else {
                return ApiResponse.success()
                    .message("未检测到BUG")
                    .data("isBug", false);
            }

        } catch (Exception e) {
            return ApiResponse.error("检测失败：" + e.getMessage());
        }
    }

    // ==================== 请求/响应对象 ====================

    @Data
    public static class SubscribeRequest {
        private String userId;
        private String openid;
        private String templateId;
        private String templateType; // ONE_TIME / LONG_TERM
    }

    @Data
    public static class UnsubscribeRequest {
        private String userId;
        private String templateId;
        private String templateType;
    }

    @Data
    public static class SyncStatusRequest {
        private String userId;
        private String templateId;
        private String templateType;
        private String frontendStatus; // accept / reject
    }

    @Data
    public static class ApiResponse {
        private int code;
        private String message;
        private Object data;

        public static ApiResponse success(Object data) {
            ApiResponse response = new ApiResponse();
            response.setCode(200);
            response.setMessage("success");
            response.setData(data);
            return response;
        }

        public static ApiResponse success(String message) {
            ApiResponse response = new ApiResponse();
            response.setCode(200);
            response.setMessage(message);
            return response;
        }

        public static ApiResponse success() {
            return success("success");
        }

        public static ApiResponse error(String message) {
            ApiResponse response = new ApiResponse();
            response.setCode(500);
            response.setMessage(message);
            return response;
        }

        public ApiResponse message(String message) {
            this.message = message;
            return this;
        }

        public ApiResponse data(String key, Object value) {
            if (this.data == null) {
                this.data = new java.util.HashMap<>();
            }
            ((java.util.Map<String, Object>) this.data).put(key, value);
            return this;
        }
    }
}
