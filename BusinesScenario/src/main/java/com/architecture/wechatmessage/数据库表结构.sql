-- 微信订阅消息表
CREATE TABLE `wechat_subscription` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID（业务系统的用户ID）',
  `openid` varchar(64) NOT NULL COMMENT '微信openid',
  `template_id` varchar(64) NOT NULL COMMENT '模板ID',
  `remaining_count` int(11) NOT NULL DEFAULT '1' COMMENT '剩余可发送次数',
  `subscribe_time` datetime NOT NULL COMMENT '订阅时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `last_send_time` datetime DEFAULT NULL COMMENT '最后发送时间',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-有效, CONSUMED-已消费, EXPIRED-已过期',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_template` (`user_id`, `template_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信订阅消息记录表';

-- 微信消息发送日志表
CREATE TABLE `wechat_message_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `openid` varchar(64) NOT NULL COMMENT '微信openid',
  `template_id` varchar(64) NOT NULL COMMENT '模板ID',
  `template_name` varchar(100) DEFAULT NULL COMMENT '模板名称',
  `message_data` text COMMENT '消息内容（JSON格式）',
  `page` varchar(255) DEFAULT NULL COMMENT '跳转页面',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  `send_result` varchar(20) NOT NULL COMMENT '发送结果：SUCCESS-成功, FAILED-失败',
  `error_code` int(11) DEFAULT NULL COMMENT '错误码（如43101）',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `msgid` varchar(100) DEFAULT NULL COMMENT '微信返回的消息ID',
  `scene` varchar(50) DEFAULT NULL COMMENT '发送场景（如order_create, payment_success）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_send_result` (`send_result`),
  KEY `idx_error_code` (`error_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信消息发送日志表';

-- 初始化常用模板配置表
CREATE TABLE `wechat_template_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` varchar(64) NOT NULL COMMENT '模板ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_type` varchar(20) NOT NULL COMMENT '模板类型：ONE_TIME-一次性, LONG_TERM-长期',
  `business_scene` varchar(50) NOT NULL COMMENT '业务场景',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, DISABLED-停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信模板配置表';

-- 插入示例模板配置
INSERT INTO `wechat_template_config` (`template_id`, `template_name`, `template_type`, `business_scene`, `status`) VALUES
('tmpl_order_update', '订单状态更新通知', 'ONE_TIME', 'ORDER', 'ACTIVE'),
('tmpl_logistics_update', '物流信息通知', 'ONE_TIME', 'LOGISTICS', 'ACTIVE'),
('tmpl_refund_notify', '退款通知', 'ONE_TIME', 'REFUND', 'ACTIVE'),
('tmpl_payment_success', '支付成功通知', 'ONE_TIME', 'PAYMENT', 'ACTIVE');

-- 订阅统计视图（用于报表分析）
CREATE OR REPLACE VIEW `v_subscription_stats` AS
SELECT
  DATE(subscribe_time) AS subscribe_date,
  template_id,
  status,
  COUNT(*) AS total_count,
  SUM(remaining_count) AS total_remaining,
  COUNT(DISTINCT user_id) AS unique_users
FROM wechat_subscription
GROUP BY DATE(subscribe_time), template_id, status;

-- 消息发送统计视图
CREATE OR REPLACE VIEW `v_message_send_stats` AS
SELECT
  DATE(send_time) AS send_date,
  template_id,
  send_result,
  error_code,
  COUNT(*) AS send_count,
  COUNT(DISTINCT user_id) AS unique_users
FROM wechat_message_log
GROUP BY DATE(send_time), template_id, send_result, error_code;

-- 43101错误统计（重点监控）
CREATE OR REPLACE VIEW `v_43101_error_stats` AS
SELECT
  DATE(send_time) AS error_date,
  template_id,
  COUNT(*) AS error_count,
  COUNT(DISTINCT user_id) AS affected_users
FROM wechat_message_log
WHERE error_code = 43101
GROUP BY DATE(send_time), template_id;
