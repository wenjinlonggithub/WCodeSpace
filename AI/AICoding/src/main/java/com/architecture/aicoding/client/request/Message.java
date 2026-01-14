package com.architecture.aicoding.client.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAI API消息模型
 * 用于聊天完成请求中的消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * 角色: system, user, assistant
     */
    @JsonProperty("role")
    private String role;

    /**
     * 消息内容
     */
    @JsonProperty("content")
    private String content;
}
