package com.architecture.aicoding.client.response;

import com.architecture.aicoding.client.request.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 选择项模型
 * 表示API返回的一个响应选项
 */
@Data
public class Choice {

    /**
     * 选择索引
     */
    @JsonProperty("index")
    private Integer index;

    /**
     * 消息内容
     */
    @JsonProperty("message")
    private Message message;

    /**
     * 完成原因: stop, length, content_filter, null
     */
    @JsonProperty("finish_reason")
    private String finishReason;
}
