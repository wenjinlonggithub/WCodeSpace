/*
package com.architecture.wechatmessage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

*/
/**
 * 微信订阅消息发送服务
 *
 * 核心功能：
 * 1. 发送前检查订阅状态
 * 2. 调用微信接口发送消息
 * 3. 处理各种错误码（重点是43101）
 * 4. 更新本地订阅状态
 *
 * @author 架构师
 * @date 2026-01-14
 *//*

@Slf4j
@Service
public class WechatMessageSender {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private WechatSubscriptionManager subscriptionManager;

    */
/**
     * 发送订阅消息
     *
     * @param userId 用户ID
     * @param openid 用户openid
     * @param templateId 模板ID
     * @param data 消息数据
     * @param page 跳转页面
     * @return 发送结果
     *//*

    public SendResult sendSubscribeMessage(
        String userId,
        String openid,
        String templateId,
        Map<String, String> data,
        String page
    ) {
        log.info("准备发送订阅消息，userId: {}, templateId: {}", userId, templateId);

        // 1. 发送前检查订阅状态
        WechatSubscriptionManager.CanSendResult canSend =
            subscriptionManager.canSend(userId, templateId);

        if (!canSend.isCanSend()) {
            log.warn("订阅状态检查失败，原因: {}", canSend.getReason());
            return SendResult.fail(SendResult.ErrorType.NOT_SUBSCRIBED, canSend.getReason());
        }

        // 2. 构建微信消息对象
        //WxMaSubscribeMessage message = buildMessage(openid, templateId, data, page);

        // 3. 调用微信接口发送
     //   try {
            //wxMaService.getMsgService().sendSubscribeMsg(message);
            log.info("发送成功，userId: {}, templateId: {}", userId, templateId);

            // 4. 发送成功，扣减次数
            subscriptionManager.onMessageSent(userId, templateId);

            return SendResult.success();

       // } catch (WxErrorException e) {
            return handleWxError(userId, templateId, e);
       // }
    }

    */
/**
     * 批量发送订阅消息
     *
     * @param messages 消息列表
     * @return 发送结果统计
     *//*

    public BatchSendResult batchSend(List<MessageRequest> messages) {
        log.info("批量发送订阅消息，总数: {}", messages.size());

        // 1. 按模板分组
        Map<String, List<MessageRequest>> groupByTemplate = new HashMap<>();
        for (MessageRequest msg : messages) {
            groupByTemplate
                .computeIfAbsent(msg.getTemplateId(), k -> new ArrayList<>())
                .add(msg);
        }

        // 2. 批量检查订阅状态
        List<MessageRequest> validMessages = new ArrayList<>();
        List<MessageRequest> invalidMessages = new ArrayList<>();

        for (Map.Entry<String, List<MessageRequest>> entry : groupByTemplate.entrySet()) {
            String templateId = entry.getKey();
            List<MessageRequest> msgList = entry.getValue();

            // 提取用户ID列表
            List<String> userIds = msgList.stream()
                .map(MessageRequest::getUserId)
                .collect(Collectors.toList());

            // 批量检查
            Map<String, Boolean> canSendMap = subscriptionManager
                .batchCanSend(userIds, templateId);

            // 分类
            for (MessageRequest msg : msgList) {
                if (Boolean.TRUE.equals(canSendMap.get(msg.getUserId()))) {
                    validMessages.add(msg);
                } else {
                    invalidMessages.add(msg);
                }
            }
        }

        log.info("订阅检查完成，可发送: {}, 不可发送: {}",
            validMessages.size(), invalidMessages.size());

        // 3. 批量发送
        int successCount = 0;
        int failCount = 0;
        List<SendResult> failResults = new ArrayList<>();

        for (MessageRequest msg : validMessages) {
            SendResult result = sendSubscribeMessage(
                msg.getUserId(),
                msg.getOpenid(),
                msg.getTemplateId(),
                msg.getData(),
                msg.getPage()
            );

            if (result.isSuccess()) {
                successCount++;
            } else {
                failCount++;
                failResults.add(result);
            }
        }

        // 4. 返回统计结果
        return BatchSendResult.builder()
            .total(messages.size())
            .successCount(successCount)
            .failCount(failCount + invalidMessages.size())
            .notSubscribedCount(invalidMessages.size())
            .failResults(failResults)
            .build();
    }

    */
/**
     * 处理微信接口错误
     *//*

    private SendResult handleWxError(String userId, String templateId, WxErrorException e) {
        int errorCode = e.getError().getErrorCode();
        String errorMsg = e.getError().getErrorMsg();

        log.error("发送失败，userId: {}, templateId: {}, errorCode: {}, errorMsg: {}",
            userId, templateId, errorCode, errorMsg);

        switch (errorCode) {
            case 43101:
                // 用户拒绝接受消息（或订阅已消费）
                log.warn("43101错误：用户拒绝接受消息或订阅已消费");
                subscriptionManager.handle43101Error(userId, templateId);
                return SendResult.fail(
                    SendResult.ErrorType.USER_REFUSED,
                    "用户拒绝接收消息或订阅次数已用完，请引导用户重新订阅"
                );

            case 40003:
                // invalid openid
                log.error("openid无效");
                return SendResult.fail(
                    SendResult.ErrorType.INVALID_OPENID,
                    "openid无效"
                );

            case 40037:
                // template_id不正确
                log.error("template_id不正确");
                return SendResult.fail(
                    SendResult.ErrorType.INVALID_TEMPLATE,
                    "模板ID不正确"
                );

            case 41030:
                // page路径不正确
                log.error("page路径不正确");
                return SendResult.fail(
                    SendResult.ErrorType.INVALID_PAGE,
                    "页面路径不正确"
                );

            case 45047:
                // 达到上限
                log.error("达到发送上限");
                return SendResult.fail(
                    SendResult.ErrorType.RATE_LIMIT,
                    "达到发送上限，请稍后重试"
                );

            case 40165:
                // 订阅消息模板未审核通过或已停用
                log.error("模板未审核通过或已停用");
                return SendResult.fail(
                    SendResult.ErrorType.TEMPLATE_DISABLED,
                    "模板未审核通过或已停用"
                );

            default:
                log.error("未知错误：{}", errorMsg);
                return SendResult.fail(
                    SendResult.ErrorType.UNKNOWN,
                    "发送失败：" + errorMsg
                );
        }
    }

    */
/**
     * 构建微信消息对象
     *//*

    private WxMaSubscribeMessage buildMessage(
        String openid,
        String templateId,
        Map<String, String> data,
        String page
    ) {
        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(openid);
        message.setTemplateId(templateId);
        message.setPage(page);

        // 转换数据格式
        List<WxMaSubscribeMessage.MsgData> msgDataList = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            WxMaSubscribeMessage.MsgData msgData = new WxMaSubscribeMessage.MsgData();
            msgData.setName(entry.getKey());
            msgData.setValue(entry.getValue());
            msgDataList.add(msgData);
        }
        message.setData(msgDataList);

        return message;
    }

    */
/**
     * 消息请求对象
     *//*

    @Data
    public static class MessageRequest {
        private String userId;
        private String openid;
        private String templateId;
        private Map<String, String> data;
        private String page;
    }

    */
/**
     * 发送结果
     *//*

    @Data
    public static class SendResult {
        private boolean success;
        private ErrorType errorType;
        private String errorMessage;
        private String userId;
        private String templateId;

        public enum ErrorType {
            NOT_SUBSCRIBED,      // 未订阅
            USER_REFUSED,        // 用户拒绝（43101）
            INVALID_OPENID,      // openid无效
            INVALID_TEMPLATE,    // 模板ID无效
            INVALID_PAGE,        // 页面路径无效
            RATE_LIMIT,          // 达到发送上限
            TEMPLATE_DISABLED,   // 模板已停用
            UNKNOWN              // 未知错误
        }

        public static SendResult success() {
            SendResult result = new SendResult();
            result.setSuccess(true);
            return result;
        }

        public static SendResult fail(ErrorType errorType, String errorMessage) {
            SendResult result = new SendResult();
            result.setSuccess(false);
            result.setErrorType(errorType);
            result.setErrorMessage(errorMessage);
            return result;
        }
    }

    */
/**
     * 批量发送结果
     *//*

    @Data
    @Builder
    public static class BatchSendResult {
        private int total;              // 总数
        private int successCount;       // 成功数
        private int failCount;          // 失败数
        private int notSubscribedCount; // 未订阅数
        private List<SendResult> failResults;  // 失败详情
    }
}
*/
